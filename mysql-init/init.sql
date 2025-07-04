-- Create single database for all services
CREATE DATABASE IF NOT EXISTS shop;

-- Grant privileges
GRANT ALL PRIVILEGES ON shop.* TO 'root'@'%';

FLUSH PRIVILEGES;
