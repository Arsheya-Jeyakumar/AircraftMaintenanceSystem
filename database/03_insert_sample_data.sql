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
-- PERSON 1 - AIRCRAFT & COMPONENT SAMPLE DATA
-- =====================================================

-- AIRCRAFT SAMPLE DATA
INSERT INTO AIRCRAFT
(registration_no, model, manufacturer, manufacture_date,
 total_flight_hours, total_cycles, status)
VALUES
('VT-ABC', 'A320-200', 'Airbus', '2018-05-15',
 4520.50, 2150, 'ACTIVE'),

('VT-XYZ', 'B737-800', 'Boeing', '2019-08-20',
 3780.25, 1820, 'ACTIVE');

-- COMPONENT SAMPLE DATA
INSERT INTO COMPONENT
(serial_number, component_type, manufacturer, model,
 service_limit_hours, service_limit_cycles,
 current_status, current_location)
VALUES
('ENG-001', 'Engine', 'CFM International', 'CFM56-5B',
 20000.00, 10000, 'INSTALLED', 'VT-ABC'),

('BRK-002', 'Brake Assembly', 'Safran', 'BRK-320',
 5000.00, 3000, 'AVAILABLE', 'Warehouse'),

('NAV-003', 'Navigation System', 'Honeywell', 'NAV-900',
 15000.00, 8000, 'INSTALLED', 'VT-XYZ');

-- =====================================================
-- COMPONENT LIFECYCLE SAMPLE DATA
-- =====================================================

INSERT INTO AIRCRAFT_COMPONENT
(aircraft_id, component_id, installed_date,
 installation_hours, installation_cycles,
 installation_reason)
VALUES
(
    (SELECT aircraft_id FROM AIRCRAFT
     WHERE registration_no = 'VT-ABC'),
    (SELECT component_id FROM COMPONENT
     WHERE serial_number = 'ENG-001'),
    '2024-01-15',
    4000.00,
    1900,
    'Initial Installation'
),

(
    (SELECT aircraft_id FROM AIRCRAFT
     WHERE registration_no = 'VT-XYZ'),
    (SELECT component_id FROM COMPONENT
     WHERE serial_number = 'NAV-003'),
    '2024-06-10',
    3200.00,
    1500,
    'Replacement'
);
