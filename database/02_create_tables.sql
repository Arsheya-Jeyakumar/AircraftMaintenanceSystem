/*
========================================================
Aircraft Maintenance & Component Lifecycle System

Person 2 - Maintenance & Inspection Module
Author: YOUR NAME
Registration No: YOUR REG NO

Tables:
    TECHNICIAN
    MAINTENANCE
    INSPECTION
    FAILURE

Indexes:
    idx_maintenance_scheduled_date
    idx_maintenance_status
    idx_inspection_date
    idx_failure_date
    idx_failure_severity
========================================================
*/

USE aircraft_maintenance_db;


-- =====================================================
-- TECHNICIAN
-- =====================================================

CREATE TABLE TECHNICIAN (
    technician_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_no VARCHAR(30) UNIQUE,
    name VARCHAR(100),
    qualification VARCHAR(100),
    specialization VARCHAR(100),
    phone VARCHAR(30),
    status VARCHAR(20)
);

ALTER TABLE TECHNICIAN
    MODIFY employee_no VARCHAR(30) NOT NULL,
    MODIFY name VARCHAR(100) NOT NULL,
    MODIFY qualification VARCHAR(100) NOT NULL,
    MODIFY specialization VARCHAR(100) NOT NULL,
    MODIFY status VARCHAR(20) NOT NULL;


-- =====================================================
-- MAINTENANCE
-- =====================================================

CREATE TABLE MAINTENANCE (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT,
    component_id INT,
    maintenance_type VARCHAR(50),
    scheduled_date DATE,
    completed_date DATE NULL,
    description VARCHAR(500),
    status VARCHAR(30),
    technician_id INT,
    FOREIGN KEY (technician_id)
        REFERENCES TECHNICIAN(technician_id)
);

ALTER TABLE MAINTENANCE
    MODIFY aircraft_id INT NOT NULL,
    MODIFY maintenance_type VARCHAR(50) NOT NULL,
    MODIFY scheduled_date DATE NOT NULL,
    MODIFY status VARCHAR(30) NOT NULL;


-- =====================================================
-- INSPECTION
-- =====================================================

CREATE TABLE INSPECTION (
    inspection_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT,
    component_id INT,
    inspection_date DATE,
    inspection_type VARCHAR(50),
    result VARCHAR(30),
    findings VARCHAR(500),
    inspector_id INT,
    FOREIGN KEY (inspector_id)
        REFERENCES TECHNICIAN(technician_id)
);

ALTER TABLE INSPECTION
    MODIFY aircraft_id INT NOT NULL,
    MODIFY inspection_date DATE NOT NULL,
    MODIFY inspection_type VARCHAR(50) NOT NULL,
    MODIFY result VARCHAR(30) NOT NULL,
    MODIFY inspector_id INT NOT NULL;


-- =====================================================
-- FAILURE
-- =====================================================

CREATE TABLE FAILURE (
    failure_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT,
    component_id INT,
    failure_date DATE,
    failure_type VARCHAR(100),
    severity VARCHAR(20),
    description VARCHAR(500),
    resolved BOOLEAN
);

ALTER TABLE FAILURE
    MODIFY aircraft_id INT NOT NULL,
    MODIFY component_id INT NOT NULL,
    MODIFY failure_date DATE NOT NULL,
    MODIFY failure_type VARCHAR(100) NOT NULL,
    MODIFY severity VARCHAR(20) NOT NULL,
    MODIFY resolved BOOLEAN NOT NULL DEFAULT FALSE;


-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_maintenance_scheduled_date
ON MAINTENANCE(scheduled_date);

CREATE INDEX idx_maintenance_status
ON MAINTENANCE(status);

CREATE INDEX idx_inspection_date
ON INSPECTION(inspection_date);

CREATE INDEX idx_failure_date
ON FAILURE(failure_date);

CREATE INDEX idx_failure_severity
ON FAILURE(severity);

-- =====================================================
-- PERSON 3 - SPARE PARTS & INVENTORY MODULE
-- =====================================================

-- =====================================================
-- SPARE_PART
-- =====================================================

CREATE TABLE SPARE_PART (
    part_id INT AUTO_INCREMENT PRIMARY KEY,
    part_number VARCHAR(50) NOT NULL UNIQUE,
    part_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    supplier VARCHAR(100),
    unit_cost DECIMAL(10,2),
    minimum_stock INT NOT NULL
);


-- =====================================================
-- INVENTORY
-- =====================================================

CREATE TABLE INVENTORY (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    part_id INT NOT NULL,
    location VARCHAR(100) NOT NULL,
    quantity_available INT NOT NULL,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (part_id)
        REFERENCES SPARE_PART(part_id)
);


-- =====================================================
-- ALERT
-- =====================================================

CREATE TABLE ALERT (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NULL,
    component_id INT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'OPEN'
);


-- =====================================================
-- INDEXES - PERSON 3
-- =====================================================

CREATE INDEX idx_inventory_part
ON INVENTORY(part_id);

CREATE INDEX idx_inventory_quantity
ON INVENTORY(quantity_available);

CREATE INDEX idx_alert_type
ON ALERT(alert_type);

CREATE INDEX idx_alert_status
ON ALERT(status);
