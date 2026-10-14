# Aircraft Maintenance & Component Lifecycle Intelligence System

A relational database management system (DBMS) project developed in MySQL and Java/JavaFX for tracking aircraft fleet operations, component lifecycles, maintenance schedules, safety inspections, inventory across warehouses, and operational risk factors.

---

## 1. Problem Statement & Project Objectives

Airlines and maintenance facilities must track thousands of flight hours, component service lifespans, and scheduled maintenance intervals under strict safety regulations. Managing this information through isolated spreadsheets or disconnected logs leads to tracking errors, overdue servicing, parts shortages, and increased flight risk.

The objectives of this project are:
- Build a relational database in MySQL that models aircraft, serialized rotable components, maintenance logs, technicians, inspections, spare parts inventory, and operational alerts.
- Enforce data integrity constraints, referential relationships, and automated rules using SQL constraints, triggers, and stored procedures.
- Provide a JavaFX desktop interface with Model-DAO-Service architecture for database operations, record management, and visual status tracking.
- Implement an explainable risk scoring mechanism that calculates component risk levels based on service limits, maintenance backlogs, failure history, and spare part availability.

---

## 2. System Features & Module Breakdown

The system is organized into three collaborative modules:

### Module 1: Aircraft & Component Lifecycle
- **Aircraft Fleet Management:** Tracks aircraft registration numbers, models, manufacturers, total flight hours, flight cycles, and operational status (`ACTIVE`, `MAINTENANCE`, `GROUNDED`).
- **Serialized Component Catalog:** Manages rotable components (engines, landing gear, avionics, radar, APUs) with defined service limits in hours and cycles.
- **Component Installation & Removal:** Records mounting and unmounting history with initial hours/cycles, removal reasons, and elapsed usage on each airframe.
- **Data Integrity Rules:** Enforces unique aircraft registration numbers and component serial numbers. Trigger `trg_prevent_duplicate_active_installation` prevents installing a component on multiple aircraft simultaneously.

### Module 2: Maintenance, Inspections & Failures
- **Technician Records:** Maintains technician qualifications, specializations (avionics, propulsion, hydraulics), and contact details.
- **Maintenance Scheduling:** Tracks scheduled and completed maintenance work orders. Supports both component-specific tasks and airframe-level tasks (with a `NULL` component identifier).
- **Dynamic Overdue Tracking:** Identifies overdue maintenance dynamically by comparing scheduled dates against the current date (`v_overdue_maintenance`, `sp_get_overdue_maintenance`).
- **Inspections & Findings:** Records inspection types, inspector assignments, pass/warning/fail outcomes, and detailed findings.
- **Incident & Failure Logging:** Logs component defects with severity classifications (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) and resolution tracking.
- **Recurring Failure Detection:** Aggregates failure logs by component and failure type, highlighting components that exceed a threshold of 3 or more incidents (`v_recurring_failures`).

### Module 3: Spare Parts, Inventory, Alerts & Risk Intelligence
- **Spare Parts Catalog:** Tracks replacement parts, designated component types, suppliers, unit costs, and minimum stock levels.
- **Multi-Location Inventory:** Tracks stock quantities across multiple warehouse and hangar facilities (e.g., Chennai Warehouse, Mumbai Store, Delhi Bay).
- **Multi-Location Stock Aggregation:** Aggregates available stock across all locations before comparing against minimum stock thresholds (`v_low_stock_parts`).
- **Automated Alerts:** Generates system alerts automatically when high-severity failures occur or when total stock falls below threshold levels.
- **Explainable Risk Scoring:** Computes a composite risk score (0–100) and category (`LOW`, `MEDIUM`, `HIGH`) for each component using rule-based scoring.

---

## 3. Technology Stack & Application Architecture

- **Database:** MySQL 8.0+ / 9.0+ using the InnoDB storage engine
- **Language & Runtime:** Java 17+ (built with release target 17, tested on Java 25)
- **User Interface:** JavaFX 21 (Controls, FXML layout, and custom CSS)
- **Build & Dependency Management:** Apache Maven 3.9+ (includes Maven Wrapper `./mvnw`)
- **Database Connectivity:** JDBC with `PreparedStatement` and MySQL Connector/J 8.4.0

### Architecture Layers:
- **`com.aircraft.model`:** Plain Java objects representing database tables and joined reporting records.
- **`com.aircraft.dao`:** Data Access Objects executing parameterized SQL queries and stored procedure calls.
- **`com.aircraft.service`:** Business logic layer handling validation, installation rules, and risk calculations.
- **`com.aircraft.controller`:** JavaFX controllers managing UI interactions, input validation, and asynchronous database updates.
- **`com.aircraft.util`:** Database connection utility (`DatabaseConfig.java`) handling properties loading and connection testing.

---

## 4. Project Directory Structure

```
AircraftMaintenanceSystem/
├── pom.xml                               # Maven build configuration
├── mvnw                                  # Maven wrapper script for Linux/macOS
├── mvnw.cmd                              # Maven wrapper script for Windows
├── .mvn/wrapper/                         # Maven wrapper distribution settings
├── .gitignore                            # Ignores target/, IDE files, and local secrets
├── README.md                             # Project documentation
├── database/                             # Database SQL scripts
│   ├── 01_create_database.sql            # Database schema creation
│   ├── 02_create_tables.sql              # 10 tables, constraints, and indexes
│   ├── 03_insert_sample_data.sql         # Test dataset across all modules
│   ├── 04_views.sql                      # 6 analytical and operational views
│   ├── 05_procedures.sql                 # Stored procedures
│   ├── 06_triggers.sql                   # Integrity and alert triggers
│   └── 07_test_queries.sql               # Verification queries with rollback tests
└── src/
    ├── main/
    │   ├── java/com/aircraft/
    │   │   ├── Main.java                 # JavaFX application entrypoint
    │   │   ├── controller/               # UI controllers
    │   │   ├── dao/                      # Database access objects
    │   │   ├── model/                    # Entity models
    │   │   ├── service/                  # Business services
    │   │   └── util/                     # Database connection helper
    │   └── resources/
    │       ├── css/styles.css            # Dark UI stylesheet
    │       ├── fxml/main.fxml            # FXML screen layout
    │       └── db.properties.example     # Template for database configuration
```

---

## 5. Database Schema & Relationships

The database consists of 10 relational tables:

```
[AIRCRAFT] 1 ────< [AIRCRAFT_COMPONENT] >──── 1 [COMPONENT]
    │                         │                       │
    ├───< [MAINTENANCE] ──────┘                       ├───< [FAILURE]
    │             │                                   │          │
    │             └───< [TECHNICIAN]                  │          │ (trigger)
    │                         ▲                       │          ▼
    ├───< [INSPECTION] ───────┘                       │       [ALERT]
    │                                                 │
    └─────────────────────────────────────────────────┘

[SPARE_PART] 1 ────< [INVENTORY]
```

### Table Definitions:
1. `AIRCRAFT`: `aircraft_id` (PK), `registration_no` (UQ), `model`, `manufacturer`, `manufacture_date`, `total_flight_hours`, `total_cycles`, `status`.
2. `COMPONENT`: `component_id` (PK), `serial_number` (UQ), `component_type`, `manufacturer`, `model`, `service_limit_hours`, `service_limit_cycles`, `current_status`, `current_location`.
3. `TECHNICIAN`: `technician_id` (PK), `employee_no` (UQ), `name`, `qualification`, `specialization`, `phone`, `status`.
4. `SPARE_PART`: `part_id` (PK), `part_number` (UQ), `part_name`, `component_type`, `supplier`, `unit_cost`, `minimum_stock`.
5. `AIRCRAFT_COMPONENT`: `installation_id` (PK), `aircraft_id` (FK), `component_id` (FK), `installed_date`, `removed_date`, `installation_hours`, `removal_hours`, `installation_cycles`, `removal_cycles`, `installation_reason`, `removal_reason`.
6. `MAINTENANCE`: `maintenance_id` (PK), `aircraft_id` (FK), `component_id` (FK, nullable), `maintenance_type`, `scheduled_date`, `completed_date`, `description`, `status`, `technician_id` (FK, nullable).
7. `INSPECTION`: `inspection_id` (PK), `aircraft_id` (FK), `component_id` (FK, nullable), `inspection_date`, `inspection_type`, `result`, `findings`, `inspector_id` (FK to TECHNICIAN).
8. `FAILURE`: `failure_id` (PK), `aircraft_id` (FK), `component_id` (FK), `failure_date`, `failure_type`, `severity`, `description`, `resolved`.
9. `INVENTORY`: `inventory_id` (PK), `part_id` (FK), `location`, `quantity_available`, `last_updated`.
10. `ALERT`: `alert_id` (PK), `aircraft_id` (FK, nullable), `component_id` (FK, nullable), `alert_type`, `severity`, `message`, `created_at`, `status`.

---

## 6. Risk Scoring Formula

Component risk is calculated in `v_risk_intelligence_summary` and `sp_generate_risk_score` using point-based criteria (capped between 0 and 100):

| Condition | Points | Description |
| :--- | :---: | :--- |
| **Service Limit Breached** | +30 | Current hours or cycles meet or exceed the component limit. |
| **Nearing Service Limit** | +20 | Usage has reached at least 85% of limit *(mutually exclusive with +30)*. |
| **Pending Overdue Maintenance** | +25 | One or more maintenance tasks for this component are past due. |
| **Recurring Failures** | +15 | Component has 3 or more logged failure incidents. |
| **Low Replacement Stock** | +10 | Aggregate stock for matching spare parts is below minimum requirements. |

### Categories:
- **LOW:** 0 – 29 points
- **MEDIUM:** 30 – 59 points
- **HIGH:** 60 – 100 points

---

## 7. Prerequisites & Installation Requirements

- **Java Development Kit (JDK):** Version 17 or higher
- **MySQL Server:** Version 8.0 or 9.0 running locally on port 3306
- **MySQL Workbench:** Recommended for running and verifying SQL scripts
- **Internet Connection:** Required on initial build to download Maven dependencies and JavaFX libraries

---

## 8. Database Setup Instructions (MySQL Workbench)

Execute the scripts in the `database/` directory in this exact order:

```sql
-- 1. Create database
SOURCE database/01_create_database.sql;

-- 2. Create tables, constraints, and indexes
SOURCE database/02_create_tables.sql;

-- 3. Populate sample dataset
SOURCE database/03_insert_sample_data.sql;

-- 4. Create database views
SOURCE database/04_views.sql;

-- 5. Create stored procedures
SOURCE database/05_procedures.sql;

-- 6. Create database triggers
SOURCE database/06_triggers.sql;

-- 7. Run verification queries
SOURCE database/07_test_queries.sql;
```

---

## 9. Database Configuration & Credentials

Do not commit real database credentials to Git. Local configuration is handled through a properties file:

1. Copy the template file:
   ```bash
   cp src/main/resources/db.properties.example db.properties
   ```
2. Edit `db.properties` with your local database password:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=aircraft_maintenance_db
   db.username=root
   db.password=your_mysql_password
   ```
3. Alternatively, credentials can be set using environment variables:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=aircraft_maintenance_db
   export DB_USER=root
   export DB_PASSWORD=your_mysql_password
   ```

`db.properties` is listed in `.gitignore` to prevent credential exposure.

---

## 10. Building and Running the Application

Use the included Maven wrapper (`./mvnw` on macOS/Linux, `mvnw.cmd` on Windows):

### Compile Source Code:
```bash
./mvnw clean compile
```

### Package Application JAR:
```bash
./mvnw package -DskipTests
```

### Launch JavaFX Desktop Interface:
```bash
./mvnw javafx:run
```

---

## 11. Testing & Verification

The test query suite in `database/07_test_queries.sql` tests database functionality:
- **Table Integrity:** Verifies row counts across all 10 tables.
- **Relational Joins:** Aggregates aircraft and currently installed components.
- **View Output:** Checks outputs of `v_overdue_maintenance`, `v_recurring_failures`, and `v_risk_intelligence_summary`.
- **Stored Procedures:** Runs `sp_get_overdue_maintenance` and `sp_generate_risk_score`.
- **Trigger Verification:** Simulates failure logging and stock changes inside transactions with `ROLLBACK` to preserve the original sample data.

### Automated Checks Performed:
- Maven compilation verified (`BUILD SUCCESS`).
- Package creation verified (`AircraftMaintenanceSystem-1.0.0.jar`).
- Maven wrapper tested and verified.

---

## 12. Known Limitations & Items Pending Verification

- **Live Database Connection:** Build and compilation checks were completed successfully. However, live GUI database testing requires executing the SQL scripts against your local MySQL instance using your MySQL root password.
- **Authentication:** The desktop application currently uses direct JDBC connections configured through `db.properties` without user role-based login screens.
- **Alert Resolution UI:** Alert status can be toggled through the alerts interface, but alert dismissal triggers are not yet mapped to external notification systems.

---

## 13. Team Contribution Summary

| Contributor | Registration Number | Primary Module | Implemented Entities & Deliverables |
| :--- | :--- | :--- | :--- |
| **Thunga Purushotham** | 25BCE2293 / vitstudent | Aircraft & Component Lifecycle | `AIRCRAFT`, `COMPONENT`, `AIRCRAFT_COMPONENT`, `sp_install_component`, `sp_remove_component`, `trg_prevent_duplicate_active_installation`, `v_aircraft_current_components`, `v_component_lifecycle_history`. |
| **Arsheya Tarmia Jeyakumar** | 25BCE2629 | Maintenance, Inspections & Failures | `TECHNICIAN`, `MAINTENANCE`, `INSPECTION`, `FAILURE`, `sp_get_overdue_maintenance`, `trg_failure_critical_alert`, `trg_maintenance_completion`, `v_overdue_maintenance`, `v_recurring_failures`. |
| **Avandika K** | 25BCE2293 | Spare Parts, Inventory & Risk | `SPARE_PART`, `INVENTORY`, `ALERT`, `sp_generate_risk_score`, `trg_inventory_low_stock`, `v_low_stock_parts`, `v_risk_intelligence_summary`. |
