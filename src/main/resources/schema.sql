CREATE TABLE IF NOT EXISTS residents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS doors (
    no VARCHAR(50) PRIMARY KEY,
    size_class VARCHAR(10) NOT NULL,
    current_parcel_id VARCHAR(255)
);