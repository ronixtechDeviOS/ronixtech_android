package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.entities.Type;

import java.util.List;

@Dao
public abstract class TypeDAO {
    @Query("SELECT * FROM type")
    public abstract List<Type> getAll();

    @Query("SELECT * FROM type WHERE category_id =:category")
    public abstract List<Type> getTypes(int category);

    @Query("SELECT * FROM type WHERE id =:id")
    public abstract Type getType(long id);

    @Query("SELECT * FROM type WHERE name =:name")
    public abstract Type getTypeByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertType(Type type);

    @Query("DELETE from type WHERE id=:typeID")
    public abstract void removeType(long typeID);
}