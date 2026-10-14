/*
====================================================================
Aircraft Maintenance & Component Lifecycle Intelligence System
Database Management System (DBMS) Project

Script 04: Database Views

Required Views:
1. v_aircraft_current_components (Person 1)
2. v_component_lifecycle_history (Person 1)
3. v_overdue_maintenance         (Person 2)
4. v_recurring_failures          (Person 2)
5. v_low_stock_parts             (Person 3)
6. v_risk_intelligence_summary   (Integrated Multi-Module Intelligence)
====================================================================
*/

USE aircraft_maintenance_db;

-- Drop existing views if they exist
DROP VIEW IF EXISTS v_risk_intelligence_summary;
DROP VIEW IF EXISTS v_low_stock_parts;
DROP VIEW IF EXISTS v_recurring_failures;
DROP VIEW IF EXISTS v_overdue_maintenance;
DROP VIEW IF EXISTS v_component_lifecycle_history;
DROP VIEW IF EXISTS v_aircraft_current_components;
DROP VIEW IF EXISTS overdue_maintenance;
DROP VIEW IF EXISTS maintenance_history;
DROP VIEW IF EXISTS component_lifecycle_history;
DROP VIEW IF EXISTS low_stock_inventory;


-- ====================================================================
-- VIEW 1: AIRCRAFT CURRENT INSTALLED COMPONENTS (PERSON 1)
-- ====================================================================

CREATE VIEW v_aircraft_current_components AS
SELECT
    a.aircraft_id,
    a.registration_no AS aircraft_registration,
    a.model AS aircraft_model,
    a.manufacturer AS aircraft_manufacturer,
    a.total_flight_hours AS aircraft_hours,
    a.total_cycles AS aircraft_cycles,
    a.status AS aircraft_status,
    ac.installation_id,
    c.component_id,
    c.serial_number AS component_serial,
    c.component_type,
    c.manufacturer AS component_manufacturer,
    c.model AS component_model,
    ac.installed_date,
    ac.installation_hours,
    ac.installation_cycles,
    ROUND(GREATEST(0, a.total_flight_hours - ac.installation_hours), 2) AS current_installation_hours,
    GREATEST(0, a.total_cycles - ac.installation_cycles) AS current_installation_cycles,
    c.service_limit_hours,
    c.service_limit_cycles,
    CASE
        WHEN c.service_limit_hours IS NOT NULL 
        THEN ROUND(c.service_limit_hours - (a.total_flight_hours - ac.installation_hours), 2)
        ELSE NULL
    END AS hours_remaining_to_limit,
    c.current_status AS component_status
FROM AIRCRAFT_COMPONENT ac
JOIN AIRCRAFT a
    ON ac.aircraft_id = a.aircraft_id
JOIN COMPONENT c
    ON ac.component_id = c.component_id
WHERE ac.removed_date IS NULL;


-- ====================================================================
-- VIEW 2: COMPONENT LIFECYCLE HISTORY (PERSON 1)
-- Complete historical trail of all installations and removals
-- ====================================================================

CREATE VIEW v_component_lifecycle_history AS
SELECT
    ac.installation_id,
    c.component_id,
    c.serial_number,
    c.component_type,
    c.manufacturer AS component_manufacturer,
    c.model AS component_model,
    a.aircraft_id,
    a.registration_no,
    a.model AS aircraft_model,
    ac.installed_date,
    ac.removed_date,
    ac.installation_hours,
    ac.removal_hours,
    ac.installation_cycles,
    ac.removal_cycles,
    CASE
        WHEN ac.removed_date IS NOT NULL THEN ROUND(ac.removal_hours - ac.installation_hours, 2)
        ELSE ROUND(GREATEST(0, a.total_flight_hours - ac.installation_hours), 2)
    END AS operational_hours_logged,
    CASE
        WHEN ac.removed_date IS NOT NULL THEN (ac.removal_cycles - ac.installation_cycles)
        ELSE GREATEST(0, a.total_cycles - ac.installation_cycles)
    END AS operational_cycles_logged,
    ac.installation_reason,
    ac.removal_reason,
    CASE
        WHEN ac.removed_date IS NULL THEN 'CURRENTLY_INSTALLED'
        ELSE 'REMOVED_HISTORICAL'
    END AS lifecycle_state
FROM AIRCRAFT_COMPONENT ac
JOIN COMPONENT c
    ON ac.component_id = c.component_id
JOIN AIRCRAFT a
    ON ac.aircraft_id = a.aircraft_id;


-- ====================================================================
-- VIEW 3: OVERDUE MAINTENANCE (PERSON 2)
-- Dynamically calculated overdue items using CURRENT_DATE
-- ====================================================================

CREATE VIEW v_overdue_maintenance AS
SELECT
    m.maintenance_id,
    m.maintenance_type,
    m.scheduled_date,
    DATEDIFF(CURRENT_DATE, m.scheduled_date) AS days_overdue,
    m.status AS maintenance_status,
    m.description,
    a.aircraft_id,
    a.registration_no,
    a.model AS aircraft_model,
    c.component_id,
    COALESCE(c.serial_number, 'AIRFRAME-LEVEL') AS component_serial,
    COALESCE(c.component_type, 'General Airframe') AS component_type,
    t.technician_id,
    COALESCE(t.name, 'UNASSIGNED') AS technician_name,
    t.employee_no AS technician_employee_no
FROM MAINTENANCE m
JOIN AIRCRAFT a
    ON m.aircraft_id = a.aircraft_id
LEFT JOIN COMPONENT c
    ON m.component_id = c.component_id
LEFT JOIN TECHNICIAN t
    ON m.technician_id = t.technician_id
WHERE m.scheduled_date < CURRENT_DATE
  AND m.status <> 'COMPLETED';


-- ====================================================================
-- VIEW 4: RECURRING FAILURES (PERSON 2)
-- Detects components with >= 3 recurring failures of the same type
-- ====================================================================

CREATE VIEW v_recurring_failures AS
SELECT
    c.component_id,
    c.serial_number,
    c.component_type,
    c.manufacturer AS component_manufacturer,
    f.failure_type,
    COUNT(f.failure_id) AS incident_count,
    MIN(f.failure_date) AS first_failure_date,
    MAX(f.failure_date) AS latest_failure_date,
    SUM(CASE WHEN f.resolved = FALSE THEN 1 ELSE 0 END) AS unresolved_incidents,
    MAX(f.severity) AS highest_severity,
    GROUP_CONCAT(DISTINCT a.registration_no ORDER BY a.registration_no SEPARATOR ', ') AS affected_aircraft
FROM FAILURE f
JOIN COMPONENT c
    ON f.component_id = c.component_id
JOIN AIRCRAFT a
    ON f.aircraft_id = a.aircraft_id
GROUP BY
    c.component_id,
    c.serial_number,
    c.component_type,
    c.manufacturer,
    f.failure_type
HAVING COUNT(f.failure_id) >= 3;


-- ====================================================================
-- VIEW 5: LOW STOCK SPARE PARTS (PERSON 3)
-- Aggregates stock across ALL locations before comparing with minimum_stock
-- ====================================================================

CREATE VIEW v_low_stock_parts AS
SELECT
    sp.part_id,
    sp.part_number,
    sp.part_name,
    sp.component_type,
    sp.supplier,
    sp.unit_cost,
    sp.minimum_stock,
    COALESCE(SUM(i.quantity_available), 0) AS total_quantity_available,
    (sp.minimum_stock - COALESCE(SUM(i.quantity_available), 0)) AS stock_deficit,
    GROUP_CONCAT(CONCAT(i.location, ' (Qty: ', i.quantity_available, ')') ORDER BY i.location SEPARATOR ' | ') AS location_breakdown
FROM SPARE_PART sp
LEFT JOIN INVENTORY i
    ON sp.part_id = i.part_id
GROUP BY
    sp.part_id,
    sp.part_number,
    sp.part_name,
    sp.component_type,
    sp.supplier,
    sp.unit_cost,
    sp.minimum_stock
HAVING COALESCE(SUM(i.quantity_available), 0) < sp.minimum_stock;


-- ====================================================================
-- VIEW 6: RISK INTELLIGENCE SUMMARY (PERSON 3 + INTEGRATED SYSTEM)
-- Explainable Risk Scoring Engine:
--   +30 pts: Component at or beyond service limit (breach)
--   +20 pts: Component near service limit (>= 85% limit, mutually exclusive with breach)
--   +25 pts: Relevant incomplete overdue maintenance on this component
--   +15 pts: >= 3 recurring failures on this component
--   +10 pts: Required replacement stock below minimum for this component_type
-- Total score clamped to [0, 100].
-- Categories: LOW (0-29), MEDIUM (30-59), HIGH (60-100).
-- ====================================================================

CREATE VIEW v_risk_intelligence_summary AS
WITH ComponentMetrics AS (
    SELECT
        c.component_id,
        c.serial_number,
        c.component_type,
        c.manufacturer,
        c.model,
        c.current_status,
        c.current_location,
        c.service_limit_hours,
        c.service_limit_cycles,
        
        -- Currently installed aircraft (if any)
        curr_ac.aircraft_id,
        curr_a.registration_no AS installed_aircraft_reg,
        
        -- Calculated lifetime hours/cycles (incorporating historical removal intervals + current installation)
        ROUND(COALESCE((
            SELECT SUM(CASE 
                WHEN ac_sub.removed_date IS NOT NULL THEN (ac_sub.removal_hours - ac_sub.installation_hours)
                ELSE GREATEST(0, a_sub.total_flight_hours - ac_sub.installation_hours)
            END)
            FROM AIRCRAFT_COMPONENT ac_sub
            JOIN AIRCRAFT a_sub ON ac_sub.aircraft_id = a_sub.aircraft_id
            WHERE ac_sub.component_id = c.component_id
        ), 0), 2) AS lifetime_hours_used,
        
        COALESCE((
            SELECT SUM(CASE 
                WHEN ac_sub.removed_date IS NOT NULL THEN (ac_sub.removal_cycles - ac_sub.installation_cycles)
                ELSE GREATEST(0, a_sub.total_cycles - ac_sub.installation_cycles)
            END)
            FROM AIRCRAFT_COMPONENT ac_sub
            JOIN AIRCRAFT a_sub ON ac_sub.aircraft_id = a_sub.aircraft_id
            WHERE ac_sub.component_id = c.component_id
        ), 0) AS lifetime_cycles_used,
        
        -- Count incomplete overdue maintenance for this component
        (
            SELECT COUNT(*)
            FROM MAINTENANCE m
            WHERE m.component_id = c.component_id
              AND m.scheduled_date < CURRENT_DATE
              AND m.status <> 'COMPLETED'
        ) AS overdue_maint_count,
        
        -- Count failures for this component
        (
            SELECT COUNT(*)
            FROM FAILURE f
            WHERE f.component_id = c.component_id
        ) AS total_failures_count,
        
        -- Check if replacement spare part for this component type is low stock
        (
            SELECT COUNT(*)
            FROM v_low_stock_parts lsp
            WHERE LOWER(lsp.component_type) = LOWER(c.component_type)
        ) AS low_spare_count
        
    FROM COMPONENT c
    LEFT JOIN AIRCRAFT_COMPONENT curr_ac
        ON c.component_id = curr_ac.component_id AND curr_ac.removed_date IS NULL
    LEFT JOIN AIRCRAFT curr_a
        ON curr_ac.aircraft_id = curr_a.aircraft_id
)
SELECT
    m.component_id,
    m.serial_number,
    m.component_type,
    m.current_status,
    m.current_location,
    COALESCE(m.installed_aircraft_reg, 'NOT INSTALLED') AS installed_aircraft,
    m.service_limit_hours,
    m.lifetime_hours_used,
    m.service_limit_cycles,
    m.lifetime_cycles_used,
    
    -- Service limit point calculation (mutually exclusive)
    CASE
        WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= m.service_limit_hours)
          OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= m.service_limit_cycles)
        THEN 30
        WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= 0.85 * m.service_limit_hours)
          OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= 0.85 * m.service_limit_cycles)
        THEN 20
        ELSE 0
    END AS service_limit_pts,
    
    -- Overdue maintenance points
    CASE WHEN m.overdue_maint_count > 0 THEN 25 ELSE 0 END AS overdue_maint_pts,
    
    -- Recurring failures points (threshold >= 3 incidents)
    CASE WHEN m.total_failures_count >= 3 THEN 15 ELSE 0 END AS failure_pts,
    
    -- Low replacement stock points
    CASE WHEN m.low_spare_count > 0 THEN 10 ELSE 0 END AS low_stock_pts,
    
    -- Total composite risk score (0 to 100)
    LEAST(100, (
        CASE
            WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= m.service_limit_hours)
              OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= m.service_limit_cycles)
            THEN 30
            WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= 0.85 * m.service_limit_hours)
              OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= 0.85 * m.service_limit_cycles)
            THEN 20
            ELSE 0
        END
        + CASE WHEN m.overdue_maint_count > 0 THEN 25 ELSE 0 END
        + CASE WHEN m.total_failures_count >= 3 THEN 15 ELSE 0 END
        + CASE WHEN m.low_spare_count > 0 THEN 10 ELSE 0 END
    )) AS risk_score,
    
    -- Risk category classification
    CASE
        WHEN LEAST(100, (
            CASE
                WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= m.service_limit_hours)
                  OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= m.service_limit_cycles)
                THEN 30
                WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= 0.85 * m.service_limit_hours)
                  OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= 0.85 * m.service_limit_cycles)
                THEN 20
                ELSE 0
            END
            + CASE WHEN m.overdue_maint_count > 0 THEN 25 ELSE 0 END
            + CASE WHEN m.total_failures_count >= 3 THEN 15 ELSE 0 END
            + CASE WHEN m.low_spare_count > 0 THEN 10 ELSE 0 END
        )) >= 60 THEN 'HIGH'
        WHEN LEAST(100, (
            CASE
                WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= m.service_limit_hours)
                  OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= m.service_limit_cycles)
                THEN 30
                WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= 0.85 * m.service_limit_hours)
                  OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= 0.85 * m.service_limit_cycles)
                THEN 20
                ELSE 0
            END
            + CASE WHEN m.overdue_maint_count > 0 THEN 25 ELSE 0 END
            + CASE WHEN m.total_failures_count >= 3 THEN 15 ELSE 0 END
            + CASE WHEN m.low_spare_count > 0 THEN 10 ELSE 0 END
        )) >= 30 THEN 'MEDIUM'
        ELSE 'LOW'
    END AS risk_level,
    
    -- Human-readable explainability factor string
    CONCAT_WS(' | ',
        CASE
            WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= m.service_limit_hours)
              OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= m.service_limit_cycles)
            THEN '+30: Service Limit Breached'
            WHEN (m.service_limit_hours IS NOT NULL AND m.lifetime_hours_used >= 0.85 * m.service_limit_hours)
              OR (m.service_limit_cycles IS NOT NULL AND m.lifetime_cycles_used >= 0.85 * m.service_limit_cycles)
            THEN '+20: Nearing Service Limit (>=85%)'
            ELSE NULL
        END,
        CASE WHEN m.overdue_maint_count > 0 THEN CONCAT('+25: Overdue Maintenance Pending (', m.overdue_maint_count, ')') ELSE NULL END,
        CASE WHEN m.total_failures_count >= 3 THEN CONCAT('+15: Repeated Failures (', m.total_failures_count, ' incidents)') ELSE NULL END,
        CASE WHEN m.low_spare_count > 0 THEN '+10: Replacement Parts Below Min Stock' ELSE NULL END
    ) AS risk_explanation

FROM ComponentMetrics m;
