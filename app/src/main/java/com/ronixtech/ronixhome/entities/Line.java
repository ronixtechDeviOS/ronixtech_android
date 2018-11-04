package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ronixtech.ronixhome.MySettings;

//This is an entity that represents a specific AC line
@Entity
public class Line {
    public static final int LINE_STATE_OFF = 0;
    public static final int LINE_STATE_ON = 1;
    public static final int LINE_STATE_PROCESSING = 2;
    public static final int DIMMING_STATE_OFF = 0;
    public static final int DIMMING_STATE_ON = 1;
    public static final int DIMMING_STATE_PROCESSING = 2;
    public static final int MODE_PRIMARY = 0;
    public static final int MODE_SECONDARY = 1;

    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "position")
    int position;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "power_state")
    int powerState;
    @ColumnInfo(name = "dimming_state")
    int dimmingState;
    @ColumnInfo(name = "dimming_value")
    int dimmingVvalue;
    @ColumnInfo(name = "device_id")
    long deviceID;
    @ColumnInfo(name = "type_name")
    String lineTypeString;
    @ColumnInfo(name = "tyoe_image_url")
    String lineTypeImageUrl;
    @ColumnInfo(name = "power_usage")
    double linePowerUsage;
    @ColumnInfo(name = "type_id")
    long typeID;
    @ColumnInfo(name = "mode")
    int mode;
    @ColumnInfo(name = "primary_device_chip_id")
    String primaryDeviceChipID;
    @ColumnInfo(name = "primary_line_position")
    int primaryLinePosition;


    public Line(){
        this.id = 0;
        this.position = -1;
        this.name = "";
        this.powerState = Line.LINE_STATE_OFF;
        this.dimmingState = Line.DIMMING_STATE_OFF;
        this.dimmingVvalue = 0;
        this.deviceID = -1;
        this.lineTypeString = "";
        this.lineTypeImageUrl = "";
        this.linePowerUsage = 0;
        this.typeID = -1;
        this.mode = Line.MODE_PRIMARY;
        this.primaryDeviceChipID = "";
        this.primaryLinePosition = -1;
    }

    public Line(Line line){
        this.id = line.getId();
        this.position = line.getPosition();
        this.name = line.getName();
        this.powerState = line.getPowerState();
        this.dimmingState = line.getDimmingState();
        this.dimmingVvalue = line.getDimmingVvalue();
        this.deviceID = line.getDeviceID();
        this.lineTypeString = line.getLineTypeString();
        this.lineTypeImageUrl = line.getLineTypeImageUrl();
        this.linePowerUsage = line.getLinePowerUsage();
        this.typeID = line.getTypeID();
        this.mode = line.getMode();
        this.primaryDeviceChipID = line.getPrimaryDeviceChipID();
        this.primaryLinePosition = line.getPrimaryLinePosition();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPowerState() {
        return powerState;
    }

    public void setPowerState(int powerState) {
        this.powerState = powerState;
    }

    public int getDimmingState() {
        return dimmingState;
    }

    public void setDimmingState(int dimmingState) {
        this.dimmingState = dimmingState;
    }

    public int getDimmingVvalue() {
        return dimmingVvalue;
    }

    public void setDimmingVvalue(int dimmingVvalue) {
        this.dimmingVvalue = dimmingVvalue;
    }

    public long getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(long deviceID) {
        this.deviceID = deviceID;
    }

    public String getLineTypeString() {
        return lineTypeString;
    }

    public void setLineTypeString(String lineTypeString) {
        this.lineTypeString = lineTypeString;
    }

    public String getLineTypeImageUrl() {
        return lineTypeImageUrl;
    }

    public void setLineTypeImageUrl(String lineTypeImageUrl) {
        this.lineTypeImageUrl = lineTypeImageUrl;
    }

    public double getLinePowerUsage() {
        return linePowerUsage;
    }

    public void setLinePowerUsage(double linePowerUsage) {
        this.linePowerUsage = linePowerUsage;
    }

    public long getTypeID(){
        return this.typeID;
    }

    public void setTypeID(long id){
        this.typeID = id;
    }

    public Type getType(){
        if(typeID != -1) {
            return MySettings.getType(typeID);
        }else{
            return MySettings.getTypeByName("LED Lamp");
        }
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getPrimaryDeviceChipID() {
        return primaryDeviceChipID;
    }

    public void setPrimaryDeviceChipID(String primaryDeviceChipID) {
        this.primaryDeviceChipID = primaryDeviceChipID;
    }

    public int getPrimaryLinePosition() {
        return primaryLinePosition;
    }

    public void setPrimaryLinePosition(int primaryLinePosition) {
        this.primaryLinePosition = primaryLinePosition;
    }

    @Override
    public boolean equals(Object object){
        Line line= (Line) object;
        if(line.getId() == this.id){
            return true;
        }else{
            return false;
        }
    }
}