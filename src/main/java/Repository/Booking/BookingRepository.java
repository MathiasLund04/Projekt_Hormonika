package Repository.Booking;

import Enums.Status;
import Model.Booking;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository {
    List<Booking> getCalendar() throws SQLException;
    void insertBooking(Booking booking) throws SQLException;
    Booking getBookingById(int id);
    int highestId();
    List<Booking> findActive() throws SQLException;
    void updateStatus(int BookingID, Status status, LocalDate date) throws SQLException;
}
