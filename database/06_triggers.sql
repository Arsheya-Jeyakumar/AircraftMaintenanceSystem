/*
========================================================
Aircraft Maintenance & Component Lifecycle System
Person 2 - Maintenance & Inspection Module

Author: ARSHEYA TARMIA JEYAKUMAR
Registration No: 25BCE2629

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

-- ============================================
-- PERSON 1 - COMPONENT LIFECYCLE TRIGGER
-- ============================================

DELIMITER $$

CREATE TRIGGER trg_component_removal
AFTER UPDATE ON AIRCRAFT_COMPONENT
FOR EACH ROW
BEGIN
    IF NEW.removed_date IS NOT NULL
       AND OLD.removed_date IS NULL THEN

        UPDATE COMPONENT
        SET current_status = 'AVAILABLE',
            current_location = 'Warehouse'
        WHERE component_id = NEW.component_id;

    END IF;
END$$

DELIMITER ;
