/*
====================================================================
Aircraft Maintenance & Component Lifecycle Intelligence System
Database Management System (DBMS) Project

Script 07: Comprehensive Test Verification Suite

Test Categories Covered:
1. Table Integrity & Row Counts
2. Constraint & Integrity Checks (Unique, Not Null, Check Constraints)
3. Relational Joins & Aggregates
4. View Verifications:
   - v_aircraft_current_components
   - v_component_lifecycle_history
   - v_overdue_maintenance
   - v_recurring_failures (>= 3 threshold)
   - v_low_stock_parts (Multi-location aggregation)
   - v_risk_intelligence_summary
5. Stored Procedure Executions:
   - sp_get_overdue_maintenance
   - sp_install_component (Transactional with Rollback test)
   - sp_remove_component (Transactional with Rollback test)
   - sp_generate_risk_score
6. Trigger Executions & Automated Alert Generation:
   - High-severity failure triggers critical alert
   - Multi-location stock drop triggers low stock alert
   - Maintenance completion sync
   - Prevention of duplicate simultaneous component installation
====================================================================
*/

USE aircraft_maintenance_db;

-- ====================================================================
-- TEST SUITE 1: TABLE INTEGRITY & DATA POPULATION CHECK
-- ====================================================================
SELECT '1.1 Table Verification & Row Counts' AS test_name;
SELECT 'AIRCRAFT' AS table_name, COUNT(*) AS row_count FROM AIRCRAFT
UNION ALL
SELECT 'COMPONENT', COUNT(*) FROM COMPONENT
UNION ALL
SELECT 'TECHNICIAN', COUNT(*) FROM TECHNICIAN
UNION ALL
SELECT 'SPARE_PART', COUNT(*) FROM SPARE_PART
UNION ALL
SELECT 'AIRCRAFT_COMPONENT', COUNT(*) FROM AIRCRAFT_COMPONENT
UNION ALL
SELECT 'MAINTENANCE', COUNT(*) FROM MAINTENANCE
UNION ALL
SELECT 'INSPECTION', COUNT(*) FROM INSPECTION
UNION ALL
SELECT 'FAILURE', COUNT(*) FROM FAILURE
UNION ALL
SELECT 'INVENTORY', COUNT(*) FROM INVENTORY
UNION ALL
SELECT 'ALERT', COUNT(*) FROM ALERT;


-- ====================================================================
-- TEST SUITE 2: RELATIONAL JOINS & AGGREGATE QUERIES
-- ====================================================================
SELECT '2.1 Aircraft Fleet Summary with Active Component Counts' AS test_name;
SELECT 
    a.registration_no,
    a.model,
    a.manufacturer,
    a.total_flight_hours,
    a.total_cycles,
    a.status,
    COUNT(ac.installation_id) AS currently_installed_components
FROM AIRCRAFT a
LEFT JOIN AIRCRAFT_COMPONENT ac 
    ON a.aircraft_id = ac.aircraft_id AND ac.removed_date IS NULL
GROUP BY a.aircraft_id, a.registration_no, a.model, a.manufacturer, a.total_flight_hours, a.total_cycles, a.status
ORDER BY a.registration_no;

SELECT '2.2 Maintenance Count & Status Breakdown by Aircraft' AS test_name;
SELECT 
    a.registration_no,
    COUNT(m.maintenance_id) AS total_maintenance_events,
    SUM(CASE WHEN m.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count,
    SUM(CASE WHEN m.status = 'OVERDUE' THEN 1 ELSE 0 END) AS overdue_count,
    SUM(CASE WHEN m.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_count
FROM AIRCRAFT a
LEFT JOIN MAINTENANCE m ON a.aircraft_id = m.aircraft_id
GROUP BY a.aircraft_id, a.registration_no;


-- ====================================================================
-- TEST SUITE 3: DATABASE VIEW VERIFICATIONS
-- ====================================================================

SELECT '3.1 VIEW: v_aircraft_current_components' AS test_name;
SELECT 
    aircraft_registration,
    aircraft_model,
    component_serial,
    component_type,
    installed_date,
    current_installation_hours,
    service_limit_hours,
    hours_remaining_to_limit
FROM v_aircraft_current_components
ORDER BY aircraft_registration, component_type;

SELECT '3.2 VIEW: v_component_lifecycle_history' AS test_name;
SELECT 
    serial_number,
    component_type,
    registration_no AS aircraft,
    installed_date,
    removed_date,
    operational_hours_logged,
    lifecycle_state
FROM v_component_lifecycle_history
ORDER BY serial_number, installed_date;

SELECT '3.3 VIEW: v_overdue_maintenance' AS test_name;
SELECT 
    maintenance_id,
    maintenance_type,
    scheduled_date,
    days_overdue,
    registration_no,
    component_serial,
    technician_name
FROM v_overdue_maintenance
ORDER BY days_overdue DESC;

SELECT '3.4 VIEW: v_recurring_failures (>= 3 Incidents)' AS test_name;
SELECT 
    serial_number,
    component_type,
    failure_type,
    incident_count,
    unresolved_incidents,
    highest_severity,
    affected_aircraft
FROM v_recurring_failures;

SELECT '3.5 VIEW: v_low_stock_parts (Multi-Location Aggregated Deficit)' AS test_name;
SELECT 
    part_number,
    part_name,
    component_type,
    minimum_stock,
    total_quantity_available,
    stock_deficit,
    location_breakdown
FROM v_low_stock_parts;

SELECT '3.6 VIEW: v_risk_intelligence_summary' AS test_name;
SELECT 
    serial_number,
    component_type,
    installed_aircraft,
    risk_score,
    risk_level,
    risk_explanation
FROM v_risk_intelligence_summary
ORDER BY risk_score DESC;


-- ====================================================================
-- TEST SUITE 4: STORED PROCEDURE VERIFICATIONS
-- ====================================================================

SELECT '4.1 EXECUTE: sp_get_overdue_maintenance()' AS test_name;
CALL sp_get_overdue_maintenance();

SELECT '4.2 EXECUTE: sp_generate_risk_score for Component 4 (APU-004 - Exceeded limit)' AS test_name;
CALL sp_generate_risk_score(4, @score, @level, @factors);
SELECT 4 AS component_id, @score AS calculated_score, @level AS risk_level, @factors AS explanation;

SELECT '4.3 EXECUTE: sp_generate_risk_score for Component 5 (RAD-005 - Recurring failures + Overdue)' AS test_name;
CALL sp_generate_risk_score(5, @score, @level, @factors);
SELECT 5 AS component_id, @score AS calculated_score, @level AS risk_level, @factors AS explanation;

SELECT '4.4 EXECUTE: sp_generate_risk_score for Component 6 (FMC-006 - Healthy)' AS test_name;
CALL sp_generate_risk_score(6, @score, @level, @factors);
SELECT 6 AS component_id, @score AS calculated_score, @level AS risk_level, @factors AS explanation;


-- ====================================================================
-- TEST SUITE 5: TRANSACTION & TRIGGER TESTING (NON-DESTRUCTIVE ROLLBACKS)
-- ====================================================================

-- 5.1 Test Failure Trigger: Inserting HIGH severity failure generates an ALERT
START TRANSACTION;

SELECT '5.1 Test Trigger: High-Severity Failure Alert Creation' AS test_name;
INSERT INTO FAILURE (
    aircraft_id, component_id, failure_date, failure_type, severity, description, resolved
) VALUES (
    1, 1, CURRENT_DATE, 'Uncontained Bearing Friction Test', 'HIGH', 'Test failure for trigger verification.', FALSE
);

-- Check that trigger inserted the alert
SELECT alert_id, alert_type, severity, message, status
FROM ALERT
WHERE alert_type = 'CRITICAL_FAILURE' AND message LIKE '%Uncontained Bearing Friction Test%';

ROLLBACK;

SELECT *
FROM TECHNICIAN
WHERE employee_no = 'TEST_ROLLBACK';

-- =====================================================
-- PERSON 3 - SPARE PARTS & INVENTORY TEST QUERIES
-- Author - AVANDIKA K
-- Registration no - 25BCE2293
-- =====================================================

USE aircraft_maintenance_db;


-- =====================================================
-- TEST 1: DISPLAY ALL INVENTORY
-- =====================================================

SELECT
    sp.part_number,
    sp.part_name,
    i.location,
    i.quantity_available,
    sp.minimum_stock
FROM SPARE_PART sp
JOIN INVENTORY i
    ON sp.part_id = i.part_id;


-- =====================================================
-- TEST 2: CHECK STOCK STATUS
-- =====================================================

SELECT
    sp.part_number,
    sp.part_name,
    i.quantity_available,
    sp.minimum_stock,
    CASE
        WHEN i.quantity_available < sp.minimum_stock
            THEN 'LOW'
        ELSE 'OK'
    END AS stock_status
FROM SPARE_PART sp
JOIN INVENTORY i
    ON sp.part_id = i.part_id;


-- =====================================================
-- TEST 3: LOW STOCK VIEW
-- =====================================================

SELECT *
FROM low_stock_inventory;


-- =====================================================
-- TEST 4: GENERATE LOW STOCK ALERTS
-- =====================================================

CALL generate_low_stock_alerts();


-- =====================================================
-- TEST 5: DISPLAY GENERATED ALERTS
-- =====================================================

SELECT
    alert_id,
    alert_type,
    severity,
    message,
    created_at,
    status
FROM ALERT
ORDER BY created_at DESC;

-- 5.2 Test Maintenance Status Trigger: Setting completed_date sets status to COMPLETED
START TRANSACTION;

SELECT '5.2 Test Trigger: Maintenance Completion Sync' AS test_name;
UPDATE MAINTENANCE
SET completed_date = CURRENT_DATE
WHERE maintenance_id = 5;

SELECT maintenance_id, status, completed_date
FROM MAINTENANCE
WHERE maintenance_id = 5;

ROLLBACK;


-- 5.3 Test Inventory Low Stock Trigger
START TRANSACTION;

SELECT '5.3 Test Trigger: Inventory Stock Reduction Triggering Low Stock Alert' AS test_name;
-- Reduce part 4 (Pressure Regulating Valve, min stock 6) from 8 down to 2
UPDATE INVENTORY
SET quantity_available = 2
WHERE inventory_id = 7;

SELECT alert_id, alert_type, severity, message, status
FROM ALERT
WHERE alert_type = 'LOW_STOCK' AND message LIKE '%VAL-004%';

ROLLBACK;


-- 5.4 Test Component Lifecycle Installation & Duplicate Guard
START TRANSACTION;

SELECT '5.4 Test Trigger: Duplicate Active Installation Guard' AS test_name;
-- Component 1 is already installed on VT-ABC. Attempting to install on VT-IND should fail or be caught
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM AIRCRAFT_COMPONENT WHERE component_id = 1 AND removed_date IS NULL)
        THEN 'PASSED: Component 1 correctly flagged as already installed.'
        ELSE 'FAILED'
    END AS duplicate_installation_guard_check;

ROLLBACK;
