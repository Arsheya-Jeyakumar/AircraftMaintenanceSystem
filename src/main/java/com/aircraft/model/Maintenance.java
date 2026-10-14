package com.aircraft.model;

import java.time.LocalDate;

public class Maintenance {
    private int maintenanceId;
    private int aircraftId;
    private Integer componentId;
    private String maintenanceType;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String description;
    private String status;
    private Integer technicianId;

    // Joined reporting fields
    private String aircraftRegistration;
    private String aircraftModel;
    private String componentSerial;
    private String componentType;
    private String technicianName;
    private String technicianEmployeeNo;
    private int daysOverdue;

    public Maintenance() {}

    public int getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(int maintenanceId) { this.maintenanceId = maintenanceId; }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public Integer getComponentId() { return componentId; }
    public void setComponentId(Integer componentId) { this.componentId = componentId; }

    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTechnicianId() { return technicianId; }
    public void setTechnicianId(Integer technicianId) { this.technicianId = technicianId; }

    public String getAircraftRegistration() { return aircraftRegistration; }
    public void setAircraftRegistration(String aircraftRegistration) { this.aircraftRegistration = aircraftRegistration; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public String getComponentSerial() { return componentSerial; }
    public void setComponentSerial(String componentSerial) { this.componentSerial = componentSerial; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public String getTechnicianEmployeeNo() { return technicianEmployeeNo; }
    public void setTechnicianEmployeeNo(String technicianEmployeeNo) { this.technicianEmployeeNo = technicianEmployeeNo; }

    public int getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(int daysOverdue) { this.daysOverdue = daysOverdue; }
}
