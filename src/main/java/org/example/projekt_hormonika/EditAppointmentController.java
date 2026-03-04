package org.example.projekt_hormonika;

import DAL.DBConfig;
import Enums.Haircuts;
import Enums.Status;
import Model.Booking;
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
import org.example.projekt_hormonika.CalendarController;
import org.example.projekt_hormonika.SceneSwitcher;

import java.time.LocalTime;
import java.util.Optional;

public class EditAppointmentController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<Hairdresser> hairdresserBox;
    @FXML private ComboBox<LocalTime> timeBox;
    @FXML private ComboBox<Haircuts> haircutTypeBox;
    @FXML private TextArea noteField;
    @FXML private DatePicker datePicker;
    @FXML private Label employeeLabel;

    private Booking booking;

    private final DBConfig db = new DBConfig();
    private final BookingRepository bookingRepo = new MySQLBookingRepository(db);
    private final BookingService bookingService = new BookingService(bookingRepo, db);
    private final HairdresserRepository hairdresserRepo = new MySQLHairdresserRepository(db);
    private final HairdresserService hairdresserService = new HairdresserService(hairdresserRepo, db);


    @FXML
    public void initialize() {

        booking = CalendarController.bookingToEdit;
        employeeLabel.setText(LoginController.hairdresserName);

        if (booking == null) {
            System.out.println("Ingen booking valgt!");
            return;
        }

        loadDropdowns();
        loadBookingData();
    }


    private void loadDropdowns() {

        hairdresserBox.getItems().setAll(hairdresserService.getHairdressers());

        haircutTypeBox.getItems().setAll(Haircuts.values());

        for (int h = 8; h <= 17; h++) {
            for (int m = 0; m < 60; m += 15) {
                timeBox.getItems().add(LocalTime.of(h, m));
            }
        }
    }


    private void loadBookingData() {

        nameField.setText(booking.getName());
        phoneField.setText(booking.getPhoneNum());
        noteField.setText(booking.getDescription());
        haircutTypeBox.setValue(booking.getHaircutType());

        Booking toGetHairdresser = bookingService.getBookingByIdDB(booking.getId());
        int tryingId = toGetHairdresser.getId();
        System.out.println("OTHERID : " + tryingId);

        int hId = booking.getHairdresserId();
        System.out.println("HairdresserId: " + hId);

        Hairdresser bookedHairdresser = hairdresserService.getHairdresserById(toGetHairdresser.getHairdresserId());
        System.out.println("Hairdresser: " + bookedHairdresser);

        hairdresserBox.setValue(bookedHairdresser);
        datePicker.setValue(booking.getDate());
        timeBox.setValue(booking.getTime());

    }


    @FXML
    private void onSave(ActionEvent event) {

        Booking updated = new Booking(
                booking.getId(),
                nameField.getText(),
                phoneField.getText(),
                datePicker.getValue(),
                timeBox.getValue(),
                haircutTypeBox.getValue(),
                hairdresserBox.getValue().getId(),
                noteField.getText(),
                booking.getStatus()
        );

        bookingService.updateBooking(updated);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Booking opdateret");
        alert.setContentText("Ændringerne er gemt.");
        alert.showAndWait();

        // ← SKIFT TILBAGE TIL KALENDEREN
        SceneSwitcher.switchTo(event, "Calendar-View");

    }


    @FXML
    private void onDelete(ActionEvent event) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Slet booking?");
        confirm.setContentText("Er du sikker på, at du vil slette denne booking?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {

                bookingService.cancelBookingDB(booking);

                Alert done = new Alert(Alert.AlertType.INFORMATION);
                done.setHeaderText("Booking slettet");
                done.showAndWait();

                SceneSwitcher.switchTo(event, "Calendar-View");
            }
        });
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
        loadDropdowns();
        loadBookingData();
    }
}
