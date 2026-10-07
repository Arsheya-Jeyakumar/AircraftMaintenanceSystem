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

