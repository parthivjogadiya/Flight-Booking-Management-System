package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import model.Passenger;

public class PassengerDAO {
    public int addPassenger(Passenger passenger) throws SQLException {
        String sql = "INSERT INTO passengers (passenger_name, phone_number, email) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, passenger.getPassengerName());
            statement.setString(2, passenger.getPhoneNumber());
            statement.setString(3, passenger.getEmail());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean updatePassenger(Passenger passenger) throws SQLException {
        String sql = """
                UPDATE passengers
                SET passenger_name = ?, phone_number = ?, email = ?
                WHERE passenger_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passenger.getPassengerName());
            statement.setString(2, passenger.getPhoneNumber());
            statement.setString(3, passenger.getEmail());
            statement.setInt(4, passenger.getPassengerId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deletePassenger(int passengerId) throws SQLException {
        String sql = "DELETE FROM passengers WHERE passenger_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, passengerId);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new SQLException("This passenger has booking history and cannot be deleted.", exception);
        }
    }

    public Passenger getPassengerById(int passengerId) throws SQLException {
        String sql = "SELECT * FROM passengers WHERE passenger_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, passengerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPassenger(resultSet);
                }
            }
        }
        return null;
    }

    public List<Passenger> getAllPassengers() throws SQLException {
        String sql = "SELECT * FROM passengers ORDER BY passenger_name ASC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return mapPassengers(resultSet);
        }
    }

    public List<Passenger> searchPassengers(String keyword) throws SQLException {
        String sql = """
                SELECT * FROM passengers
                WHERE passenger_name LIKE ?
                   OR phone_number LIKE ?
                   OR email LIKE ?
                   OR CAST(passenger_id AS CHAR) LIKE ?
                ORDER BY passenger_name ASC
                """;
        String likeKeyword = "%" + keyword + "%";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 4; index++) {
                statement.setString(index, likeKeyword);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapPassengers(resultSet);
            }
        }
    }

    public int getTotalPassengers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM passengers";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    private List<Passenger> mapPassengers(ResultSet resultSet) throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        while (resultSet.next()) {
            passengers.add(mapPassenger(resultSet));
        }
        return passengers;
    }

    private Passenger mapPassenger(ResultSet resultSet) throws SQLException {
        return new Passenger(
                resultSet.getInt("passenger_id"),
                resultSet.getString("passenger_name"),
                resultSet.getString("phone_number"),
                resultSet.getString("email"));
    }
}
