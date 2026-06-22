package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

import service.BookingService;
import service.FlightService;
import service.PassengerService;

public class DashboardFrame extends JFrame {
    public static final Color NAV_BACKGROUND = new Color(22, 34, 51);
    public static final Color NAV_ACTIVE = new Color(37, 99, 235);
    public static final Color PAGE_BACKGROUND = new Color(244, 247, 251);
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color TEXT_DARK = new Color(31, 41, 55);
    public static final Color TEXT_MUTED = new Color(107, 114, 128);
    public static final Color BORDER_COLOR = new Color(226, 232, 240);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color DANGER = new Color(220, 38, 38);

    private static final String DASHBOARD_CARD = "Dashboard";
    private static final String FLIGHTS_CARD = "Flights";
    private static final String PASSENGERS_CARD = "Passengers";
    private static final String BOOKINGS_CARD = "Bookings";

    private final FlightService flightService;
    private final PassengerService passengerService;
    private final BookingService bookingService;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final List<JButton> navigationButtons;

    private JLabel totalFlightsValue;
    private JLabel totalPassengersValue;
    private JLabel totalBookingsValue;

    private FlightPanel flightPanel;
    private PassengerPanel passengerPanel;
    private BookingPanel bookingPanel;

    public DashboardFrame() {
        this.flightService = new FlightService();
        this.passengerService = new PassengerService();
        this.bookingService = new BookingService();
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.navigationButtons = new ArrayList<>();

        setTitle("Flight Booking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createNavigationPanel(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
        showPanel(DASHBOARD_CARD);
    }

    public FlightService getFlightService() {
        return flightService;
    }

    public PassengerService getPassengerService() {
        return passengerService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }

    public void refreshAfterFlightChange() {
        refreshDashboardStats();
        if (bookingPanel != null) {
            bookingPanel.loadReferenceData();
        }
    }

    public void refreshAfterPassengerChange() {
        refreshDashboardStats();
        if (bookingPanel != null) {
            bookingPanel.loadReferenceData();
        }
    }

    public void refreshAfterBookingChange() {
        refreshDashboardStats();
        if (flightPanel != null) {
            flightPanel.loadFlights();
        }
        if (bookingPanel != null) {
            bookingPanel.loadReferenceData();
        }
    }

    public void refreshDashboardStats() {
        try {
            totalFlightsValue.setText(String.valueOf(flightService.getTotalFlights()));
            totalPassengersValue.setText(String.valueOf(passengerService.getTotalPassengers()));
            totalBookingsValue.setText(String.valueOf(bookingService.getTotalBookings()));
        } catch (SQLException exception) {
            totalFlightsValue.setText("N/A");
            totalPassengersValue.setText("N/A");
            totalBookingsValue.setText("N/A");
        }
    }

    private JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setBackground(NAV_BACKGROUND);
        navigationPanel.setPreferredSize(new Dimension(245, 0));

        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(28, 24, 24, 24));

        JLabel title = new JLabel("<html>Flight Booking<br>Management</html>");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Swing + MySQL");
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setBorder(new EmptyBorder(10, 0, 0, 0));

        brandPanel.add(title, BorderLayout.NORTH);
        brandPanel.add(subtitle, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(8, 18, 0, 18));

        JButton dashboardButton = createNavigationButton("Dashboard");
        dashboardButton.addActionListener(event -> showPanel(DASHBOARD_CARD));
        JButton flightsButton = createNavigationButton("Flights");
        flightsButton.addActionListener(event -> showPanel(FLIGHTS_CARD));
        JButton passengersButton = createNavigationButton("Passengers");
        passengersButton.addActionListener(event -> showPanel(PASSENGERS_CARD));
        JButton bookingsButton = createNavigationButton("Bookings");
        bookingsButton.addActionListener(event -> showPanel(BOOKINGS_CARD));

        buttonPanel.add(dashboardButton);
        buttonPanel.add(flightsButton);
        buttonPanel.add(passengersButton);
        buttonPanel.add(bookingsButton);

        JLabel footer = new JLabel("Java 17 Desktop App");
        footer.setForeground(new Color(148, 163, 184));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setBorder(new EmptyBorder(20, 24, 24, 24));

        navigationPanel.add(brandPanel, BorderLayout.NORTH);
        navigationPanel.add(buttonPanel, BorderLayout.CENTER);
        navigationPanel.add(footer, BorderLayout.SOUTH);
        return navigationPanel;
    }

    private JPanel createContentPanel() {
        contentPanel.setBackground(PAGE_BACKGROUND);

        flightPanel = new FlightPanel(this);
        passengerPanel = new PassengerPanel(this);
        bookingPanel = new BookingPanel(this);

        contentPanel.add(createDashboardPanel(), DASHBOARD_CARD);
        contentPanel.add(flightPanel, FLIGHTS_CARD);
        contentPanel.add(passengerPanel, PASSENGERS_CARD);
        contentPanel.add(bookingPanel, BOOKINGS_CARD);
        return contentPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout(24, 24));
        dashboardPanel.setBackground(PAGE_BACKGROUND);
        dashboardPanel.setBorder(new EmptyBorder(30, 32, 30, 32));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel heading = new JLabel("Dashboard");
        heading.setForeground(TEXT_DARK);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel caption = new JLabel("Quick operational overview for flights, passengers, and bookings.");
        caption.setForeground(TEXT_MUTED);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        caption.setBorder(new EmptyBorder(8, 0, 0, 0));

        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(caption, BorderLayout.CENTER);

        totalFlightsValue = createStatValueLabel();
        totalPassengersValue = createStatValueLabel();
        totalBookingsValue = createStatValueLabel();

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 22, 22));
        cardsPanel.setOpaque(false);
        cardsPanel.add(createStatCard("Total Flights", totalFlightsValue, NAV_ACTIVE));
        cardsPanel.add(createStatCard("Total Passengers", totalPassengersValue, SUCCESS));
        cardsPanel.add(createStatCard("Total Bookings", totalBookingsValue, new Color(245, 158, 11)));

        JPanel notePanel = new JPanel(new GridBagLayout());
        notePanel.setBackground(CARD_BACKGROUND);
        notePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(26, 28, 26, 28)));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        JLabel moduleTitle = new JLabel("Operational Workspace");
        moduleTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        moduleTitle.setForeground(TEXT_DARK);

        JLabel moduleText = new JLabel(
                "<html>Flight schedules, passenger profiles, and booking transactions are organized for daily reservation desk work. "
                        + "Seat availability stays synchronized with confirmed and cancelled bookings.</html>");
        moduleText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        moduleText.setForeground(TEXT_MUTED);
        moduleText.setBorder(new EmptyBorder(8, 0, 0, 0));

        notePanel.add(moduleTitle, constraints);
        constraints.gridy++;
        notePanel.add(moduleText, constraints);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 24));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(notePanel, BorderLayout.CENTER);

        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        dashboardPanel.add(centerPanel, BorderLayout.CENTER);
        return dashboardPanel;
    }

    private JLabel createStatValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 42));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, accentColor),
                new EmptyBorder(24, 24, 24, 24)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_MUTED);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JButton createNavigationButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(new Color(226, 232, 240));
        button.setBackground(NAV_BACKGROUND);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(13, 18, 13, 18));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        navigationButtons.add(button);
        return button;
    }

    private void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        setActiveNavigation(panelName);

        if (DASHBOARD_CARD.equals(panelName)) {
            refreshDashboardStats();
        } else if (FLIGHTS_CARD.equals(panelName)) {
            flightPanel.loadFlights();
        } else if (PASSENGERS_CARD.equals(panelName)) {
            passengerPanel.loadPassengers();
        } else if (BOOKINGS_CARD.equals(panelName)) {
            bookingPanel.loadReferenceData();
            bookingPanel.loadBookings();
        }
    }

    private void setActiveNavigation(String panelName) {
        for (JButton button : navigationButtons) {
            boolean selected = button.getText().equals(panelName);
            button.setBackground(selected ? NAV_ACTIVE : NAV_BACKGROUND);
            button.setForeground(Color.WHITE);
        }
    }

    public static JPanel createPageHeader(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_DARK);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, NAV_ACTIVE, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, new Color(226, 232, 240), TEXT_DARK);
    }

    private static JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(229, 231, 235));
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
    }

    public static void showError(Component parent, Exception exception) {
        JOptionPane.showMessageDialog(parent, friendlyMessage(exception), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static String friendlyMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Something went wrong. Please try again.";
        }
        if (message.toLowerCase().contains("communications link failure")) {
            return "Cannot connect to MySQL. Make sure MySQL Server is running on localhost:3306.";
        }
        if (message.toLowerCase().contains("access denied")) {
            return "Database access denied. Check the MySQL username and password in DBConnection.java.";
        }
        if (message.toLowerCase().contains("unknown database")) {
            return "Database not found. Run database/schema.sql before opening the application.";
        }
        if (message.toLowerCase().contains("duplicate entry")) {
            return "Duplicate record found. Flight numbers and passenger emails must be unique.";
        }
        return message;
    }

    public static GridBagConstraints formConstraints(int row, int column) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = column == 1 ? 1 : 0;
        return constraints;
    }
}
