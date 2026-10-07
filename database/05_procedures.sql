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
-- PERSON 1 - COMPONENT LIFECYCLE HISTORY PROCEDURE
-- =====================================================

DELIMITER $$

CREATE PROCEDURE GetComponentLifecycleHistory(
    IN p_serial_number VARCHAR(50)
)
BEGIN
    SELECT
        installation_id,
        registration_no,
        aircraft_model,
        serial_number,
        component_type,
        component_manufacturer,
        component_model,
        installed_date,
        removed_date,
        installation_hours,
        removal_hours,
        installation_cycles,
        removal_cycles,
        installation_reason,
        removal_reason
    FROM component_lifecycle_history
    WHERE serial_number = p_serial_number
    ORDER BY installed_date;
END$$

DELIMITER ;
