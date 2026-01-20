package com.ufony.trackersdk;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;


public class UserResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private String email;
    private String dateOfBirth;
    private String sex;
    private String firstName;
    private String middleName;
    private String lastName;
    private long id;
    private String imageUrl;
    private String largeImageUrl;
    private String role;
    private List<Integer> permissions;
    private List<String> ignoreRoles;
    private List<Long> ignoreUsers;
    private Address address;
    private String locale;
    private float mapMaxZoomLevel;
    private long schoolBranchId;
    private ArrayList<String> paymentGateways;
    private String membershipStatus;
    private boolean showControlNumber;
    private List<PaymentGatewaysV2> paymentGatewaysV2;
    private String signalRRootUrl;
    @Nullable
    private String signalRCoreUrl;
    private int timeZoneOffset;
    private boolean isCollege;

    @Nullable
    private String vcBaseUrl;

    public String getSignalRRootUrl() {
        return signalRRootUrl;
    }

    public void setSignalRRootUrl(String signalRRootUrl) {
        this.signalRRootUrl = signalRRootUrl;
    }

    @Nullable
    public String getSignalRCoreUrl() {
        return signalRCoreUrl;
    }

    public void setSignalRCoreUrl(@Nullable String signalRCoreUrl) {
        this.signalRCoreUrl = signalRCoreUrl;
    }

    public static class PaymentGatewaysV2 implements Serializable {
        String paymentGateway;
        String webUrl;
        String returnUrl;
        Integer emiAmount;
        Long id;

        public String getPaymentGateway() {
            return paymentGateway;
        }

        public void setPaymentGateway(String paymentGateway) {
            this.paymentGateway = paymentGateway;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public void setWebUrl(String webUrl) {
            this.webUrl = webUrl;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }

        public Integer getEmiAmount() {
            return emiAmount;
        }

        public void setEmiAmount(Integer emiAmount) {
            this.emiAmount = emiAmount;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public List<PaymentGatewaysV2> getPaymentGatewaysV2() {
        return paymentGatewaysV2;
    }

    public void setPaymentGatewaysV2(List<PaymentGatewaysV2> paymentGatewaysV2) {
        this.paymentGatewaysV2 = paymentGatewaysV2;
    }

    public boolean isCollege() {
        return isCollege;
    }

    public void setCollege(boolean college) {
        isCollege = college;
    }

    public static class Address implements Serializable {
        private double latitude;
        private double longitude;
        private String street;
        private String city;
        private String state;
        private String zip;
        private String country;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    public List<Long> getIgnoreUsers() {
        return ignoreUsers;
    }

    public void setIgnoreUsers(List<Long> ignoreUsers) {
        this.ignoreUsers = ignoreUsers;
    }

    private List<AcademicYears> academicYears;


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @SerializedName(value = "phone")
    private Phone phone = new Phone();

    public static class Phone implements Serializable {
        private static final long serialVersionUID = 1L;
        private String countryCode;
        private String nationalNumber;

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getNationalNumber() {
            return nationalNumber;
        }

        public void setNationalNumber(String nationalNumber) {
            this.nationalNumber = nationalNumber;
        }
    }

    public static class AcademicYears implements Serializable {
        private static final long serialVersionUID = 1L;

        private long id;
        private String label;
        private String fromDate;
        private String toDate;
        private boolean isCurrentYear;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getFromDate() {
            return fromDate;
        }

        public void setFromDate(String fromDate) {
            this.fromDate = fromDate;
        }

        public String getToDate() {
            return toDate;
        }

        public void setToDate(String toDate) {
            this.toDate = toDate;
        }

        public boolean isCurrentYear() {
            return isCurrentYear;
        }

        public void setCurrentYear(boolean currentYear) {
            isCurrentYear = currentYear;
        }
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() { return middleName; }

    public void setMiddleName(String middleName) { this.middleName = middleName; }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }


    public List<AcademicYears> getAcademicYears() {
        return academicYears;
    }

    public void setAcademicYears(List<AcademicYears> academicYears) {
        this.academicYears = academicYears;
    }

    public List<String> getIgnoreRoles() {
        return ignoreRoles;
    }

    public void setIgnoreRoles(List<String> ignoreRoles) {
        this.ignoreRoles = ignoreRoles;
    }
    public List<Integer> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Integer> permissions) {
        this.permissions = permissions;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public float getMapMaxZoomLevel() { return mapMaxZoomLevel; }

    public void setMapMaxZoomLevel(float mapMaxZoomLevel) { this.mapMaxZoomLevel = mapMaxZoomLevel; }

    public long getSchoolBranchId() {
        return schoolBranchId;
    }

    public void setSchoolBranchId(long schoolBranchId) {
        this.schoolBranchId = schoolBranchId;
    }

    public ArrayList<String> getPaymentGateways() {
        return paymentGateways;
    }

    public void setPaymentGateways(ArrayList<String> paymentGateways) {
        this.paymentGateways = paymentGateways;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public boolean getShowControlNumber() {
        return showControlNumber;
    }

    public void setShowControlNumber(boolean showControlNumber) {
        this.showControlNumber = showControlNumber;
    }

    public int getTimeZoneOffset() { return timeZoneOffset; }

    public void setTimeZoneOffset(int timeZoneOffset) { this.timeZoneOffset = timeZoneOffset; }

    @Nullable
    public String getVcBaseUrl() {
        return vcBaseUrl;
    }

    public void setVcBaseUrl(@Nullable  String vcBaseUrl) {
        this.vcBaseUrl = vcBaseUrl;
    }
}

