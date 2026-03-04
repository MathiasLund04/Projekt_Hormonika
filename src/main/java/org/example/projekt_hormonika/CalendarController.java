package org.example.projekt_hormonika;

import DAL.DBConfig;
import Model.Booking;
import Enums.Status;
import Model.Hairdresser;
import Repository.Booking.BookingRepository;
import Repository.Booking.MySQLBookingRepository;
import Repository.Hairdresser.HairdresserRepository;
import Repository.Hairdresser.MySQLHairdresserRepository;
import Service.BookingService;
import Service.HairdresserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarController {

    @FXML private DatePicker datePicker;
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane grid;
    @FXML private Label employeeLabel;

    // Repository + service
    private final DBConfig db = new DBConfig();
    private final BookingRepository bookingRepo = new MySQLBookingRepository(db);
    private final BookingService bookingService = new BookingService(bookingRepo, db);
    private final HairdresserRepository hRepo = new MySQLHairdresserRepository(db);
    private final HairdresserService hairdresserService = new HairdresserService(hRepo, db);

    private Booking selectedBooking;
    public static Booking bookingToEdit;

    // Frisører i rækkefølge = kolonner
    private final List<String> employees = new ArrayList<>();

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());

        for (Hairdresser h : hairdresserService.getHairdressers()){
            employees.add(h.getName());
        }
        buildCalendar();
        setLoggedInHairdresser();

    }

    @FXML
    private void onUpdate() {
        buildCalendar();
    }

    @FXML
    private void onNewAppointment(ActionEvent event) {
        SceneSwitcher.switchTo(event, "Appointment-View");
    }

    @FXML
    private void onEditAppointment(ActionEvent event) {

        if (selectedBooking == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Ingen booking valgt");
            alert.setContentText("Klik på en booking i kalenderen før du trykker 'Rediger'.");
            alert.showAndWait();
            return;
        }
        SceneSwitcher.switchTo(event, "EditAppointment-View");
    }

    private void buildCalendar() {
        grid.getChildren().clear();
        grid.getRowConstraints().clear();
        grid.getColumnConstraints().clear();

        buildEmployeeColumns();
        buildTimeRows();
        loadBookingsForDate(datePicker.getValue());

        // Tving JavaFX til at tegne linjer
        grid.setGridLinesVisible(false);
        grid.setGridLinesVisible(true);
    }

    private void buildEmployeeColumns() {

        grid.getColumnConstraints().clear();


        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setPrefWidth(80);
        grid.getColumnConstraints().add(timeCol);

        // Kolonner til frisører
        for (int i = 0; i < employees.size(); i++) {

            Label label = new Label(employees.get(i));
            label.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            grid.add(label, i + 1, 0);

            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(100);
            grid.getColumnConstraints().add(cc);
        }
    }

    private void buildTimeRows() {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);

        int row = 1;
        for (LocalTime t = start; !t.isAfter(end); t = t.plusMinutes(15)) {

            // Giv rækken højde (så linjer kan tegnes)
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(30);
            rc.setPrefHeight(30);
            grid.getRowConstraints().add(rc);

            // Tid i venstre side
            Label timeLabel = new Label(t.toString());
            timeLabel.setStyle("-fx-padding: 5;");
            grid.add(timeLabel, 0, row);

            row++;
        }
    }

    private void loadBookingsForDate(LocalDate date) {
        List<Booking> bookings = bookingService.getActiveCalendar();

        for (Booking b : bookings) {
            if (b.getDate().equals(date) && b.getStatus() == Status.ACTIVE) {
                placeBooking(b);
            }
        }
    }

    private void placeBooking(Booking b) {


        int col = b.getHairdresserId();

        LocalTime dayStart = LocalTime.of(8, 0);

        int startRow = (int) Duration.between(dayStart, b.getTime()).toMinutes() / 15 + 1;

        // Varighed fra haircutType (fx 30, 45, 60, 120 min)
        int duration = b.getHaircutType().getTime();

        // Beregn sluttidspunkt
        LocalTime endTime = b.getTime().plusMinutes(duration);
        int endRow = (int) Duration.between(dayStart, endTime).toMinutes() / 15 + 1;

        // Tooltip tider
        LocalTime start = b.getTime();

        // Container der fylder hele cellen
        StackPane container = new StackPane();
        container.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(container, Priority.ALWAYS);

        // Selve booking-boksen
        Pane bookingBox = new Pane();
        bookingBox.setStyle(
                "-fx-background-color: " + getColorForHairdresser(b.getHairdresserId()) + ";" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 2;"
        );
        bookingBox.setMaxHeight(Double.MAX_VALUE);

        // Tooltip
        Tooltip.install(bookingBox, new Tooltip(
                "Kunde: " + b.getName() + "\n" +
                        "Telefon: " + b.getPhoneNum() + "\n" +
                        "Behandling: " + b.getHaircutType().getDescription() + "\n" +
                        "Tid: " + start + " - " + endTime + "\n" +
                        "Note: " + (b.getDescription() == null ? "-" : b.getDescription())
        ));

        // Klik-event – KUN booking-boksen er klikbar
        bookingBox.setOnMouseClicked(event -> {
            selectedBooking = b;
            bookingToEdit = b;

            // Fjern highlight fra alle andre bookinger
            grid.getChildren().forEach(node -> {
                if (node instanceof StackPane sp && !sp.getChildren().isEmpty()) {
                    Pane box = (Pane) sp.getChildren().get(0);
                    box.setStyle(
                            "-fx-background-color: " + getColorForHairdresser(b.getHairdresserId()) + ";" +
                                    "-fx-border-color: black;" +
                                    "-fx-border-width: 2;"

                    );
                }
            });

            // Giv denne booking en tydelig markering
            bookingBox.setStyle(
                    "-fx-background-color: " + getColorForHairdresser(b.getHairdresserId()) + ";" +
                            "-fx-border-color: black;" +
                            "-fx-border-width: 4;"
            );
        });

        // Læg boksen ind i containeren
        container.getChildren().add(bookingBox);

        // Læg containeren i grid'et og stræk over rækker
        grid.add(container, col, startRow, 1, endRow - startRow);
    }


    private String getColorForHairdresser(int id) {
        return switch (id) {
            case 1 -> "#f4cccc"; // Mads
            case 2 -> "#c9daf8"; // Ida
            case 3 -> "#d9ead3"; // Fie
            case 4 -> "#fff2cc"; // Mie
            case 5 -> "#ead1dc"; // Monika
            default -> "#cccccc";
        };
    }

    public void setLoggedInHairdresser() {
        employeeLabel.setText(LoginController.hairdresserName);
    }
}