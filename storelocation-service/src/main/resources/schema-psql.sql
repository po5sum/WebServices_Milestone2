DROP TABLE IF EXISTS stores;

create table if not exists stores (
    id SERIAL,
    store_id VARCHAR(255) UNIQUE NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    manager_name VARCHAR(255),
    store_rating DOUBLE PRECISION,
    phone_number VARCHAR(50),
    email VARCHAR(255),
    open_hours VARCHAR(255),
    street_address VARCHAR(255),
    city VARCHAR(100),
    province VARCHAR(100),
    postal_code VARCHAR(20),
    PRIMARY KEY(id)
);

