package com.ufony.trackersdk;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Child implements Serializable {

    private static final long serialVersionUID = 1L;
    private long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String dateOfBirth;
    private boolean isInDayCare, isRTEStudent;

    //private int status;
    private String status;
    private int statusProfile;

    private String enrollmentNumber;
    private String externalReferenceStatus;

    private String allergy;
    private String checkInTime;
    private String checkOutTime;

    private Route inRoute;
    private Route outRoute;

    private String imageUrl;
    private String largeImageUrl;

    private String imagePath;
    private int rollNumber;

    private RouteStop inRouteStop;
    private RouteStop outRouteStop;

    private Long inRouteAlertDistance;
    private Long outRouteAlertDistance;

    private String bloodGroup;
    private String enrollmentDate;

    private String gender;
    private String scholarNumber;
    private String controlNumber;
    private String aadharNumber;
    private String sssmNumber;
    private String house;

    private Long inRouteId;
    private Long outRouteId;
    private String motherName;
    private String fatherName;

    public Child() {
    }

    public Child(long id, String firstName, String lastName, String fullName, String dateOfBirth, String status, String allergy, String checkInTime, String checkOutTime, int parentId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
         this.status = status;
        this.allergy = allergy;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    /**
     * @return the inRoute
     */
    public Route getInRoute() {
        return inRoute;
    }
    /**
     * @param inRoute the inRoute to set
     */
    public void setInRoute(Route inRoute) {
        this.inRoute = inRoute;
    }
    /**
     * @return the outRoute
     */
    public Route getOutRoute() {
        return outRoute;
    }
    /**
     * @param outRoute the outRoute to set
     */
    public void setOutRoute(Route outRoute) {
        this.outRoute = outRoute;
    }
    public String getAllergy() {
        return allergy;
    }
    public void setAllergy(String allergy) {
        this.allergy = allergy;
    }
    public String getCheckInTime() {
        return checkInTime;
    }
    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }
    public String getCheckOutTime() {
        return checkOutTime;
    }
    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
    /*public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }*/
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getDateOfBirth() {return dateOfBirth;}
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusProfile() {
        return statusProfile;
    }

    public void setStatusProfile(int statusProfile) {
        this.statusProfile = statusProfile;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getExternalReferenceStatus() {
        return externalReferenceStatus;
    }

    public void setExternalReferenceStatus(String externalReferenceStatus) {
        this.externalReferenceStatus = externalReferenceStatus;
    }

    public boolean isInDayCare() {
        return isInDayCare;
    }

    public void setInDayCare(boolean inDayCare) {
        isInDayCare = inDayCare;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
    }

    public RouteStop getInRouteStop() {
        return inRouteStop;
    }

    public void setInRouteStop(RouteStop inRouteStop) {
        this.inRouteStop = inRouteStop;
    }

    public RouteStop getOutRouteStop() {
        return outRouteStop;
    }

    public void setOutRouteStop(RouteStop outRouteStop) {
        this.outRouteStop = outRouteStop;
    }

    public Long getInRouteAlertDistance() { return inRouteAlertDistance; }

    public void setInRouteAlertDistance(Long inRouteAlertDistance) { this.inRouteAlertDistance = inRouteAlertDistance; }

    public Long getOutRouteAlertDistance() { return outRouteAlertDistance; }

    public void setOutRouteAlertDistance(Long outRouteAlertDistance) { this.outRouteAlertDistance = outRouteAlertDistance; }

    public String getBloodGroup() { return bloodGroup; }

    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getControlNumber() {
        return controlNumber;
    }

    public void setControlNumber(String controlNumber) {
        this.controlNumber = controlNumber;
    }

    public String getScholarNumber() {
        return scholarNumber;
    }

    public void setScholarNumber(String scholarNumber) {
        this.scholarNumber = scholarNumber;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getSssmNumber() {
        return sssmNumber;
    }

    public void setSssmNumber(String sssmNumber) {
        this.sssmNumber = sssmNumber;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public boolean isRTEStudent() {
        return isRTEStudent;
    }

    public void setRTEStudent(boolean RTEStudent) {
        isRTEStudent = RTEStudent;
    }

    public String getMiddleName() { return middleName;}

    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public Long getInRouteId() { return inRouteId; }

    public void setInRouteId(Long inRouteId) { this.inRouteId = inRouteId; }

    public Long getOutRouteId() { return outRouteId; }

    public void setOutRouteId(Long outRouteId) { this.outRouteId = outRouteId; }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }
}


class Route implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;

    private String name;

    private String vehicleLicenseNumber;

    private Coordinate startLocation;

    private Coordinate endLocation;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the vehicleLicenseNumber
     */
    public String getVehicleLicenseNumber() {
        return vehicleLicenseNumber;
    }

    /**
     * @param vehicleLicenseNumber the vehicleLicenseNumber to set
     */
    public void setVehicleLicenseNumber(String vehicleLicenseNumber) {
        this.vehicleLicenseNumber = vehicleLicenseNumber;
    }

    /**
     * @return the startLocation
     */
    public Coordinate getStartLocation() {
        return startLocation;
    }

    /**
     * @param startLocation the startLocation to set
     */
    public void setStartLocation(Coordinate startLocation) {
        this.startLocation = startLocation;
    }

    /**
     * @return the endLocation
     */
    public Coordinate getEndLocation() {
        return endLocation;
    }

    /**
     * @param endLocation the endLocation to set
     */
    public void setEndLocation(Coordinate endLocation) {
        this.endLocation = endLocation;
    }

}

