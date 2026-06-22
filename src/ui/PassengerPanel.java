package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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

import model.Passenger;

public class PassengerPanel extends JPanel {
    private final DashboardFrame dashboardFrame;
    private final JTextField passengerIdField;
    private final JTextField passengerNameField;
    private final JTextField phoneNumberField;
    private final JTextField emailField;
    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable passengerTable;

    public PassengerPanel(DashboardFrame dashboardFrame) {
        this.dashboardFrame = dashboardFrame;
        this.passengerIdField = createTextField();
        this.passengerNameField = createTextField();
        this.phoneNumberField = createTextField();
        this.emailField = createTextField();
        this.searchField = createTextField();
        this.tableModel = createTableModel();
        this.passengerTable = new JTable(tableModel);

        setLayout(new BorderLayout(18, 18));
        setBackground(DashboardFrame.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(30, 32, 30, 32));

        add(DashboardFrame.createPageHeader("Passenger Management", "Maintain passenger contact records."),
                BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    public void loadPassengers() {
        try {
            loadPassengers(dashboardFrame.getPassengerService().getAllPassengers());
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

        passengerIdField.setEditable(false);
        addField(fieldsPanel, 0, "Passenger ID", passengerIdField);
        addField(fieldsPanel, 1, "Passenger Name", passengerNameField);
        addField(fieldsPanel, 2, "Phone Number", phoneNumberField);
        addField(fieldsPanel, 3, "Email", emailField);

        JButton addButton = DashboardFrame.createSuccessButton("Add");
        JButton updateButton = DashboardFrame.createPrimaryButton("Update");
        JButton deleteButton = DashboardFrame.createDangerButton("Delete");
        JButton clearButton = DashboardFrame.createSecondaryButton("Clear");

        addButton.addActionListener(event -> addPassenger());
        updateButton.addActionListener(event -> updatePassenger());
        deleteButton.addActionListener(event -> deletePassenger());
        clearButton.addActionListener(event -> clearForm());

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
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
        JButton viewAllButton = DashboardFrame.createSecondaryButton("View All");
        searchButton.addActionListener(event -> searchPassengers());
        viewAllButton.addActionListener(event -> loadPassengers());

        JPanel searchButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        searchButtons.setOpaque(false);
        searchButtons.add(searchButton);
        searchButtons.add(viewAllButton);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        DashboardFrame.styleTable(passengerTable);
        passengerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passengerTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(passengerTable);
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
        String[] columns = { "ID", "Passenger Name", "Phone Number", "Email" };
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void loadPassengers(List<Passenger> passengers) {
        tableModel.setRowCount(0);
        for (Passenger passenger : passengers) {
            tableModel.addRow(new Object[] {
                    passenger.getPassengerId(),
                    passenger.getPassengerName(),
                    passenger.getPhoneNumber(),
                    passenger.getEmail()
            });
        }
    }

    private void addPassenger() {
        try {
            int passengerId = dashboardFrame.getPassengerService().addPassenger(readPassengerFromForm(false));
            JOptionPane.showMessageDialog(this, "Passenger added successfully. Passenger ID: " + passengerId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadPassengers();
            dashboardFrame.refreshAfterPassengerChange();
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void updatePassenger() {
        try {
            dashboardFrame.getPassengerService().updatePassenger(readPassengerFromForm(true));
            JOptionPane.showMessageDialog(this, "Passenger updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadPassengers();
            dashboardFrame.refreshAfterPassengerChange();
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void deletePassenger() {
        try {
            int passengerId = readId(passengerIdField.getText(), "Passenger ID");
            int choice = JOptionPane.showConfirmDialog(this, "Delete selected passenger?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dashboardFrame.getPassengerService().deletePassenger(passengerId);
                JOptionPane.showMessageDialog(this, "Passenger deleted successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadPassengers();
                dashboardFrame.refreshAfterPassengerChange();
            }
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private void searchPassengers() {
        try {
            loadPassengers(dashboardFrame.getPassengerService().searchPassengers(searchField.getText()));
        } catch (Exception exception) {
            DashboardFrame.showError(this, exception);
        }
    }

    private Passenger readPassengerFromForm(boolean requireId) {
        Passenger passenger = new Passenger();
        if (requireId) {
            passenger.setPassengerId(readId(passengerIdField.getText(), "Passenger ID"));
        }
        passenger.setPassengerName(passengerNameField.getText().trim());
        passenger.setPhoneNumber(phoneNumberField.getText().trim());
        passenger.setEmail(emailField.getText().trim());
        return passenger;
    }

    private void populateFormFromSelection() {
        int selectedRow = passengerTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = passengerTable.convertRowIndexToModel(selectedRow);
        passengerIdField.setText(String.valueOf(tableModel.getValueAt(modelRow, 0)));
        passengerNameField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
        phoneNumberField.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
        emailField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
    }

    private void clearForm() {
        passengerIdField.setText("");
        passengerNameField.setText("");
        phoneNumberField.setText("");
        emailField.setText("");
        passengerTable.clearSelection();
    }

    private int readId(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required. Select a row first.");
        }
        return readInt(value, fieldName);
    }

    private int readInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }
}
