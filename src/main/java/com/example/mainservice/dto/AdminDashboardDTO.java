package com.example.mainservice.dto;

public class AdminDashboardDTO{
    private long doctorCount;
    private long specialdoctorCount;
    private long patientCount;

    public AdminDashboardDTO(long doctorCount, long specialdoctorCount, long patientCount) {
        this.doctorCount = doctorCount;
        this.specialdoctorCount = specialdoctorCount;
        this.patientCount = patientCount;
    }

    public long getDoctorCount() {
        return doctorCount;
    }

    public long getSpecialDoctorCount() {
        return specialdoctorCount;
    }

    public long getPatientCount() {
        return patientCount;
    }


}
