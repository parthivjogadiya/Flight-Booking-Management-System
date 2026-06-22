package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import model.Booking;
import model.Flight;
import model.Passenger;

public class BookingPanel extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DashboardFrame dashboardFrame;
    private final JComboBox<PassengerItem> passengerComboBox;
    private final JComboBox<FlightItem> flightComboBox;
    private final JTextField bookingIdField;
    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable bookingTable;

    public BookingPanel(DashboardFrame dashboardFrame) {
        this.dashboardFrame = dashboardFrame;
        this.passengerComboBox = new JComboBox<>();
        this.flightComboBox = new JComboBox<>();
        this.bookingIdField = createTextField();
        this.searchField = createTextField();
        this.tableModel = createTableModel();
        this.bookingTable = new JTable(tableModel);

        setLayout(new BorderLayout(18, 18));
        setBackground(DashboardFrame.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(30, 32, 30, 32));

        add(DashboardFrame.createPageHeader("Booking Management", "Create, cancel, and search reservations."),
                BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    public void loadReferenceData() {
        try {
            loadPassengersIntoComboBox(dashboardFrame.getPassengerService().getAllPassengers());
            loadFlightsIntoComboBox(dashboardFrame.getFlightService().getAllFlights());
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    public void loadBookings() {
        try {
            loadBookings(dashboardFrame.getBookingService().getAllBookings());
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(18, 18));
        mainPanel.setOpaque(false);
        mainPanel.add(createFormPanel(), BorderLayout.WEST);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout(0, 16));
        formPanel.setBackground(DashboardFrame.CARD_BACKGROUND);
        formPanel.setPreferredSize(new Dimension(420, 0));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardFrame.BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        bookingIdField.setEditable(false);
        styleComboBox(passengerComboBox);
        styleComboBox(flightComboBox);

        addComboField(fieldsPanel, 0, "Passenger", passengerComboBox);
        addComboField(fieldsPanel, 1, "Flight", flightComboBox);
        addTextField(fieldsPanel, 2, "Booking ID", bookingIdField);

        JButton createButton = DashboardFrame.createSuccessButton("Create Booking");
        JButton cancelButton = DashboardFrame.createDangerButton("Cancel Booking");
        JButton clearButton = DashboardFrame.createSecondaryButton("Clear");

        createButton.addActionListener(event -> createBooking());
        cancelButton.addActionListener(event -> cancelBooking());
        clearButton.addActionListener(event -> clearForm());

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);

        formPanel.add(fieldsPanel, BorderLayout.NORTH);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = DashboardFrame.createPrimaryButton("Search");
        JButton historyButton = DashboardFrame.createSecondaryButton("View History");
        searchButton.addActionListener(event -> searchBookings());
        historyButton.addActionListener(event -> loadBookings());

        JPanel searchButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        searchButtons.setOpaque(false);
        searchButtons.add(searchButton);
        searchButtons.add(historyButton);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        DashboardFrame.styleTable(bookingTable);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(DashboardFrame.BORDER_COLOR));

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private void addTextField(JPanel panel, int row, String labelText, JTextField textField) {
        JLabel label = createLabel(labelText);
        panel.add(label, DashboardFrame.formConstraints(row, 0));
        panel.add(textField, DashboardFrame.formConstraints(row, 1));
    }

    private void addComboField(JPanel panel, int row, String labelText, JComboBox<?> comboBox) {
        JLabel label = createLabel(labelText);
        panel.add(label, DashboardFrame.formConstraints(row, 0));
        panel.add(comboBox, DashboardFrame.formConstraints(row, 1));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(DashboardFrame.TEXT_DARK);
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardFrame.BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)));
        return textField;
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(DashboardFrame.CARD_BACKGROUND);
        comboBox.setBorder(BorderFactory.createLineBorder(DashboardFrame.BORDER_COLOR));
    }

    private DefaultTableModel createTableModel() {
        String[] columns = {
                "Booking ID", "Passenger ID", "Passenger", "Flight ID", "Flight No", "Route", "Booking Date", "Status"
        };
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void loadPassengersIntoComboBox(List<Passenger> passengers) {
        DefaultComboBoxModel<PassengerItem> model = new DefaultComboBoxModel<>();
        for (Passenger passenger : passengers) {
            model.addElement(new PassengerItem(passenger.getPassengerId(), passenger.getPassengerName()));
        }
        passengerComboBox.setModel(model);
    }

    private void loadFlightsIntoComboBox(List<Flight> flights) {
        DefaultComboBoxModel<FlightItem> model = new DefaultComboBoxModel<>();
        for (Flight flight : flights) {
            model.addElement(new FlightItem(
                    flight.getFlightId(),
                    flight.getFlightNumber(),
                    flight.getSource(),
                    flight.getDestination(),
                    flight.getAvailableSeats(),
                    flight.getPrice()));
        }
        flightComboBox.setModel(model);
    }

    private void loadBookings(List<Booking> bookings) {
        tableModel.setRowCount(0);
        for (Booking booking : bookings) {
            tableModel.addRow(new Object[] {
                    booking.getBookingId(),
                    booking.getPassengerId(),
                    booking.getPassengerName(),
                    booking.getFlightId(),
                    booking.getFlightNumber(),
                    booking.getSource() + " -> " + booking.getDestination(),
                    formatDateTime(booking.getBookingDate()),
                    booking.getBookingStatus()
            });
        }
    }

    private void createBooking() {
        try {
            PassengerItem passenger = (PassengerItem) passengerComboBox.getSelectedItem();
            FlightItem flight = (FlightItem) flightComboBox.getSelectedItem();
            if (passenger == null) {
                throw new IllegalArgumentException("Add a passenger before creating a booking.");
            }
            if (flight == null) {
                throw new IllegalArgumentException("Add a flight before creating a booking.");
            }

            int bookingId = dashboardFrame.getBookingService().createBooking(passenger.getPassengerId(),
                    flight.getFlightId());
            JOptionPane.showMessageDialog(this, "Booking created successfully. Booking ID: " + bookingId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadBookings();
            dashboardFrame.refreshAfterBookingChange();
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void cancelBooking() {
        try {
            int bookingId = readId(bookingIdField.getText(), "Booking ID");
            int choice = JOptionPane.showConfirmDialog(this, "Cancel selected booking?", "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dashboardFrame.getBookingService().cancelBooking(bookingId);
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadBookings();
                dashboardFrame.refreshAfterBookingChange();
            }
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void searchBookings() {
        try {
            loadBookings(dashboardFrame.getBookingService().searchBookings(searchField.getText()));
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void populateFormFromSelection() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
        bookingIdField.setText(String.valueOf(tableModel.getValueAt(modelRow, 0)));
    }

    private void clearForm() {
        bookingIdField.setText("");
        bookingTable.clearSelection();
        if (passengerComboBox.getItemCount() > 0) {
            passengerComboBox.setSelectedIndex(0);
        }
        if (flightComboBox.getItemCount() > 0) {
            flightComboBox.setSelectedIndex(0);
        }
    }

    private int readId(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required. Select a booking row first.");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private static class PassengerItem {
        private final int passengerId;
        private final String passengerName;

        PassengerItem(int passengerId, String passengerName) {
            this.passengerId = passengerId;
            this.passengerName = passengerName;
        }

        int getPassengerId() {
            return passengerId;
        }

        @Override
        public String toString() {
            return passengerId + " - " + passengerName;
        }
    }

    private static class FlightItem {
        private final int flightId;
        private final String flightNumber;
        private final String source;
        private final String destination;
        private final int availableSeats;
        private final BigDecimal price;

        FlightItem(int flightId, String flightNumber, String source, String destination, int availableSeats,
                BigDecimal price) {
            this.flightId = flightId;
            this.flightNumber = flightNumber;
            this.source = source;
            this.destination = destination;
            this.availableSeats = availableSeats;
            this.price = price;
        }

        int getFlightId() {
            return flightId;
        }

        @Override
        public String toString() {
            return flightId + " - " + flightNumber + " | " + source + " -> " + destination
                    + " | Seats: " + availableSeats + " | Rs. " + price;
        }
    }
}
