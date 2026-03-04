package Repository.Booking;

import Enums.Status;
import Model.Booking;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository {
    List<Booking> getActiveCalendar() throws SQLException;
    void insertBooking(Booking booking) throws SQLException;
    Booking getBookingById(int id) throws SQLException;
    void cancelBooking(Booking booking) throws SQLException;
    void finishBooking(Booking booking) throws SQLException;
    int highestId() throws SQLException;
    void updateStatus(int BookingID, Status status, LocalDate date) throws SQLException;
    void updateBooking(Booking booking) throws SQLException;
}
