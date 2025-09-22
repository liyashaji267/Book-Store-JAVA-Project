CREATE DATABASE IF NOT EXISTS bookstore_db;
USE bookstore_db;

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    edition VARCHAR(50),
    publisher VARCHAR(255),
    isbn VARCHAR(50) UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    location VARCHAR(100),
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10,2) NOT NULL,
    customer_name VARCHAR(255)
);

CREATE TABLE bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    book_id INT NOT NULL,
    title VARCHAR(255),
    qty INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

INSERT INTO books (title, author, edition, publisher, isbn, price, location, stock)
VALUES
('Effective Java', 'Joshua Bloch', '3rd', 'Addison-Wesley', '9780134685991', 45.00, 'A1-Rack-3', 8),
('Clean Code', 'Robert C. Martin', '1st', 'Prentice Hall', '9780132350884', 40.00, 'A1-Rack-7', 5),
('Design Patterns', 'Gang of Four', '1st', 'Addison-Wesley', '9780201633610', 55.00, 'B2-Rack-1', 10),
('Java: The Complete Reference', 'Herbert Schildt', '11th', 'McGraw-Hill', '9781260463417', 35.00, 'C3-Rack-2', 6),
('The Temple Of The Ruby Of Fire', 'Elisabetta Dami', NULL, 'Edizioni Piemme', '9780545103746', 295.00, 'R6 C3', 7),
('Attack Of The Bandit Cats', 'Elisabetta Dami', NULL, 'Edizioni Piemme', '9780439559683', 350.00, 'R6 C3', 8),
('Paws Off, Cheddarface', 'Elisabetta Dami', NULL, 'Edizioni Piemme', '9780439691437', 295.00, 'R6 C3', 9),
('Digital Logic and Computer Design', 'M. Morris Mano', '2.0', 'Prentice Hall', '9780132145107', 520.00, 'C10', 10),
('Look Out Secret Seven', 'Enid Blyton', NULL, 'Hodder & Stoughton', '9781444933749', 150.00, 'R7 C4', 4),
('Five Get Into Trouble', 'Enid Blyton', NULL, 'Hodder & Stoughton', '9780340894695', 199.00, 'R8 C5', 10),
('The Naughtiest Girl', 'Enid Blyton', NULL, 'Hodder & Stoughton', '9781444953433', 165.00, 'R9 C1', 2),
('Gulliver''s Travels Other Stories', 'Neela Subramaniam', NULL, 'Children''s Book Trust', '9788123701044', 45.00, 'R12 C8', 14),
('Digitale Eletronices And Logic design', 'Marina Crompton And Kailas Sree Chandran', '1.0', 'TechBooks', '9789351234567', 300.00, 'R13 C6', 8),
('ഒരച്ഛൻ മകൾക്കയച്ച കത്തുകൾ', 'Jawaharlal Nehru', '19.0', 'DC Books', '9788126401231', 90.00, 'A', 4);

SELECT * FROM books;