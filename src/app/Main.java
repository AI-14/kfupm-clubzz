/**
 * ICS324 - Database Systems
 * Project: KCDBS - A Kfupm club management system.
 * Authors: AMAAN IZHAR, FARHAN ABDULQADIR, ABDULJAWAD MOHAMMED
 */

/**
 * The main class - application starts here.
 */

package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{ 
        Parent root = FXMLLoader.load(getClass().getResource("../views/login/LoginMain.fxml"));
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("KFUPM Student Clubs - Login/Register");
        primaryStage.setScene(scene);
        primaryStage.resizableProperty().setValue(Boolean.FALSE);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
