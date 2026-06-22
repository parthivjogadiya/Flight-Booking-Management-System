package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/flight_booking_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "2302";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL Connector/J was not found. Add mysql-connector-j to the project build path.",
                    exception);
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
