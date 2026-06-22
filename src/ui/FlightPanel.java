package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import model.Flight;

public class FlightPanel extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DashboardFrame dashboardFrame;
    private final JTextField flightIdField;
    private final JTextField flightNumberField;
    private final JTextField sourceField;
    private final JTextField destinationField;
    private final JTextField departureTimeField;
    private final JTextField arrivalTimeField;
    private final JTextField availableSeatsField;
    private final JTextField priceField;
    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable flightTable;

    public FlightPanel(DashboardFrame dashboardFrame) {
        this.dashboardFrame = dashboardFrame;
        this.flightIdField = createTextField();
        this.flightNumberField = createTextField();
        this.sourceField = createTextField();
        this.destinationField = createTextField();
        this.departureTimeField = createTextField();
        this.arrivalTimeField = createTextField();
        this.availableSeatsField = createTextField();
        this.priceField = createTextField();
        this.searchField = createTextField();
        this.tableModel = createTableModel();
        this.flightTable = new JTable(tableModel);

        setLayout(new BorderLayout(18, 18));
        setBackground(DashboardFrame.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(30, 32, 30, 32));

        add(DashboardFrame.createPageHeader("Flight Management", "Create and maintain flight schedules."), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    public void loadFlights() {
        try {
            loadFlights(dashboardFrame.getFlightService().getAllFlights());
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
        formPanel.setPreferredSize(new Dimension(365, 0));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardFrame.BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        flightIdField.setEditable(false);
        addField(fieldsPanel, 0, "Flight ID", flightIdField);
        addField(fieldsPanel, 1, "Flight Number", flightNumberField);
        addField(fieldsPanel, 2, "Source", sourceField);
        addField(fieldsPanel, 3, "Destination", destinationField);
        addField(fieldsPanel, 4, "Departure (yyyy-MM-dd HH:mm)", departureTimeField);
        addField(fieldsPanel, 5, "Arrival (yyyy-MM-dd HH:mm)", arrivalTimeField);
        addField(fieldsPanel, 6, "Available Seats", availableSeatsField);
        addField(fieldsPanel, 7, "Price", priceField);

        JButton addButton = DashboardFrame.createSuccessButton("Add");
        JButton updateButton = DashboardFrame.createPrimaryButton("Update");
        JButton deleteButton = DashboardFrame.createDangerButton("Delete");
        JButton clearButton = DashboardFrame.createSecondaryButton("Clear");

        addButton.addActionListener(event -> addFlight());
        updateButton.addActionListener(event -> updateFlight());
        deleteButton.addActionListener(event -> deleteFlight());
        clearButton.addActionListener(event -> clearForm());

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel searchButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        searchButtons.setOpaque(false);
        JButton searchButton = DashboardFrame.createPrimaryButton("Search");
        JButton viewAllButton = DashboardFrame.createSecondaryButton("View All");
        searchButton.addActionListener(event -> searchFlights());
        viewAllButton.addActionListener(event -> loadFlights());
        searchButtons.add(searchButton);
        searchButtons.add(viewAllButton);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        DashboardFrame.styleTable(flightTable);
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(DashboardFrame.BORDER_COLOR));

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private void addField(JPanel panel, int row, String labelText, JTextField textField) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(DashboardFrame.TEXT_DARK);
        panel.add(label, DashboardFrame.formConstraints(row, 0));
        panel.add(textField, DashboardFrame.formConstraints(row, 1));
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardFrame.BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)));
        return textField;
    }

    private DefaultTableModel createTableModel() {
        String[] columns = {
                "ID", "Flight No", "Source", "Destination", "Departure", "Arrival", "Seats", "Price"
        };
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void loadFlights(List<Flight> flights) {
        tableModel.setRowCount(0);
        for (Flight flight : flights) {
            tableModel.addRow(new Object[] {
                    flight.getFlightId(),
                    flight.getFlightNumber(),
                    flight.getSource(),
                    flight.getDestination(),
                    formatDateTime(flight.getDepartureTime()),
                    formatDateTime(flight.getArrivalTime()),
                    flight.getAvailableSeats(),
                    flight.getPrice()
            });
        }
    }

    private void addFlight() {
        try {
            int flightId = dashboardFrame.getFlightService().addFlight(readFlightFromForm(false));
            JOptionPane.showMessageDialog(this, "Flight added successfully. Flight ID: " + flightId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadFlights();
            dashboardFrame.refreshAfterFlightChange();
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void updateFlight() {
        try {
            dashboardFrame.getFlightService().updateFlight(readFlightFromForm(true));
            JOptionPane.showMessageDialog(this, "Flight updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadFlights();
            dashboardFrame.refreshAfterFlightChange();
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void deleteFlight() {
        try {
            int flightId = readId(flightIdField.getText(), "Flight ID");
            int choice = JOptionPane.showConfirmDialog(this, "Delete selected flight?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dashboardFrame.getFlightService().deleteFlight(flightId);
                JOptionPane.showMessageDialog(this, "Flight deleted successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadFlights();
                dashboardFrame.refreshAfterFlightChange();
            }
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void searchFlights() {
        try {
            loadFlights(dashboardFrame.getFlightService().searchFlights(searchField.getText()));
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private Flight readFlightFromForm(boolean requireId) {
        Flight flight = new Flight();
        if (requireId) {
            flight.setFlightId(readId(flightIdField.getText(), "Flight ID"));
        }
        flight.setFlightNumber(flightNumberField.getText().trim());
        flight.setSource(sourceField.getText().trim());
        flight.setDestination(destinationField.getText().trim());
        flight.setDepartureTime(parseDateTime(departureTimeField.getText(), "Departure time"));
        flight.setArrivalTime(parseDateTime(arrivalTimeField.getText(), "Arrival time"));
        flight.setAvailableSeats(readInt(availableSeatsField.getText(), "Available seats"));
        flight.setPrice(readPrice(priceField.getText()));
        return flight;
    }

    private void populateFormFromSelection() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = flightTable.convertRowIndexToModel(selectedRow);
        flightIdField.setText(String.valueOf(tableModel.getValueAt(modelRow, 0)));
        flightNumberField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
        sourceField.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
        destinationField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
        departureTimeField.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
        arrivalTimeField.setText(String.valueOf(tableModel.getValueAt(modelRow, 5)));
        availableSeatsField.setText(String.valueOf(tableModel.getValueAt(modelRow, 6)));
        priceField.setText(String.valueOf(tableModel.getValueAt(modelRow, 7)));
    }

    private void clearForm() {
        flightIdField.setText("");
        flightNumberField.setText("");
        sourceField.setText("");
        destinationField.setText("");
        departureTimeField.setText("");
        arrivalTimeField.setText("");
        availableSeatsField.setText("");
        priceField.setText("");
        flightTable.clearSelection();
    }

    private int readId(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required. Select a row first.");
        }
        return readInt(value, fieldName);
    }

    private int readInt(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    private BigDecimal readPrice(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Price is required.");
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Price must be a valid amount.");
        }
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + " must use yyyy-MM-dd HH:mm format.");
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }
}
