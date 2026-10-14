package com.aircraft.model;

import java.time.LocalDate;

public class Failure {
    private int failureId;
    private int aircraftId;
    private int componentId;
    private LocalDate failureDate;
    private String failureType;
    private String severity;
    private String description;
    private boolean resolved;

    // Joined reporting fields
    private String aircraftRegistration;
    private String componentSerial;
    private String componentType;
    private int incidentCount; // For recurring failure reports

    public Failure() {}

    public int getFailureId() { return failureId; }
    public void setFailureId(int failureId) { this.failureId = failureId; }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }

    public LocalDate getFailureDate() { return failureDate; }
    public void setFailureDate(LocalDate failureDate) { this.failureDate = failureDate; }

    public String getFailureType() { return failureType; }
    public void setFailureType(String failureType) { this.failureType = failureType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getAircraftRegistration() { return aircraftRegistration; }
    public void setAircraftRegistration(String aircraftRegistration) { this.aircraftRegistration = aircraftRegistration; }

    public String getComponentSerial() { return componentSerial; }
    public void setComponentSerial(String componentSerial) { this.componentSerial = componentSerial; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public int getIncidentCount() { return incidentCount; }
    public void setIncidentCount(int incidentCount) { this.incidentCount = incidentCount; }
}
