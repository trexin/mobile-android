package com.trexin.model;

public class DashboardItem {
    private String name;
    private String location;
    private boolean formSubmitted;
    private boolean formApproved;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isFormSubmitted() {
        return this.formSubmitted;
    }

    public void setFormSubmitted(boolean formSubmitted) {
        this.formSubmitted = formSubmitted;
    }

    public boolean isFormApproved() {
        return this.formApproved;
    }

    public void setFormApproved(boolean formApproved) {
        this.formApproved = formApproved;
    }
}
