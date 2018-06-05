package gui.jeu;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXListView;
import gui.App;
import gui.jeu.board.Board;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class JeuController {
    @FXML
    private StackPane plateauContainer;

    @FXML
    private JFXDrawer drawer;

    @FXML
    private JFXListView<Action> drawerList;

    private final Board board;

    private final ObservableList<Action> actions;

    JeuController(App.MontrerIntro goBack, Board board) {
        this.board = board;

        Action revnirAuMenuPrincipal = new Action() {
            @Override
            void onClick() {
                goBack.montrerIntro();
            }

            @Override
            String getDescription() {
                return "Revenir au menu principal";
            }
        };

        actions = FXCollections.observableArrayList(revnirAuMenuPrincipal);
    }

    @FXML
    private void initialize() {
        plateauContainer.getChildren().add(board);
        drawerList.setItems(actions);

        drawerList.setOnMouseClicked(event -> drawerList.getSelectionModel().getSelectedItem().onClick());
    }

    @FXML
    private void handleHamburger() {
        drawer.open();
    }

    public abstract static class Action {
        abstract void onClick();

        abstract String getDescription();

        @Override
        public String toString() {
            return getDescription();
        }
    }
}
