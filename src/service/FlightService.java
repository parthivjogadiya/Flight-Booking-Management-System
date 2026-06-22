package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import dao.FlightDAO;
import model.Flight;

public class FlightService {
    private final FlightDAO flightDAO;

    public FlightService() {
        this.flightDAO = new FlightDAO();
    }

    public int addFlight(Flight flight) throws SQLException {
        validateFlight(flight, false);
        return flightDAO.addFlight(flight);
    }

    public boolean updateFlight(Flight flight) throws SQLException {
        validateFlight(flight, true);
        boolean updated = flightDAO.updateFlight(flight);
        if (!updated) {
            throw new IllegalArgumentException("Flight not found.");
        }
        return true;
    }

    public boolean deleteFlight(int flightId) throws SQLException {
        validatePositiveId(flightId, "Flight ID");
        boolean deleted = flightDAO.deleteFlight(flightId);
        if (!deleted) {
            throw new IllegalArgumentException("Flight not found.");
        }
        return true;
    }

    public Flight getFlightById(int flightId) throws SQLException {
        validatePositiveId(flightId, "Flight ID");
        return flightDAO.getFlightById(flightId);
    }

    public List<Flight> getAllFlights() throws SQLException {
        return flightDAO.getAllFlights();
    }

    public List<Flight> searchFlights(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllFlights();
        }
        return flightDAO.searchFlights(keyword.trim());
    }

    public int getTotalFlights() throws SQLException {
        return flightDAO.getTotalFlights();
    }

    private void validateFlight(Flight flight, boolean requireId) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight details are required.");
        }
        if (requireId) {
            validatePositiveId(flight.getFlightId(), "Flight ID");
        }
        requireText(flight.getFlightNumber(), "Flight number");
        requireText(flight.getSource(), "Source");
        requireText(flight.getDestination(), "Destination");
        if (flight.getSource().trim().equalsIgnoreCase(flight.getDestination().trim())) {
            throw new IllegalArgumentException("Source and destination cannot be the same.");
        }
        if (flight.getDepartureTime() == null) {
            throw new IllegalArgumentException("Departure time is required.");
        }
        if (flight.getArrivalTime() == null) {
            throw new IllegalArgumentException("Arrival time is required.");
        }
        if (!flight.getArrivalTime().isAfter(flight.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time must be after departure time.");
        }
        if (flight.getAvailableSeats() < 0) {
            throw new IllegalArgumentException("Available seats cannot be negative.");
        }
        BigDecimal price = flight.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
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
