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

