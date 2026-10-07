/*
========================================================
Aircraft Maintenance & Component Lifecycle System
Person 2 - Maintenance & Inspection Module

Author: ARSHEYA TARMIA JEYAKUMAR
Registration No: 25BCE2629

Stored Procedure:
    - GetOverdueMaintenance
========================================================
*/

USE aircraft_maintenance_db;

DELIMITER $$

CREATE PROCEDURE GetOverdueMaintenance()
BEGIN
    SELECT
        maintenance_id,
        aircraft_id,
        component_id,
        maintenance_type,
        scheduled_date,
        status,
        technician_id
    FROM MAINTENANCE
    WHERE scheduled_date < CURRENT_DATE
      AND status <> 'COMPLETED';
END$$

DELIMITER ;

-- =====================================================
-- PERSON 3 - INVENTORY ALERT PROCEDURE
-- Auhthor - AVANDIKA K
-- Registration number - 25BCE2293
-- =====================================================

DELIMITER $$

CREATE PROCEDURE generate_low_stock_alerts()
BEGIN
    INSERT INTO ALERT
    (alert_type, severity, message)
    SELECT
        'LOW_STOCK',
        'HIGH',
        CONCAT(
            sp.part_name,
            ' stock is below minimum level. Available: ',
            i.quantity_available,
            ', Minimum required: ',
            sp.minimum_stock
        )
    FROM SPARE_PART sp
    JOIN INVENTORY i
        ON sp.part_id = i.part_id
    WHERE i.quantity_available < sp.minimum_stock;
END$$

DELIMITER ;
