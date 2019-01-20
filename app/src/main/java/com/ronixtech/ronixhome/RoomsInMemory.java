package com.ronixtech.ronixhome;

import com.ronixtech.ronixhome.entities.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomsInMemory {
    private static List<Room> rooms;
    static {
        rooms = new ArrayList<>();
    }

    public static void setRooms(List<Room> newRooms){
        rooms.clear();
        rooms.addAll(newRooms);
    }

    public static List<Room> getRooms(){
        return rooms;
    }

    public static void updateDevice(Room room){
        int index = rooms.indexOf(room);
        if(index != -1 && index < rooms.size()){
            rooms.set(index, room);
        }
    }

    public static Room getRoom(Room room){
        int index = rooms.indexOf(room);
        if(index != -1 && index < rooms.size()){
            return rooms.get(index);
        }
        return null;
    }

    public static void removeRoom(Room room){
        rooms.remove(room);
    }
}
