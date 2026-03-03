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
                       b.Description,
                       b.status,
                       b.duration_Minutes
                   FROM Booking b
                   JOIN Hairdresser h ON b.Hairdresser = h.id
                   JOIN Customer c ON b.PhoneNum = c.phoneNUM
                    Where b.status = 'ACTIVE';
                """;

        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("customer_name");
                String phoneNr = rs.getString("PhoneNum");
                LocalDate date = rs.getDate("Date").toLocalDate();
                LocalTime time = rs.getTime("Time").toLocalTime();
                Haircuts hairStyle = Haircuts.valueOf(rs.getString("HaircutType"));
                String hairdresser =  rs.getString("hairdresser_name");
                String description = rs.getString("Description");
                Status status = Status.valueOf(rs.getString("status"));
                int duration_Minutes = rs.getInt("duration_Minutes");

                Booking adding = new Booking(id, name, phoneNr, date, time, hairStyle, hairdresser, description, status, duration_Minutes);
                bookings.add(adding);
            }
            return bookings;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void insertBooking(Booking booking) throws SQLException {
        String sql = """
            INSERT INTO Booking
            (PhoneNum, Date, Time, HaircutType, Hairdresser, Description, status, duration_Minutes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, booking.getPhoneNr());
            ps.setObject(2, booking.getDate());
            ps.setObject(3, booking.getTime());
            ps.setString(4, booking.getHaircutType().name());
            ps.setInt(5, booking.getHairdresserId());
            ps.setString(6,booking.getDescription());
            ps.setString(7,booking.getStatus().name());
            ps.setInt(8,booking.getHaircutType().getTime());
            ps.executeUpdate();

        } catch (SQLException e){
            throw new SQLException("Fejl i tilføjelse af tidsbestilling"); //WIP
        }
    }

    public Booking getBookingById(int id) {
        String sql = """
                SELECT
                       c.name AS customer_name,
                       b.PhoneNum,
                       b.Date,
                       b.Time,
                       b.HaircutType,
                       h.name AS hairdresser_name,
                       b.Description,
                       b.status,
                       b.duration_Minutes
                   FROM Booking b
                   JOIN Hairdresser h ON b.Hairdresser = h.id
                   JOIN Customer c ON b.PhoneNum = c.phoneNUM
                   WHERE b.id = ?;
                """;

        try (Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("customer_name");
                    String phoneNum = rs.getString("phoneNum");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    LocalTime time = rs.getTime("time").toLocalTime();
                    Haircuts haircutType = Haircuts.valueOf(rs.getString("HaircutType"));
                    String hairdresser = rs.getString("hairdresser_name");
                    String description = rs.getString("Description");
                    Status status = Status.valueOf(rs.getString("status"));
                    int duration = rs.getInt("duration_Minutes");

                    Booking resultat = new Booking(id, name, phoneNum, date, time, haircutType, hairdresser, description, status, duration);
                    return resultat;
                }
            } catch (SQLException e){
                throw new SQLException("Kunne ikke indlæse data");
            }

            } catch (SQLException e){
            throw new DataAccessException("Kunne ikke finde bookingen!" + e);
        }
        return null; //Returner anden besked?
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


    public int highestId(){
        String sql = """
                SELECT max(booking.id) FROM booking;
        """;
        try (Connection conn = db.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            while (rs.next()){
                    if (rs.getInt("id") == 0 ){
                        return 1;
                    }
                return rs.getInt(1);
            }
        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke oprette id til denne booking");
        }
        return 0;
    }

    public List<Booking> findAll() throws SQLException{
        String sql = """
                SELECT
                       b.id,
                       c.name AS customer_name,
                       b.PhoneNum,
                       b.Date,
                       b.Time,
                       b.HaircutType,
                       h.name AS hairdresser_name,
                       b.Description,
                       b.status,
                       b.duration_Minutes
                   FROM Booking b
                   JOIN Hairdresser h ON b.Hairdresser = h.id 
                   JOIN Customer c ON b.PhoneNum = c.phoneNUM;
                """;

        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

                while (rs.next()){
                    int id = rs.getInt("id");
                    String name = rs.getString("customer_name");
                    String phoneNr = rs.getString("PhoneNum");
                    LocalDate date = rs.getDate("Date").toLocalDate();
                    LocalTime time = rs.getTime("Time").toLocalTime();
                    Haircuts hairStyle = Haircuts.valueOf(rs.getString("HaircutType"));
                    String hairdresser =  rs.getString("hairdresser_name");
                    String description = rs.getString("Description");
                    Status status = Status.valueOf(rs.getString("status"));
                    int duration_minutes = rs.getInt("duration_Minutes");

                    Booking adding = new Booking(id, name, phoneNr, date, time, hairStyle, hairdresser, description, status, duration_minutes);
                    bookings.add(adding);
                }

        } catch (SQLException e){
            throw new SQLException("Kunne ikke finde aktive bookinger");
        }
        return bookings;
    }
}
