package Service;

import Enums.Haircuts;
import Enums.Status;
import Exceptions.DataAccessException;
import Model.Booking;
import DAL.DBConfig;
import Repository.Booking.BookingRepository;
import Repository.Customer.CustomerRepository;
import Repository.Customer.MySQLCustomerRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private final DBConfig db;
    private final BookingRepository bRepo;
    private final CustomerRepository cRepo;
    private final CustomerService customerService;
    private List<Booking> calendar;


    public BookingService(BookingRepository bRepo, DBConfig db) {
        this.db = db;
        this.bRepo = bRepo;
        this.cRepo = new MySQLCustomerRepository(db);
        this.customerService = new CustomerService(cRepo, db);
        this.calendar = new ArrayList<>();
    }

    // Hent kalender direkte fra databasen hver gang
    public List<Booking> getActiveCalendar() {
        reload();
        return new ArrayList<>(calendar);
    }

    public Booking createBooking(String name, String phoneNum, LocalDate date, LocalTime time,
                                 Haircuts haircutType, int hairdresserId,
                                 String description) {

        // Validering
        if (!validateBookingTime(date, time, hairdresserId)) {
            throw new IllegalArgumentException("Tidsbestilling kan ikke oprettes til dette tidspunkt");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Navn skal udfyldes");
        }
        if (phoneNum == null || phoneNum.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefonnummer skal udfyldes");
        }
        if (date == null) {
            throw new IllegalArgumentException("Dato skal udfyldes");
        }
        if (time == null) {
            throw new IllegalArgumentException("Tid skal udfyldes");
        }
        if (haircutType == null) {
            throw new IllegalArgumentException("Klipning skal udfyldes");
        }
        if (hairdresserId == 0) {
            throw new IllegalArgumentException("Frisør skal vælges");
        }

        // Dobbeltbooking-check
        if (!validateBookingTime(date, time, hairdresserId)) {
            return null; // tiden er optaget
        }

        // Opret kunde hvis ny
        customerService.createCustomerIfNotExist(name, phoneNum);


        Booking newBooking = new Booking(
                name,
                phoneNum,
                date,
                time,
                haircutType,
                hairdresserId,
                description
        );

        // Gem i databasen
        addBookingDB(newBooking);

        return newBooking;
    }


    //Validering til at sikre at en medarbejder ikke kan dobbeltbookes
    private boolean validateBookingTime(LocalDate date, LocalTime time, int hairdresserId){
        try {
            for (Booking b : bRepo.getActiveCalendar()) {

                boolean sameHairdresser = b.getHairdresserId() == hairdresserId;
                boolean sameDate = b.getDate().equals(date);
                boolean sameTime = b.getTime().equals(time);
                boolean stillActive = b.getStatus() == Status.ACTIVE;

                if (sameHairdresser && sameDate && sameTime && stillActive) {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke validere bookingtid", e);
        }

        return true;
    }

    public void addBookingDB(Booking booking) {
        try {
            bRepo.insertBooking(booking);
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke indsætte booking i databasen", e);
        }
    }


    public void cancelBookingDB(Booking booking) {
        try {
            LocalDate endDate = LocalDate.now();
            bRepo.updateStatus(booking.getId(), Status.CANCELLED, endDate);
            reload();
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke annullere booking", e);
        }
    }

    public void finishBookingDB(Booking booking) {
        try {
            LocalDate endDate = LocalDate.now();
            Status end = Status.COMPLETED;
            bRepo.updateStatus(booking.getId(), end, endDate);
            reload();
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke afslutte booking", e);
        }
    }

    public void updateBooking(Booking booking) {
        try {
            bRepo.updateBooking(booking);
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke opdatere booking", e);
        }
    }

    //Opdatere listen med kalenderen
    private void reload(){
        try {
            calendar.clear();
            calendar.addAll(bRepo.getActiveCalendar());
        } catch (SQLException e) {
            throw new DataAccessException("Kunne ikke hente kalenderen fra DB: " + e.getMessage(), e);
        }
    }

    public Booking getBookingByIdDB(int id) {
        try {
            return bRepo.getBookingById(id);
        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke hente Bookingen fra DB: " + e.getMessage(), e);
        }
    }

}