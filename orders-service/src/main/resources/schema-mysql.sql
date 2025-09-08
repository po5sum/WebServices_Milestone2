
create table if not exists orders (
                                      id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      order_id VARCHAR(36) UNIQUE,
    artist_id VARCHAR(36),
    album_id VARCHAR(36),
    customer_id VARCHAR(36),
    store_id VARCHAR(36),
    order_date VARCHAR(100),
    order_status ENUM('PENDING', 'SHIPPED', 'DELIVERED','CANCELLED') NOT NULL,
    order_price DECIMAL(10,2),
    payment_method ENUM('PAYPAL', 'CREDIT_CARD', 'CASH', 'GIFT_CARD', 'DEBIT_CARD', 'CHEQUE') NOT NULL
    );