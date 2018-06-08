package gui.jeu.board;

import gui.jeu.board.layout.CasePosition;
import gui.jeu.board.layout.GraveyardPosition;
import gui.jeu.board.view.Case;
import gui.jeu.board.view.PiecePane;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;
import modele.Couleur;
import modele.JeuData;
import modele.moves.Mouvement;
import modele.pieces.Piece;
import modele.plateau.Plateau;
import modele.plateau.Position;
import modele.plateau.PositionIterator;
import modele.plateau.Tableau;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

/**
 * Controle le plateau de jeu
 */
public class PlateauPane extends Pane {
    //La liste de case
    @NotNull
    private final Tableau<Case> cases = new Tableau<>();

    private final List<PiecePane> piecePanes = new ArrayList<>();

    private final AnimationController animationController = new AnimationController();

    //Le modele du jeu (contient le plateau et les pièces)
    @NotNull
    private final JeuData jeuData;

    //Controller pour surligner les cases
    @NotNull
    private final HighlightController highlightController = new HighlightController(cases);

    //Objet qui spécifie si l'on veut obtenir des mouvements de l'utilisateur
    @Nullable
    private DemandeDeMouvement moveRequest;

    private final EnumMap<Couleur, GraveyardController> graveyardControllers = new EnumMap<>(Couleur.class);

    public PlateauPane(@NotNull JeuData jeuData) {
        this.jeuData = jeuData;

        this.graveyardControllers.put(Couleur.BLANC, new GraveyardController(this.heightProperty(),
                new SimpleIntegerProperty(0), false));
        this.graveyardControllers.put(Couleur.NOIR, new GraveyardController(this.heightProperty(),
                this.heightProperty().add(graveyardControllers.get(Couleur.BLANC).getLargeurTotal()), true));

        //Crée une case pour chaque position
        PositionIterator positionIterator = new PositionIterator();

        while (positionIterator.hasNext()) {
            Position position = positionIterator.next();

            //Créer un controleur
            Case aCase = new Case(
                    (position.getColonne() + position.getRangee()) % 2 == 0, //Calcule si la case devrait être blanche (en-haut à gauche est blanc)
                    this::handleClick,
                    new CasePosition(position, this.heightProperty(), graveyardControllers.get(Couleur.BLANC).getLargeurTotal())
            );

            //Ajouter la case au plateau et à la liste
            cases.add(position, aCase);
            this.getChildren().addAll(aCase);

            //Si il y a une pièce à cette position créer une pièce
            Piece piece = jeuData.getPlateau().getPiece(position);

            if (piece != null) {
                PiecePane piecePane = new PiecePane(
                        piece,
                        new CasePosition(position, this.heightProperty(), graveyardControllers.get(Couleur.BLANC).getLargeurTotal())
                );

                //Ajouter les listeners
                piecePane.setOnMousePressed(event -> handleClick(jeuData.getPlateau().getPosition(piece)));

                //Ajouter la pièce à la liste de pièce
                piecePanes.add(piecePane);
            }
        }

        //Ajouter toutes les cases et pièces au plateau
        this.getChildren().addAll(piecePanes);

        this.jeuData.setChangeListener(this::updateBoard);
        updateBoard(jeuData.getPlateau());
    }

    /**
     * Appelé par un joueur pour demander à l'objet d'enregistrer le mouvement du joueur
     *
     * @param moveRequest l'information sur le mouvement demandé
     */
    public void demanderMouvement(DemandeDeMouvement moveRequest) {
        this.moveRequest = moveRequest;
    }

    private void handleClick(Position position) {
        //Si aucun moveRequest ne rien faire
        if (moveRequest == null || moveRequest.isCompleted()) return;

        Piece pieceClicked = jeuData.getPlateau().getPiece(position);

        //Si une pièce est déjà sélectionné
        if (highlightController.isSelected()) {
            //Si la case est une des options appliquer le movement
            if (highlightController.isOption(position)) {
                Mouvement mouvementChoisi = highlightController.getMouvement(position);
                highlightController.deSelectionner();
                moveRequest.apply(mouvementChoisi);
            }

            highlightController.deSelectionner(); //Déselectionner tout
        } else {
            //Quitter si il n'y a rien a faire
            if (pieceClicked == null || moveRequest.getCouleurDeLaDemande() != pieceClicked.getCouleur())
                return;

            //Calculer les mouvements possibles
            Set<Mouvement> moves = jeuData.filterOnlyLegal(pieceClicked.generateAllMoves(jeuData.getPlateau()), pieceClicked.getCouleur());

            highlightController.selectionner(position, moves);
        }
    }

    /**
     * Pour chaque case afficher la pièce à cette case
     */
    private void updateBoard(Plateau plateau) {
        Platform.runLater(() -> {
            List<PiecePane> piecesToRemove = new ArrayList<>();
            for (PiecePane piecePane : piecePanes) {
                Position position = plateau.getPosition(piecePane.getPiece());

                if (position != null) {
                    animationController.addToQueue(piecePane, new CasePosition(position, this.heightProperty(), graveyardControllers.get(Couleur.BLANC).getLargeurTotal()));
                } else {
                    animationController.addToQueue(piecePane, new GraveyardPosition(graveyardControllers.get(piecePane.getPiece().getCouleur())));

                    piecesToRemove.add(piecePane);
                }
            }

            for (PiecePane piecePane : piecesToRemove) {
                piecePanes.remove(piecePane);
            }
        });
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.VERTICAL;
    }

    /**
     * Définit la largeur préféré si il y a un display calculator
     */
    @Override
    protected double computePrefWidth(double height) {
        double ratio = 1;

        for (GraveyardController graveyardController : graveyardControllers.values()) {
            ratio += graveyardController.getTotalWidthRatio();
        }

        return height * ratio;
    }
}