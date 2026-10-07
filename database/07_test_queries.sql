START TRANSACTION;

INSERT INTO TECHNICIAN
(employee_no, name, qualification, specialization, phone, status)
VALUES
('TEST_ROLLBACK', 'Temporary Test', 'Test Qualification',
 'Test Specialization', '0000000000', 'ACTIVE');

SELECT *
FROM TECHNICIAN
WHERE employee_no = 'TEST_ROLLBACK';

ROLLBACK;

SELECT *
FROM TECHNICIAN
WHERE employee_no = 'TEST_ROLLBACK';

-- =====================================================
-- PERSON 3 - SPARE PARTS & INVENTORY TEST QUERIES
-- Author - AVANDIKA K
-- Registration no - 25BCE2293
-- =====================================================

USE aircraft_maintenance_db;


-- =====================================================
-- TEST 1: DISPLAY ALL INVENTORY
-- =====================================================

SELECT
    sp.part_number,
    sp.part_name,
    i.location,
    i.quantity_available,
    sp.minimum_stock
FROM SPARE_PART sp
JOIN INVENTORY i
    ON sp.part_id = i.part_id;


-- =====================================================
-- TEST 2: CHECK STOCK STATUS
-- =====================================================

SELECT
    sp.part_number,
    sp.part_name,
    i.quantity_available,
    sp.minimum_stock,
    CASE
        WHEN i.quantity_available < sp.minimum_stock
            THEN 'LOW'
        ELSE 'OK'
    END AS stock_status
FROM SPARE_PART sp
JOIN INVENTORY i
    ON sp.part_id = i.part_id;


-- =====================================================
-- TEST 3: LOW STOCK VIEW
-- =====================================================

SELECT *
FROM low_stock_inventory;


-- =====================================================
-- TEST 4: GENERATE LOW STOCK ALERTS
-- =====================================================

CALL generate_low_stock_alerts();


-- =====================================================
-- TEST 5: DISPLAY GENERATED ALERTS
-- =====================================================

SELECT
    alert_id,
    alert_type,
    severity,
    message,
    created_at,
    status
FROM ALERT
ORDER BY created_at DESC;
