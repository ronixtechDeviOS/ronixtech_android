package com.ronixtech.ronixhome.entities;

public class Count {
    public static int count = 0;

    public static synchronized void increment(){
        count = count + 1;
    }

    public static synchronized void reset(){
        count = 1;
    }
}
