package model;

import model.player.Player;
import model.util.BoardMap;
import model.util.Colour;

import java.io.*;
import java.util.EnumMap;

/**
 * Responsable de saveGame et recharger le fichier contenant le Game
 */
public class Loader {
    private final File file = new File("savedGame.txt"); //Le nom du fichier
    private Game game;

    /**
     * Sauvegarde le game
     */
    private void saveGame() {
        try {
            if (!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.createNewFile(); //Si le fichier de game n'existe pas créer le fichier

            //Écrire le game au fichier
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(game);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * charge le game du fichier
     *
     * @return Si on a chargé le game avec success
     */
    public boolean loadGameFromFile() {
        if (!file.exists()) return false; //Si aucun fichier -> échec

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            this.game = (Game) objectInputStream.readObject(); //Lire le game
            this.game.turnMarkerProperty().addListener((observable, oldValue, newValue) -> saveGame()); //Quand le game change sauvegarder
            objectInputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createNewGame(EnumMap<Colour, Player> joueurs) {
        //Créer les rois

        //Créer le modèle de game
        GameData gameData = new GameData(BoardMap.createStartingBoard());

        //Créer et notifyNextPlayer la partie
        this.game = new Game(gameData, joueurs);
        game.turnMarkerProperty().addListener((observable, oldValue, newValue) -> saveGame());
    }

    public Game getGame() {
        return game;
    }
}
