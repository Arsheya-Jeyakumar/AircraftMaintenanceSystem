/*
========================================================
Aircraft Maintenance & Component Lifecycle System

Person 2 - Maintenance & Inspection Module
Author: ARSHEYA TARMIA JEYAKUMAR
Registration No: 25BCE2629

Sample Data:
    TECHNICIAN
========================================================
*/

USE aircraft_maintenance_db;

INSERT INTO TECHNICIAN
(employee_no, name, qualification, specialization, phone, status)
VALUES
('TECH001', 'Arjun Kumar', 'AME License',
 'Engine Maintenance', '9876543210', 'ACTIVE'),

('TECH002', 'Priya Nair', 'AME License',
 'Avionics', '9876543211', 'ACTIVE'),

('TECH003', 'Rahul Menon', 'Diploma in Aircraft Maintenance',
 'Landing Gear', '9876543212', 'ACTIVE');

-- =====================================================
-- PERSON 3 -  SPARE PARTS & INVENTORY SAMPLE DATA 
-- Author-AVANDIKA K
-- Registration no:25BCE2293
-- =====================================================

-- SPARE PARTS
INSERT INTO SPARE_PART
(part_number, part_name, component_type, supplier, unit_cost, minimum_stock)
VALUES
('HYD-P-001', 'Hydraulic Pump', 'Hydraulic System',
 'AeroParts Ltd', 45000.00, 5),

('BRG-002', 'Engine Bearing', 'Engine',
 'SkyParts', 12000.00, 10),

('FLT-003', 'Fuel Filter', 'Fuel System',
 'AeroSupply', 3500.00, 8),

('VAL-004', 'Pressure Valve', 'Hydraulic System',
 'AeroParts Ltd', 7500.00, 6);


-- INVENTORY
INSERT INTO INVENTORY
(part_id, location, quantity_available)
VALUES
(1, 'Chennai Warehouse', 2),
(2, 'Bangalore Warehouse', 15),
(3, 'Chennai Warehouse', 3),
(4, 'Chennai Warehouse', 8);
