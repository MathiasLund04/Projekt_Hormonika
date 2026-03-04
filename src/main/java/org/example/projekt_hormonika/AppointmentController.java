package org.example.projekt_hormonika;

import DAL.DBConfig;
import Enums.Haircuts;
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

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentController {

    @FXML private TextField customerName;
    @FXML private TextField phoneNumber;
    @FXML private DatePicker newAppointmentDataPicker;
    @FXML private ComboBox<Hairdresser> hairdresser;   // medarbejder
    @FXML private ComboBox<String> haircutTypeBox;  // tid
    @FXML private TextArea descriptionArea;

    @FXML private RadioButton womanCut;
    @FXML private RadioButton manCut;
    @FXML private RadioButton childCut;
    @FXML private RadioButton beard;
    @FXML private RadioButton perm;
    @FXML private RadioButton colour;
    @FXML private RadioButton other;

    @FXML private Label errorChoiceLabel;
    @FXML private Label employeeLabel;

    private final DBConfig db = new DBConfig();
    private final BookingRepository bRepo = new MySQLBookingRepository(db);
    private final BookingService bookingService = new BookingService(bRepo, db);
    private final HairdresserRepository hRepo = new MySQLHairdresserRepository(db);
    private final HairdresserService hairdresserService = new HairdresserService(hRepo, db);

    @FXML
    public void initialize() {

        // Medarbejdere
        hairdresser.getItems().addAll(hairdresserService.getHairdressers());
        employeeLabel.setText(LoginController.hairdresserName);


        // Tider (hver 15. minut)
        for (LocalTime t = LocalTime.of(8, 0); !t.isAfter(LocalTime.of(17, 0)); t = t.plusMinutes(15)) {
            haircutTypeBox.getItems().add(t.toString());
        }

        // ToggleGroup til klipningstyper
        ToggleGroup group = new ToggleGroup();
        womanCut.setToggleGroup(group);
        manCut.setToggleGroup(group);
        childCut.setToggleGroup(group);
        beard.setToggleGroup(group);
        perm.setToggleGroup(group);
        colour.setToggleGroup(group);
        other.setToggleGroup(group);

        // Default dato
        newAppointmentDataPicker.setValue(LocalDate.now());
    }

    private Haircuts getSelectedHairStyle() {
        if (womanCut.isSelected()) return Haircuts.WOMANCUT;
        if (manCut.isSelected()) return Haircuts.MANCUT;
        if (childCut.isSelected()) return Haircuts.CHILDCUT;
        if (beard.isSelected()) return Haircuts.BEARD;
        if (perm.isSelected()) return Haircuts.PERM;
        if (colour.isSelected()) return Haircuts.COLOUR;
        if (other.isSelected()) return Haircuts.OTHER;
        return null;
    }

    @FXML
    private void onMakeNewAppointment(ActionEvent event) {

        // Manuelt tjek for tomme felter
        if (customerName.getText().isEmpty() ||
                phoneNumber.getText().isEmpty() ||
                newAppointmentDataPicker.getValue() == null ||
                hairdresser.getValue() == null ||
                haircutTypeBox.getValue() == null ||
                getSelectedHairStyle() == null) {

            errorChoiceLabel.setText("Udfyld alle felter.");
            return;
        }

        try {
            String name = customerName.getText();
            String phone = phoneNumber.getText();
            LocalDate date = newAppointmentDataPicker.getValue();
            LocalTime time = LocalTime.parse(haircutTypeBox.getValue());
            Haircuts haircut = getSelectedHairStyle();
            String description = descriptionArea.getText();

            Hairdresser selectedHairdresser = hairdresser.getValue();
            int hairdresserId = selectedHairdresser.getId();   // NU: direkte fra objektet

            Booking booking = bookingService.createBooking(
                    name,
                    phone,
                    date,
                    time,
                    haircut,
                    hairdresserId,
                    description
            );

            if (booking == null) {
                errorChoiceLabel.setText("Tiden er optaget!");
                return;
            }

            errorChoiceLabel.setText("Booking oprettet!");
            bookingService.getActiveCalendar();

            // Skift tilbage til kalenderen efter 1 sekund
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() ->
                            SceneSwitcher.switchTo(event, "Calendar-View")
                    );
                } catch (InterruptedException ignored) {}
            }).start();

        } catch (Exception e) {
            e.printStackTrace(); // Lad den stå lidt, så du kan se evt. fejl i konsollen
            errorChoiceLabel.setText("Udfyld alle felter korrekt.");
        }
    }
    @FXML
    private void onBackPressed(ActionEvent event) {
        SceneSwitcher.switchTo(event, "Calendar-View");
    }


}
