package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;

import java.util.List;

@Dao
public abstract class PlaceDAO {
    @Query("SELECT * FROM place")
    public abstract List<Place> getAll();

    @Query("SELECT * FROM place WHERE id =:id")
    public abstract Place getPlace(long id);

    @Query("SELECT * FROM place WHERE name LIKE :placeName")
    public abstract Place getPlaceByName(String placeName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPlace(Place place);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertFloors(List<Floor> floors);

    @Query("SELECT * FROM floor WHERE place_id =:placeID")
    public abstract List<Floor> getFloorList(long placeID);


    @Query("UPDATE place SET mode =:mode WHERE id =:placeID")
    public abstract void updatePlaceMode(long placeID, int mode);


    @Query("DELETE from place WHERE id=:placeID")
    public abstract void removePlace(long placeID);

    public void insertPlaceWithFloors(Place place) {
        List<Floor> floors= place.getFloors();
        for (int i = 0; i < floors.size(); i++) {
            floors.get(i).setPlaceID(place.getId());
        }
        insertFloors(floors);
        insertPlace(place);
    }

    public Place getPlaceWIthFloors(long id) {
        Place place = getPlace(id);
        List<Floor> floors = getFloorList(id);
        if(place != null) {
            place.setFloors(floors);
        }
        return place;
    }

    public void removePlaceWithFloors(Place place){
        List<Floor> placeFloors= MySettings.getPlaceFloors(place.getId());
        if(placeFloors != null && placeFloors.size() >= 1){
            for (Floor floor:placeFloors) {
                MySettings.removeFloor(floor);
            }
        }
        removePlace(place.getId());
    }
}
