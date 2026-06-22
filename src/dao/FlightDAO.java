package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Flight;

public class FlightDAO {
    public int addFlight(Flight flight) throws SQLException {
        String sql = """
                INSERT INTO flights
                    (flight_number, source, destination, departure_time, arrival_time, available_seats, price)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setFlightParameters(statement, flight, false);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean updateFlight(Flight flight) throws SQLException {
        String sql = """
                UPDATE flights
                SET flight_number = ?, source = ?, destination = ?, departure_time = ?,
                    arrival_time = ?, available_seats = ?, price = ?
                WHERE flight_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            setFlightParameters(statement, flight, true);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteFlight(int flightId) throws SQLException {
        String sql = "DELETE FROM flights WHERE flight_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, flightId);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new SQLException("This flight has booking history and cannot be deleted.", exception);
        }
    }

    public Flight getFlightById(int flightId) throws SQLException {
        String sql = "SELECT * FROM flights WHERE flight_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, flightId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapFlight(resultSet);
                }
            }
        }
        return null;
    }

    public List<Flight> getAllFlights() throws SQLException {
        String sql = "SELECT * FROM flights ORDER BY departure_time ASC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return mapFlights(resultSet);
        }
    }

    public List<Flight> searchFlights(String keyword) throws SQLException {
        String sql = """
                SELECT * FROM flights
                WHERE flight_number LIKE ?
                   OR source LIKE ?
                   OR destination LIKE ?
                   OR CAST(flight_id AS CHAR) LIKE ?
                ORDER BY departure_time ASC
                """;
        String likeKeyword = "%" + keyword + "%";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 4; index++) {
                statement.setString(index, likeKeyword);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapFlights(resultSet);
            }
        }
    }

    public int getTotalFlights() throws SQLException {
        String sql = "SELECT COUNT(*) FROM flights";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    private void setFlightParameters(PreparedStatement statement, Flight flight, boolean includeId) throws SQLException {
        statement.setString(1, flight.getFlightNumber());
        statement.setString(2, flight.getSource());
        statement.setString(3, flight.getDestination());
        statement.setTimestamp(4, Timestamp.valueOf(flight.getDepartureTime()));
        statement.setTimestamp(5, Timestamp.valueOf(flight.getArrivalTime()));
        statement.setInt(6, flight.getAvailableSeats());
        statement.setBigDecimal(7, flight.getPrice());
        if (includeId) {
            statement.setInt(8, flight.getFlightId());
        }
    }

    private List<Flight> mapFlights(ResultSet resultSet) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        while (resultSet.next()) {
            flights.add(mapFlight(resultSet));
        }
        return flights;
    }

    private Flight mapFlight(ResultSet resultSet) throws SQLException {
        return new Flight(
                resultSet.getInt("flight_id"),
                resultSet.getString("flight_number"),
                resultSet.getString("source"),
                resultSet.getString("destination"),
                resultSet.getTimestamp("departure_time").toLocalDateTime(),
                resultSet.getTimestamp("arrival_time").toLocalDateTime(),
                resultSet.getInt("available_seats"),
                resultSet.getBigDecimal("price"));
    }
}
