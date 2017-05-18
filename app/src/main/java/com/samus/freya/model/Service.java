package com.samus.freya.model;

/**
 * Created by samus on 10.11.2016.
 * Service class used to represent a service row in DB
 */

public class Service {

    private int id; // unique id to identify service in db
    private String desc; // few letter description of the service
    private float val = 0.0f; // amount of workhours for this job
    private int def = 1; // 1 if standard job, 0 otherwise; used to seperate jobs in 2 categories
    private int spe = 0; // only used for vacation, maybe also good for Ng, represents user specific wh
    private int ena = 1; // 1 if still active account or 0 for for past references

    public Service () { } // empyt constructor for short writing style
    public Service(int id, String desc, float val, int def, int spe, int ena) { // used for few compact lines
        this.id = id;
        this.desc = desc;
        this.val = val;
        this.def = def;
        this.spe = spe;
        this.ena = ena;
    }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDesc(String desc) { this.desc = desc; }
    public void setVal(float val) { this.val = val; }
    public void setDef(int def) { this.def = def; }
    public void setSpe(int spe) { this.spe = spe; }
    public void setEna(int ena) { this.ena = ena; }

    // Getters
    public int getId() { return this.id; }
    public String getDesc() { return this.desc; }
    public float getVal() { return this.val; }
    public int getDef() { return this.def; }
    public boolean getSpe() { return spe==1; }
    public int getEna() { return ena; }

    // used for finding and comparing
    @Override
    public boolean equals(Object object){
        boolean same = false;

        if (object != null && object instanceof Service) {
            same = this.id == ((Service) object).getId();
        }

        return same;
    }
}
