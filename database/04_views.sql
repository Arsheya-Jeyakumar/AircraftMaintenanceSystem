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

    -- =====================================================
-- PERSON 3 - INVENTORY VIEW
-- Author - AVANDIKA K
-- Registration no - 25BCE2293
-- =====================================================

-- VIEW: LOW STOCK INVENTORY
CREATE VIEW low_stock_inventory AS
SELECT
    sp.part_id,
    sp.part_number,
    sp.part_name,
    sp.component_type,
    i.location,
    i.quantity_available,
    sp.minimum_stock
FROM SPARE_PART sp
JOIN INVENTORY i
    ON sp.part_id = i.part_id
WHERE i.quantity_available < sp.minimum_stock;
    m.scheduled_date,
    m.completed_date,
    m.status,
    t.employee_no,
    t.name AS technician_name
FROM MAINTENANCE m
LEFT JOIN TECHNICIAN t
    ON m.technician_id = t.technician_id;
