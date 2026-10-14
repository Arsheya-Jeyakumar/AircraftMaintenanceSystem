package com.aircraft.model;

import java.time.LocalDateTime;

public class Alert {
    private int alertId;
    private Integer aircraftId;
    private Integer componentId;
    private String alertType;
    private String severity;
    private String message;
    private LocalDateTime createdAt;
    private String status;

    // Joined reporting fields
    private String aircraftRegistration;
    private String componentSerial;

    public Alert() {}

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }

    public Integer getAircraftId() { return aircraftId; }
    public void setAircraftId(Integer aircraftId) { this.aircraftId = aircraftId; }

    public Integer getComponentId() { return componentId; }
    public void setComponentId(Integer componentId) { this.componentId = componentId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAircraftRegistration() { return aircraftRegistration; }
    public void setAircraftRegistration(String aircraftRegistration) { this.aircraftRegistration = aircraftRegistration; }

    public String getComponentSerial() { return componentSerial; }
    public void setComponentSerial(String componentSerial) { this.componentSerial = componentSerial; }
}
