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
    @FXML private ComboBox<String> hairdresser;   // medarbejder
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

    private final DBConfig db = new DBConfig();
    private final BookingRepository bRepo = new MySQLBookingRepository(db);
    private final BookingService bookingService =
            new BookingService(bRepo, db);
    private final HairdresserRepository hRepo = new MySQLHairdresserRepository(db);
    private final HairdresserService hairdresserService =
            new HairdresserService(hRepo, db);

    @FXML
    public void initialize() {

        // Medarbejdere
        for (Hairdresser h : hairdresserService.getHairdressers()) {
            hairdresser.getItems().addAll(h.getName());
        }

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
        boolean running = false;
        try {
            String name = customerName.getText();
            System.out.println(name);
            String phone = phoneNumber.getText();
            System.out.println(phone);
            LocalDate date = newAppointmentDataPicker.getValue();
            System.out.println(date);
            String employeeName = hairdresser.getValue();
            System.out.println((employeeName));
            LocalTime time = LocalTime.parse(haircutTypeBox.getValue());
            System.out.println((time));
            Haircuts haircut = getSelectedHairStyle();
            System.out.println((haircut));
            String description = descriptionArea.getText();
            System.out.println(description);


            // Konverter medarbejdernavn → ID
            int hairdresserId = switch (employeeName) {
                case "Mads" -> 1;
                case "Ida" -> 2;
                case "Fie" -> 3;
                case "Mie" -> 4;
                case "Monika" -> 5;
                default -> 0;
            };

            System.out.println("\n"+ hairdresserId);

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
            running = true;


        } catch (NullPointerException e) {
            errorChoiceLabel.setText("Udfyld alle felter korrekt.");
        }

        if (running) {
            // Skift tilbage til kalenderen efter 1 sekund
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        SceneSwitcher.switchTo(event, "Calendar-View");
                    });
                } catch (InterruptedException ignored) {
                }
            }).start();
        }
    }


}