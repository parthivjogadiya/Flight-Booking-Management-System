DROP DATABASE IF EXISTS flight_booking_db;
CREATE DATABASE flight_booking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE flight_booking_db;

CREATE TABLE flights (
    flight_id INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL UNIQUE,
    source VARCHAR(80) NOT NULL,
    destination VARCHAR(80) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    available_seats INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_flights_seats CHECK (available_seats >= 0),
    CONSTRAINT chk_flights_price CHECK (price >= 0),
    CONSTRAINT chk_flights_time CHECK (arrival_time > departure_time)
) ENGINE=InnoDB;

CREATE TABLE passengers (
    passenger_id INT AUTO_INCREMENT PRIMARY KEY,
    passenger_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    passenger_id INT NOT NULL,
    flight_id INT NOT NULL,
    booking_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_bookings_passenger
        FOREIGN KEY (passenger_id)
        REFERENCES passengers(passenger_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_flight
        FOREIGN KEY (flight_id)
        REFERENCES flights(flight_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_booking_status
        CHECK (booking_status IN ('CONFIRMED', 'CANCELLED'))
) ENGINE=InnoDB;

CREATE INDEX idx_flights_route ON flights(source, destination);
CREATE INDEX idx_bookings_passenger ON bookings(passenger_id);
CREATE INDEX idx_bookings_flight ON bookings(flight_id);

INSERT INTO flights
    (flight_number, source, destination, departure_time, arrival_time, available_seats, price)
VALUES
    ('AI-101', 'Delhi', 'Mumbai', '2026-07-01 08:30:00', '2026-07-01 10:45:00', 120, 5499.00),
    ('UK-204', 'Bengaluru', 'Delhi', '2026-07-01 12:15:00', '2026-07-01 15:05:00', 95, 6799.00),
    ('6E-332', 'Hyderabad', 'Chennai', '2026-07-02 06:45:00', '2026-07-02 08:05:00', 80, 3299.00),
    ('SG-718', 'Pune', 'Goa', '2026-07-03 17:20:00', '2026-07-03 18:35:00', 60, 2899.00);

INSERT INTO passengers
    (passenger_name, phone_number, email)
VALUES
    ('Aarav Sharma', '9876543210', 'aarav.sharma@example.com'),
    ('Ananya Verma', '9988776655', 'ananya.verma@example.com'),
    ('Kunal Mehta', '9123456780', 'kunal.mehta@example.com');
