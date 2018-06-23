package ui.game

import engine.Game
import engine.moves.Move
import engine.pieces.Piece
import engine.util.Board
import engine.util.Colour
import engine.util.Position
import engine.util.PositionIterator
import javafx.geometry.Orientation
import javafx.scene.layout.Pane
import ui.game.components.PiecePane
import ui.game.components.SquarePane
import ui.game.layout.GraveyardGraphicPosition
import ui.game.layout.LayoutCalculator
import ui.game.layout.SquareGraphicPosition
import java.util.*

/**
 * Controls the main region containing the pieceMap and graveyards
 *
 * @property game the game engine
 */
internal class BoardPane(private val game: Game) : Pane() {
    /**
     * The list of squares
     */
    //Warning if refactoring pieceMap data structure make sure highlight controller get populated list
    private val boardSquares = Board<SquarePane>()

    private val layoutCalculator = LayoutCalculator(heightProperty())

    private val animationController = AnimationController()

    private val highlightController = HighlightController(boardSquares)

    /**
     * A map of pieces and their associated piecePane
     */
    private val piecePanes = HashMap<Piece, PiecePane>()

    /**
     * Object representing the current move request
     */
    private var moveRequest: MoveRequest? = null

    init {

        //For each position create square
        val positionIterator = PositionIterator()

        while (positionIterator.hasNext()) {
            val position = positionIterator.next()

            val graphicPosition = layoutCalculator.createSquarePosition(position)

            //Create the square
            val squarePane = SquarePane(
                    (position.column + position.row) % 2 == 0, //Calculates whether the square should be white or black (gray) (top-left is white)
                    this::squareClick,
                    graphicPosition,
                    layoutCalculator.componentSize
            )

            //Add the square to the list
            boardSquares.add(position, squarePane)

            //If there is a pieceMap at this position create a piecePane it
            val piece = game.gameData.pieceMap[position]

            if (piece != null) {
                val piecePane = PiecePane(piece, graphicPosition, layoutCalculator.componentSize)

                piecePane.setOnMousePressed { _ -> pieceClick(piecePane) }

                //Add the piecePane to the map
                piecePanes[piece] = piecePane
            }
        }

        //For each already eaten pieceMap create a piecePane and put to graveyard of that colour
        for (colour in Colour.values()) {
            for ((graveyardPositionOfNext, eatenPiece) in game.gameData.getEatenPieces(colour).withIndex()) {
                val piecePane = PiecePane(
                        eatenPiece,
                        layoutCalculator.createGraveyardPosition(colour, graveyardPositionOfNext),
                        layoutCalculator.componentSize
                )

                piecePane.setOnMouseClicked { _ -> this.pieceClick(piecePane) }

                piecePanes[eatenPiece] = piecePane
            }
        }

        //Add all the squares and pieces to the pieceMap
        this.children.addAll(boardSquares.data)
        this.children.addAll(piecePanes.values)

        game.addBoardChangeListener(this::updateBoard) //If the pieceMap engine changes update the display

        //When the game is no longer waiting for a move, wait for all animations to finish then trigger the next player to play
        game.statusProperty().addListener { _, _, newValue ->
            if (newValue == Game.Status.INACTIVE) {
                if (animationController.isRunning.get())
                //When animations finish remove listener and trigger next player
                    animationController.setOnFinishListener {
                        animationController.setOnFinishListener(null)
                        game.notifyNextPlayer()
                    }
                else
                    game.notifyNextPlayer() //If animations not running trigger next player immediately
            }
        }
    }

    /**
     * Called by a player to request the pieceMap to record a users move
     *
     * @param callback the callback method to which the move should be submitted
     * @param colour   the colour of the player that should submit the move
     */
    fun requestMove(callback: (Move) -> Unit, colour: Colour) {
        this.moveRequest = MoveRequest(callback, colour)
    }

    private fun pieceClick(piecePane: PiecePane) {
        //If pieceMap not at a square do nothing (if it's in graveyard)
        if (piecePane.currentPosition !is SquareGraphicPosition) return

        //If there are no active moveRequests do nothing
        if (moveRequest == null || moveRequest!!.isSubmitted) return

        val pieceClicked = piecePane.piece
        val position = (piecePane.currentPosition as SquareGraphicPosition).position

        //If pieceMap already selected this click was to submit move
        if (highlightController.isSelected) {
            trySubmittingMove(position)
            return
        }

        //If move request for other player do nothing
        if (moveRequest!!.colour != pieceClicked.colour) return


        //Calculate possible moves and highlight those moves
        highlightController.select(position,
                game.gameData.filterOnlyLegal(
                        pieceClicked.generatePossibleMoves(game.gameData, position),
                        pieceClicked.colour
                )
        )
    }

    /**
     * Called when a component (pieceMap or square) is clicked
     *
     * @param panePosition the components position
     */
    private fun squareClick(panePosition: SquareGraphicPosition) {
        //If no pieceMap already selected do nothing
        if (!highlightController.isSelected) return

        //Get the position and try submitting move
        trySubmittingMove(panePosition.position)
    }

    private fun trySubmittingMove(position: Position) {
        //If not a move option remove highlight
        if (!highlightController.isPossibleMove(position)) {
            highlightController.eraseSelection()
            return
        }

        //Else get selected move and apply (and erase highlight)
        val selectedMove = highlightController.getPossibleMove(position)
        highlightController.eraseSelection()
        moveRequest!!.submit(selectedMove!!)
    }

    /**
     * Called when the engine changes to update the display
     */
    @Synchronized
    private fun updateBoard() {
        //For each pieceMap on the pieceMap
        for ((position, piece) in game.gameData.pieceMap) {
            //Create graphic position
            val panePosition = layoutCalculator.createSquarePosition(position)

            //Get piecePane
            val piecePane = piecePanes[piece]

            //If pieceMap pane not already at position make animation
            if (!piecePane!!.isAtPosition(panePosition)) animationController.addAnimation(piecePane, panePosition)
        }

        //For each colour of pieces
        for (colour in Colour.values()) {
            //For each pieceMap in the graveyard
            val eatenPieces = game.gameData.getEatenPieces(colour)
            for (piece in eatenPieces) {
                val piecePane = piecePanes[piece]

                //If piecePane not already in graveyard move the piecePane to the graveyard
                if (piecePane!!.currentPosition !is GraveyardGraphicPosition)
                    animationController.addAnimation(
                            piecePane,
                            layoutCalculator.createGraveyardPosition(colour, eatenPieces.size - 1)
                    )
            }
        }
    }

    //So that the width is dependant on the height based on the height
    override fun getContentBias(): Orientation {
        return Orientation.VERTICAL
    }

    /**
     * Sets the preferred width as a factor of the height
     */
    override fun computePrefWidth(height: Double): Double {
        return height * layoutCalculator.widthRatio //The pieceMap takes up 1 * height and the graveyards (4 / 8) * height
    }

    /**
     * An object representing a request to the UI for a move
     *
     * @property moveCallback The callback method to which the selected move should be submitted
     * @property colour The colour of the player that should submit the move
     */
    private data class MoveRequest internal constructor(private val moveCallback: (Move) -> Unit, internal val colour: Colour) {
        /**
         * True if the move has already been submitted
         */
        internal var isSubmitted = false
            private set

        internal fun submit(move: Move) {
            isSubmitted = true
            moveCallback(move)
        }
    }
}