package com.samus.freya.model;

/**
 * Created by samus on 10.11.2016.
 * Month class used to represent a month row in DB
 */

public class Month {

    private int id; // unique id to identify month in db
    private int year; // Year for this momth
    private int month; // Month number in this year
    private int full = 0; // 1 if all contacts have the req hours

    // German names for all months
    public static String[] name = {"Jänner", "Februar", "März", "April", "Mai", "Juni",
    "Juli", "August", "September", "Oktober", "November", "Dezember"};

    // only empty constructor needed
    public  Month () {}

    // Setters
    public void setId(int id) { this.id = id; }
    public void setYear(int year) { this.year = year; }
    public void setMonth(int month) { this.month = month; }
    public void setFull(int full) { this.full = full; }

    // Getters
    public int getId() { return id; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getFull() { return full; }

    // to get a displayable name for this month
    @Override
    public String toString() { return name[month-1] + " " + year; }

    // used for finding and comparing
    @Override
    public boolean equals(Object object){
        boolean same = false;

        if (object != null && object instanceof Month) {
            same = this.id == ((Month) object).getId();
        }

        return same;
    }
}
