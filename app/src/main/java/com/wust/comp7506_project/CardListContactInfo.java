package com.wust.comp7506_project;

public class CardListContactInfo {
    protected String exception = "undefined";
    protected String battery_location = "undefined";
    protected String battery_id = "undefined";
    protected String last_sync = "undefined";
    protected String warning = "undefined";
    protected Double soc = null;
    protected String state = null;
    protected Double temperature = null;
    protected Double electricity = null;
    protected Double voltage = null;

    protected static final String LOCATION_PREFIX = "Battery Location: ";
    protected static final String ID_PREFIX = "Battery ID: ";
    protected static final String SYNC_PREFIX = "Sync: ";
    protected static final String SOC_PREFIX = "%";
    protected static final String STATE_PREFIX = "";

    public CardListContactInfo(String battery_location, String battery_id, String last_sync, String warning, Double soc, String state, Double temperature, Double electricity, Double voltage, String exception){
        this.battery_location = battery_location;
        this.battery_id = battery_id;
        this.last_sync = last_sync;
        this.warning = warning;
        this.soc = soc;
        this.state = state;
        this.temperature = temperature;
        this.electricity = electricity;
        this.voltage = voltage;
        this.exception = exception;
    }
}
