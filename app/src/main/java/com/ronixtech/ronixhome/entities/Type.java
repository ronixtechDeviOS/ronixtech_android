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

    public Type(){
        this.categoryID = 0;
        this.id = -1;
        this.name = "";
        this.imageUrl = "";
        this.imageResourceID = 0;
        this.imageResourceName = "";
    }

    public Type(int categoryID, String typeName, String typeImageUrl, int typeImageResourceID, String imageResourceName){
        this.categoryID = categoryID;
        this.name = typeName;
        this.imageUrl = typeImageUrl;
        this.imageResourceID = typeImageResourceID;
        this.imageResourceName = imageResourceName;
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
}
