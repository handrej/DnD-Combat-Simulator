package fhtw;

import fhtw.View.ViewManager;
import javafx.application.Application;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Main extends Application {

    /**
     * Launches FX Application
     * @param args (optional) command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Setup stage, scene and start application logic.
     * @param stage Application SuperClass Parameter
     */
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Pane());
        ViewManager vm = new ViewManager(scene);
        vm.showLoginScreen();
        stage.setWidth(900);
        stage.setHeight(700);
        //stage.setResizable(false);
        stage.setTitle("Dungeon");
        stage.setScene(scene);
        stage.show();
    }
}
