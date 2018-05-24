package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modele.Modele;
import modele.board.Board;
import modele.board.Position;
import modele.pieces.Roi;

public class App extends Application {
    private static final String TITRE = "Échec et Mat";

    private static Modele modele;

    public static void main(String[] args) {
        Board board = new Board();
        board.ajouter(new Position(0, 0), new Roi(true));
        board.ajouter(new Position(7, 0), new Roi(false));
        modele = new Modele(board);

        launch(args);
    }

    /**
     * Commence l'interface graphique
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(TITRE); //Définir le titre

        //Load l'interface
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/board.fxml"));

        BoardController controller = new BoardController(modele);
        fxmlLoader.setController(controller);

        primaryStage.setScene(
                new Scene(
                        fxmlLoader.load()
                )
        );

        //Montrer l'interface
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
