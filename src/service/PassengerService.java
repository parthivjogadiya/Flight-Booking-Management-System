package service;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import dao.PassengerDAO;
import model.Passenger;

public class PassengerService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?[0-9]{7,15}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final PassengerDAO passengerDAO;

    public PassengerService() {
        this.passengerDAO = new PassengerDAO();
    }

    public int addPassenger(Passenger passenger) throws SQLException {
        validatePassenger(passenger, false);
        return passengerDAO.addPassenger(passenger);
    }

    public boolean updatePassenger(Passenger passenger) throws SQLException {
        validatePassenger(passenger, true);
        boolean updated = passengerDAO.updatePassenger(passenger);
        if (!updated) {
            throw new IllegalArgumentException("Passenger not found.");
        }
        return true;
    }

    public boolean deletePassenger(int passengerId) throws SQLException {
        validatePositiveId(passengerId, "Passenger ID");
        boolean deleted = passengerDAO.deletePassenger(passengerId);
        if (!deleted) {
            throw new IllegalArgumentException("Passenger not found.");
        }
        return true;
    }

    public Passenger getPassengerById(int passengerId) throws SQLException {
        validatePositiveId(passengerId, "Passenger ID");
        return passengerDAO.getPassengerById(passengerId);
    }

    public List<Passenger> getAllPassengers() throws SQLException {
        return passengerDAO.getAllPassengers();
    }

    public List<Passenger> searchPassengers(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPassengers();
        }
        return passengerDAO.searchPassengers(keyword.trim());
    }

    public int getTotalPassengers() throws SQLException {
        return passengerDAO.getTotalPassengers();
    }

    private void validatePassenger(Passenger passenger, boolean requireId) {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger details are required.");
        }
        if (requireId) {
            validatePositiveId(passenger.getPassengerId(), "Passenger ID");
        }
        requireText(passenger.getPassengerName(), "Passenger name");
        requireText(passenger.getPhoneNumber(), "Phone number");
        requireText(passenger.getEmail(), "Email");

        if (!PHONE_PATTERN.matcher(passenger.getPhoneNumber().trim()).matches()) {
            throw new IllegalArgumentException("Phone number must contain 7 to 15 digits.");
        }
        if (!EMAIL_PATTERN.matcher(passenger.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number.");
        }
    }
}
