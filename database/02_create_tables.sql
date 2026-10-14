/*
====================================================================
Aircraft Maintenance & Component Lifecycle Intelligence System
Database Management System (DBMS) Project

Script 02: Table Definitions, Constraints & Indexes

Module Allocations & Contributors:
- Person 1 (Aircraft & Component Lifecycle):
    Author: Thunga Purushotham (vitstudent)
    Tables: AIRCRAFT, COMPONENT, AIRCRAFT_COMPONENT
- Person 2 (Maintenance, Inspections & Failures):
    Author: Arsheya Tarmia Jeyakumar (25BCE2629)
    Tables: TECHNICIAN, MAINTENANCE, INSPECTION, FAILURE
- Person 3 (Spare Parts, Inventory & Alerts):
    Author: Avandika K (25BCE2293)
    Tables: SPARE_PART, INVENTORY, ALERT
====================================================================
*/

USE aircraft_maintenance_db;

-- Disable foreign key checks during table drop/re-creation if needed
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS ALERT;
DROP TABLE IF EXISTS INVENTORY;
DROP TABLE IF EXISTS FAILURE;
DROP TABLE IF EXISTS INSPECTION;
DROP TABLE IF EXISTS MAINTENANCE;
DROP TABLE IF EXISTS AIRCRAFT_COMPONENT;
DROP TABLE IF EXISTS SPARE_PART;
DROP TABLE IF EXISTS TECHNICIAN;
DROP TABLE IF EXISTS COMPONENT;
DROP TABLE IF EXISTS AIRCRAFT;

SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================================
-- PERSON 1: AIRCRAFT & COMPONENT LIFECYCLE MODULE
-- ====================================================================

-- 1. AIRCRAFT
CREATE TABLE AIRCRAFT (
    aircraft_id INT AUTO_INCREMENT PRIMARY KEY,
    registration_no VARCHAR(20) NOT NULL UNIQUE,
    model VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(50) NOT NULL,
    manufacture_date DATE NULL,
    total_flight_hours DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_cycles INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_aircraft_hours CHECK (total_flight_hours >= 0),
    CONSTRAINT chk_aircraft_cycles CHECK (total_cycles >= 0),
    CONSTRAINT chk_aircraft_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'GROUNDED', 'DECOMMISSIONED'))
) ENGINE=InnoDB;

-- 2. COMPONENT
CREATE TABLE COMPONENT (
    component_id INT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    component_type VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(50) NOT NULL,
    model VARCHAR(50) NULL,
    service_limit_hours DECIMAL(10,2) NULL,
    service_limit_cycles INT NULL,
    current_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    current_location VARCHAR(100) NOT NULL DEFAULT 'Warehouse',
    CONSTRAINT chk_component_limit_hours CHECK (service_limit_hours IS NULL OR service_limit_hours >= 0),
    CONSTRAINT chk_component_limit_cycles CHECK (service_limit_cycles IS NULL OR service_limit_cycles >= 0),
    CONSTRAINT chk_component_status CHECK (current_status IN ('AVAILABLE', 'INSTALLED', 'MAINTENANCE', 'INSPECTION', 'SCRAPPED'))
) ENGINE=InnoDB;


-- ====================================================================
-- PERSON 2: TECHNICIAN ENTITY
-- ====================================================================

-- 3. TECHNICIAN
CREATE TABLE TECHNICIAN (
    technician_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_no VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_technician_status CHECK (status IN ('ACTIVE', 'ON_LEAVE', 'INACTIVE'))
) ENGINE=InnoDB;


-- ====================================================================
-- PERSON 3: SPARE PARTS ENTITY
-- ====================================================================

-- 4. SPARE_PART
CREATE TABLE SPARE_PART (
    part_id INT AUTO_INCREMENT PRIMARY KEY,
    part_number VARCHAR(50) NOT NULL UNIQUE,
    part_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    supplier VARCHAR(100) NULL,
    unit_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    minimum_stock INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_part_cost CHECK (unit_cost >= 0),
    CONSTRAINT chk_part_min_stock CHECK (minimum_stock >= 0)
) ENGINE=InnoDB;


-- ====================================================================
-- PERSON 1: AIRCRAFT_COMPONENT (INSTALLATION & LIFECYCLE RELATIONSHIP)
-- ====================================================================

-- 5. AIRCRAFT_COMPONENT
CREATE TABLE AIRCRAFT_COMPONENT (
    installation_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NOT NULL,
    component_id INT NOT NULL,
    installed_date DATE NOT NULL,
    removed_date DATE NULL,
    installation_hours DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    removal_hours DECIMAL(10,2) NULL,
    installation_cycles INT NOT NULL DEFAULT 0,
    removal_cycles INT NULL,
    installation_reason VARCHAR(100) NOT NULL DEFAULT 'Initial Installation',
    removal_reason VARCHAR(100) NULL,
    
    CONSTRAINT fk_ac_aircraft
        FOREIGN KEY (aircraft_id)
        REFERENCES AIRCRAFT(aircraft_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
        
    CONSTRAINT fk_ac_component
        FOREIGN KEY (component_id)
        REFERENCES COMPONENT(component_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
        
    CONSTRAINT chk_ac_hours_order CHECK (removal_hours IS NULL OR removal_hours >= installation_hours),
    CONSTRAINT chk_ac_cycles_order CHECK (removal_cycles IS NULL OR removal_cycles >= installation_cycles),
    CONSTRAINT chk_ac_dates_order CHECK (removed_date IS NULL OR removed_date >= installed_date)
) ENGINE=InnoDB;


-- ====================================================================
-- PERSON 2: MAINTENANCE, INSPECTION & FAILURE MODULE
-- ====================================================================

-- 6. MAINTENANCE
CREATE TABLE MAINTENANCE (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NOT NULL,
    component_id INT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    scheduled_date DATE NOT NULL,
    completed_date DATE NULL,
    description VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    technician_id INT NULL,
    
    CONSTRAINT fk_maint_aircraft
        FOREIGN KEY (aircraft_id)
        REFERENCES AIRCRAFT(aircraft_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
        
    CONSTRAINT fk_maint_component
        FOREIGN KEY (component_id)
        REFERENCES COMPONENT(component_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
        
    CONSTRAINT fk_maint_technician
        FOREIGN KEY (technician_id)
        REFERENCES TECHNICIAN(technician_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
        
    CONSTRAINT chk_maint_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'OVERDUE'))
) ENGINE=InnoDB;

-- 7. INSPECTION
CREATE TABLE INSPECTION (
    inspection_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NOT NULL,
    component_id INT NULL,
    inspection_date DATE NOT NULL,
    inspection_type VARCHAR(50) NOT NULL,
    result VARCHAR(30) NOT NULL,
    findings VARCHAR(500) NULL,
    inspector_id INT NOT NULL,
    
    CONSTRAINT fk_insp_aircraft
        FOREIGN KEY (aircraft_id)
        REFERENCES AIRCRAFT(aircraft_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
        
    CONSTRAINT fk_insp_component
        FOREIGN KEY (component_id)
        REFERENCES COMPONENT(component_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
        
    CONSTRAINT fk_insp_inspector
        FOREIGN KEY (inspector_id)
        REFERENCES TECHNICIAN(technician_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
        
    CONSTRAINT chk_insp_result CHECK (result IN ('PASSED', 'FAILED', 'WARNING', 'ACTION_REQUIRED'))
) ENGINE=InnoDB;

-- 8. FAILURE
CREATE TABLE FAILURE (
    failure_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NOT NULL,
    component_id INT NOT NULL,
    failure_date DATE NOT NULL,
    failure_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description VARCHAR(500) NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_fail_aircraft
        FOREIGN KEY (aircraft_id)
        REFERENCES AIRCRAFT(aircraft_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
        
    CONSTRAINT fk_fail_component
        FOREIGN KEY (component_id)
        REFERENCES COMPONENT(component_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
        
    CONSTRAINT chk_fail_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
) ENGINE=InnoDB;


-- ====================================================================
-- PERSON 3: INVENTORY & ALERTS MODULE
-- ====================================================================

-- 9. INVENTORY
CREATE TABLE INVENTORY (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    part_id INT NOT NULL,
    location VARCHAR(100) NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_inv_part
        FOREIGN KEY (part_id)
        REFERENCES SPARE_PART(part_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
        
    CONSTRAINT chk_inv_quantity CHECK (quantity_available >= 0)
) ENGINE=InnoDB;

-- 10. ALERT
CREATE TABLE ALERT (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NULL,
    component_id INT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    
    CONSTRAINT fk_alert_aircraft
        FOREIGN KEY (aircraft_id)
        REFERENCES AIRCRAFT(aircraft_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
        
    CONSTRAINT fk_alert_component
        FOREIGN KEY (component_id)
        REFERENCES COMPONENT(component_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
        
    CONSTRAINT chk_alert_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_alert_status CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED'))
) ENGINE=InnoDB;


-- ====================================================================
-- PERFORMANCE INDEXES
-- ====================================================================

-- Person 1 Indexes
CREATE INDEX idx_aircraft_status ON AIRCRAFT(status);
CREATE INDEX idx_aircraft_model ON AIRCRAFT(model);
CREATE INDEX idx_component_status ON COMPONENT(current_status);
CREATE INDEX idx_component_type ON COMPONENT(component_type);
CREATE INDEX idx_component_location ON COMPONENT(current_location);
CREATE INDEX idx_ac_aircraft ON AIRCRAFT_COMPONENT(aircraft_id);
CREATE INDEX idx_ac_component ON AIRCRAFT_COMPONENT(component_id);
CREATE INDEX idx_ac_installed_date ON AIRCRAFT_COMPONENT(installed_date);
CREATE INDEX idx_ac_removed_date ON AIRCRAFT_COMPONENT(removed_date);

-- Person 2 Indexes
CREATE INDEX idx_technician_status ON TECHNICIAN(status);
CREATE INDEX idx_maintenance_scheduled_date ON MAINTENANCE(scheduled_date);
CREATE INDEX idx_maintenance_status ON MAINTENANCE(status);
CREATE INDEX idx_maintenance_aircraft ON MAINTENANCE(aircraft_id);
CREATE INDEX idx_maintenance_component ON MAINTENANCE(component_id);
CREATE INDEX idx_maintenance_technician ON MAINTENANCE(technician_id);
CREATE INDEX idx_inspection_date ON INSPECTION(inspection_date);
CREATE INDEX idx_inspection_aircraft ON INSPECTION(aircraft_id);
CREATE INDEX idx_inspection_result ON INSPECTION(result);
CREATE INDEX idx_failure_date ON FAILURE(failure_date);
CREATE INDEX idx_failure_severity ON FAILURE(severity);
CREATE INDEX idx_failure_type ON FAILURE(failure_type);
CREATE INDEX idx_failure_component ON FAILURE(component_id);
CREATE INDEX idx_failure_aircraft ON FAILURE(aircraft_id);
CREATE INDEX idx_failure_resolved ON FAILURE(resolved);

-- Person 3 Indexes
CREATE INDEX idx_spare_part_type ON SPARE_PART(component_type);
CREATE INDEX idx_inventory_part ON INVENTORY(part_id);
CREATE INDEX idx_inventory_location ON INVENTORY(location);
CREATE INDEX idx_inventory_quantity ON INVENTORY(quantity_available);
CREATE INDEX idx_alert_type ON ALERT(alert_type);
CREATE INDEX idx_alert_status ON ALERT(status);
CREATE INDEX idx_alert_severity ON ALERT(severity);
CREATE INDEX idx_alert_aircraft ON ALERT(aircraft_id);
CREATE INDEX idx_alert_component ON ALERT(component_id);
