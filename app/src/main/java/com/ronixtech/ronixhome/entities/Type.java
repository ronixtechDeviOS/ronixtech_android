package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Type {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "category_id")
    private int categoryID;
    @ColumnInfo(name = "image_url")
    private String imageUrl;
    @ColumnInfo(name = "image_resource_id")
    private int imageResourceID;
    @ColumnInfo(name = "image_resource_name")
    private String imageResourceName;
    @ColumnInfo(name = "background_image_url")
    private String backgroundImageUrl;
    @ColumnInfo(name = "background_image_resource_id")
    private int backgroundImageResourceID;
    @ColumnInfo(name = "background_image_resource_name")
    private String backgroundImageResourceName;
    @ColumnInfo(name = "color_hex_code")
    private String colorHexCode;

    public Type(){
        this.id = 0;
        this.categoryID = 0;
        this.name = "";
        this.imageUrl = "";
        this.imageResourceID = 0;
        this.imageResourceName = "";
        this.backgroundImageUrl = "";
        this.backgroundImageResourceID = 0;
        this.backgroundImageResourceName = "";
        this.colorHexCode = "";
    }

    public Type(int categoryID, String typeName, String typeImageUrl, int typeImageResourceID, String imageResourceName, String colorHexCode){
        this.id = 0;
        this.categoryID = categoryID;
        this.name = typeName;
        this.imageUrl = typeImageUrl;
        this.imageResourceID = typeImageResourceID;
        this.imageResourceName = imageResourceName;
        this.backgroundImageUrl = "";
        this.backgroundImageResourceID = 0;
        this.backgroundImageResourceName = "";
        this.colorHexCode = colorHexCode;
    }

    public Type(int categoryID, String typeName, String typeImageUrl, int typeImageResourceID, String imageResourceName, String backgroundImageUrl, int backgroundImageResourceID, String backgroundImageResourceName, String colorHexCode){
        this.id = 0;
        this.categoryID = categoryID;
        this.name = typeName;
        this.imageUrl = typeImageUrl;
        this.imageResourceID = typeImageResourceID;
        this.imageResourceName = imageResourceName;
        this.backgroundImageUrl = backgroundImageUrl;
        this.backgroundImageResourceID = backgroundImageResourceID;
        this.backgroundImageResourceName = backgroundImageResourceName;
        this.colorHexCode = colorHexCode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageResourceID() {
        return imageResourceID;
    }

    public void setImageResourceID(int imageResourceID) {
        this.imageResourceID = imageResourceID;
    }

    public String getImageResourceName() {
        return imageResourceName;
    }

    public void setImageResourceName(String imageResourceName) {
        this.imageResourceName = imageResourceName;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    public int getBackgroundImageResourceID() {
        return backgroundImageResourceID;
    }

    public void setBackgroundImageResourceID(int backgroundImageResourceID) {
        this.backgroundImageResourceID = backgroundImageResourceID;
    }

    public String getBackgroundImageResourceName() {
        return backgroundImageResourceName;
    }

    public void setBackgroundImageResourceName(String backgroundImageResourceName) {
        this.backgroundImageResourceName = backgroundImageResourceName;
    }

    public String getColorHexCode() {
        return colorHexCode;
    }

    public void setColorHexCode(String colorHexCode) {
        this.colorHexCode = colorHexCode;
    }
}
