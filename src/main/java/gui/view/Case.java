package gui.view;

import javafx.beans.binding.NumberBinding;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import modele.plateau.Position;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Controle une case
 */
public class Case extends Rectangle {
    /**
     * Les différentes couleurs possible pour la case
     */
    public enum Highlight {
        NORMAL,
        ROUGE,
        BLUE
    }

    //Si la case est blanche ou noir (gris)
    private final boolean isBlanc;

    //La position de la case
    @NotNull
    private final Position position;

    /**
     * @param taille        la taille de la case
     * @param isBlanc       si la case est blanche
     * @param position      la position de la case
     */
    public Case(NumberBinding taille, boolean isBlanc, @NotNull Position position) {
        super();
        this.isBlanc = isBlanc;
        this.position = position;

        this.widthProperty().bind(taille);
        this.heightProperty().bind(taille);

        setCouleur(Highlight.NORMAL);  //Met la couleur de l'arrière plan de la case
    }

    /**
     * @param highlight la nouvelle couleur de l'arrière plan de l'arrière plan
     */
    public void setCouleur(@NotNull Highlight highlight) {
        this.setFill(getCouleur(highlight));
    }

    @Contract(pure = true)
    private Paint getCouleur(@NotNull Highlight highlight) {
        switch (highlight) {
            case BLUE:
                return isBlanc ? Color.LIGHTBLUE : Color.CORNFLOWERBLUE;
            case ROUGE:
                return Color.PALEVIOLETRED;
            case NORMAL:
                return isBlanc ? Color.WHITE : Color.LIGHTGRAY;
            default:
                throw new IllegalArgumentException("Couleur de highlight inconnue");
        }
    }

    @NotNull
    public Position getPosition() {
        return position;
    }
}