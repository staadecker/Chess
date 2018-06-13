package model;

import model.moves.Move;
import model.pieces.King;
import model.pieces.Piece;
import model.util.BoardMap;
import model.util.Colour;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Stack;

/**
 * Represents the board state (piece's positions) and the pieces that were eaten
 */
public class GameData implements Serializable {
    @NotNull
    private final BoardMap board;

    @NotNull
    private final EnumMap<Colour, Stack<Piece>> eatenPieces = new EnumMap<>(Colour.class);

    /**
     * The kings for each player.
     */
    @NotNull
    private final EnumMap<Colour, King> kings = new EnumMap<>(Colour.class);

    public GameData(@NotNull BoardMap boardMap) {
        this.board = boardMap;

        for (Colour colour : Colour.values()) {
            eatenPieces.put(colour, new Stack<>());
        }

        for (Piece piece : boardMap.iteratePieces()) {
            if (piece instanceof King) {
                if (kings.containsKey(piece.getColour()))
                    throw new RuntimeException("There are two kings for the same player");

                kings.put(piece.getColour(), (King) piece);
            }
        }
    }

    @NotNull
    public BoardMap getBoard() {
        return board;
    }

    /**
     * @return the king of this colour
     */
    @NotNull
    King getKing(Colour colour) {
        return kings.get(colour);
    }

    /**
     * @return a collection of possible legal moves that can be player
     */
    @NotNull
    public Collection<Move> getPossibleLegalMoves(Colour colour) {
        return filterOnlyLegal(board.getAllPossibleMoves(colour), colour);
    }

    /**
     * @param moves a collection of legal and illegal moves
     * @param verifierPour legal for who (will remove all moves that throw this colour's king in check)
     * @return a filter collection containing only legal moves
     */
    @NotNull
    public Collection<Move> filterOnlyLegal(Collection<Move> moves, Colour verifierPour) {
        Collection<Move> legalMoves = new ArrayList<>();

        //Pour chaque moves apply et vérifier si l'on attaque le roi
        for (Move move : moves) {
            move.apply(this);

            if (!getBoard().isPieceAttacked(kings.get(verifierPour))) {
                legalMoves.add(move);
            }

            move.undo(this);
        }

        return legalMoves;
    }

    /**
     * @return the stack of eaten pieces for this colour
     */
    public Stack<Piece> getEatenPieces(Colour colour) {
        return eatenPieces.get(colour);
    }
}