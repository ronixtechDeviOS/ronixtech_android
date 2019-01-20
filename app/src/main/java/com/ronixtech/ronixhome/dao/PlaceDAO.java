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


    @Query("SELECT * FROM floor WHERE place_id =:placeID")
    public abstract List<Floor> getFloorList(long placeID);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPlace(Place place);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertFloors(List<Floor> floors);


    @Query("UPDATE place SET mode =:mode WHERE id =:placeID")
    public abstract void updatePlaceMode(long placeID, int mode);

    @Query("UPDATE place SET name =:newName WHERE id =:placeID")
    public abstract void updatePlaceName(long placeID, String newName);

    @Query("UPDATE place SET type_id =:newTypeID WHERE id =:placeID")
    public abstract void updatePlaceType(long placeID, long newTypeID);

    @Query("UPDATE place SET type_name =:newTypeName WHERE id =:placeID")
    public abstract void updatePlaceType(long placeID, String newTypeName);

    @Query("UPDATE place SET latitude =:newLatitude WHERE id =:placeID")
    public abstract void updatePlaceLatitude(long placeID, double newLatitude);

    @Query("UPDATE place SET longitude =:newLongitude WHERE id =:placeID")
    public abstract void updatePlaceLongitude(long placeID, double newLongitude);

    @Query("UPDATE place SET address =:newAddress WHERE id =:placeID")
    public abstract void updatePlaceAddress(long placeID, String newAddress);

    @Query("UPDATE place SET city =:newCity WHERE id =:placeID")
    public abstract void updatePlaceCity(long placeID, String newCity);

    @Query("UPDATE place SET state =:newState WHERE id =:placeID")
    public abstract void updatePlaceState(long placeID, String newState);

    @Query("UPDATE place SET country =:newCountry WHERE id =:placeID")
    public abstract void updatePlaceCountry(long placeID, String newCountry);

    @Query("UPDATE place SET zip_code =:newZipCode WHERE id =:placeID")
    public abstract void updatePlaceZipCode(long placeID, String newZipCode);



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
