package org.example.projekt_hormonika;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        URL resource = HelloApplication.class.getResource("/Login-View.fxml");

        System.out.println("Path: " + resource);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setTitle("Hårmonika");
        stage.setScene(scene);
        stage.show();

    }
}
