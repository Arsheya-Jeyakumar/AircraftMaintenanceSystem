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
-- PERSON 1 - AIRCRAFT & COMPONENT LIFECYCLE MODULE
-- =====================================================

-- AIRCRAFT TABLE
CREATE TABLE AIRCRAFT (
    aircraft_id INT AUTO_INCREMENT PRIMARY KEY,
    registration_no VARCHAR(20) NOT NULL UNIQUE,
    model VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(50) NOT NULL,
    manufacture_date DATE,
    total_flight_hours DECIMAL(10,2) DEFAULT 0,
    total_cycles INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- COMPONENT TABLE
CREATE TABLE COMPONENT (
    component_id INT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    component_type VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(50) NOT NULL,
    model VARCHAR(50),
    service_limit_hours DECIMAL(10,2),
    service_limit_cycles INT,
    current_status VARCHAR(30) DEFAULT 'AVAILABLE',
    current_location VARCHAR(100)
);

-- AIRCRAFT_COMPONENT TABLE
CREATE TABLE AIRCRAFT_COMPONENT (
    installation_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NOT NULL,
    component_id INT NOT NULL,
    installed_date DATE NOT NULL,
    removed_date DATE,
    installation_hours DECIMAL(10,2) NOT NULL,
    removal_hours DECIMAL(10,2),
    installation_cycles INT NOT NULL,
    removal_cycles INT,
    installation_reason VARCHAR(100),
    removal_reason VARCHAR(100),

    CONSTRAINT fk_ac_aircraft
        FOREIGN KEY (aircraft_id)
        REFERENCES AIRCRAFT(aircraft_id),

    CONSTRAINT fk_ac_component
        FOREIGN KEY (component_id)
        REFERENCES COMPONENT(component_id)
);
