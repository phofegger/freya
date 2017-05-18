package com.samus.freya.model;

/**
 * Created by samus on 10.11.2016.
 * Day class used to represent a day entity in DB
 */

public class Day {

    private int id; // unique id to identify day in db
    private int month; // month_id which associates the day with the
    private int date; // date of the day in the month

    // only empty constructor needed
    public Day() { }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setMonth(int month) { this.month = month; }
    public void setDate(int date) { this.date = date; }

    // Getters
    public int getId() { return this.id; }
    public int getMonth() { return this.month; }
    public int getDate() { return this.date; }

    // used for finding and comparing
    @Override
    public boolean equals(Object object){
        boolean same = false;

        if (object != null && object instanceof Day) {
            same = this.id == ((Day) object).getId();
        }

        return same;
    }
}
