package jfx2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainJFX extends Application {

    private static String[] args;

    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length == 0)
            throw new ClassNotFoundException("First argument must be .fxml file");
        MainJFX.args = args;
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(args[0] + ".fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();

        Scene scene = new Scene(root);
        primaryStage.setTitle(args[0]);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            controller.close();
        });
    }
}
