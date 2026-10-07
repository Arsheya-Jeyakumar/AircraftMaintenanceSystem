START TRANSACTION;

INSERT INTO TECHNICIAN
(employee_no, name, qualification, specialization, phone, status)
VALUES
('TEST_ROLLBACK', 'Temporary Test', 'Test Qualification',
 'Test Specialization', '0000000000', 'ACTIVE');

SELECT *
FROM TECHNICIAN
WHERE employee_no = 'TEST_ROLLBACK';

ROLLBACK;

SELECT *
FROM TECHNICIAN
WHERE employee_no = 'TEST_ROLLBACK';

-- ============================================
-- PERSON 1 - AIRCRAFT & COMPONENT TEST QUERIES
-- ============================================

USE aircraft_maintenance_db;

-- Test 1: Display all aircraft
SELECT *
FROM AIRCRAFT;

-- Test 2: Display all components
SELECT *
FROM COMPONENT;

-- Test 3: Display complete component lifecycle
SELECT *
FROM component_lifecycle_history
ORDER BY installed_date;

-- Test 4: Search lifecycle history for one component
SELECT *
FROM component_lifecycle_history
WHERE serial_number = 'ENG-001'
ORDER BY installed_date;

-- Test 5: Check currently installed components
SELECT
    a.registration_no,
    c.serial_number,
    c.component_type,
    ac.installed_date
FROM AIRCRAFT_COMPONENT ac
JOIN AIRCRAFT a
    ON ac.aircraft_id = a.aircraft_id
JOIN COMPONENT c
    ON ac.component_id = c.component_id
WHERE ac.removed_date IS NULL;

-- Test 6: Test component removal trigger
UPDATE AIRCRAFT_COMPONENT
SET removed_date = '2026-01-15',
    removal_hours = 5000.00,
    removal_cycles = 2500,
    removal_reason = 'Scheduled Replacement'
WHERE installation_id = 1;

-- Verify trigger updated component status
SELECT
    component_id,
    serial_number,
    current_status,
    current_location
FROM COMPONENT
WHERE component_id = 1;

-- Roll back test change
START TRANSACTION;

UPDATE AIRCRAFT_COMPONENT
SET removed_date = NULL,
    removal_hours = NULL,
    removal_cycles = NULL,
    removal_reason = NULL
WHERE installation_id = 1;

ROLLBACK;
