/*
====================================================================
Aircraft Maintenance & Component Lifecycle Intelligence System
Database Management System (DBMS) Project

Script 06: Database Triggers

Implemented Triggers:
1. trg_failure_critical_alert (Person 2 / Person 3)
   - Automatically generates an alert when a HIGH or CRITICAL failure is logged.
2. trg_inventory_low_stock (Person 3)
   - Evaluates aggregated multi-location inventory on update and triggers
     a LOW_STOCK alert if total quantity drops below minimum_stock,
     safeguarding against duplicate open alerts.
3. trg_maintenance_completion (Person 2)
   - Automatically synchronizes status and completed_date on MAINTENANCE.
4. trg_component_removal_sync (Person 1)
   - Automatically resets component status to 'AVAILABLE' in 'Warehouse'
     when an installation record has removed_date set.
5. trg_prevent_duplicate_active_installation (Person 1)
   - Enforces referential integrity to prevent a component from being
     simultaneously installed on multiple aircraft.
====================================================================
*/

USE aircraft_maintenance_db;

DROP TRIGGER IF EXISTS trg_failure_critical_alert;
DROP TRIGGER IF EXISTS trg_inventory_low_stock;
DROP TRIGGER IF EXISTS trg_maintenance_completion;
DROP TRIGGER IF EXISTS trg_component_removal_sync;
DROP TRIGGER IF EXISTS trg_prevent_duplicate_active_installation;
DROP TRIGGER IF EXISTS trg_inventory_update;
DROP TRIGGER IF EXISTS trg_component_removal;


-- ====================================================================
-- TRIGGER 1: AUTOMATIC FAILURE ALERT
-- Fires AFTER INSERT on FAILURE: Generates alert for HIGH or CRITICAL failures
-- ====================================================================

DELIMITER $$

CREATE TRIGGER trg_failure_critical_alert
AFTER INSERT ON FAILURE
FOR EACH ROW
BEGIN
    IF NEW.severity IN ('HIGH', 'CRITICAL') THEN
        INSERT INTO ALERT (
            aircraft_id,
            component_id,
            alert_type,
            severity,
            message,
            created_at,
            status
        ) VALUES (
            NEW.aircraft_id,
            NEW.component_id,
            'CRITICAL_FAILURE',
            NEW.severity,
            CONCAT(
                'High severity failure recorded: [', NEW.failure_type, '] - ',
                COALESCE(NEW.description, 'Immediate inspection required.')
            ),
            CURRENT_TIMESTAMP,
            'OPEN'
        );
    END IF;
END$$

DELIMITER ;


-- ====================================================================
-- TRIGGER 2: MULTI-LOCATION LOW STOCK ALERT
-- Fires AFTER UPDATE on INVENTORY: Aggregates across all locations and
-- raises alert if total < minimum_stock, avoiding duplicate open alerts.
-- ====================================================================

DELIMITER $$

CREATE TRIGGER trg_inventory_low_stock
AFTER UPDATE ON INVENTORY
FOR EACH ROW
BEGIN
    DECLARE v_total_stock INT DEFAULT 0;
    DECLARE v_min_stock INT DEFAULT 0;
    DECLARE v_part_name VARCHAR(100);
    DECLARE v_part_no VARCHAR(50);
    DECLARE v_existing_alerts INT DEFAULT 0;

    -- Retrieve spare part configuration
    SELECT part_number, part_name, minimum_stock
    INTO v_part_no, v_part_name, v_min_stock
    FROM SPARE_PART
    WHERE part_id = NEW.part_id;

    -- Aggregate quantity across all warehouses and hangars
    SELECT COALESCE(SUM(quantity_available), 0)
    INTO v_total_stock
    FROM INVENTORY
    WHERE part_id = NEW.part_id;

    -- Check if total stock is below threshold
    IF v_total_stock < v_min_stock THEN
        -- Prevent spamming duplicate OPEN alerts for the same part
        SELECT COUNT(*)
        INTO v_existing_alerts
        FROM ALERT
        WHERE alert_type = 'LOW_STOCK'
          AND status = 'OPEN'
          AND message LIKE CONCAT('%', v_part_no, '%');

        IF v_existing_alerts = 0 THEN
            INSERT INTO ALERT (
                aircraft_id,
                component_id,
                alert_type,
                severity,
                message,
                created_at,
                status
            ) VALUES (
                NULL,
                NULL,
                'LOW_STOCK',
                'HIGH',
                CONCAT(
                    'Part [', v_part_no, ' - ', v_part_name,
                    '] aggregate stock has dropped to ', v_total_stock,
                    ' (minimum required: ', v_min_stock, ').'
                ),
                CURRENT_TIMESTAMP,
                'OPEN'
            );
        END IF;
    END IF;
END$$

DELIMITER ;


-- ====================================================================
-- TRIGGER 3: MAINTENANCE STATUS & COMPLETION DATE SYNCHRONIZATION
-- Fires BEFORE UPDATE on MAINTENANCE
-- ====================================================================

DELIMITER $$

CREATE TRIGGER trg_maintenance_completion
BEFORE UPDATE ON MAINTENANCE
FOR EACH ROW
BEGIN
    -- If completed_date is assigned, automatically mark status as COMPLETED
    IF NEW.completed_date IS NOT NULL AND (OLD.completed_date IS NULL OR OLD.status <> 'COMPLETED') THEN
        SET NEW.status = 'COMPLETED';
    END IF;
    
    -- If status is set to COMPLETED without a completed_date, populate current date
    IF NEW.status = 'COMPLETED' AND NEW.completed_date IS NULL THEN
        SET NEW.completed_date = CURRENT_DATE;
    END IF;
END$$

DELIMITER ;


-- ====================================================================
-- TRIGGER 4: COMPONENT REMOVAL SYNCHRONIZATION
-- Fires AFTER UPDATE on AIRCRAFT_COMPONENT: Re-enables component as AVAILABLE
-- ====================================================================

DELIMITER $$

CREATE TRIGGER trg_component_removal_sync
AFTER UPDATE ON AIRCRAFT_COMPONENT
FOR EACH ROW
BEGIN
    IF NEW.removed_date IS NOT NULL AND OLD.removed_date IS NULL THEN
        UPDATE COMPONENT
        SET current_status = 'AVAILABLE',
            current_location = 'Warehouse'
        WHERE component_id = NEW.component_id;
    END IF;
END$$

DELIMITER ;


-- ====================================================================
-- TRIGGER 5: PREVENT DUPLICATE SIMULTANEOUS COMPONENT INSTALLATIONS
-- Fires BEFORE INSERT on AIRCRAFT_COMPONENT: Guarantees a component is
-- not actively installed on more than one aircraft at the same time.
-- ====================================================================

DELIMITER $$

CREATE TRIGGER trg_prevent_duplicate_active_installation
BEFORE INSERT ON AIRCRAFT_COMPONENT
FOR EACH ROW
BEGIN
    DECLARE v_active_count INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_active_count
    FROM AIRCRAFT_COMPONENT
    WHERE component_id = NEW.component_id
      AND removed_date IS NULL;

    IF v_active_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Data Integrity Violation: Component is currently installed on an aircraft and cannot be installed again until removed.';
    END IF;
END$$

DELIMITER ;
