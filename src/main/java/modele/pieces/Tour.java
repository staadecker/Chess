package modele.pieces;

import modele.plateau.Offset;
import modele.plateau.Plateau;
import modele.plateau.Position;

public class Tour extends DirectionPiece {
    private static final Offset[] OFFSET = {
            Offset.HAUT_CENTRE,
            Offset.MILIEU_GAUCHE,
            Offset.MILIEU_DROIT,
            Offset.BAS_CENTRE
    };

    public Tour(Couleur couleur) {
        super(couleur);
    }

    @Override
    public boolean attaquePosition(Plateau plateau, Position position) {
        //Si la rangée ou colonne n'est pas identique on sait immédiatement que c'est faux
        Position currentPosition = plateau.getPosition(this);
        if (position.getColonne() != currentPosition.getColonne() && position.getRangee() != currentPosition.getRangee())
            return false;

        return super.attaquePosition(plateau, position);
    }

    @Override
    int unicodeForWhite() {
        return 9814;
    }

    @Override
    int unicodeForBlack() {
        return 9820;
    }

    @Override
    Offset[] getDirections() {
        return OFFSET;
    }

    @Override
    public int getValeurPositive() {
        return 5;
    }
}
