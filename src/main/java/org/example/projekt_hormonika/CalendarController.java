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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private void onEditAppointment() {
        System.out.println("Rediger booking – implementeres senere");
    }

    private void buildCalendar() {
        grid.getChildren().clear();
        buildEmployeeColumns();
        buildTimeRows();
        loadBookingsForDate(datePicker.getValue());
    }

    private void buildEmployeeColumns() {
        int col = 1;
        for (String emp : employees) {
            Label label = new Label(emp);
            label.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            grid.add(label, col, 0);
            col++;
        }
    }

    private void buildTimeRows() {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);

        int row = 1;
        for (LocalTime t = start; !t.isAfter(end); t = t.plusMinutes(15)) {
            Label timeLabel = new Label(t.toString());
            timeLabel.setStyle("-fx-padding: 5;");
            grid.add(timeLabel, 0, row);
            row++;
        }
    }

    private void loadBookingsForDate(LocalDate date) {
        List<Booking> bookings = bookingService.getCalendar();

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

        LocalTime endTime = b.getTime().plusMinutes(b.getHaircutType().getTime());
        int endRow = (int) Duration.between(dayStart, endTime).toMinutes() / 15+1;

        Pane p = new Pane();
        p.setStyle("-fx-background-color: " +
                getColorForHairdresser(b.getHairdresserId()) +
                "; -fx-border-color: gray;");

        Tooltip.install(p, new Tooltip(
                "Kunde: " + b.getName() + "\n" +
                        "Telefon: " + b.getPhoneNr() + "\n" +
                        "Behandling: " + b.getHaircutType().getDescription() + "\n" +
                        "Note: " + (b.getDescription() == null ? "-" : b.getDescription())
        ));

        grid.add(p, col, startRow, 1, endRow - startRow);
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