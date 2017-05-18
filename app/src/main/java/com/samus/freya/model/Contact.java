package com.samus.freya.model;

/**
 * Created by samus on 10.11.2016.
 * Contact class used to represent a contact entity in DB
 */

public class Contact {

    private int id = -1; // unique id to identify contact in db
    private String name; // Name of worker
    private float wh = 10.0f; // weekly work-hours
    private int enabled = 1; // 1 if still active account or 0 for for past references

    public Contact() {} // empyt constructor for short writing style
    public Contact(int id, String name, float wh, int enabled) {
        this.id = id;
        this.name = name;
        this.wh = wh;
        this.enabled = enabled;
    }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setWh(float wh) { this.wh = wh; }
    public void setEnabled(int enabled) { this.enabled = enabled; }

    // Getters
    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public String getSName() { // returns a shortened name used to save space
        String tmp;
        if (this.name.split(" ").length == 2) {
            int pos = this.name.indexOf(" ");
            tmp = this.name.substring(0, pos+2)+".";
        }
        else tmp = this.name.split(" ")[0];
        if (tmp.length() > 12) return tmp.substring(0, 12);
        else return tmp;
    }
    public float getWh() { return this.wh; }
    public int getEnabled() { return this.enabled; }

    // used for finding and comparing
    @Override
    public boolean equals(Object object){
        boolean same = false;

        if (object != null && object instanceof Contact) {
            same = this.id == ((Contact) object).getId();
        }

        return same;
    }
}