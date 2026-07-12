CREATE TABLE workshops (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    manager_name VARCHAR(30),
    location VARCHAR(100),
    phone VARCHAR(20),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_workshops_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE devices (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    model VARCHAR(50),
    factory_date DATE,
    purchase_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    workshop_id BIGINT UNSIGNED,
    responsible_person VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_USE',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_devices_code UNIQUE (code),
    CONSTRAINT chk_devices_price CHECK (purchase_price >= 0),
    CONSTRAINT chk_devices_status CHECK (status IN ('IN_USE', 'MAINTENANCE', 'RETIRED')),
    CONSTRAINT fk_devices_workshop
        FOREIGN KEY (workshop_id) REFERENCES workshops(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    INDEX idx_devices_workshop_id (workshop_id),
    INDEX idx_devices_status (status),
    INDEX idx_devices_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE maintenance_records (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT UNSIGNED NOT NULL,
    maintenance_time DATETIME NOT NULL,
    fault_type VARCHAR(50),
    fault_description TEXT,
    repair_description TEXT,
    maintenance_cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    operator_name VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_maintenance_cost CHECK (maintenance_cost >= 0),
    CONSTRAINT fk_maintenance_records_device
        FOREIGN KEY (device_id) REFERENCES devices(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_maintenance_records_device_id (device_id),
    INDEX idx_maintenance_records_time (maintenance_time),
    INDEX idx_maintenance_records_fault_type (fault_type),
    INDEX idx_maintenance_records_device_time (device_id, maintenance_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;