package service;

import java.sql.SQLException;
import java.util.List;

import dao.BookingDAO;
import model.Booking;

public class BookingService {
    private final BookingDAO bookingDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
    }

    public int createBooking(int passengerId, int flightId) throws SQLException {
        validatePositiveId(passengerId, "Passenger ID");
        validatePositiveId(flightId, "Flight ID");
        return bookingDAO.createBooking(passengerId, flightId);
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        validatePositiveId(bookingId, "Booking ID");
        return bookingDAO.cancelBooking(bookingId);
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        validatePositiveId(bookingId, "Booking ID");
        return bookingDAO.getBookingById(bookingId);
    }

    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.getAllBookings();
    }

    public List<Booking> searchBookings(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBookings();
        }
        return bookingDAO.searchBookings(keyword.trim());
    }

    public int getTotalBookings() throws SQLException {
        return bookingDAO.getTotalBookings();
    }

    private void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number.");
        }
    }
}
