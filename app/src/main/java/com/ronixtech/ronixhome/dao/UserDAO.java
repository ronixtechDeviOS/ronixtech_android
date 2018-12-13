package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.entities.User;

import java.util.List;

@Dao
public abstract class UserDAO {
    @Query("SELECT * FROM user")
    public abstract List<User> getAll();

    @Query("SELECT * FROM user WHERE id IN (:userIds)")
    public abstract List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :firstName LIMIT 1")
    public abstract User findByName(String firstName);

    @Query("SELECT * FROM user WHERE email LIKE :email LIMIT 1")
    public abstract User findByMEmail(String email);

    @Query("SELECT * FROM user WHERE linked_account = 1")
    public abstract List<User> getAllLinkedAccounts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(User... users);

    @Delete
    public abstract void delete(User user);
}