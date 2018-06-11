package graphique.jeu.plateau.element;

import graphique.jeu.plateau.placement.PositionGraphique;
import javafx.scene.CacheHint;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import modele.pieces.Piece;

/**
 * Une pièce affichée sur l'écran
 */
public class PiecePane extends StackPane {
    private static final float RAPPORT_TAILLE_FONT_SIZE = 0.75F;

    /**
     * La pièce qui se fait afficher
     */
    private final Piece piece;

    private final Text text = new Text();

    /**
     * @param piece la pièce à afficher
     */
    public PiecePane(Piece piece, PositionGraphique position) {
        super();

        this.piece = piece;

        //Attacher la displayCalculator
        this.prefHeightProperty().bind(position.getTaille());
        this.prefWidthProperty().bind(position.getTaille());
        bind(position);

        //Ajouter le text
        setText();
        this.getChildren().add(text);

        //Faire que la displayCalculator du text reste propertionelle
        this.prefWidthProperty().addListener(
                (observable, oldValue, newValue) ->
                        text.setFont(new Font(newValue.doubleValue() * RAPPORT_TAILLE_FONT_SIZE))
        );
    }

    public void setText() {
        text.setText(Character.toString((char) piece.getNumeroUnicode()));
    }

    public Piece getPiece() {
        return piece;
    }

    /**
     * Place la pièce à la position
     */
    public void bind(PositionGraphique position) {
        this.layoutXProperty().bind(position.getX());
        this.layoutYProperty().bind(position.getY());
        position.notifyPlaced();
        this.setCacheHint(CacheHint.DEFAULT);
    }

    public void unBind() {
        this.setCacheHint(CacheHint.SPEED);
        this.layoutXProperty().unbind();
        this.layoutYProperty().unbind();
    }

    public boolean isAtPosition(PositionGraphique position) {
        return this.layoutXProperty().isBound() &&
                this.layoutYProperty().isBound() &&
                this.getLayoutX() == position.getX().getValue().doubleValue() &&
                this.getLayoutY() == position.getY().getValue().doubleValue();
    }
}