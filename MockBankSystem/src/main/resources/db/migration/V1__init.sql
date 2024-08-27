CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_full_name VARCHAR(255) UNIQUE NOT NULL,
    card_number BIGINT UNIQUE NOT NULL,
    card_expiration_date VARCHAR(10) NOT NULL,
    cvv SMALLINT NOT NULL,
    balance INT NOT NULL
);

INSERT INTO clients (client_full_name, card_number, card_expiration_date, cvv, balance)
VALUES
('VIP Client', 1025874555689562, '12/25', 111, 9989934),
('muzz app', 1234567890123456, '12/25', 111, 75971);
