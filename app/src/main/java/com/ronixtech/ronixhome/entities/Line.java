package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

//This is an entity that represents a specific AC line
@Entity
public class Line {
    public static final int LINE_STATE_OFF = 0;
    public static final int LINE_STATE_ON = 1;
    public static final int LINE_STATE_PROCESSING = 2;
    public static final int DIMMING_STATE_OFF = 0;
    public static final int DIMMING_STATE_ON = 1;
    public static final int DIMMING_STATE_PROCESSING = 2;

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
    @ColumnInfo(name = "type_id")
    int lineType;
    @ColumnInfo(name = "type_name")
    String lineTypeString;
    @ColumnInfo(name = "tyoe_image_url")
    String lineTypeImageUrl;
    @ColumnInfo(name = "power_usage")
    double linePowerUsage;

    public Line(){
        this.id = 0;
        this.position = -1;
        this.name = "";
        this.powerState = Line.LINE_STATE_OFF;
        this.dimmingState = Line.DIMMING_STATE_OFF;
        this.dimmingVvalue = 0;
        this.deviceID = -1;
        this.lineType = -1;
        this.lineTypeString = "";
        this.lineTypeImageUrl = "";
        this.linePowerUsage = 0;
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

    public int getLineType() {
        return lineType;
    }

    public void setLineType(int lineType) {
        this.lineType = lineType;
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