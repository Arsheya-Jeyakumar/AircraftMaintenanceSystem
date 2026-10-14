package com.aircraft.model;

public class Technician {
    private int technicianId;
    private String employeeNo;
    private String name;
    private String qualification;
    private String specialization;
    private String phone;
    private String status;

    public Technician() {}

    public Technician(int technicianId, String employeeNo, String name, String qualification,
                      String specialization, String phone, String status) {
        this.technicianId = technicianId;
        this.employeeNo = employeeNo;
        this.name = name;
        this.qualification = qualification;
        this.specialization = specialization;
        this.phone = phone;
        this.status = status;
    }

    public int getTechnicianId() { return technicianId; }
    public void setTechnicianId(int technicianId) { this.technicianId = technicianId; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return name + " (" + employeeNo + " - " + specialization + ")";
    }
}
