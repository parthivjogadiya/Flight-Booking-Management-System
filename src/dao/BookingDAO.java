package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Booking;

public class BookingDAO {
    public int createBooking(int passengerId, int flightId) throws SQLException {
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            if (!passengerExists(connection, passengerId)) {
                throw new IllegalArgumentException("Passenger not found.");
            }

            Integer seats = getAvailableSeatsForUpdate(connection, flightId);
            if (seats == null) {
                throw new IllegalArgumentException("Flight not found.");
            }
            if (seats <= 0) {
                throw new IllegalStateException("No seats are available on this flight.");
            }

            decreaseSeat(connection, flightId);
            int bookingId = insertBooking(connection, passengerId, flightId);
            connection.commit();
            return bookingId;
        } catch (SQLException | RuntimeException exception) {
            rollback(connection);
            throw exception;
        } finally {
            closeConnection(connection);
        }
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            Booking booking = getBookingForUpdate(connection, bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking ID not found.");
            }
            if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
                throw new IllegalStateException("This booking is already cancelled.");
            }

            updateBookingStatus(connection, bookingId, "CANCELLED");
            increaseSeat(connection, booking.getFlightId());
            connection.commit();
            return true;
        } catch (SQLException | RuntimeException exception) {
            rollback(connection);
            throw exception;
        } finally {
            closeConnection(connection);
        }
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        String sql = bookingSelectSql() + " WHERE b.booking_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapBooking(resultSet);
                }
            }
        }
        return null;
    }

    public List<Booking> getAllBookings() throws SQLException {
        String sql = bookingSelectSql() + " ORDER BY b.booking_date DESC, b.booking_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return mapBookings(resultSet);
        }
    }

    public List<Booking> searchBookings(String keyword) throws SQLException {
        String sql = bookingSelectSql() + """
                 WHERE CAST(b.booking_id AS CHAR) LIKE ?
                    OR p.passenger_name LIKE ?
                    OR f.flight_number LIKE ?
                    OR b.booking_status LIKE ?
                    OR f.source LIKE ?
                    OR f.destination LIKE ?
                 ORDER BY b.booking_date DESC, b.booking_id DESC
                """;
        String likeKeyword = "%" + keyword + "%";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 6; index++) {
                statement.setString(index, likeKeyword);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapBookings(resultSet);
            }
        }
    }

    public int getTotalBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    private boolean passengerExists(Connection connection, int passengerId) throws SQLException {
        String sql = "SELECT passenger_id FROM passengers WHERE passenger_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, passengerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Integer getAvailableSeatsForUpdate(Connection connection, int flightId) throws SQLException {
        String sql = "SELECT available_seats FROM flights WHERE flight_id = ? FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, flightId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("available_seats");
                }
            }
        }
        return null;
    }

    private void decreaseSeat(Connection connection, int flightId) throws SQLException {
        String sql = "UPDATE flights SET available_seats = available_seats - 1 WHERE flight_id = ? AND available_seats > 0";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, flightId);
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException("No seats are available on this flight.");
            }
        }
    }

    private void increaseSeat(Connection connection, int flightId) throws SQLException {
        String sql = "UPDATE flights SET available_seats = available_seats + 1 WHERE flight_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, flightId);
            statement.executeUpdate();
        }
    }

    private int insertBooking(Connection connection, int passengerId, int flightId) throws SQLException {
        String sql = """
                INSERT INTO bookings (passenger_id, flight_id, booking_date, booking_status)
                VALUES (?, ?, NOW(), 'CONFIRMED')
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, passengerId);
            statement.setInt(2, flightId);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Booking was created, but the generated booking ID could not be read.");
    }

    private Booking getBookingForUpdate(Connection connection, int bookingId) throws SQLException {
        String sql = "SELECT booking_id, passenger_id, flight_id, booking_date, booking_status FROM bookings WHERE booking_id = ? FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Booking(
                            resultSet.getInt("booking_id"),
                            resultSet.getInt("passenger_id"),
                            resultSet.getInt("flight_id"),
                            resultSet.getTimestamp("booking_date").toLocalDateTime(),
                            resultSet.getString("booking_status"));
                }
            }
        }
        return null;
    }

    private void updateBookingStatus(Connection connection, int bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = ? WHERE booking_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, bookingId);
            statement.executeUpdate();
        }
    }

    private String bookingSelectSql() {
        return """
                SELECT b.booking_id, b.passenger_id, b.flight_id, b.booking_date, b.booking_status,
                       p.passenger_name, f.flight_number, f.source, f.destination
                FROM bookings b
                INNER JOIN passengers p ON b.passenger_id = p.passenger_id
                INNER JOIN flights f ON b.flight_id = f.flight_id
                """;
    }

    private List<Booking> mapBookings(ResultSet resultSet) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        while (resultSet.next()) {
            bookings.add(mapBooking(resultSet));
        }
        return bookings;
    }

    private Booking mapBooking(ResultSet resultSet) throws SQLException {
        Timestamp bookingTimestamp = resultSet.getTimestamp("booking_date");
        Booking booking = new Booking(
                resultSet.getInt("booking_id"),
                resultSet.getInt("passenger_id"),
                resultSet.getInt("flight_id"),
                bookingTimestamp == null ? null : bookingTimestamp.toLocalDateTime(),
                resultSet.getString("booking_status"));
        booking.setPassengerName(resultSet.getString("passenger_name"));
        booking.setFlightNumber(resultSet.getString("flight_number"));
        booking.setSource(resultSet.getString("source"));
        booking.setDestination(resultSet.getString("destination"));
        return booking;
    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // Preserve the original exception for the caller.
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException ignored) {
                // Closing problems are secondary to the operation result.
            }
        }
    }
}
