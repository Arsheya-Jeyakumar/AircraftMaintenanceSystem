/*
========================================================
Aircraft Maintenance & Component Lifecycle System
Person 2 - Maintenance & Inspection Module

Author: ARSHEYA TARMIA JEYAKUMAR
Registration No: 25BCE2629

Views:
    - overdue_maintenance
    - maintenance_history
========================================================
*/

USE aircraft_maintenance_db;


-- =====================================================
-- VIEW 1: OVERDUE MAINTENANCE
-- =====================================================

CREATE VIEW overdue_maintenance AS
SELECT
    m.maintenance_id,
    m.aircraft_id,
    m.component_id,
    m.maintenance_type,
    m.scheduled_date,
    m.status,
    m.technician_id
FROM MAINTENANCE m
WHERE m.scheduled_date < CURRENT_DATE
  AND m.status <> 'COMPLETED';


-- =====================================================
-- VIEW 2: MAINTENANCE HISTORY
-- =====================================================

CREATE VIEW maintenance_history AS
SELECT
    m.maintenance_id,
    m.aircraft_id,
    m.component_id,
    m.maintenance_type,
    m.scheduled_date,
    m.completed_date,
    m.status,
    t.employee_no,
    t.name AS technician_name
FROM MAINTENANCE m
LEFT JOIN TECHNICIAN t
    ON m.technician_id = t.technician_id;

-- =====================================================
-- PERSON 1 - COMPONENT LIFECYCLE HISTORY VIEW
-- =====================================================

CREATE VIEW component_lifecycle_history AS
SELECT
    ac.installation_id,
    a.registration_no,
    a.model AS aircraft_model,
    c.serial_number,
    c.component_type,
    c.manufacturer AS component_manufacturer,
    c.model AS component_model,
    ac.installed_date,
    ac.removed_date,
    ac.installation_hours,
    ac.removal_hours,
    ac.installation_cycles,
    ac.removal_cycles,
    ac.installation_reason,
    ac.removal_reason
FROM AIRCRAFT_COMPONENT ac
JOIN AIRCRAFT a
    ON ac.aircraft_id = a.aircraft_id
JOIN COMPONENT c
    ON ac.component_id = c.component_id;

