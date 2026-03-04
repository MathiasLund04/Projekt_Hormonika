package Repository.Booking;

import DAL.DBConfig;
import Enums.Haircuts;
import Enums.Status;
import Exceptions.DataAccessException;
import Model.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLBookingRepository implements BookingRepository {
    public final DBConfig db;

    public MySQLBookingRepository(DBConfig db){
        this.db = db;
    }

    public List<Booking> getActiveCalendar() throws SQLException {
        List<Booking> bookings = new ArrayList<>();

        String sql = """
                SELECT
                       b.id,
                       c.name AS customer_name,
                       b.PhoneNum,
                       b.Date,
                       b.Time,
                       b.HaircutType,
                       h.name AS hairdresser_name,
                       b.hairdresser,
                       b.Description,
                       b.status,
                       b.duration_Minutes
                    FROM Booking b
                    JOIN Hairdresser h ON b.Hairdresser = h.id
                    JOIN Customer c ON b.PhoneNum = c.PhoneNUM
                    WHERE b.status = 'ACTIVE';
                """;

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {

                int id = rs.getInt("id");
                String name = rs.getString("customer_name");
                String phone = rs.getString("PhoneNum");
                LocalDate date = rs.getDate("Date").toLocalDate();
                LocalTime time = rs.getTime("Time").toLocalTime();
                Haircuts haircut = Haircuts.valueOf(rs.getString("HaircutType"));
                String hairdresser = rs.getString("hairdresser_name");
                int hairdresserId = rs.getInt("hairdresser");
                String description = rs.getString("Description");

                // Map database status → Java enum
                Status status = switch (rs.getString("status")) {
                    case "ACTIVE" -> Status.ACTIVE;
                    case "CANCEL" -> Status.CANCELLED;
                    case "FINISH" -> Status.COMPLETED;
                    default -> Status.ACTIVE;
                };

                bookings.add(new Booking(id, name, phone, date, time, haircut, hairdresserId, hairdresser, description, status));
            }
        } catch (SQLException e) {
            throw new SQLException("Kunne ikke hente aktive tidsbestillinger: " + e);
        }

        return bookings;
    }

    public void insertBooking(Booking booking) throws SQLException {
        String sql = """
            INSERT INTO Booking
            (PhoneNum, Date, Time, HaircutType, Hairdresser, Description, status, duration_Minutes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, booking.getPhoneNum());
            ps.setDate(2, Date.valueOf(booking.getDate()));
            ps.setTime(3, Time.valueOf(booking.getTime()));
            ps.setString(4, booking.getHaircutType().name());
            ps.setInt(5, booking.getHairdresserId());
            ps.setString(6, booking.getDescription());

            // Map Java enum → database enum
            String dbStatus = switch (booking.getStatus()) {
                case ACTIVE -> "ACTIVE";
                case CANCELLED -> "CANCEL";
                case COMPLETED -> "FINISH";
            };

            ps.setString(7, dbStatus);
            ps.setInt(8,booking.getHaircutType().getTime());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Fejl i tilføjelse af tidsbestilling: " + e.getMessage());
        }
    }

    public Booking getBookingById(int id) throws SQLException {
        String sql = """
                    SELECT
                       c.name AS customer_name,
                       b.PhoneNum,
                       b.Date,
                       b.Time,
                       b.HaircutType,
                       b.Hairdresser,
                       b.Description,
                       b.status,
                       b.duration_Minutes
                    FROM Booking b
                    JOIN Customer c ON b.PhoneNum = c.PhoneNUM
                    WHERE b.id = ?;
        """;

        try (Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    String name = rs.getString("customer_name");
                    String phone = rs.getString("PhoneNum");
                    LocalDate date = rs.getDate("Date").toLocalDate();
                    LocalTime time = rs.getTime("Time").toLocalTime();
                    Haircuts haircut = Haircuts.valueOf(rs.getString("HaircutType"));
                    int hairdresserId = rs.getInt("Hairdresser");
                    String description = rs.getString("Description");

                    Status status = switch (rs.getString("status")) {
                        case "ACTIVE" -> Status.ACTIVE;
                        case "CANCEL" -> Status.CANCELLED;
                        case "FINISH" -> Status.COMPLETED;
                        default -> Status.ACTIVE;
                    };

                    return new Booking(id, name, phone, date, time, haircut, hairdresserId, description, status);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Kunne ikke finde booking med id " + id + ": " + e.getMessage());
        }

        return null;
    }

    public void cancelBooking(Booking booking) throws SQLException {
        String sql = """
            UPDATE Booking
            SET status = 'CANCEL'
            WHERE id = ?
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, booking.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Kunne ikke aflyse tidsbestilling: " + e.getMessage());
        }
    }

    public void updateStatus(int bookingID, Status status, LocalDate cancelledAt) throws SQLException{
        String sql = """
                UPDATE booking
                set status = ?,
                cancelledAt = ?
                where booking.id = ?;
        """;


        try (Connection conn = db.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setDate(2, new java.sql.Date(cancelledAt.getDayOfMonth()));
            ps.setInt(3, bookingID);
        }
    }

    public void finishBooking(Booking booking) throws SQLException {
        String sql = """
            UPDATE Booking
            SET status = 'FINISH'
            WHERE id = ?
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, booking.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Kunne ikke færdiggøre tidsbestilling: " + e.getMessage());
        }
    }

    public int highestId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) AS maxId FROM Booking";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("maxId");
            }
        }

        return 0;
    }

    public void updateBooking(Booking booking) throws SQLException {
        String sql = """
            UPDATE Booking
            SET Date=?, Time=?, HaircutType=?, Hairdresser=?, Description=?, status=?
            WHERE id=?
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(booking.getDate()));
            stmt.setTime(2, Time.valueOf(booking.getTime()));
            stmt.setString(3, booking.getHaircutType().name());
            stmt.setInt(4, booking.getHairdresserId());
            stmt.setString(5, booking.getDescription());

            String dbStatus = switch (booking.getStatus()) {
                case ACTIVE -> "ACTIVE";
                case CANCELLED -> "CANCEL";
                case COMPLETED -> "FINISH";
            };

            stmt.setString(6, dbStatus);
            stmt.setInt(7, booking.getId());

            stmt.executeUpdate();
        }
    }
}
