package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Type {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "category_id")
    public int categoryID;
    @ColumnInfo(name = "image_url")
    public String imageUrl;
    @ColumnInfo(name = "image_resource_id")
    public int imageResourceID;

    public Type(){
        this.categoryID = 0;
        this.id = -1;
        this.name = "";
        this.imageUrl = "";
        this.imageResourceID = 0;
    }

    public Type(int categoryID, String typeName, String typeImageUrl, int typeImageResourceID){
        this.categoryID = categoryID;
        this.name = typeName;
        this.imageUrl = typeImageUrl;
        this.imageResourceID = typeImageResourceID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
