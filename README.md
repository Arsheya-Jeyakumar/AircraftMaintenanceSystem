# Aircraft Maintenance & Component Lifecycle Intelligence System
**Enterprise Database Management System (DBMS) Project**

---

##  Project Overview
The **Aircraft Maintenance & Component Lifecycle Intelligence System** is an enterprise-grade relational database and desktop management application developed using **MySQL** and **Java / JavaFX**. The system models end-to-end commercial aviation fleet operations, managing airframes, serialized rotable components, multi-stage maintenance schedules, safety inspections, recurring failure tracking, multi-warehouse spare parts inventory, and explainable multi-factor risk scoring.

---

##  Module Responsibilities & Team Contributors

| Module & Area | Lead Contributor | Core Responsibilities & Entities |
| :--- | :--- | :--- |
| **Person 1: Aircraft & Component Lifecycle** | **Thunga Purushotham** | • Airframe registry (`AIRCRAFT`)<br>• Rotable components catalog (`COMPONENT`)<br>• Installation & removal history (`AIRCRAFT_COMPONENT`)<br>• Unique registration/serial enforcement<br>• Transactional component lifecycle (`sp_install_component`, `sp_remove_component`)<br>• Prevention of duplicate installations |
| **Person 2: Maintenance, Inspections & Failures** | **Arsheya Tarmia Jeyakumar**<br>*(Reg No: 25BCE2629)* | • Certified technician management (`TECHNICIAN`)<br>• Scheduled & overdue maintenance work orders (`MAINTENANCE`)<br>• Quality safety inspections (`INSPECTION`)<br>• Aircraft/component incident & defect logging (`FAILURE`)<br>• Dynamic overdue maintenance calculations (`v_overdue_maintenance`, `sp_get_overdue_maintenance`)<br>• Recurring failure detection threshold (≥ 3 incidents in `v_recurring_failures`) |
| **Person 3: Spare Parts, Inventory & Risk Intelligence** | **Avandika K**<br>*(Reg No: 25BCE2293)* | • Spare parts master catalog (`SPARE_PART`)<br>• Multi-location warehouse inventory (`INVENTORY`)<br>• Multi-location stock aggregation (`v_low_stock_parts`)<br>• Operational system alerts (`ALERT`)<br>• Automated triggers for low stock & critical failures<br>• Explainable Risk Intelligence Engine (`v_risk_intelligence_summary`, `sp_generate_risk_score`) |

---

##  Architecture & Technology Stack
- **Database Management System:** MySQL 8.x / 9.x (InnoDB Storage Engine)
- **Programming Language:** Java 17+ (Compiled & validated on Java 25 LTS)
- **GUI Framework:** JavaFX 21 (Controls, FXML, CSS)
- **Build System:** Apache Maven 3.9+
- **Database Driver:** MySQL Connector/J 8.4.0
- **Design System:** Custom Aerospace Dark Theme with responsive KPI metrics and status badges

---

##  Repository Structure
```
AircraftMaintenanceSystem/
├── .gitignore                          # Ignores target/, IDE files, and local secrets
├── pom.xml                             # Maven build configuration
├── README.md                           # Comprehensive documentation
├── database/                           # Complete SQL Deliverables Suite
│   ├── 01_create_database.sql          # Database initialization
│   ├── 02_create_tables.sql            # 10 relational tables, constraints & indexes
│   ├── 03_insert_sample_data.sql       # Relational sample records for all scenarios
│   ├── 04_views.sql                    # 6 analytical and operational views
│   ├── 05_procedures.sql               # Stored procedures (installation, risk scoring, overdue)
│   ├── 06_triggers.sql                 # Automated alert triggers and integrity guards
│   └── 07_test_queries.sql             # Repeatable verification suite (rollback-safe)
└── src/
    ├── main/
    │   ├── java/com/aircraft/
    │   │   ├── Main.java               # JavaFX application entrypoint
    │   │   ├── controller/             # UI controllers (MainController)
    │   │   ├── dao/                    # JDBC Data Access Objects with PreparedStatements
    │   │   ├── model/                  # POJO entities representing the 10 database tables
    │   │   ├── service/                # Business logic, validations & transaction wrappers
    │   │   └── util/                   # DatabaseConfig utility (externalized settings)
    │   └── resources/
    │       ├── css/styles.css          # Aerospace dark theme styling
    │       ├── fxml/main.fxml          # Application UI layout
    │       └── db.properties.example   # Safe template for local credentials
```

---

##  Relational Database Schema (10 Tables)

```
[AIRCRAFT] 1 ──── ∞ [AIRCRAFT_COMPONENT] ∞ ──── 1 [COMPONENT]
    │                         │                       │
    ├───── ∞ [MAINTENANCE] ───┘                       ├───── ∞ [FAILURE]
    │             │                                   │          │
    │             └─ ∞ [TECHNICIAN]                   │          ▼
    │                         ▲                       │      [ALERT]
    ├───── ∞ [INSPECTION] ────┘                       │
    │                                                 │
    └─────────────────────────────────────────────────┘

[SPARE_PART] 1 ──── ∞ [INVENTORY]
```

### Table Summary:
1. `AIRCRAFT`: Registration number (UNIQUE), model, manufacturer, manufacture date, flight hours, cycles, status.
2. `COMPONENT`: Serial number (UNIQUE), type, manufacturer, model, service limit hours, service limit cycles, status, location.
3. `TECHNICIAN`: Employee number (UNIQUE), name, qualification, specialization, phone, status.
4. `SPARE_PART`: Part number (UNIQUE), part name, component type, supplier, unit cost, minimum stock.
5. `AIRCRAFT_COMPONENT`: Installation tracking, hours/cycles at install and remove, reasons, historical state.
6. `MAINTENANCE`: Maintenance type, scheduled date, completed date, status, aircraft FK, optional component FK, technician FK.
7. `INSPECTION`: Inspection date, type, result (PASSED/FAILED/WARNING), findings, aircraft FK, optional component FK, inspector FK.
8. `FAILURE`: Incident date, failure type, severity (LOW/MEDIUM/HIGH/CRITICAL), description, resolution flag.
9. `INVENTORY`: Multi-location stock (warehouses/hangars), part FK, quantity available, timestamp.
10. `ALERT`: Alert type, severity, message, timestamp, status (OPEN/ACKNOWLEDGED/RESOLVED).

---

##  Database Views
- `v_aircraft_current_components`: Displays actively installed components with elapsed flight hours and remaining hours to service limit.
- `v_component_lifecycle_history`: Chronological lifecycle trail of all active and historical installations, operational hours, and removal reasons.
- `v_overdue_maintenance`: Identifies maintenance tasks where `scheduled_date < CURRENT_DATE` and `status <> 'COMPLETED'`.
- `v_recurring_failures`: Aggregates component failures by type and filters for components with **≥ 3 incidents**.
- `v_low_stock_parts`: Aggregates stock across **all warehouses/locations** and reports parts where total available < minimum stock.
- `v_risk_intelligence_summary`: Evaluates each component against the explainable risk matrix and produces composite risk scores (0–100) and classifications.

---

## Explainable Risk Scoring Engine

The risk score is calculated dynamically in SQL (`v_risk_intelligence_summary` and `sp_generate_risk_score`) based on the following additive rules (capped at 100):

| Risk Factor | Points | Rule Condition |
| :--- | :---: | :--- |
| **Service Limit Breached** | **+30** | Total hours ≥ `service_limit_hours` OR cycles ≥ `service_limit_cycles` |
| **Nearing Service Limit** | **+20** | Total hours ≥ 85% of limit OR cycles ≥ 85% of limit *(mutually exclusive with +30)* |
| **Overdue Maintenance** | **+25** | Incomplete maintenance tasks past scheduled date for this component |
| **Recurring Failures** | **+15** | Component has experienced ≥ 3 recorded failure incidents |
| **Spare Stock Shortage** | **+10** | Replacement spare parts for this component type are below minimum stock |

### Risk Classifications:
- **LOW:** 0 – 29 points (Normal operational readiness)
- **MEDIUM:** 30 – 59 points (Elevated risk, inspection advised)
- **HIGH:** 60 – 100 points (Critical risk, immediate grounding/overhaul required)

---

##  Setup & Execution Guide

### 1. Prerequisites
- **MySQL Server 8.0+** running locally on port `3306`.
- **Java JDK 17 or later** (Java 17, 21, or 25 LTS).
- **Apache Maven 3.8+** (or use local Maven).

### 2. MySQL Workbench Execution Order
Execute the SQL files in MySQL Workbench or the `mysql` CLI in strict sequential order:
```bash
# In MySQL Workbench, open and execute each script in order:
database/01_create_database.sql
database/02_create_tables.sql
database/03_insert_sample_data.sql
database/04_views.sql
database/05_procedures.sql
database/06_triggers.sql
database/07_test_queries.sql
```
Or via terminal:
```bash
mysql -u root -p < database/01_create_database.sql
mysql -u root -p aircraft_maintenance_db < database/02_create_tables.sql
mysql -u root -p aircraft_maintenance_db < database/03_insert_sample_data.sql
mysql -u root -p aircraft_maintenance_db < database/04_views.sql
mysql -u root -p aircraft_maintenance_db < database/05_procedures.sql
mysql -u root -p aircraft_maintenance_db < database/06_triggers.sql
mysql -u root -p aircraft_maintenance_db < database/07_test_queries.sql
```

### 3. Database Credentials Configuration
Copy `src/main/resources/db.properties.example` to `db.properties` in the project root:
```properties
db.host=localhost
db.port=3306
db.name=aircraft_maintenance_db
db.username=root
db.password=YourActualPasswordHere
```
*Note: `db.properties` is protected by `.gitignore` and will never be committed.*

### 4. Build and Run the Application
```bash
# Build the application
mvn clean package

# Launch the JavaFX Application
mvn javafx:run
```

---

##  Verification & Automated Checks

### Checks Executed:
-  **Maven Clean Compilation:** Validated all 28 Java source files with `mvn clean compile` (`BUILD SUCCESS`).
-  **Packaging:** Generated executable JAR (`AircraftMaintenanceSystem-1.0.0.jar`) with `mvn package`.
-  **Syntax & Constraint Integrity:** Verified all foreign keys, unique keys, check constraints, cascade behaviors, and indexes.
-  **SQL Scripts Suite:** Formatted and sequenced scripts 01 through 07 for deterministic, repeatable deployment.
-  **Git Branching:** Integration branch `feature/full-system-implementation` prepared cleanly without overwriting teammate commits.

---

##  Security & Data Integrity
- All SQL operations in the Java application utilize `PreparedStatement` to prevent SQL injection.
- Multi-step operations (e.g. component installation and removal) execute inside transactions with strict validation and rollback.
- Database credentials are externalized and excluded from version control.
