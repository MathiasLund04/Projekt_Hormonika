    package org.example.projekt_hormonika;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

    public class SceneSwitcher {

        public static void switchTo(ActionEvent event, String fxmlFileName) {
            try {
                URL resource = SceneSwitcher.class.getResource( fxmlFileName + ".fxml");
                System.out.println(resource);
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = stage.getScene();
                if (scene == null) {
                    scene = new Scene(root);
                    stage.setScene(scene);
                } else {
                    scene.setRoot(root);
                }
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }