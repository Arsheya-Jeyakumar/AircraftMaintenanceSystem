package com.aircraft.controller;

import com.aircraft.model.Aircraft;
import com.aircraft.model.AircraftComponent;
import com.aircraft.model.Component;
import com.aircraft.model.Failure;
import com.aircraft.model.Inspection;
import com.aircraft.model.Inventory;
import com.aircraft.model.Maintenance;
import com.aircraft.model.RiskSummary;
import com.aircraft.model.SparePart;
import com.aircraft.model.Technician;
import com.aircraft.service.FleetService;
import com.aircraft.service.InventoryRiskService;
import com.aircraft.service.MaintenanceService;
import com.aircraft.util.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label dbStatusBadge;
    @FXML private Button btnReconnect;
    @FXML private Button btnRefreshAll;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavFleet;
    @FXML private Button btnNavComponents;
    @FXML private Button btnNavLifecycle;
    @FXML private Button btnNavMaintenance;
    @FXML private Button btnNavInspections;
    @FXML private Button btnNavInventory;
    @FXML private Button btnNavAlerts;

    @FXML private StackPane contentContainer;

    private final FleetService fleetService = new FleetService();
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final InventoryRiskService inventoryRiskService = new InventoryRiskService();

    private Button currentNavButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentNavButton = btnNavDashboard;
        checkDatabaseConnection();
        showDashboardView();
    }

    private void setActiveNav(Button btn) {
        if (currentNavButton != null) {
            currentNavButton.getStyleClass().remove("nav-button-active");
        }
        currentNavButton = btn;
        if (!currentNavButton.getStyleClass().contains("nav-button-active")) {
            currentNavButton.getStyleClass().add("nav-button-active");
        }
    }

    private boolean checkDatabaseConnection() {
        boolean connected = DatabaseConfig.testConnection();
        if (connected) {
            dbStatusBadge.setText("● Connected: aircraft_maintenance_db");
            dbStatusBadge.getStyleClass().removeAll("badge-critical", "badge-warning");
            dbStatusBadge.getStyleClass().add("badge-passed");
        } else {
            dbStatusBadge.setText("● Disconnected: MySQL Connection Offline");
            dbStatusBadge.getStyleClass().removeAll("badge-passed", "badge-warning");
            dbStatusBadge.getStyleClass().add("badge-critical");
        }
        return connected;
    }

    @FXML
    public void handleReconnect() {
        boolean ok = checkDatabaseConnection();
        if (ok) {
            showInfo("Database Connected", "Successfully verified connection to aircraft_maintenance_db.");
            handleRefreshAll();
        } else {
            showError("Connection Failed", "Unable to connect to MySQL database.\nPlease verify MySQL is running and credentials in 'db.properties'.");
        }
    }

    @FXML
    public void handleRefreshAll() {
        if (currentNavButton == btnNavDashboard) showDashboardView();
        else if (currentNavButton == btnNavFleet) showFleetView();
        else if (currentNavButton == btnNavComponents) showComponentsView();
        else if (currentNavButton == btnNavLifecycle) showLifecycleView();
        else if (currentNavButton == btnNavMaintenance) showMaintenanceView();
        else if (currentNavButton == btnNavInspections) showInspectionsView();
        else if (currentNavButton == btnNavInventory) showInventoryView();
        else if (currentNavButton == btnNavAlerts) showAlertsView();
    }

    // ====================================================================
    // VIEW 1: FLEET & OPERATIONS DASHBOARD
    // ====================================================================
    @FXML
    public void showDashboardView() {
        setActiveNav(btnNavDashboard);
        VBox root = new VBox(20);
        root.setPadding(new Insets(10));

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Fleet Operations & Lifecycle Intelligence Dashboard");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Live telemetry aggregated across aircraft status, component service limits, overdue maintenance and risk indices.");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        // Fetch Live Database Stats
        int aircraftCount = 0;
        int compCount = 0;
        int activeInstalls = 0;
        int overdueCount = 0;
        int lowStockCount = 0;
        int openAlertsCount = 0;
        int highRiskCount = 0;

        try {
            aircraftCount = fleetService.getTotalAircraftCount();
            compCount = fleetService.getTotalComponentCount();
            activeInstalls = fleetService.getActiveInstallations().size();
            overdueCount = maintenanceService.getOverdueCount();
            lowStockCount = inventoryRiskService.getLowStockCount();
            openAlertsCount = inventoryRiskService.getOpenAlertsCount();
            highRiskCount = inventoryRiskService.getHighRiskCount();
        } catch (SQLException e) {
            showError("Database Query Error", "Could not load dashboard statistics: " + e.getMessage());
        }

        // KPI Grid
        HBox kpiRow1 = new HBox(16);
        kpiRow1.getChildren().addAll(
            createKpiCard("TOTAL AIRCRAFT", String.valueOf(aircraftCount), "Active & Maintenance Fleet", "card-blue"),
            createKpiCard("TOTAL COMPONENTS", String.valueOf(compCount), activeInstalls + " Currently Installed", "card-blue"),
            createKpiCard("OVERDUE MAINTENANCE", String.valueOf(overdueCount), "Requires Urgent Scheduling", overdueCount > 0 ? "card-red" : "card-green")
        );
        for (javafx.scene.Node n : kpiRow1.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        HBox kpiRow2 = new HBox(16);
        kpiRow2.getChildren().addAll(
            createKpiCard("LOW STOCK PARTS", String.valueOf(lowStockCount), "Below Aggregate Threshold", lowStockCount > 0 ? "card-amber" : "card-green"),
            createKpiCard("OPEN ALERTS", String.valueOf(openAlertsCount), "Pending Acknowledgment", openAlertsCount > 0 ? "card-red" : "card-green"),
            createKpiCard("HIGH RISK COMPONENTS", String.valueOf(highRiskCount), "Risk Score >= 60 / 100", highRiskCount > 0 ? "card-red" : "card-green")
        );
        for (javafx.scene.Node n : kpiRow2.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // Dashboard Live Quick Tables
        HBox tablesRow = new HBox(16);
        tablesRow.setPrefHeight(350);

        // Urgent Overdue Maintenance Table
        VBox overduePanel = new VBox(8);
        overduePanel.getStyleClass().add("panel-card");
        HBox.setHgrow(overduePanel, Priority.ALWAYS);
        Label overdueTitle = new Label("⚠️ Overdue Maintenance Backlog (sp_get_overdue_maintenance)");
        overdueTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #f87171;");

        TableView<Maintenance> tblOverdue = new TableView<>();
        TableColumn<Maintenance, String> colOMType = new TableColumn<>("Type");
        colOMType.setCellValueFactory(new PropertyValueFactory<>("maintenanceType"));
        TableColumn<Maintenance, String> colOMReg = new TableColumn<>("Aircraft");
        colOMReg.setCellValueFactory(new PropertyValueFactory<>("aircraftRegistration"));
        TableColumn<Maintenance, String> colOMComp = new TableColumn<>("Component");
        colOMComp.setCellValueFactory(new PropertyValueFactory<>("componentSerial"));
        TableColumn<Maintenance, Integer> colOMDays = new TableColumn<>("Days Overdue");
        colOMDays.setCellValueFactory(new PropertyValueFactory<>("daysOverdue"));
        tblOverdue.getColumns().addAll(colOMType, colOMReg, colOMComp, colOMDays);
        tblOverdue.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            tblOverdue.setItems(FXCollections.observableArrayList(maintenanceService.getOverdueMaintenance()));
        } catch (Exception ignored) {}
        overduePanel.getChildren().addAll(overdueTitle, tblOverdue);

        // Top Risk Intelligence Items Table
        VBox riskPanel = new VBox(8);
        riskPanel.getStyleClass().add("panel-card");
        HBox.setHgrow(riskPanel, Priority.ALWAYS);
        Label riskTitle = new Label("🧠 Risk Intelligence Priority Matrix (v_risk_intelligence_summary)");
        riskTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        TableView<RiskSummary> tblRisk = new TableView<>();
        TableColumn<RiskSummary, String> colRSerial = new TableColumn<>("Serial");
        colRSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        TableColumn<RiskSummary, String> colRType = new TableColumn<>("Type");
        colRType.setCellValueFactory(new PropertyValueFactory<>("componentType"));
        TableColumn<RiskSummary, Integer> colRScore = new TableColumn<>("Score");
        colRScore.setCellValueFactory(new PropertyValueFactory<>("riskScore"));
        TableColumn<RiskSummary, String> colRLevel = new TableColumn<>("Level");
        colRLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        TableColumn<RiskSummary, String> colRExpl = new TableColumn<>("Primary Factors");
        colRExpl.setCellValueFactory(new PropertyValueFactory<>("riskExplanation"));
        tblRisk.getColumns().addAll(colRSerial, colRType, colRScore, colRLevel, colRExpl);
        tblRisk.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            tblRisk.setItems(FXCollections.observableArrayList(inventoryRiskService.getRiskIntelligenceSummary()));
        } catch (Exception ignored) {}
        riskPanel.getChildren().addAll(riskTitle, tblRisk);

        tablesRow.getChildren().addAll(overduePanel, riskPanel);

        root.getChildren().addAll(header, kpiRow1, kpiRow2, tablesRow);
        contentContainer.getChildren().setAll(root);
    }

    private VBox createKpiCard(String titleText, String valueText, String subtextText, String accentClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("kpi-card", accentClass);
        Label title = new Label(titleText);
        title.getStyleClass().add("kpi-title");
        Label value = new Label(valueText);
        value.getStyleClass().add("kpi-value");
        Label sub = new Label(subtextText);
        sub.getStyleClass().add("kpi-subtext");
        card.getChildren().addAll(title, value, sub);
        return card;
    }

    // ====================================================================
    // VIEW 2: AIRCRAFT FLEET REGISTRY (PERSON 1)
    // ====================================================================
    @FXML
    public void showFleetView() {
        setActiveNav(btnNavFleet);
        VBox root = new VBox(16);
        root.setPadding(new Insets(10));

        // Header & Action Bar
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(4);
        Label title = new Label("Aircraft Fleet Registry");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Person 1 Module: Manage aircraft airframes, operating hours, cycles and operational status.");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("+ Register New Aircraft");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> openAddAircraftDialog());

        topRow.getChildren().addAll(header, spacer, btnAdd);

        // Table
        TableView<Aircraft> table = new TableView<>();
        TableColumn<Aircraft, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("aircraftId"));
        colId.setPrefWidth(60);

        TableColumn<Aircraft, String> colReg = new TableColumn<>("Registration No");
        colReg.setCellValueFactory(new PropertyValueFactory<>("registrationNo"));

        TableColumn<Aircraft, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Aircraft, String> colMfr = new TableColumn<>("Manufacturer");
        colMfr.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));

        TableColumn<Aircraft, LocalDate> colMDate = new TableColumn<>("Manufacture Date");
        colMDate.setCellValueFactory(new PropertyValueFactory<>("manufactureDate"));

        TableColumn<Aircraft, Double> colHours = new TableColumn<>("Flight Hours");
        colHours.setCellValueFactory(new PropertyValueFactory<>("totalFlightHours"));

        TableColumn<Aircraft, Integer> colCycles = new TableColumn<>("Total Cycles");
        colCycles.setCellValueFactory(new PropertyValueFactory<>("totalCycles"));

        TableColumn<Aircraft, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colReg, colModel, colMfr, colMDate, colHours, colCycles, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Load data
        try {
            table.setItems(FXCollections.observableArrayList(fleetService.getAllAircraft()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load aircraft: " + e.getMessage());
        }

        // Action Toolbar below table
        HBox actionToolbar = new HBox(12);
        actionToolbar.setAlignment(Pos.CENTER_LEFT);
        Button btnSetActive = new Button("Set Status: ACTIVE");
        btnSetActive.getStyleClass().add("btn-success");
        btnSetActive.setOnAction(e -> updateSelectedAircraftStatus(table, "ACTIVE"));

        Button btnSetMaint = new Button("Set Status: MAINTENANCE");
        btnSetMaint.getStyleClass().add("btn-secondary");
        btnSetMaint.setOnAction(e -> updateSelectedAircraftStatus(table, "MAINTENANCE"));

        Button btnSetGrounded = new Button("Set Status: GROUNDED");
        btnSetGrounded.getStyleClass().add("btn-danger");
        btnSetGrounded.setOnAction(e -> updateSelectedAircraftStatus(table, "GROUNDED"));

        actionToolbar.getChildren().addAll(new Label("Quick Status Update:"), btnSetActive, btnSetMaint, btnSetGrounded);

        root.getChildren().addAll(topRow, table, actionToolbar);
        contentContainer.getChildren().setAll(root);
    }

    private void updateSelectedAircraftStatus(TableView<Aircraft> table, String newStatus) {
        Aircraft selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Aircraft Selected", "Please select an aircraft from the table first.");
            return;
        }
        try {
            fleetService.setAircraftStatus(selected.getAircraftId(), newStatus);
            showInfo("Status Updated", "Aircraft " + selected.getRegistrationNo() + " status set to " + newStatus);
            showFleetView();
        } catch (SQLException e) {
            showError("Update Failed", "Could not update status: " + e.getMessage());
        }
    }

    private void openAddAircraftDialog() {
        Dialog<Aircraft> dialog = new Dialog<>();
        dialog.setTitle("Register New Aircraft");
        dialog.setHeaderText("Enter Aircraft Details");

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField regField = new TextField();
        regField.setPromptText("e.g. VT-NEW");
        TextField modelField = new TextField();
        modelField.setPromptText("e.g. A320neo");
        TextField mfrField = new TextField();
        mfrField.setPromptText("e.g. Airbus");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField hoursField = new TextField("0.00");
        TextField cyclesField = new TextField("0");
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "MAINTENANCE", "GROUNDED"));
        statusBox.setValue("ACTIVE");

        grid.add(new Label("Registration No:"), 0, 0); grid.add(regField, 1, 0);
        grid.add(new Label("Model:"), 0, 1); grid.add(modelField, 1, 1);
        grid.add(new Label("Manufacturer:"), 0, 2); grid.add(mfrField, 1, 2);
        grid.add(new Label("Manufacture Date:"), 0, 3); grid.add(datePicker, 1, 3);
        grid.add(new Label("Flight Hours:"), 0, 4); grid.add(hoursField, 1, 4);
        grid.add(new Label("Flight Cycles:"), 0, 5); grid.add(cyclesField, 1, 5);
        grid.add(new Label("Status:"), 0, 6); grid.add(statusBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                try {
                    Aircraft a = new Aircraft();
                    a.setRegistrationNo(regField.getText().trim());
                    a.setModel(modelField.getText().trim());
                    a.setManufacturer(mfrField.getText().trim());
                    a.setManufactureDate(datePicker.getValue());
                    a.setTotalFlightHours(Double.parseDouble(hoursField.getText().trim()));
                    a.setTotalCycles(Integer.parseInt(cyclesField.getText().trim()));
                    a.setStatus(statusBox.getValue());
                    return a;
                } catch (Exception ex) {
                    showError("Validation Error", "Invalid input: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            try {
                fleetService.addAircraft(a);
                showInfo("Aircraft Registered", "Successfully registered aircraft " + a.getRegistrationNo());
                showFleetView();
            } catch (Exception e) {
                showError("Registration Failed", e.getMessage());
            }
        });
    }

    // ====================================================================
    // VIEW 3: COMPONENTS CATALOG (PERSON 1)
    // ====================================================================
    @FXML
    public void showComponentsView() {
        setActiveNav(btnNavComponents);
        VBox root = new VBox(16);
        root.setPadding(new Insets(10));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(4);
        Label title = new Label("Components Master Catalog");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Person 1 Module: Manage serialized rotable components, life limits and locations.");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("+ Add New Component");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> openAddComponentDialog());

        topRow.getChildren().addAll(header, spacer, btnAdd);

        // Table
        TableView<Component> table = new TableView<>();
        TableColumn<Component, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("componentId"));
        colId.setPrefWidth(50);

        TableColumn<Component, String> colSerial = new TableColumn<>("Serial No");
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));

        TableColumn<Component, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("componentType"));

        TableColumn<Component, String> colMfr = new TableColumn<>("Manufacturer");
        colMfr.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));

        TableColumn<Component, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Component, Double> colLimitH = new TableColumn<>("Limit (Hours)");
        colLimitH.setCellValueFactory(new PropertyValueFactory<>("serviceLimitHours"));

        TableColumn<Component, Integer> colLimitC = new TableColumn<>("Limit (Cycles)");
        colLimitC.setCellValueFactory(new PropertyValueFactory<>("serviceLimitCycles"));

        TableColumn<Component, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("currentStatus"));

        TableColumn<Component, String> colLoc = new TableColumn<>("Current Location");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("currentLocation"));

        table.getColumns().addAll(colId, colSerial, colType, colMfr, colModel, colLimitH, colLimitC, colStatus, colLoc);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        try {
            table.setItems(FXCollections.observableArrayList(fleetService.getAllComponents()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load components: " + e.getMessage());
        }

        root.getChildren().addAll(topRow, table);
        contentContainer.getChildren().setAll(root);
    }

    private void openAddComponentDialog() {
        Dialog<Component> dialog = new Dialog<>();
        dialog.setTitle("Register New Component");
        dialog.setHeaderText("Enter Component Specifications");

        ButtonType btnType = new ButtonType("Save Component", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serialField = new TextField(); serialField.setPromptText("e.g. ENG-999");
        TextField typeField = new TextField(); typeField.setPromptText("e.g. Engine, Weather Radar");
        TextField mfrField = new TextField(); mfrField.setPromptText("e.g. CFM, Honeywell");
        TextField modelField = new TextField(); modelField.setPromptText("e.g. CFM56");
        TextField limitHField = new TextField("20000.00");
        TextField limitCField = new TextField("10000");
        TextField locationField = new TextField("Warehouse");

        grid.add(new Label("Serial Number:"), 0, 0); grid.add(serialField, 1, 0);
        grid.add(new Label("Component Type:"), 0, 1); grid.add(typeField, 1, 1);
        grid.add(new Label("Manufacturer:"), 0, 2); grid.add(mfrField, 1, 2);
        grid.add(new Label("Model:"), 0, 3); grid.add(modelField, 1, 3);
        grid.add(new Label("Limit (Hours):"), 0, 4); grid.add(limitHField, 1, 4);
        grid.add(new Label("Limit (Cycles):"), 0, 5); grid.add(limitCField, 1, 5);
        grid.add(new Label("Location:"), 0, 6); grid.add(locationField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> {
            if (b == btnType) {
                try {
                    Component c = new Component();
                    c.setSerialNumber(serialField.getText().trim());
                    c.setComponentType(typeField.getText().trim());
                    c.setManufacturer(mfrField.getText().trim());
                    c.setModel(modelField.getText().trim());
                    c.setServiceLimitHours(Double.parseDouble(limitHField.getText().trim()));
                    c.setServiceLimitCycles(Integer.parseInt(limitCField.getText().trim()));
                    c.setCurrentStatus("AVAILABLE");
                    c.setCurrentLocation(locationField.getText().trim());
                    return c;
                } catch (Exception ex) {
                    showError("Validation Error", "Invalid input: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            try {
                fleetService.addComponent(c);
                showInfo("Component Created", "Component " + c.getSerialNumber() + " registered successfully.");
                showComponentsView();
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        });
    }

    // ====================================================================
    // VIEW 4: INSTALLATIONS & LIFECYCLE HISTORY (PERSON 1)
    // ====================================================================
    @FXML
    public void showLifecycleView() {
        setActiveNav(btnNavLifecycle);
        VBox root = new VBox(16);
        root.setPadding(new Insets(10));

        // Header & Actions
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(4);
        Label title = new Label("Component Installations & Lifecycle Audit");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Person 1 Module: Transactional installation & removal, tracking operational hours and complete history.");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnInstall = new Button("📥 Install Component on Aircraft");
        btnInstall.getStyleClass().add("btn-primary");
        btnInstall.setOnAction(e -> openInstallComponentDialog());

        topRow.getChildren().addAll(header, spacer, btnInstall);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Currently Installed Components (v_aircraft_current_components)
        Tab tabCurrent = new Tab("Currently Installed Components");
        VBox currentBox = new VBox(10);
        currentBox.setPadding(new Insets(10, 0, 0, 0));

        TableView<AircraftComponent> tblCurrent = new TableView<>();
        TableColumn<AircraftComponent, String> colCAir = new TableColumn<>("Aircraft");
        colCAir.setCellValueFactory(new PropertyValueFactory<>("aircraftRegistration"));
        TableColumn<AircraftComponent, String> colCSerial = new TableColumn<>("Component Serial");
        colCSerial.setCellValueFactory(new PropertyValueFactory<>("componentSerial"));
        TableColumn<AircraftComponent, String> colCType = new TableColumn<>("Component Type");
        colCType.setCellValueFactory(new PropertyValueFactory<>("componentType"));
        TableColumn<AircraftComponent, LocalDate> colCDate = new TableColumn<>("Installed Date");
        colCDate.setCellValueFactory(new PropertyValueFactory<>("installedDate"));
        TableColumn<AircraftComponent, Double> colCHours = new TableColumn<>("Hours Logged on Aircraft");
        colCHours.setCellValueFactory(new PropertyValueFactory<>("operationalHoursLogged"));
        TableColumn<AircraftComponent, Integer> colCCycles = new TableColumn<>("Cycles Logged");
        colCCycles.setCellValueFactory(new PropertyValueFactory<>("operationalCyclesLogged"));

        tblCurrent.getColumns().addAll(colCAir, colCSerial, colCType, colCDate, colCHours, colCCycles);
        tblCurrent.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblCurrent, Priority.ALWAYS);

        try {
            tblCurrent.setItems(FXCollections.observableArrayList(fleetService.getActiveInstallations()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load active installations: " + e.getMessage());
        }

        Button btnRemove = new Button("📤 Remove Selected Component (sp_remove_component)");
        btnRemove.getStyleClass().add("btn-danger");
        btnRemove.setOnAction(e -> {
            AircraftComponent sel = tblCurrent.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showWarning("Selection Required", "Please select an installed component from the table to remove.");
                return;
            }
            openRemoveComponentDialog(sel);
        });

        currentBox.getChildren().addAll(tblCurrent, btnRemove);
        tabCurrent.setContent(currentBox);

        // Tab 2: Full Lifecycle History (v_component_lifecycle_history)
        Tab tabHistory = new Tab("Comprehensive Lifecycle History");
        VBox histBox = new VBox(10);
        histBox.setPadding(new Insets(10, 0, 0, 0));

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Filter by component serial number...");
        Button btnSearch = new Button("Search Lifecycle");
        btnSearch.getStyleClass().add("btn-secondary");

        TableView<AircraftComponent> tblHist = new TableView<>();
        TableColumn<AircraftComponent, String> colHSerial = new TableColumn<>("Serial");
        colHSerial.setCellValueFactory(new PropertyValueFactory<>("componentSerial"));
        TableColumn<AircraftComponent, String> colHType = new TableColumn<>("Type");
        colHType.setCellValueFactory(new PropertyValueFactory<>("componentType"));
        TableColumn<AircraftComponent, String> colHAir = new TableColumn<>("Aircraft");
        colHAir.setCellValueFactory(new PropertyValueFactory<>("aircraftRegistration"));
        TableColumn<AircraftComponent, LocalDate> colHInstDate = new TableColumn<>("Installed Date");
        colHInstDate.setCellValueFactory(new PropertyValueFactory<>("installedDate"));
        TableColumn<AircraftComponent, LocalDate> colHRemDate = new TableColumn<>("Removed Date");
        colHRemDate.setCellValueFactory(new PropertyValueFactory<>("removedDate"));
        TableColumn<AircraftComponent, Double> colHOpHours = new TableColumn<>("Operating Hours");
        colHOpHours.setCellValueFactory(new PropertyValueFactory<>("operationalHoursLogged"));
        TableColumn<AircraftComponent, String> colHState = new TableColumn<>("State");
        colHState.setCellValueFactory(new PropertyValueFactory<>("lifecycleState"));
        TableColumn<AircraftComponent, String> colHReason = new TableColumn<>("Removal Reason");
        colHReason.setCellValueFactory(new PropertyValueFactory<>("removalReason"));

        tblHist.getColumns().addAll(colHSerial, colHType, colHAir, colHInstDate, colHRemDate, colHOpHours, colHState, colHReason);
        tblHist.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblHist, Priority.ALWAYS);

        Runnable loadHistory = () -> {
            try {
                tblHist.setItems(FXCollections.observableArrayList(fleetService.getLifecycleHistory(searchField.getText())));
            } catch (SQLException ex) {
                showError("History Load Error", ex.getMessage());
            }
        };
        btnSearch.setOnAction(e -> loadHistory.run());
        loadHistory.run();

        filterBox.getChildren().addAll(new Label("Filter Serial:"), searchField, btnSearch);
        histBox.getChildren().addAll(filterBox, tblHist);
        tabHistory.setContent(histBox);

        tabPane.getTabs().addAll(tabCurrent, tabHistory);
        root.getChildren().addAll(topRow, tabPane);
        contentContainer.getChildren().setAll(root);
    }

    private void openInstallComponentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Install Component (sp_install_component)");
        dialog.setHeaderText("Transactionally mount component onto aircraft");

        ButtonType btnInstallType = new ButtonType("Execute Installation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnInstallType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Aircraft> aircraftBox = new ComboBox<>();
        ComboBox<Component> componentBox = new ComboBox<>();
        try {
            aircraftBox.setItems(FXCollections.observableArrayList(fleetService.getAllAircraft()));
            componentBox.setItems(FXCollections.observableArrayList(fleetService.getAvailableComponents()));
        } catch (Exception ignored) {}

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField hoursField = new TextField("0.00");
        TextField cyclesField = new TextField("0");
        TextField reasonField = new TextField("Routine Replacement");

        grid.add(new Label("Target Aircraft:"), 0, 0); grid.add(aircraftBox, 1, 0);
        grid.add(new Label("Available Component:"), 0, 1); grid.add(componentBox, 1, 1);
        grid.add(new Label("Installation Date:"), 0, 2); grid.add(datePicker, 1, 2);
        grid.add(new Label("Aircraft Hours at Install:"), 0, 3); grid.add(hoursField, 1, 3);
        grid.add(new Label("Aircraft Cycles at Install:"), 0, 4); grid.add(cyclesField, 1, 4);
        grid.add(new Label("Installation Reason:"), 0, 5); grid.add(reasonField, 1, 5);

        // Pre-fill hours and cycles when aircraft selected
        aircraftBox.setOnAction(e -> {
            Aircraft a = aircraftBox.getValue();
            if (a != null) {
                hoursField.setText(String.valueOf(a.getTotalFlightHours()));
                cyclesField.setText(String.valueOf(a.getTotalCycles()));
            }
        });

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnInstallType) {
                Aircraft air = aircraftBox.getValue();
                Component comp = componentBox.getValue();
                if (air == null || comp == null) {
                    showError("Selection Missing", "Please select both an aircraft and an available component.");
                    return;
                }
                try {
                    double h = Double.parseDouble(hoursField.getText().trim());
                    int c = Integer.parseInt(cyclesField.getText().trim());
                    int installId = fleetService.installComponent(
                        air.getAircraftId(), comp.getComponentId(), datePicker.getValue(), h, c, reasonField.getText().trim()
                    );
                    showInfo("Installation Successful", "Component " + comp.getSerialNumber() + " installed on " + air.getRegistrationNo() + " (Record #" + installId + ")");
                    showLifecycleView();
                } catch (Exception ex) {
                    showError("Installation Failed", ex.getMessage());
                }
            }
        });
    }

    private void openRemoveComponentDialog(AircraftComponent item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Remove Component (sp_remove_component)");
        dialog.setHeaderText("Remove " + item.getComponentSerial() + " from " + item.getAircraftRegistration());

        ButtonType btnRemoveType = new ButtonType("Confirm Removal", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnRemoveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField hoursField = new TextField(String.valueOf(item.getInstallationHours() + 50.0));
        TextField cyclesField = new TextField(String.valueOf(item.getInstallationCycles() + 20));
        TextField reasonField = new TextField("Scheduled Overhaul");
        TextField locationField = new TextField("Central Warehouse");
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("AVAILABLE", "MAINTENANCE", "INSPECTION", "SCRAPPED"));
        statusBox.setValue("AVAILABLE");

        grid.add(new Label("Removal Date:"), 0, 0); grid.add(datePicker, 1, 0);
        grid.add(new Label("Aircraft Flight Hours:"), 0, 1); grid.add(hoursField, 1, 1);
        grid.add(new Label("Aircraft Flight Cycles:"), 0, 2); grid.add(cyclesField, 1, 2);
        grid.add(new Label("Removal Reason:"), 0, 3); grid.add(reasonField, 1, 3);
        grid.add(new Label("Storage Location:"), 0, 4); grid.add(locationField, 1, 4);
        grid.add(new Label("Component New Status:"), 0, 5); grid.add(statusBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnRemoveType) {
                try {
                    double h = Double.parseDouble(hoursField.getText().trim());
                    int c = Integer.parseInt(cyclesField.getText().trim());
                    fleetService.removeComponent(
                        item.getInstallationId(), datePicker.getValue(), h, c,
                        reasonField.getText().trim(), locationField.getText().trim(), statusBox.getValue()
                    );
                    showInfo("Removal Completed", "Component " + item.getComponentSerial() + " successfully uninstalled and returned to inventory.");
                    showLifecycleView();
                } catch (Exception ex) {
                    showError("Removal Error", ex.getMessage());
                }
            }
        });
    }

    // ====================================================================
    // VIEW 5: MAINTENANCE OPERATIONS (PERSON 2)
    // ====================================================================
    @FXML
    public void showMaintenanceView() {
        setActiveNav(btnNavMaintenance);
        VBox root = new VBox(16);
        root.setPadding(new Insets(10));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(4);
        Label title = new Label("Maintenance Operations & Work Orders");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Person 2 Module: Scheduled, in-progress and overdue maintenance tracking with technician assignments.");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSchedule = new Button("+ Schedule Maintenance");
        btnSchedule.getStyleClass().add("btn-primary");
        btnSchedule.setOnAction(e -> openScheduleMaintenanceDialog());

        topRow.getChildren().addAll(header, spacer, btnSchedule);

        TableView<Maintenance> table = new TableView<>();
        TableColumn<Maintenance, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("maintenanceId"));
        colId.setPrefWidth(50);

        TableColumn<Maintenance, String> colType = new TableColumn<>("Maintenance Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("maintenanceType"));

        TableColumn<Maintenance, String> colAir = new TableColumn<>("Aircraft");
        colAir.setCellValueFactory(new PropertyValueFactory<>("aircraftRegistration"));

        TableColumn<Maintenance, String> colComp = new TableColumn<>("Component");
        colComp.setCellValueFactory(new PropertyValueFactory<>("componentSerial"));

        TableColumn<Maintenance, LocalDate> colSched = new TableColumn<>("Scheduled Date");
        colSched.setCellValueFactory(new PropertyValueFactory<>("scheduledDate"));

        TableColumn<Maintenance, LocalDate> colCompDate = new TableColumn<>("Completed Date");
        colCompDate.setCellValueFactory(new PropertyValueFactory<>("completedDate"));

        TableColumn<Maintenance, String> colTech = new TableColumn<>("Assigned Technician");
        colTech.setCellValueFactory(new PropertyValueFactory<>("technicianName"));

        TableColumn<Maintenance, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colType, colAir, colComp, colSched, colCompDate, colTech, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        try {
            table.setItems(FXCollections.observableArrayList(maintenanceService.getAllMaintenance()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load maintenance records: " + e.getMessage());
        }

        // Action: Complete Selected Task
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        Button btnComplete = new Button("✓ Mark Selected Maintenance as Completed");
        btnComplete.getStyleClass().add("btn-success");
        btnComplete.setOnAction(e -> {
            Maintenance sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showWarning("Selection Required", "Please select a maintenance record to complete.");
                return;
            }
            try {
                maintenanceService.completeMaintenance(sel.getMaintenanceId(), LocalDate.now());
                showInfo("Completed", "Work order #" + sel.getMaintenanceId() + " marked as COMPLETED.");
                showMaintenanceView();
            } catch (SQLException ex) {
                showError("Completion Error", ex.getMessage());
            }
        });
        bottomBar.getChildren().addAll(btnComplete);

        root.getChildren().addAll(topRow, table, bottomBar);
        contentContainer.getChildren().setAll(root);
    }

    private void openScheduleMaintenanceDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Schedule Maintenance");
        dialog.setHeaderText("Create Maintenance Task / Assign Technician");

        ButtonType btnType = new ButtonType("Schedule Task", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Aircraft> airBox = new ComboBox<>();
        ComboBox<Component> compBox = new ComboBox<>();
        ComboBox<Technician> techBox = new ComboBox<>();
        try {
            airBox.setItems(FXCollections.observableArrayList(fleetService.getAllAircraft()));
            compBox.setItems(FXCollections.observableArrayList(fleetService.getAllComponents()));
            techBox.setItems(FXCollections.observableArrayList(maintenanceService.getActiveTechnicians()));
        } catch (Exception ignored) {}

        TextField typeField = new TextField(); typeField.setPromptText("e.g. B-Check, Hydraulic Overhaul");
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(7));
        TextField descField = new TextField(); descField.setPromptText("Work description...");

        grid.add(new Label("Aircraft:"), 0, 0); grid.add(airBox, 1, 0);
        grid.add(new Label("Component (Optional):"), 0, 1); grid.add(compBox, 1, 1);
        grid.add(new Label("Maintenance Type:"), 0, 2); grid.add(typeField, 1, 2);
        grid.add(new Label("Scheduled Date:"), 0, 3); grid.add(datePicker, 1, 3);
        grid.add(new Label("Assigned Technician:"), 0, 4); grid.add(techBox, 1, 4);
        grid.add(new Label("Description:"), 0, 5); grid.add(descField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnType) {
                Aircraft air = airBox.getValue();
                if (air == null) {
                    showError("Validation Error", "Target aircraft is required.");
                    return;
                }
                try {
                    Maintenance m = new Maintenance();
                    m.setAircraftId(air.getAircraftId());
                    if (compBox.getValue() != null) m.setComponentId(compBox.getValue().getComponentId());
                    m.setMaintenanceType(typeField.getText().trim());
                    m.setScheduledDate(datePicker.getValue());
                    if (techBox.getValue() != null) m.setTechnicianId(techBox.getValue().getTechnicianId());
                    m.setDescription(descField.getText().trim());
                    m.setStatus("SCHEDULED");
                    maintenanceService.scheduleMaintenance(m);
                    showInfo("Maintenance Scheduled", "Task created successfully.");
                    showMaintenanceView();
                } catch (Exception ex) {
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    // ====================================================================
    // VIEW 6: INSPECTIONS & FAILURES (PERSON 2)
    // ====================================================================
    @FXML
    public void showInspectionsView() {
        setActiveNav(btnNavInspections);
        VBox root = new VBox(16);
        root.setPadding(new Insets(10));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(4);
        Label title = new Label("Inspections & Failure Intelligence");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Person 2 Module: Audit inspections, log defects, identify recurring component failure patterns.");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAddInsp = new Button("+ Record Inspection");
        btnAddInsp.getStyleClass().add("btn-secondary");
        btnAddInsp.setOnAction(e -> openRecordInspectionDialog());

        Button btnAddFail = new Button("+ Log Failure / Incident");
        btnAddFail.getStyleClass().add("btn-danger");
        btnAddFail.setOnAction(e -> openLogFailureDialog());

        topRow.getChildren().addAll(header, spacer, btnAddInsp, btnAddFail);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Inspections
        Tab tabInsp = new Tab("Inspections Log");
        TableView<Inspection> tblInsp = new TableView<>();
        TableColumn<Inspection, Integer> colIId = new TableColumn<>("ID");
        colIId.setCellValueFactory(new PropertyValueFactory<>("inspectionId"));
        colIId.setPrefWidth(50);
        TableColumn<Inspection, String> colIAir = new TableColumn<>("Aircraft");
        colIAir.setCellValueFactory(new PropertyValueFactory<>("aircraftRegistration"));
        TableColumn<Inspection, String> colIComp = new TableColumn<>("Component");
        colIComp.setCellValueFactory(new PropertyValueFactory<>("componentSerial"));
        TableColumn<Inspection, LocalDate> colIDate = new TableColumn<>("Date");
        colIDate.setCellValueFactory(new PropertyValueFactory<>("inspectionDate"));
        TableColumn<Inspection, String> colIType = new TableColumn<>("Type");
        colIType.setCellValueFactory(new PropertyValueFactory<>("inspectionType"));
        TableColumn<Inspection, String> colIRes = new TableColumn<>("Result");
        colIRes.setCellValueFactory(new PropertyValueFactory<>("result"));
        TableColumn<Inspection, String> colIFind = new TableColumn<>("Findings");
        colIFind.setCellValueFactory(new PropertyValueFactory<>("findings"));
        TableColumn<Inspection, String> colIInsp = new TableColumn<>("Inspector");
        colIInsp.setCellValueFactory(new PropertyValueFactory<>("inspectorName"));

        tblInsp.getColumns().addAll(colIId, colIAir, colIComp, colIDate, colIType, colIRes, colIFind, colIInsp);
        tblInsp.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        try {
            tblInsp.setItems(FXCollections.observableArrayList(maintenanceService.getAllInspections()));
        } catch (Exception ignored) {}
        tabInsp.setContent(tblInsp);

        // Tab 2: Failures
        Tab tabFail = new Tab("Failure Incidents");
        VBox failBox = new VBox(8);
        TableView<Failure> tblFail = new TableView<>();
        TableColumn<Failure, Integer> colFId = new TableColumn<>("ID");
        colFId.setCellValueFactory(new PropertyValueFactory<>("failureId"));
        colFId.setPrefWidth(50);
        TableColumn<Failure, String> colFAir = new TableColumn<>("Aircraft");
        colFAir.setCellValueFactory(new PropertyValueFactory<>("aircraftRegistration"));
        TableColumn<Failure, String> colFComp = new TableColumn<>("Component");
        colFComp.setCellValueFactory(new PropertyValueFactory<>("componentSerial"));
        TableColumn<Failure, LocalDate> colFDate = new TableColumn<>("Date");
        colFDate.setCellValueFactory(new PropertyValueFactory<>("failureDate"));
        TableColumn<Failure, String> colFType = new TableColumn<>("Failure Type");
        colFType.setCellValueFactory(new PropertyValueFactory<>("failureType"));
        TableColumn<Failure, String> colFSeverity = new TableColumn<>("Severity");
        colFSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        TableColumn<Failure, String> colFDesc = new TableColumn<>("Description");
        colFDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Failure, Boolean> colFResolved = new TableColumn<>("Resolved");
        colFResolved.setCellValueFactory(new PropertyValueFactory<>("resolved"));

        tblFail.getColumns().addAll(colFId, colFAir, colFComp, colFDate, colFType, colFSeverity, colFDesc, colFResolved);
        tblFail.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblFail, Priority.ALWAYS);
        try {
            tblFail.setItems(FXCollections.observableArrayList(maintenanceService.getAllFailures()));
        } catch (Exception ignored) {}

        Button btnToggleRes = new Button("Toggle Selected Failure Resolution");
        btnToggleRes.getStyleClass().add("btn-secondary");
        btnToggleRes.setOnAction(e -> {
            Failure sel = tblFail.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    maintenanceService.resolveFailure(sel.getFailureId(), !sel.isResolved());
                    showInspectionsView();
                } catch (SQLException ex) {
                    showError("Error", ex.getMessage());
                }
            }
        });
        failBox.getChildren().addAll(tblFail, btnToggleRes);
        tabFail.setContent(failBox);

        // Tab 3: Recurring Failures (v_recurring_failures)
        Tab tabRec = new Tab("⚠️ Recurring Failures (Threshold >= 3)");
        TableView<Failure> tblRec = new TableView<>();
        TableColumn<Failure, String> colRComp = new TableColumn<>("Component Serial");
        colRComp.setCellValueFactory(new PropertyValueFactory<>("componentSerial"));
        TableColumn<Failure, String> colRType = new TableColumn<>("Component Type");
        colRType.setCellValueFactory(new PropertyValueFactory<>("componentType"));
        TableColumn<Failure, String> colRFType = new TableColumn<>("Failure Type");
        colRFType.setCellValueFactory(new PropertyValueFactory<>("failureType"));
        TableColumn<Failure, Integer> colRCount = new TableColumn<>("Total Incidents");
        colRCount.setCellValueFactory(new PropertyValueFactory<>("incidentCount"));
        TableColumn<Failure, String> colRSeverity = new TableColumn<>("Highest Severity");
        colRSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        TableColumn<Failure, String> colRDesc = new TableColumn<>("Scope & Status");
        colRDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        tblRec.getColumns().addAll(colRComp, colRType, colRFType, colRCount, colRSeverity, colRDesc);
        tblRec.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        try {
            tblRec.setItems(FXCollections.observableArrayList(maintenanceService.getRecurringFailures()));
        } catch (Exception ignored) {}
        tabRec.setContent(tblRec);

        tabPane.getTabs().addAll(tabInsp, tabFail, tabRec);
        root.getChildren().addAll(topRow, tabPane);
        contentContainer.getChildren().setAll(root);
    }

    private void openRecordInspectionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Inspection");
        ButtonType btnType = new ButtonType("Save Inspection", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Aircraft> airBox = new ComboBox<>();
        ComboBox<Component> compBox = new ComboBox<>();
        ComboBox<Technician> inspBox = new ComboBox<>();
        try {
            airBox.setItems(FXCollections.observableArrayList(fleetService.getAllAircraft()));
            compBox.setItems(FXCollections.observableArrayList(fleetService.getAllComponents()));
            inspBox.setItems(FXCollections.observableArrayList(maintenanceService.getActiveTechnicians()));
        } catch (Exception ignored) {}

        TextField typeField = new TextField("Pre-Flight Structural Check");
        ComboBox<String> resBox = new ComboBox<>(FXCollections.observableArrayList("PASSED", "WARNING", "FAILED", "ACTION_REQUIRED"));
        resBox.setValue("PASSED");
        TextField findingsField = new TextField("All parameters nominal.");

        grid.add(new Label("Aircraft:"), 0, 0); grid.add(airBox, 1, 0);
        grid.add(new Label("Component (Optional):"), 0, 1); grid.add(compBox, 1, 1);
        grid.add(new Label("Inspection Type:"), 0, 2); grid.add(typeField, 1, 2);
        grid.add(new Label("Result:"), 0, 3); grid.add(resBox, 1, 3);
        grid.add(new Label("Findings:"), 0, 4); grid.add(findingsField, 1, 4);
        grid.add(new Label("Inspector:"), 0, 5); grid.add(inspBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnType) {
                if (airBox.getValue() == null || inspBox.getValue() == null) {
                    showError("Error", "Aircraft and Inspector are required.");
                    return;
                }
                try {
                    Inspection i = new Inspection();
                    i.setAircraftId(airBox.getValue().getAircraftId());
                    if (compBox.getValue() != null) i.setComponentId(compBox.getValue().getComponentId());
                    i.setInspectionDate(LocalDate.now());
                    i.setInspectionType(typeField.getText().trim());
                    i.setResult(resBox.getValue());
                    i.setFindings(findingsField.getText().trim());
                    i.setInspectorId(inspBox.getValue().getTechnicianId());
                    maintenanceService.recordInspection(i);
                    showInfo("Recorded", "Inspection record added.");
                    showInspectionsView();
                } catch (Exception ex) {
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    private void openLogFailureDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Log Aircraft/Component Failure");
        dialog.setHeaderText("Logging a HIGH or CRITICAL severity automatically fires trg_failure_critical_alert!");

        ButtonType btnType = new ButtonType("Log Incident", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Aircraft> airBox = new ComboBox<>();
        ComboBox<Component> compBox = new ComboBox<>();
        try {
            airBox.setItems(FXCollections.observableArrayList(fleetService.getAllAircraft()));
            compBox.setItems(FXCollections.observableArrayList(fleetService.getAllComponents()));
        } catch (Exception ignored) {}

        TextField typeField = new TextField(); typeField.setPromptText("e.g. Hydraulic Leak, Signal Loss");
        ComboBox<String> sevBox = new ComboBox<>(FXCollections.observableArrayList("LOW", "MEDIUM", "HIGH", "CRITICAL"));
        sevBox.setValue("HIGH");
        TextField descField = new TextField(); descField.setPromptText("Detailed incident report...");

        grid.add(new Label("Aircraft:"), 0, 0); grid.add(airBox, 1, 0);
        grid.add(new Label("Component:"), 0, 1); grid.add(compBox, 1, 1);
        grid.add(new Label("Failure Type:"), 0, 2); grid.add(typeField, 1, 2);
        grid.add(new Label("Severity:"), 0, 3); grid.add(sevBox, 1, 3);
        grid.add(new Label("Description:"), 0, 4); grid.add(descField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnType) {
                if (airBox.getValue() == null || compBox.getValue() == null) {
                    showError("Error", "Both Aircraft and Component are required.");
                    return;
                }
                try {
                    Failure f = new Failure();
                    f.setAircraftId(airBox.getValue().getAircraftId());
                    f.setComponentId(compBox.getValue().getComponentId());
                    f.setFailureDate(LocalDate.now());
                    f.setFailureType(typeField.getText().trim());
                    f.setSeverity(sevBox.getValue());
                    f.setDescription(descField.getText().trim());
                    f.setResolved(false);
                    maintenanceService.logFailure(f);
                    showInfo("Failure Logged", "Failure registered. Triggers checked for alert creation.");
                    showInspectionsView();
                } catch (Exception ex) {
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    // ====================================================================
    // VIEW 7: INVENTORY & SPARE PARTS (PERSON 3)
    // ====================================================================
    @FXML
    public void showInventoryView() {
        setActiveNav(btnNavInventory);
        VBox root = new VBox(16);
        root.setPadding(new Insets(10));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(4);
        Label title = new Label("Spare Parts Catalog & Multi-Location Inventory");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Person 3 Module: Stock aggregated across warehouses; automated low-stock detection.");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAddPart = new Button("+ Add Spare Part");
        btnAddPart.getStyleClass().add("btn-primary");
        btnAddPart.setOnAction(e -> openAddSparePartDialog());

        topRow.getChildren().addAll(header, spacer, btnAddPart);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Catalog & Aggregated Stock
        Tab tabCatalog = new Tab("Spare Parts Master");
        TableView<SparePart> tblParts = new TableView<>();
        TableColumn<SparePart, String> colPNum = new TableColumn<>("Part Number");
        colPNum.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        TableColumn<SparePart, String> colPName = new TableColumn<>("Name");
        colPName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        TableColumn<SparePart, String> colPType = new TableColumn<>("Component Type");
        colPType.setCellValueFactory(new PropertyValueFactory<>("componentType"));
        TableColumn<SparePart, String> colPSupp = new TableColumn<>("Supplier");
        colPSupp.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        TableColumn<SparePart, Double> colPCost = new TableColumn<>("Unit Cost ($)");
        colPCost.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        TableColumn<SparePart, Integer> colPMin = new TableColumn<>("Min Stock");
        colPMin.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));
        TableColumn<SparePart, Integer> colPTotal = new TableColumn<>("Total Available (All Depots)");
        colPTotal.setCellValueFactory(new PropertyValueFactory<>("totalQuantityAvailable"));

        tblParts.getColumns().addAll(colPNum, colPName, colPType, colPSupp, colPCost, colPMin, colPTotal);
        tblParts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        try {
            tblParts.setItems(FXCollections.observableArrayList(inventoryRiskService.getAllSpareParts()));
        } catch (Exception ignored) {}
        tabCatalog.setContent(tblParts);

        // Tab 2: Multi-location Details
        Tab tabLocations = new Tab("Warehouse / Depot Stock");
        VBox locBox = new VBox(8);
        TableView<Inventory> tblLoc = new TableView<>();
        TableColumn<Inventory, String> colLNum = new TableColumn<>("Part Number");
        colLNum.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        TableColumn<Inventory, String> colLName = new TableColumn<>("Part Name");
        colLName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        TableColumn<Inventory, String> colLLoc = new TableColumn<>("Location");
        colLLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
        TableColumn<Inventory, Integer> colLQty = new TableColumn<>("Quantity Available");
        colLQty.setCellValueFactory(new PropertyValueFactory<>("quantityAvailable"));
        TableColumn<Inventory, Object> colLUpd = new TableColumn<>("Last Updated");
        colLUpd.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));

        tblLoc.getColumns().addAll(colLNum, colLName, colLLoc, colLQty, colLUpd);
        tblLoc.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblLoc, Priority.ALWAYS);
        try {
            tblLoc.setItems(FXCollections.observableArrayList(inventoryRiskService.getAllInventory()));
        } catch (Exception ignored) {}

        Button btnUpdateQty = new Button("✏️ Update Stock Quantity (Tests trg_inventory_low_stock)");
        btnUpdateQty.getStyleClass().add("btn-secondary");
        btnUpdateQty.setOnAction(e -> {
            Inventory sel = tblLoc.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showWarning("Selection Required", "Select an inventory record to modify.");
                return;
            }
            openUpdateStockDialog(sel);
        });
        locBox.getChildren().addAll(tblLoc, btnUpdateQty);
        tabLocations.setContent(locBox);

        // Tab 3: Low Stock Deficit Report (v_low_stock_parts)
        Tab tabLow = new Tab("⚠️ Low Stock Deficit Report (v_low_stock_parts)");
        TableView<SparePart> tblLow = new TableView<>();
        TableColumn<SparePart, String> colWNum = new TableColumn<>("Part Number");
        colWNum.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        TableColumn<SparePart, String> colWName = new TableColumn<>("Part Name");
        colWName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        TableColumn<SparePart, Integer> colWMin = new TableColumn<>("Min Required");
        colWMin.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));
        TableColumn<SparePart, Integer> colWCur = new TableColumn<>("Total Available");
        colWCur.setCellValueFactory(new PropertyValueFactory<>("totalQuantityAvailable"));
        TableColumn<SparePart, Integer> colWDef = new TableColumn<>("Deficit");
        colWDef.setCellValueFactory(new PropertyValueFactory<>("stockDeficit"));
        TableColumn<SparePart, String> colWLocs = new TableColumn<>("Locations Breakdown");
        colWLocs.setCellValueFactory(new PropertyValueFactory<>("locationBreakdown"));

        tblLow.getColumns().addAll(colWNum, colWName, colWMin, colWCur, colWDef, colWLocs);
        tblLow.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        try {
            tblLow.setItems(FXCollections.observableArrayList(inventoryRiskService.getLowStockParts()));
        } catch (Exception ignored) {}
        tabLow.setContent(tblLow);

        tabPane.getTabs().addAll(tabCatalog, tabLocations, tabLow);
        root.getChildren().addAll(topRow, tabPane);
        contentContainer.getChildren().setAll(root);
    }

    private void openAddSparePartDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Spare Part");
        ButtonType btnType = new ButtonType("Save Part", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField noField = new TextField(); noField.setPromptText("e.g. RNG-009");
        TextField nameField = new TextField(); nameField.setPromptText("e.g. O-Ring High Pressure");
        TextField typeField = new TextField(); typeField.setPromptText("e.g. Hydraulic System");
        TextField suppField = new TextField(); suppField.setPromptText("e.g. AeroParts Ltd");
        TextField costField = new TextField("150.00");
        TextField minField = new TextField("10");

        grid.add(new Label("Part Number:"), 0, 0); grid.add(noField, 1, 0);
        grid.add(new Label("Part Name:"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("Component Type:"), 0, 2); grid.add(typeField, 1, 2);
        grid.add(new Label("Supplier:"), 0, 3); grid.add(suppField, 1, 3);
        grid.add(new Label("Unit Cost ($):"), 0, 4); grid.add(costField, 1, 4);
        grid.add(new Label("Minimum Stock:"), 0, 5); grid.add(minField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnType) {
                try {
                    SparePart sp = new SparePart();
                    sp.setPartNumber(noField.getText().trim());
                    sp.setPartName(nameField.getText().trim());
                    sp.setComponentType(typeField.getText().trim());
                    sp.setSupplier(suppField.getText().trim());
                    sp.setUnitCost(Double.parseDouble(costField.getText().trim()));
                    sp.setMinimumStock(Integer.parseInt(minField.getText().trim()));
                    inventoryRiskService.addSparePart(sp);
                    showInfo("Created", "Spare part registered.");
                    showInventoryView();
                } catch (Exception ex) {
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    private void openUpdateStockDialog(Inventory item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getQuantityAvailable()));
        dialog.setTitle("Update Stock Quantity");
        dialog.setHeaderText("Set quantity for " + item.getPartNumber() + " at " + item.getLocation());
        dialog.setContentText("New Quantity Available:");

        dialog.showAndWait().ifPresent(qtyStr -> {
            try {
                int newQty = Integer.parseInt(qtyStr.trim());
                inventoryRiskService.updateStockQuantity(item.getInventoryId(), newQty);
                showInfo("Updated", "Stock updated. Automated low-stock trigger evaluated.");
                showInventoryView();
            } catch (Exception ex) {
                showError("Update Error", ex.getMessage());
            }
        });
    }

    // ====================================================================
    // VIEW 8: ALERTS & RISK INTELLIGENCE ENGINE (PERSON 3)
    // ====================================================================
    @FXML
    public void showAlertsView() {
        setActiveNav(btnNavAlerts);
        VBox root = new VBox(16);
        root.setPadding(new Insets(10));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(4);
        Label title = new Label("Alerts & Component Risk Intelligence Engine");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Person 3 Module: Multi-factor explainable risk scoring (+30 limit breach, +20 near limit, +25 overdue, +15 failures, +10 low parts).");
        subtitle.getStyleClass().add("section-description");
        header.getChildren().addAll(title, subtitle);

        topRow.getChildren().addAll(header);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Live Risk Matrix (v_risk_intelligence_summary)
        Tab tabRisk = new Tab("🧠 Component Risk Intelligence Matrix");
        VBox riskBox = new VBox(10);
        riskBox.setPadding(new Insets(10, 0, 0, 0));

        TableView<RiskSummary> tblRisk = new TableView<>();
        TableColumn<RiskSummary, String> colRSerial = new TableColumn<>("Component Serial");
        colRSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        TableColumn<RiskSummary, String> colRType = new TableColumn<>("Type");
        colRType.setCellValueFactory(new PropertyValueFactory<>("componentType"));
        TableColumn<RiskSummary, String> colRAir = new TableColumn<>("Installed Aircraft");
        colRAir.setCellValueFactory(new PropertyValueFactory<>("installedAircraft"));
        TableColumn<RiskSummary, Double> colRHours = new TableColumn<>("Hours Used");
        colRHours.setCellValueFactory(new PropertyValueFactory<>("lifetimeHoursUsed"));
        TableColumn<RiskSummary, Double> colRLimit = new TableColumn<>("Service Limit");
        colRLimit.setCellValueFactory(new PropertyValueFactory<>("serviceLimitHours"));
        TableColumn<RiskSummary, Integer> colRScore = new TableColumn<>("Risk Score");
        colRScore.setCellValueFactory(new PropertyValueFactory<>("riskScore"));
        TableColumn<RiskSummary, String> colRLevel = new TableColumn<>("Risk Level");
        colRLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        TableColumn<RiskSummary, String> colRExpl = new TableColumn<>("Explainable Score Points Breakdown");
        colRExpl.setCellValueFactory(new PropertyValueFactory<>("riskExplanation"));

        tblRisk.getColumns().addAll(colRSerial, colRType, colRAir, colRHours, colRLimit, colRScore, colRLevel, colRExpl);
        tblRisk.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblRisk, Priority.ALWAYS);
        try {
            tblRisk.setItems(FXCollections.observableArrayList(inventoryRiskService.getRiskIntelligenceSummary()));
        } catch (Exception ignored) {}

        Button btnExplain = new Button("🔍 Run Live Stored Procedure: sp_generate_risk_score");
        btnExplain.getStyleClass().add("btn-primary");
        btnExplain.setOnAction(e -> {
            RiskSummary sel = tblRisk.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showWarning("Selection Required", "Select a component row to run sp_generate_risk_score.");
                return;
            }
            try {
                RiskSummary result = inventoryRiskService.calculateRiskForComponent(sel.getComponentId());
                showInfo("Risk Score Calculated via Stored Procedure",
                    "Component: " + sel.getSerialNumber() + "\n"
                    + "Calculated Risk Score: " + result.getRiskScore() + " / 100\n"
                    + "Risk Category: " + result.getRiskLevel() + "\n\n"
                    + "Explainable Factor Breakdown:\n" + result.getRiskExplanation()
                );
            } catch (SQLException ex) {
                showError("Procedure Error", ex.getMessage());
            }
        });

        riskBox.getChildren().addAll(tblRisk, btnExplain);
        tabRisk.setContent(riskBox);

        // Tab 2: System Alerts
        Tab tabAlerts = new Tab("🔔 System Operational Alerts");
        VBox alertBox = new VBox(10);
        alertBox.setPadding(new Insets(10, 0, 0, 0));

        TableView<com.aircraft.model.Alert> tblAlerts = new TableView<>();
        TableColumn<com.aircraft.model.Alert, Integer> colAId = new TableColumn<>("ID");
        colAId.setCellValueFactory(new PropertyValueFactory<>("alertId"));
        colAId.setPrefWidth(50);
        TableColumn<com.aircraft.model.Alert, String> colAType = new TableColumn<>("Alert Type");
        colAType.setCellValueFactory(new PropertyValueFactory<>("alertType"));
        TableColumn<com.aircraft.model.Alert, String> colASeverity = new TableColumn<>("Severity");
        colASeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        TableColumn<com.aircraft.model.Alert, String> colAMessage = new TableColumn<>("Message");
        colAMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        TableColumn<com.aircraft.model.Alert, Object> colATime = new TableColumn<>("Created At");
        colATime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        TableColumn<com.aircraft.model.Alert, String> colAStatus = new TableColumn<>("Status");
        colAStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblAlerts.getColumns().addAll(colAId, colAType, colASeverity, colAMessage, colATime, colAStatus);
        tblAlerts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblAlerts, Priority.ALWAYS);
        try {
            tblAlerts.setItems(FXCollections.observableArrayList(inventoryRiskService.getAllAlerts()));
        } catch (Exception ignored) {}

        HBox alertActions = new HBox(12);
        Button btnAck = new Button("Acknowledge Alert");
        btnAck.getStyleClass().add("btn-secondary");
        btnAck.setOnAction(e -> {
            com.aircraft.model.Alert sel = tblAlerts.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    inventoryRiskService.updateAlertStatus(sel.getAlertId(), "ACKNOWLEDGED");
                    showAlertsView();
                } catch (Exception ex) { showError("Error", ex.getMessage()); }
            }
        });

        Button btnResAlert = new Button("Resolve Alert");
        btnResAlert.getStyleClass().add("btn-success");
        btnResAlert.setOnAction(e -> {
            com.aircraft.model.Alert sel = tblAlerts.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    inventoryRiskService.updateAlertStatus(sel.getAlertId(), "RESOLVED");
                    showAlertsView();
                } catch (Exception ex) { showError("Error", ex.getMessage()); }
            }
        });

        alertActions.getChildren().addAll(btnAck, btnResAlert);
        alertBox.getChildren().addAll(tblAlerts, alertActions);
        tabAlerts.setContent(alertBox);

        tabPane.getTabs().addAll(tabRisk, tabAlerts);
        root.getChildren().addAll(topRow, tabPane);
        contentContainer.getChildren().setAll(root);
    }

    // Helper alerts
    private void showInfo(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
