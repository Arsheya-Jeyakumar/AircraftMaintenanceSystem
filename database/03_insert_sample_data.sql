/*
========================================================
Aircraft Maintenance & Component Lifecycle System

Person 2 - Maintenance & Inspection Module
Author: ARSHEYA TARMIA JEYAKUMAR
Registration No: 25BCE2629

Sample Data:
    TECHNICIAN
========================================================
*/

USE aircraft_maintenance_db;

INSERT INTO TECHNICIAN
(employee_no, name, qualification, specialization, phone, status)
VALUES
('TECH001', 'Arjun Kumar', 'AME License',
 'Engine Maintenance', '9876543210', 'ACTIVE'),

('TECH002', 'Priya Nair', 'AME License',
 'Avionics', '9876543211', 'ACTIVE'),

('TECH003', 'Rahul Menon', 'Diploma in Aircraft Maintenance',
 'Landing Gear', '9876543212', 'ACTIVE');
