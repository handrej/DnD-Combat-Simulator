package fhtw.View;

import fhtw.Model.Client;
import java.io.IOException;
import fhtw.Controller.GameController;
import fhtw.Controller.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;

public class ViewManager {
    private Scene scene;

    /**
     * Allows us to load difference scenes for the application
     * @param scene Scene object reference created and started in our application (main)
     */
    public ViewManager(Scene scene) {
        this.scene = scene;
    }

    /**
     * Callback from LoginController to load the next Scene
     * @param session Client object with Username, Read/Write Stream etc.
     */
    public void authenticated(Client session) {
        showMainView(session);
    }

    /**
     * Initial Scene when starting the Client part of the application. LoginController for more.
     */
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Login.fxml")
            );
            LoginController controller = new LoginController();
            loader.setController(controller);
            scene.setRoot(loader.load());
            controller.initLogin(this);
        } catch (IOException ex) {
            System.err.println("Error loading Login.fxml");
        }
    }

    /**
     * Main Game Scene after successfully passing login procedure in LoginController. GameController handles things from here on.
     * Two Listeners monitor StringProperties of the Client object and run appropriate functions to update data in our View.
     * @param session Client object with Username, Read/Write Stream etc.
     */
    private void showMainView(Client session) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Game.fxml")
            );
            GameController controller = new GameController(session);
            loader.setController(controller);
            scene.setRoot(loader.load());
            controller.initLogout(this);

            session.chatroomProperty().addListener((source, old, to) -> {
                // update controller on FX Application Thread:
                Platform.runLater(() -> controller.updateRoom(to));
            });

            session.messageProperty().addListener((source, old, to) -> {
                // update controller on FX Application Thread:
                Platform.runLater(() -> controller.updateText(to));
            });
        } catch (IOException ex) {
            System.err.println("Error loading Game.fxml");
        }
    }

    public void logout() {
        showLoginScreen();
    }
}