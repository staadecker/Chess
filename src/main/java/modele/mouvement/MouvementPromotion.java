package modele.mouvement;

import modele.pieces.Piece;
import modele.pieces.Reine;
import modele.util.Position;

public class MouvementPromotion extends MouvementNormal {
    public MouvementPromotion(Piece piece, Position end) {
        super(piece, end);
    }

    @Override
    public int getValeur() {
        return new Reine(piece.getCouleur()).getValeur() - piece.getValeur();
    }
}
