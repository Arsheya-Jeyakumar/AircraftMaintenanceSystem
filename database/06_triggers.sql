/*
========================================================
Aircraft Maintenance & Component Lifecycle System
Person 2 - Maintenance & Inspection Module

Author: YOUR NAME
Registration No: YOUR REG NO

Trigger:
    - trg_maintenance_completion
========================================================
*/

USE aircraft_maintenance_db;

DELIMITER $$

CREATE TRIGGER trg_maintenance_completion
BEFORE UPDATE ON MAINTENANCE
FOR EACH ROW
BEGIN
    IF NEW.completed_date IS NOT NULL THEN
        SET NEW.status = 'COMPLETED';
    END IF;
END$$

DELIMITER ;