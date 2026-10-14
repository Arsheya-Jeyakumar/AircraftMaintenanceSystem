/*
====================================================================
Aircraft Maintenance & Component Lifecycle Intelligence System
Database Management System (DBMS) Project

Script 03: Comprehensive Relational Sample Data

Module Allocations & Contributors:
- Person 1: Thunga Purushotham (Aircraft & Components Lifecycle)
- Person 2: Arsheya Tarmia Jeyakumar (Technicians, Maintenance, Inspections & Failures)
- Person 3: Avandika K (Spare Parts, Inventory & Risk Intelligence)
====================================================================
*/

USE aircraft_maintenance_db;


-- Clear existing data in reverse foreign-key order
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE ALERT;
TRUNCATE TABLE INVENTORY;
TRUNCATE TABLE FAILURE;
TRUNCATE TABLE INSPECTION;
TRUNCATE TABLE MAINTENANCE;
TRUNCATE TABLE AIRCRAFT_COMPONENT;
TRUNCATE TABLE SPARE_PART;
TRUNCATE TABLE TECHNICIAN;
TRUNCATE TABLE COMPONENT;
TRUNCATE TABLE AIRCRAFT;

SET FOREIGN_KEY_CHECKS = 1;


-- ====================================================================
-- 1. AIRCRAFT (PERSON 1)
-- ====================================================================

INSERT INTO AIRCRAFT
(aircraft_id, registration_no, model, manufacturer, manufacture_date,
 total_flight_hours, total_cycles, status)
VALUES
(1, 'VT-ABC', 'A320-200', 'Airbus', '2018-05-15', 26000.00, 12000, 'ACTIVE'),
(2, 'VT-XYZ', 'B737-800', 'Boeing', '2019-08-20', 4180.25, 2020, 'ACTIVE'),
(3, 'VT-IND', 'A350-900', 'Airbus', '2021-03-10', 2850.75, 1120, 'ACTIVE'),
(4, 'VT-EXP', 'ATR 72-600', 'ATR', '2017-11-05', 31400.00, 9800, 'MAINTENANCE'),
(5, 'VT-SKY', 'B777-300ER', 'Boeing', '2016-02-18', 8920.00, 3100, 'ACTIVE');


-- ====================================================================
-- 2. COMPONENT (PERSON 1)
-- Components include:
-- - Healthy installed components
-- - Component nearing service limit (ENG-002: 19,400 / 20,000 hrs = 97%)
-- - Component exceeding service limit (APU-004: 15,300 / 15,000 hrs = BREACHED)
-- - Component with recurring failures (RAD-005)
-- - Spare components in warehouse
-- ====================================================================

INSERT INTO COMPONENT
(component_id, serial_number, component_type, manufacturer, model,
 service_limit_hours, service_limit_cycles, current_status, current_location)
VALUES

-- Healthy Engine installed on VT-ABC
(1, 'ENG-001', 'Turbofan Engine', 'CFM International', 'CFM56-5B',
 20000.00, 10000, 'INSTALLED', 'VT-ABC'),

-- Engine near service limit (19,400 hrs used out of 20,000 hrs = 97%) installed on VT-EXP
(2, 'ENG-002', 'Turboprop Engine', 'Pratt & Whitney', 'PW127M',
 20000.00, 12000, 'INSTALLED', 'VT-EXP'),

-- Landing gear assembly installed on VT-XYZ
(3, 'LDG-003', 'Landing Gear Main', 'Safran Landing', 'SLG-737',
 12000.00, 8000, 'INSTALLED', 'VT-XYZ'),

-- APU that has EXCEEDED service limit (15,300 hrs out of 15,000 hrs limit!)
(4, 'APU-004', 'Auxiliary Power Unit', 'Honeywell', 'HGT-1700',
 15000.00, 9000, 'AVAILABLE', 'Mumbai Hangar Q-Zone'),

-- Weather Radar with recurring failures (3+ incidents) installed on VT-ABC
(5, 'RAD-005', 'Weather Radar', 'Collins Aerospace', 'WXR-2100',
 10000.00, 6000, 'INSTALLED', 'VT-ABC'),

-- Flight Management Computer installed on VT-IND
(6, 'FMC-006', 'Flight Mgmt Computer', 'Honeywell', 'Pegasus-FMC',
 25000.00, 15000, 'INSTALLED', 'VT-IND'),

-- Hydraulic Pump installed on VT-XYZ
(7, 'HYD-007', 'Hydraulic Pump Main', 'Parker Aerospace', 'HP-5000',
 8000.00, 5000, 'INSTALLED', 'VT-XYZ'),

-- Available spare engine in Warehouse
(8, 'ENG-008', 'Turbofan Engine', 'CFM International', 'CFM56-5B',
 20000.00, 10000, 'AVAILABLE', 'Chennai Central Warehouse'),

-- Available spare brake assembly in Warehouse
(9, 'BRK-009', 'Brake Assembly', 'Safran', 'BRK-320',
 5000.00, 3000, 'AVAILABLE', 'Delhi Regional Warehouse'),

-- Scrapped component after life expiration
(10, 'ACT-010', 'Elevator Actuator', 'Moog', 'ACT-E320',
 6000.00, 4000, 'SCRAPPED', 'Salvage Yard');


-- ====================================================================
-- 3. TECHNICIAN (PERSON 2)
-- ====================================================================

INSERT INTO TECHNICIAN
(technician_id, employee_no, name, qualification, specialization, phone, status)
VALUES
(1, 'TECH001', 'Arjun Kumar', 'AME License (Cat A & C)',
 'Engine Maintenance', '+91-9876543210', 'ACTIVE'),

(2, 'TECH002', 'Priya Nair', 'AME License (Cat B2)',
 'Avionics & Radar', '+91-9876543211', 'ACTIVE'),

(3, 'TECH003', 'Rahul Menon', 'Diploma in Aircraft Maintenance',
 'Landing Gear & Hydraulics', '+91-9876543212', 'ACTIVE'),

(4, 'TECH004', 'Sneha Rao', 'AME License (Cat B1)',
 'Airframe & Powerplant', '+91-9876543213', 'ACTIVE'),

(5, 'TECH005', 'Vikram Joshi', 'DGCA Certified Inspector',
 'Quality Assurance & Inspection', '+91-9876543214', 'ACTIVE');


-- ====================================================================
-- 4. SPARE_PART (PERSON 3)
-- ====================================================================

INSERT INTO SPARE_PART
(part_id, part_number, part_name, component_type, supplier, unit_cost, minimum_stock)
VALUES
(1, 'HYD-P-001', 'Hydraulic Pump Core', 'Hydraulic System',
 'AeroParts Ltd', 45000.00, 5),

(2, 'BRG-002', 'Turbine Bearing High-Temp', 'Engine',
 'SkyParts International', 12000.00, 10),

(3, 'FLT-003', 'Dual Stage Fuel Filter', 'Fuel System',
 'AeroSupply Corp', 3500.00, 8),

(4, 'VAL-004', 'Pressure Regulating Valve', 'Hydraulic System',
 'AeroParts Ltd', 7500.00, 6),

(5, 'RAD-M-005', 'Receiver Antenna Module', 'Weather Radar',
 'Collins Systems', 28000.00, 4),

(6, 'BRK-D-006', 'Carbon Brake Rotor Disc', 'Landing Gear',
 'Safran Braking', 16500.00, 6);


-- ====================================================================
-- 5. AIRCRAFT_COMPONENT (PERSON 1: CURRENT & HISTORICAL INSTALLATIONS)
-- ====================================================================

INSERT INTO AIRCRAFT_COMPONENT
(installation_id, aircraft_id, component_id, installed_date, removed_date,
 installation_hours, removal_hours, installation_cycles, removal_cycles,
 installation_reason, removal_reason)
VALUES

-- Historical installation: APU-004 previously installed on VT-ABC and removed
(1, 1, 4, '2018-05-15', '2024-05-18', 0.00, 15300.00,
 0, 9150, 'Periodic Rotation', 'Service Limit Reached'),

-- Historical installation: ENG-008 previously installed on VT-XYZ and removed for overhaul
(2, 2, 8, '2021-06-15', '2023-11-20', 1800.00, 3200.00,
 900, 1600, 'Initial Installation', 'Major Overhaul Required'),

-- Current installation: ENG-001 on VT-ABC
(3, 1, 1, '2024-01-15', NULL, 15000.00, NULL,
 9000, NULL, 'Engine Upgrade', NULL),

-- Current installation: ENG-002 on VT-EXP (near service limit)
(4, 4, 2, '2022-04-10', NULL, 12000.00, NULL,
 7500, NULL, 'Replacement Installation', NULL),

-- Current installation: LDG-003 on VT-XYZ
(5, 2, 3, '2023-08-12', NULL, 3000.00, NULL,
 1500, NULL, 'Gear Overhaul Replacement', NULL),

-- Current installation: RAD-005 on VT-ABC (recurring failure item)
(6, 1, 5, '2024-02-01', NULL, 15100.00, NULL,
 9050, NULL, 'System Modification', NULL),

-- Current installation: FMC-006 on VT-IND
(7, 3, 6, '2023-12-05', NULL, 1800.00, NULL,
 750, NULL, 'Initial Factory Fit', NULL),

-- Current installation: HYD-007 on VT-XYZ
(8, 2, 7, '2024-04-20', NULL, 2200.00, NULL,
 1300, NULL, 'Component Exchange', NULL);


-- ====================================================================
-- 6. MAINTENANCE (PERSON 2: COMPLETED, IN_PROGRESS & OVERDUE)
-- ====================================================================

INSERT INTO MAINTENANCE
(maintenance_id, aircraft_id, component_id, maintenance_type,
 scheduled_date, completed_date, description, status, technician_id)
VALUES

-- Completed maintenance
(1, 1, 1, 'C-Check Inspection & Borescope', '2024-03-01', '2024-03-04',
 'Full borescope inspection of compressor and turbine stages. Clean.',
 'COMPLETED', 1),

(2, 2, 3, 'Hydraulic Fluid Flush', '2024-04-15', '2024-04-15',
 'Replaced hydraulic return lines and flushed fluid. Pressure tested.',
 'COMPLETED', 3),

-- Overdue maintenance (scheduled in past, not completed)
(3, 4, 2, 'Hot Section Inspection', '2026-08-10', NULL,
 'Critical inspection of high-pressure turbine blades due to high operating hours.',
 'OVERDUE', 1),

(4, 1, 5, 'Antenna Waveguide Tuning', '2026-09-05', NULL,
 'Calibrate radar waveguide and transceiver sensitivity after signal degradation.',
 'OVERDUE', 2),

-- In-progress maintenance
(5, 2, 7, 'Pump Seal Replacement', '2026-10-12', NULL,
 'Active replacement of weeping O-rings in hydraulic manifold.',
 'IN_PROGRESS', 3),

-- Aircraft-level maintenance (component_id is NULL)
(6, 3, NULL, 'A-Check Routine Airframe Inspection', '2026-10-25', NULL,
 'Routine scheduled 500-hour airframe, emergency lighting and avionics check.',
 'SCHEDULED', 4),

(7, 5, NULL, 'Overdue Structural NDT Test', '2026-07-20', NULL,
 'Ultrasonic crack inspection along wing root joints. Awaiting slot.',
 'OVERDUE', 5);


-- ====================================================================
-- 7. INSPECTION (PERSON 2)
-- ====================================================================

INSERT INTO INSPECTION
(inspection_id, aircraft_id, component_id, inspection_date,
 inspection_type, result, findings, inspector_id)
VALUES
(1, 1, 1, '2024-03-04', 'Post-Maintenance Borescope',
 'PASSED', 'Turbine blades within normal wear limits. No cracks detected.', 5),

(2, 2, 3, '2024-04-15', 'Landing Gear Retraction Test',
 'PASSED', 'Emergency free-fall and retraction cycle times within nominal bounds.', 5),

(3, 4, 2, '2026-08-01', 'Engine Vibration Analysis',
 'WARNING', 'Slight elevated vibration detected on spool 2. Advised 50-hour recheck.', 5),

(4, 1, 5, '2026-09-02', 'Weather Radar Diagnostic Scan',
 'FAILED', 'Intermittent transceiver signal loss detected during azimuth sweep.', 2),

(5, 3, NULL, '2024-05-10', 'Ramp Safety Inspection',
 'PASSED', 'Oxygen masks, escape slides, and emergency transmitters verified operational.', 5);


-- ====================================================================
-- 8. FAILURE (PERSON 2: RECURRING FAILURES & VARIOUS SEVERITIES)
-- Component 5 (RAD-005) has 3 RECURRING incidents of 'Signal Loss / Transceiver Dropout'
-- ====================================================================

INSERT INTO FAILURE
(failure_id, aircraft_id, component_id, failure_date,
 failure_type, severity, description, resolved)
VALUES

-- Incident 1 for RAD-005
(1, 1, 5, '2024-06-12', 'Signal Loss / Transceiver Dropout',
 'MEDIUM', 'Radar screen blacked out during turbulent climb through FL180.', TRUE),

-- Incident 2 for RAD-005
(2, 1, 5, '2024-07-28', 'Signal Loss / Transceiver Dropout',
 'HIGH', 'Radar lost echo returns in storm front vicinity. Pilot switched to standby.', TRUE),

-- Incident 3 for RAD-005 (Triggering threshold >= 3 recurring incidents!)
(3, 1, 5, '2026-09-01', 'Signal Loss / Transceiver Dropout',
 'CRITICAL', 'Complete antenna lockup and signal dropout during cross-country flight.', FALSE),

-- Single failure for Engine 2
(4, 4, 2, '2026-08-15', 'Turbine Oil Pressure Drop',
 'HIGH', 'Oil pressure dropped to 38 PSI during descent. Warning enunciator illuminated.', FALSE),

-- Solved failure for Hydraulic Pump
(5, 2, 7, '2024-04-10', 'Hydraulic Fluid Weep',
 'LOW', 'Minor hydraulic seepage observed around mounting flange gasket.', TRUE);


-- ====================================================================
-- 9. INVENTORY (PERSON 3: MULTI-LOCATION STOCK & SHORTAGES)
-- Demonstrating multi-location aggregation:
-- Part 1: Chennai (2) + Mumbai (1) = 3 (Below min stock 5 -> SHORTAGE!)
-- Part 2: Bangalore (6) + Delhi (5) = 11 (Above min stock 10 -> OK)
-- Part 3: Chennai (3) + Bangalore (2) = 5 (Below min stock 8 -> SHORTAGE!)
-- Part 4: Chennai (8) = 8 (Above min stock 6 -> OK)
-- Part 5: Delhi (1) = 1 (Below min stock 4 -> CRITICAL SHORTAGE!)
-- Part 6: Mumbai (4) + Delhi (3) = 7 (Above min stock 6 -> OK)
-- ====================================================================

INSERT INTO INVENTORY
(inventory_id, part_id, location, quantity_available, last_updated)
VALUES

-- Part 1: Hydraulic Pump Core (Min: 5, Total: 3)
(1, 1, 'Chennai Central Warehouse', 2, '2026-10-01 10:00:00'),

(2, 1, 'Mumbai Hangar Store', 1, '2026-10-05 14:30:00'),

-- Part 2: Turbine Bearing High-Temp (Min: 10, Total: 11)
(3, 2, 'Bangalore Regional Depot', 6, '2026-09-28 09:15:00'),

(4, 2, 'Delhi Hangar Bay', 5, '2026-10-08 11:20:00'),

-- Part 3: Fuel Filter (Min: 8, Total: 5)
(5, 3, 'Chennai Central Warehouse', 3, '2026-10-02 16:45:00'),

(6, 3, 'Bangalore Regional Depot', 2, '2026-10-06 13:00:00'),

-- Part 4: Pressure Regulating Valve (Min: 6, Total: 8)
(7, 4, 'Chennai Central Warehouse', 8, '2026-10-04 12:10:00'),

-- Part 5: Receiver Antenna Module (Min: 4, Total: 1)
(8, 5, 'Delhi Hangar Bay', 1, '2026-10-09 15:40:00'),

-- Part 6: Carbon Brake Rotor Disc (Min: 6, Total: 7)
(9, 6, 'Mumbai Hangar Store', 4, '2026-09-30 08:50:00'),

(10, 6, 'Delhi Hangar Bay', 3, '2026-10-07 17:15:00');


-- ====================================================================
-- 10. ALERT (PERSON 3: ALERTS COVERING DIVERSE CATEGORIES)
-- ====================================================================

INSERT INTO ALERT
(alert_id, aircraft_id, component_id, alert_type, severity,
 message, created_at, status)
VALUES

-- Critical Failure Alert
(1, 1, 5, 'CRITICAL_FAILURE', 'CRITICAL',
 'Complete antenna lockup and signal dropout on Weather Radar RAD-005 (VT-ABC).',
 '2026-09-01 14:22:00', 'OPEN'),

-- Service Limit Breach Alert
(2, NULL, 4, 'SERVICE_LIMIT_EXCEEDED', 'CRITICAL',
 'Auxiliary Power Unit APU-004 has operated 15,300 hrs, exceeding limit of 15,000 hrs.',
 '2024-05-18 09:00:00', 'OPEN'),

-- Overdue Maintenance Alert
(3, 4, 2, 'OVERDUE_MAINTENANCE', 'HIGH',
 'Hot Section Inspection for Engine ENG-002 on VT-EXP is past due date (2026-08-10).',
 '2026-08-11 06:00:00', 'OPEN'),

-- Low Stock Alert
(4, NULL, NULL, 'LOW_STOCK', 'HIGH',
 'Hydraulic Pump Core (HYD-P-001) total stock is 3 units across all locations, below minimum of 5.',
 '2026-10-05 14:35:00', 'OPEN'),

-- Low Stock Alert
(5, NULL, NULL, 'LOW_STOCK', 'HIGH',
 'Receiver Antenna Module (RAD-M-005) total stock is 1 unit, critically below minimum of 4.',
 '2026-10-09 15:45:00', 'OPEN'),

-- Acknowledged alert
(6, 1, 5, 'RECURRING_FAILURE', 'HIGH',
 'Component RAD-005 has suffered 3 recurring failures of type Signal Loss / Transceiver Dropout.',
 '2026-09-01 15:00:00', 'ACKNOWLEDGED'),

-- Resolved alert
(7, 2, 7, 'MAINTENANCE_REQUIRED', 'LOW',
 'Hydraulic weeping detected on HP-5000; inspected and seal scheduled.',
 '2024-04-10 11:00:00', 'RESOLVED');


-- =====================================================
-- PERSON 3 - SPARE PARTS & INVENTORY SAMPLE DATA
-- Author-AVANDIKA K
-- Registration no:25BCE2293
-- =====================================================
-- Spare parts and inventory records for Person 3 are already included
-- in sections 4 and 9 above. They are not inserted again because the
-- part numbers are unique and the main dataset already covers all stock locations.
