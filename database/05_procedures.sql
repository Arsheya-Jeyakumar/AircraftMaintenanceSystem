/*
Aircraft Maintenance & Component Lifecycle System
Person 2 - Maintenance & Inspection Module
Aircraft Maintenance & Component Lifecycle Intelligence System
Database Management System (DBMS) Project

Script 05: Stored Procedures

Required Stored Procedures:
1. sp_get_overdue_maintenance (Person 2)-ARSHEYA
2. sp_install_component       (Person 1)-PURUSHOTHAM
3. sp_remove_component        (Person 1)
4. sp_generate_risk_score     (Person 3 / Risk Intelligence Engine)-AVANDIKA
====================================================================
*/

USE aircraft_maintenance_db;

DROP PROCEDURE IF EXISTS sp_get_overdue_maintenance;
DROP PROCEDURE IF EXISTS sp_install_component;
DROP PROCEDURE IF EXISTS sp_remove_component;
DROP PROCEDURE IF EXISTS sp_generate_risk_score;
DROP PROCEDURE IF EXISTS GetOverdueMaintenance;
DROP PROCEDURE IF EXISTS GetComponentLifecycleHistory;
DROP PROCEDURE IF EXISTS generate_low_stock_alerts;


-- ====================================================================
-- PROCEDURE 1: GET OVERDUE MAINTENANCE (PERSON 2)
-- Retrieves all maintenance events that have exceeded scheduled date
-- ====================================================================

DELIMITER $$

CREATE PROCEDURE sp_get_overdue_maintenance()
BEGIN
    SELECT
        m.maintenance_id,
        m.maintenance_type,
        m.scheduled_date,
        DATEDIFF(CURRENT_DATE, m.scheduled_date) AS days_overdue,
        m.status,
        m.description,
        a.aircraft_id,
        a.registration_no,
        a.model AS aircraft_model,
        c.component_id,
        COALESCE(c.serial_number, 'AIRFRAME') AS component_serial,
        COALESCE(c.component_type, 'General Airframe') AS component_type,
        t.technician_id,
        t.employee_no AS technician_employee_no,
        t.name AS technician_name
    FROM MAINTENANCE m
    JOIN AIRCRAFT a
        ON m.aircraft_id = a.aircraft_id
    LEFT JOIN COMPONENT c
        ON m.component_id = c.component_id
    LEFT JOIN TECHNICIAN t
        ON m.technician_id = t.technician_id
    WHERE m.scheduled_date < CURRENT_DATE
      AND m.status <> 'COMPLETED'
    ORDER BY m.scheduled_date ASC;
END$$

DELIMITER ;


-- ====================================================================
-- PROCEDURE 2: INSTALL COMPONENT (PERSON 1)
-- Transactionally installs a component onto an aircraft:
-- 1. Verifies component is not already installed anywhere
-- 2. Inserts AIRCRAFT_COMPONENT record
-- 3. Updates COMPONENT status to 'INSTALLED' and location to Aircraft Reg
-- ====================================================================

DELIMITER $$

CREATE PROCEDURE sp_install_component(
    IN p_aircraft_id INT,
    IN p_component_id INT,
    IN p_installed_date DATE,
    IN p_installation_hours DECIMAL(10,2),
    IN p_installation_cycles INT,
    IN p_reason VARCHAR(100),
    OUT p_installation_id INT
)
proc_label: BEGIN
    DECLARE v_already_installed INT DEFAULT 0;
    DECLARE v_aircraft_reg VARCHAR(20);
    DECLARE v_aircraft_status VARCHAR(20);
    
    -- Error handler for rollback
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- Validate aircraft exists and is active
    SELECT registration_no, status INTO v_aircraft_reg, v_aircraft_status
    FROM AIRCRAFT
    WHERE aircraft_id = p_aircraft_id;
    
    IF v_aircraft_reg IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Target aircraft does not exist.';
    END IF;
    
    IF v_aircraft_status = 'DECOMMISSIONED' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot install component on a decommissioned aircraft.';
    END IF;

    -- Check if component is currently installed on any aircraft
    SELECT COUNT(*) INTO v_already_installed
    FROM AIRCRAFT_COMPONENT
    WHERE component_id = p_component_id
      AND removed_date IS NULL;
      
    IF v_already_installed > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Validation Error: Component is currently installed on an aircraft and must be removed first.';
    END IF;

    -- Execute transactional installation
    START TRANSACTION;
    
    INSERT INTO AIRCRAFT_COMPONENT (
        aircraft_id,
        component_id,
        installed_date,
        installation_hours,
        installation_cycles,
        installation_reason
    ) VALUES (
        p_aircraft_id,
        p_component_id,
        p_installed_date,
        p_installation_hours,
        p_installation_cycles,
        COALESCE(p_reason, 'Standard Installation')
    );
    
    SET p_installation_id = LAST_INSERT_ID();
    
    UPDATE COMPONENT
    SET current_status = 'INSTALLED',
        current_location = v_aircraft_reg
    WHERE component_id = p_component_id;
    
    COMMIT;
END$$

DELIMITER ;


-- ====================================================================
-- PROCEDURE 3: REMOVE COMPONENT (PERSON 1)
-- Transactionally removes a component from an aircraft:
-- 1. Validates active installation exists
-- 2. Validates removal counters >= installation counters
-- 3. Updates AIRCRAFT_COMPONENT removal fields
-- 4. Updates COMPONENT status to 'AVAILABLE' / 'MAINTENANCE' and location
-- ====================================================================

DELIMITER $$

CREATE PROCEDURE sp_remove_component(
    IN p_installation_id INT,
    IN p_removed_date DATE,
    IN p_removal_hours DECIMAL(10,2),
    IN p_removal_cycles INT,
    IN p_reason VARCHAR(100),
    IN p_new_location VARCHAR(100),
    IN p_new_status VARCHAR(30)
)
BEGIN
    DECLARE v_comp_id INT;
    DECLARE v_inst_hours DECIMAL(10,2);
    DECLARE v_inst_cycles INT;
    DECLARE v_already_removed DATE;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- Lookup existing installation
    SELECT component_id, installation_hours, installation_cycles, removed_date
    INTO v_comp_id, v_inst_hours, v_inst_cycles, v_already_removed
    FROM AIRCRAFT_COMPONENT
    WHERE installation_id = p_installation_id;

    IF v_comp_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Installation record not found.';
    END IF;

    IF v_already_removed IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Component has already been removed from this installation.';
    END IF;

    IF p_removal_hours < v_inst_hours THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Removal flight hours cannot be less than installation flight hours.';
    END IF;

    IF p_removal_cycles < v_inst_cycles THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Removal cycles cannot be less than installation cycles.';
    END IF;

    START TRANSACTION;

    UPDATE AIRCRAFT_COMPONENT
    SET removed_date = p_removed_date,
        removal_hours = p_removal_hours,
        removal_cycles = p_removal_cycles,
        removal_reason = COALESCE(p_reason, 'Operational Removal')
    WHERE installation_id = p_installation_id;

    UPDATE COMPONENT
    SET current_status = COALESCE(p_new_status, 'AVAILABLE'),
        current_location = COALESCE(p_new_location, 'Central Warehouse')
    WHERE component_id = v_comp_id;

    COMMIT;
END$$

DELIMITER ;


-- ====================================================================
-- PROCEDURE 4: GENERATE RISK SCORE (PERSON 3 / RISK INTELLIGENCE)
-- Explainable Risk Scoring Engine:
--   +30 points: At or beyond service limit (breach)
--   +20 points: Nearing service limit (>= 85%, mutually exclusive with breach)
--   +25 points: Incomplete overdue maintenance
--   +15 points: >= 3 recurring failures
--   +10 points: Replacement spare stock below minimum
-- Output: score (0-100), level (LOW/MEDIUM/HIGH), and explainability string
-- ====================================================================

DELIMITER $$

CREATE PROCEDURE sp_generate_risk_score(
    IN p_component_id INT,
    OUT p_risk_score INT,
    OUT p_risk_level VARCHAR(20),
    OUT p_factors VARCHAR(500)
)
BEGIN
    DECLARE v_limit_hours DECIMAL(10,2);
    DECLARE v_limit_cycles INT;
    DECLARE v_comp_type VARCHAR(50);
    DECLARE v_used_hours DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_used_cycles INT DEFAULT 0;
    DECLARE v_overdue_count INT DEFAULT 0;
    DECLARE v_failure_count INT DEFAULT 0;
    DECLARE v_low_stock_count INT DEFAULT 0;
    
    DECLARE v_service_pts INT DEFAULT 0;
    DECLARE v_maint_pts INT DEFAULT 0;
    DECLARE v_fail_pts INT DEFAULT 0;
    DECLARE v_stock_pts INT DEFAULT 0;
    DECLARE v_total_score INT DEFAULT 0;
    DECLARE v_explanation VARCHAR(500) DEFAULT '';

    -- Get component baseline
    SELECT service_limit_hours, service_limit_cycles, component_type
    INTO v_limit_hours, v_limit_cycles, v_comp_type
    FROM COMPONENT
    WHERE component_id = p_component_id;

    IF v_comp_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Component not found.';
    END IF;

    -- Calculate lifetime hours & cycles logged across historical and current installations
    SELECT 
        ROUND(COALESCE(SUM(
            CASE 
                WHEN ac.removed_date IS NOT NULL THEN (ac.removal_hours - ac.installation_hours)
                ELSE GREATEST(0, a.total_flight_hours - ac.installation_hours)
            END
        ), 0), 2),
        COALESCE(SUM(
            CASE 
                WHEN ac.removed_date IS NOT NULL THEN (ac.removal_cycles - ac.installation_cycles)
                ELSE GREATEST(0, a.total_cycles - ac.installation_cycles)
            END
        ), 0)
    INTO v_used_hours, v_used_cycles
    FROM AIRCRAFT_COMPONENT ac
    JOIN AIRCRAFT a ON ac.aircraft_id = a.aircraft_id
    WHERE ac.component_id = p_component_id;

    -- Rule 1 & 2: Service Limit Scoring (+30 breached, +20 nearing limit, mutually exclusive)
    IF (v_limit_hours IS NOT NULL AND v_used_hours >= v_limit_hours)
       OR (v_limit_cycles IS NOT NULL AND v_used_cycles >= v_limit_cycles) THEN
        SET v_service_pts = 30;
        SET v_explanation = CONCAT(v_explanation, '+30: Service limit exceeded; ');
    ELSEIF (v_limit_hours IS NOT NULL AND v_used_hours >= 0.85 * v_limit_hours)
       OR (v_limit_cycles IS NOT NULL AND v_used_cycles >= 0.85 * v_limit_cycles) THEN
        SET v_service_pts = 20;
        SET v_explanation = CONCAT(v_explanation, '+20: Nearing service limit (>=85%); ');
    END IF;

    -- Rule 3: Overdue Maintenance (+25 points)
    SELECT COUNT(*) INTO v_overdue_count
    FROM MAINTENANCE
    WHERE component_id = p_component_id
      AND scheduled_date < CURRENT_DATE
      AND status <> 'COMPLETED';

    IF v_overdue_count > 0 THEN
        SET v_maint_pts = 25;
        SET v_explanation = CONCAT(v_explanation, '+25: Pending overdue maintenance (', v_overdue_count, '); ');
    END IF;

    -- Rule 4: Recurring Failures (+15 points for >= 3 incidents)
    SELECT COUNT(*) INTO v_failure_count
    FROM FAILURE
    WHERE component_id = p_component_id;

    IF v_failure_count >= 3 THEN
        SET v_fail_pts = 15;
        SET v_explanation = CONCAT(v_explanation, '+15: Recurring failures (', v_failure_count, ' incidents); ');
    END IF;

    -- Rule 5: Low Replacement Spare Stock (+10 points)
    SELECT COUNT(*) INTO v_low_stock_count
    FROM v_low_stock_parts
    WHERE LOWER(component_type) = LOWER(v_comp_type);

    IF v_low_stock_count > 0 THEN
        SET v_stock_pts = 10;
        SET v_explanation = CONCAT(v_explanation, '+10: Replacement parts in short supply; ');
    END IF;

    -- Total score clamped to 0-100
    SET v_total_score = v_service_pts + v_maint_pts + v_fail_pts + v_stock_pts;
    IF v_total_score > 100 THEN
        SET v_total_score = 100;
    END IF;

    SET p_risk_score = v_total_score;

    -- Determine category
    IF v_total_score >= 60 THEN
        SET p_risk_level = 'HIGH';
    ELSEIF v_total_score >= 30 THEN
        SET p_risk_level = 'MEDIUM';
    ELSE
        SET p_risk_level = 'LOW';
    END IF;

    IF LENGTH(v_explanation) = 0 THEN
        SET p_factors = 'Optimal: No risk factors active.';
    ELSE
        SET p_factors = TRIM(TRAILING '; ' FROM v_explanation);
    END IF;
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
