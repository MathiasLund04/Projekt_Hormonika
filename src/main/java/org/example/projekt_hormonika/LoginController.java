package org.example.projekt_hormonika;

import Exceptions.DataAccessException;
import Model.Hairdresser;
import Repository.Hairdresser.HairdresserRepository;
import Repository.Hairdresser.MySQLHairdresserRepository;
import Service.HairdresserService;
import DAL.DBConfig;

import javafx.event.ActionEvent;
import  javafx.fxml.FXML;
import  javafx.fxml.FXMLLoader;
import  javafx.scene.Scene;
import  javafx.scene.control.*;
import  javafx.stage.Stage;

import  java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    public static String hairdresserName;

    private final DBConfig db = new DBConfig();
    private final HairdresserRepository hRepo = new MySQLHairdresserRepository(db);
    private final HairdresserService hairdresserService = new HairdresserService(hRepo, db);



    public LoginController() {

    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Udfyld både brugernavn og adgangskode");
            return;
        }

        try {
            Optional<Hairdresser> loggedIn = hairdresserService.login(username, password);

            if (loggedIn.isPresent()) {
                openCalendarView(event, loggedIn);
            } else {
                errorLabel.setText("Forkert brugernavn eller adgangskode");
            }
        } catch (DataAccessException e) {
            errorLabel.setText("Databasefejl, kunne ikke logge ind");
            e.printStackTrace();
        }
    }

    private void openCalendarView(ActionEvent event, Optional<Hairdresser> hairdresser) {
            // Send den loggede frisør videre til kalenderen
            hairdresserName = hairdresser.get().getName();

            SceneSwitcher.switchTo(event, "Calendar-View");


    }
}
