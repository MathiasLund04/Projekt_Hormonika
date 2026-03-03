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
    private final CustomerService customerService;

    private final List<Booking> calendar;

    public BookingService(BookingRepository bRepo, DBConfig db) {
        this.db = db;
        this.bRepo = bRepo;
        CustomerRepository cRepo = new MySQLCustomerRepository(db);
        this.customerService = new CustomerService(cRepo,db);
        try {
            calendar = new ArrayList<>(bRepo.getActiveCalendar());
        } catch (SQLException e) {
            throw new DataAccessException("Fejl i indlæsning af DB: " + e.getMessage(), e);
        }
    }

    public Booking createBooking(String name, String phoneNr, LocalDate date, LocalTime time,
                                 Haircuts haircutType, int hairdresserId,
                                 String description) {

        // Validering
        if (!validateBookingTime(date, time, hairdresserId)) {
            return null;
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Navn skal udfyldes");
        }
        if (phoneNr == null || phoneNr.trim().isEmpty()) {
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

        // Opret kunde hvis ny
        customerService.createCustomerIfNotExist(name, phoneNr);

        // Opret booking
        Booking newBooking = new Booking(
                name,
                phoneNr,
                date,
                time,
                haircutType,
                hairdresserId,
                description
        );

        calendar.add(newBooking);
        addBookingDB(newBooking);
        return newBooking;
    }


    //Validering til at sikre at en medarbejder ikke kan dobbeltbookes
    private boolean validateBookingTime(LocalDate date, LocalTime time, int hairdresserId){
        for (Booking b : calendar) {

            boolean sameHairdresser = b.getHairdresserId() == hairdresserId;
            boolean sameDate = b.getDate().equals(date);
            boolean sameTime = b.getTime().equals(time);
            //Ekstra for lige at tjekke at tidsbestillingen stadig er aktiv
            boolean stillActive = b.getStatus().equals(Status.ACTIVE);

            if (sameHairdresser && sameDate && sameTime && stillActive){
                //Dette fortæller at frisøren allerede er booket på denne tid (til en der stadig er aktiv)
                return false;
            }
        }
        return true;
    }

    public List<Booking> getCalendar(){
        reload();
        return new ArrayList<>(calendar);
    }

    public void reload(){
        try {
            calendar.clear();
            calendar.addAll(bRepo.getActiveCalendar());
        } catch (SQLException e) {
            throw new DataAccessException("Kunne ikke hente kalenderen fra DB");
        }
    }

    public void addBookingDB(Booking booking){
        try {
            bRepo.insertBooking(booking);
            reload();
        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke indsætte booking i DB");//Returner en besked i konsollen WIP
        }
    }

    public void cancelBookingDB(Booking booking){
        try {
            LocalDate endDate = LocalDate.now();
            Status cancel = Status.CANCELLED;
            bRepo.updateStatus(booking.getId(), cancel, endDate);
            reload();
        } catch (SQLException e){
            //WIP
        }
    }

    public void finishBookingDB(Booking booking){
        try {
            LocalDate endDate = LocalDate.now();
            Status end = Status.COMPLETED;
            bRepo.updateStatus(booking.getId(), end, endDate);
            reload();
        } catch (SQLException e){
            //WIP
        }
    }


}
