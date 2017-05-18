package com.samus.freya.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import com.samus.freya.model.*;

/**
 * Created by samus on 10.11.2016.
 * TODO maybe make singleton
 */

public class DBHelper extends SQLiteOpenHelper {

    // General database information
    private static final String DATABASE_NAME = "MyDBName.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_CONTACT = "contacts";
    private static final String TABLE_MONTH = "month";
    private static final String TABLE_DAY = "day";
    private static final String TABLE_SERVICE = "services";
    private static final String TABLE_MONTH_CONTACT = "month_contact";
    private static final String TABLE_MONTH_SERVICE = "month_service";
    private static final String TABLE_DAY_SERVICE = "day_service";

    // CONTACTS table - column names
    private static final String CONTACTS_COLUMN_ID = "id";
    private static final String CONTACTS_COLUMN_NAME = "name";
    private static final String CONTACTS_COLUMN_WH = "wh";
    private static final String CONTACTS_COLUMN_ENABLED = "enabled";

    // MONTH table - column names
    private static final String MONTH_COLUMN_ID = "id";
    private static final String MONTH_COLUMN_YEAR = "year";
    private static final String MONTH_COLUMN_MONTH = "month";
    private static final String MONTH_COLUMN_FULL = "full";

    // DAY table - column names
    private static final String DAY_COLUMN_ID = "id";
    private static final String DAY_COLUMN_MONTH = "month_id";
    private static final String DAY_COLUMN_DATE = "date";

    // SERVICES table - column names
    private static final String SERVICES_COLUMN_ID = "id";
    private static final String SERVICES_COLUMN_DESC = "desc";
    private static final String SERVICES_COLUMN_VAL = "val";
    private static final String SERVICES_COLUMN_DEF = "def";
    private static final String SERVICES_COLUMN_SPE = "spe";
    private static final String SERVICES_COLUMN_ENA = "ena";

    // MONTH_CONTACT - column names
    private static final String MC_COLUMN_MONTH = "month_id";
    private static final String MC_COLUMN_CONTACT = "contact_id";
    private static final String MC_COLUMN_REQ = "req";

    // MONTH_SERVICE - column names
    private static final String MS_COLUMN_MONTH = "month_id";
    private static final String MS_COLUMN_SERVICE = "service_id";

    // DAY_SERVICE - column names
    private static final String DS_COLUMN_DAY = "day_id";
    private static final String DS_COLUMN_SERVICE = "service_id";
    private static final String DS_COLUMN_CONTACT = "contact_id";

    // Table create statements
    private static final String CREATE_TABLE_CONTACTS = "create table "
            + TABLE_CONTACT + "( " + CONTACTS_COLUMN_ID
            + " integer primary key autoincrement, " + CONTACTS_COLUMN_NAME
            + " text not null, " + CONTACTS_COLUMN_WH
            + " real not null, " + CONTACTS_COLUMN_ENABLED
            + " integer not null);";

    private static final String CREATE_TABLE_MONTH = "create table "
            + TABLE_MONTH + " ( " + MONTH_COLUMN_ID
            + " integer primary key autoincrement, " + MONTH_COLUMN_YEAR
            + " integer not null, " + MONTH_COLUMN_MONTH
            + " integer not null, " + MONTH_COLUMN_FULL
            + " integer not null);";

    private static final String CREATE_TABLE_DAY = "create table "
            + TABLE_DAY + " ( " + DAY_COLUMN_ID
            + " integer primary key autoincrement, " + DAY_COLUMN_MONTH
            + " integer, " + DAY_COLUMN_DATE
            + " integer not null, foreign key (" + DAY_COLUMN_MONTH
            + ") references " + TABLE_MONTH + "("
            + MONTH_COLUMN_ID + ") on delete cascade);";

    private static final String CREATE_TABLE_SERVICES = "create table "
            + TABLE_SERVICE + " ( " + SERVICES_COLUMN_ID
            + " integer primary key autoincrement, " + SERVICES_COLUMN_DESC
            + " text not null, " + SERVICES_COLUMN_DEF
            + " integer not null, " + SERVICES_COLUMN_VAL
            + " integer not null, " + SERVICES_COLUMN_ENA
            + " integer not null, " + SERVICES_COLUMN_SPE
            + " float not null);";

    private static final String CREATE_TABLE_MC = "create table "
            + TABLE_MONTH_CONTACT + " ( " + MC_COLUMN_REQ
            + " real not null, " + MC_COLUMN_MONTH
            + " integer, " +  MC_COLUMN_CONTACT
            + " integer, foreign key (" + MC_COLUMN_MONTH
            + ") references " + TABLE_MONTH + "("
            + MONTH_COLUMN_ID + ") on delete cascade, foreign key (" + MC_COLUMN_CONTACT
            + ") references " + TABLE_CONTACT + "("
            + CONTACTS_COLUMN_ID + ") on delete cascade, primary key ("
            + MC_COLUMN_MONTH + "," + MC_COLUMN_CONTACT + "));";

    private static final String CREATE_TABLE_MS = "create table "
            + TABLE_MONTH_SERVICE + " ( " + MS_COLUMN_MONTH
            + " integer, " +  MS_COLUMN_SERVICE
            + " integer, foreign key (" + MS_COLUMN_MONTH
            + ") references " + TABLE_MONTH + "("
            + MONTH_COLUMN_ID + ") on delete cascade, foreign key (" + MS_COLUMN_SERVICE
            + ") references " + TABLE_SERVICE + "("
            + SERVICES_COLUMN_ID + ") on delete cascade, primary key ("
            + MS_COLUMN_MONTH + "," + MS_COLUMN_SERVICE + "));";

    private static final String CREATE_TABLE_DS = "create table "
            + TABLE_DAY_SERVICE + " ( " + DS_COLUMN_DAY
            + " integer not null, " + DS_COLUMN_CONTACT
            + " integer not null, " + DS_COLUMN_SERVICE
            + " integer not null, foreign key (" + DS_COLUMN_DAY
            + ") references " + TABLE_DAY + "("
            + DAY_COLUMN_ID + ") on delete cascade, foreign key (" + DS_COLUMN_CONTACT
            + ") references " + TABLE_CONTACT + "("
            + CONTACTS_COLUMN_ID + ") on delete cascade, foreign key (" + DS_COLUMN_SERVICE
            + ") references " + TABLE_SERVICE + "("
            + SERVICES_COLUMN_ID + ") on delete cascade, primary key ("
            + DS_COLUMN_DAY + "," + DS_COLUMN_CONTACT
            + "," + DS_COLUMN_SERVICE + "));";

    // Constructor
    public DBHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_CONTACTS);
        db.execSQL(CREATE_TABLE_MONTH);
        db.execSQL(CREATE_TABLE_DAY);
        db.execSQL(CREATE_TABLE_SERVICES);
        db.execSQL(CREATE_TABLE_MC);
        db.execSQL(CREATE_TABLE_MS);
        db.execSQL(CREATE_TABLE_DS);
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH_SERVICE);
        db.execSQL("DROP TABLE IF ECISTS " + TABLE_DAY_SERVICE);

        // create new tables
        onCreate(db);
    }

    public void resetDB() {

        // delete every entry from all tables
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MONTH, null, null);
        db.delete(TABLE_CONTACT, null, null);
        db.delete(TABLE_SERVICE, null, null);
    }

    // ---------------------- contacts table methods ------------------------//

    /**
     * Inserting a contact into db
     * @param contact Contact object
     * @return id if succesfull, -1 if exception
     */
    public int insertContact(Contact contact) {

        SQLiteDatabase db = this.getWritableDatabase();
        int contact_id = -1;

        try {
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACTS_COLUMN_NAME, contact.getName());
            contentValues.put(CONTACTS_COLUMN_WH, contact.getWh());
            contentValues.put(CONTACTS_COLUMN_ENABLED, contact.getEnabled());

            // insert row
            contact_id = (int) db.insert(TABLE_CONTACT, null, contentValues);

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            db.close();
        }
        finally {
            db.endTransaction();
        }

        db.close();
        return contact_id;
    }

    /**
     * Get a single Contact from db
     * @param contact_id ID required for finding the contact in db
     * @return Contact object
     */
    public Contact getContact(int contact_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACT +
                " WHERE " + CONTACTS_COLUMN_ID + " = " + contact_id;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Contact con = new Contact();
        con.setId(c.getInt(c.getColumnIndex(CONTACTS_COLUMN_ID)));
        con.setName(c.getString(c.getColumnIndex(CONTACTS_COLUMN_NAME)));
        con.setWh(c.getFloat(c.getColumnIndex(CONTACTS_COLUMN_WH)));
        con.setEnabled(c.getInt(c.getColumnIndex(CONTACTS_COLUMN_ENABLED)));

        c.close();
        db.close();
        return con;
    }

    /**
     * Change an existing contact
     * @param con Contact object
     * @return 1 if successfull
     */
    public int updateContact(Contact con) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTACTS_COLUMN_NAME, con.getName());
        values.put(CONTACTS_COLUMN_WH, con.getWh());
        values.put(CONTACTS_COLUMN_ENABLED, con.getEnabled());

        // updating row
        return db.update(TABLE_CONTACT, values, CONTACTS_COLUMN_ID + " = ?",
                new String[] { String.valueOf(con.getId()) });
    }

    /**
     * Delete a contact from the db
     * @param contact_id contact id
     */
    public void deleteContact(int contact_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACT, CONTACTS_COLUMN_ID + " = ?",
                new String[] { String.valueOf(contact_id) });
    }

    /**
     * Get all valid contacts from db
     * @return returns all Contacts from DB
     */
    public SparseArray<Contact> getAllContacts() {
        SparseArray<Contact> contacts = new SparseArray<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACT
                + " WHERE " + CONTACTS_COLUMN_ENABLED + " = 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Contact co = new Contact();
                co.setId(c.getInt(c.getColumnIndex(CONTACTS_COLUMN_ID)));
                co.setName(c.getString(c.getColumnIndex(CONTACTS_COLUMN_NAME)));
                co.setWh(c.getFloat(c.getColumnIndex(CONTACTS_COLUMN_WH)));
                co.setEnabled(c.getInt(c.getColumnIndex(CONTACTS_COLUMN_ENABLED)));
                // adding to contacts list
                contacts.put(co.getId(), co);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return contacts;
    }

    // ---------------------- month table methods ------------------------//

    /**
     * Inserting a month into db
     * @param month Month object
     * @return id if succesfull, -1 if exception
     */
    public int insertMonth(Month month) {

        SQLiteDatabase db = this.getWritableDatabase();
        int month_id = -1;

        try {
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MONTH_COLUMN_YEAR, month.getYear());
            contentValues.put(MONTH_COLUMN_MONTH, month.getMonth());
            contentValues.put(MONTH_COLUMN_FULL, month.getFull());

            // insert row
            month_id = (int) db.insert(TABLE_MONTH, null, contentValues);

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            db.close();
        }
        finally {
            db.endTransaction();
        }

        db.close();
        return month_id;
    }

    /**
     * Get a single month from db
     * @param month_id ID required for finding the month in db
     * @return returns Month object
     */
    public Month getMonth(int month_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MONTH +
                " WHERE " + MONTH_COLUMN_ID + " = " + month_id;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Month mon = new Month();
        mon.setId(c.getInt(c.getColumnIndex(MONTH_COLUMN_ID)));
        mon.setYear(c.getInt(c.getColumnIndex(MONTH_COLUMN_YEAR)));
        mon.setMonth(c.getInt(c.getColumnIndex(MONTH_COLUMN_MONTH)));
        mon.setFull(c.getInt(c.getColumnIndex(MONTH_COLUMN_FULL)));

        c.close();
        db.close();
        return mon;
    }

    /**
     * Change an existing month
     * @param mon Month object to update
     * @return 1 if successfull
     */
    public int updateMonth(Month mon) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MONTH_COLUMN_YEAR, mon.getYear());
        values.put(MONTH_COLUMN_MONTH, mon.getMonth());
        values.put(MONTH_COLUMN_FULL, mon.getFull());

        // updating row
        return db.update(TABLE_MONTH, values, MONTH_COLUMN_ID + " = ?",
                new String[] { String.valueOf(mon.getId()) });
    }

    /**
     * Delete a month from the db
     * @param month_id moth id to delete from
     */
    public void deleteMonth(int month_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MONTH, MONTH_COLUMN_ID + " = ?",
                new String[] { String.valueOf(month_id) });
    }

    /**
     * Get all months from db
     * @return returns all Months from DB
     */
    public List<Month> getAllMonths() {
        List<Month> months = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MONTH;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Month mo = new Month();
                mo.setId(c.getInt(c.getColumnIndex(MONTH_COLUMN_ID)));
                mo.setYear(c.getInt(c.getColumnIndex(MONTH_COLUMN_YEAR)));
                mo.setMonth(c.getInt(c.getColumnIndex(MONTH_COLUMN_MONTH)));
                mo.setFull(c.getInt(c.getColumnIndex(MONTH_COLUMN_FULL)));

                // adding to contacts list
                months.add(mo);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return months;
    }

    // ---------------------- day table methods ------------------------//

    /**
     * Inserting a day into db
     * @param day Day object
     * @return id if succesfull, -1 if exception
     */
    public int insertDay(Day day) {

        SQLiteDatabase db = this.getWritableDatabase();
        int day_id = -1;

        try {
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DAY_COLUMN_MONTH, day.getMonth());
            contentValues.put(DAY_COLUMN_DATE, day.getDate());

            // insert row
            day_id = (int) db.insert(TABLE_DAY, null, contentValues);

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            db.close();
        }
        finally {
            db.endTransaction();
        }

        db.close();
        return day_id;
    }

    /**
     * Get a single day from db
     * @param day_id ID required for finding the month in db
     * @return returns the Day object
     */
    public Day getDay(int day_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_DAY +
                " WHERE " + DAY_COLUMN_ID + " = " + day_id;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Day day = new Day();
        day.setId(c.getInt(c.getColumnIndex(DAY_COLUMN_ID)));
        day.setMonth(c.getInt(c.getColumnIndex(DAY_COLUMN_MONTH)));
        day.setDate(c.getInt(c.getColumnIndex(DAY_COLUMN_DATE)));

        c.close();
        db.close();
        return day;
    }

    /**
     * Change an existing day, useless
     * @param day Day object to update
     * @return 1 if successfull
     */
    public int updateDay(Day day) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DAY_COLUMN_MONTH, day.getMonth());
        values.put(DAY_COLUMN_DATE, day.getDate());

        // updating row
        return db.update(TABLE_DAY, values, DAY_COLUMN_ID + " = ?",
                new String[] { String.valueOf(day.getId()) });
    }

    /**
     * Delete a day from the db, useless cause of cascade
     * @param day_id Day id to delete from db
     */
    public void deleteDay(int day_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_DAY, DAY_COLUMN_ID + " = ?",
                new String[] { String.valueOf(day_id) });
    }

    /**
     * Get all days for a given month from db
     * @return returns all Days of the specific month
     */
    public SparseArray<Day> getAllDaysForMonth(int month_id) {
        SparseArray<Day> days = new SparseArray<>();
        String selectQuery = "SELECT * FROM " + TABLE_DAY
                + " WHERE " + DAY_COLUMN_MONTH + " = " + month_id
                + " ORDER BY " + DAY_COLUMN_DATE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Day day = new Day();
                day.setId(c.getInt(c.getColumnIndex(DAY_COLUMN_ID)));
                day.setMonth(c.getInt(c.getColumnIndex(DAY_COLUMN_MONTH)));
                day.setDate(c.getInt(c.getColumnIndex(DAY_COLUMN_DATE)));

                // adding to contacts list
                days.put(day.getDate()-1, day);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return days;
    }

    // ---------------------- services table methods ------------------------//

    /**
     * Inserting a service into db
     * @param service Service object to add to db
     * @return id if succesfull, -1 if exception
     */
    public int insertService(Service service) {

        SQLiteDatabase db = this.getWritableDatabase();
        int service_id = -1;

        try {
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(SERVICES_COLUMN_DESC, service.getDesc());
            contentValues.put(SERVICES_COLUMN_VAL, service.getVal());
            contentValues.put(SERVICES_COLUMN_DEF, service.getDef());
            contentValues.put(SERVICES_COLUMN_SPE, service.getSpe() ? 1 : 0);
            contentValues.put(SERVICES_COLUMN_ENA, service.getEna());

            // insert row
            service_id = (int) db.insert(TABLE_SERVICE, null, contentValues);

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            db.close();
        }
        finally {
            db.endTransaction();
        }

        db.close();
        return service_id;
    }

    /**
     * Get a single service from db
     * @param service_id ID required for finding the service in db
     * @return returns the Service object
     */
    public Service getService(int service_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_SERVICE +
                " WHERE " + SERVICES_COLUMN_ID + " = " + service_id;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Service ser = new Service();
        ser.setId(c.getInt(c.getColumnIndex(SERVICES_COLUMN_ID)));
        ser.setDesc(c.getString(c.getColumnIndex(SERVICES_COLUMN_DESC)));
        ser.setVal(c.getFloat(c.getColumnIndex(SERVICES_COLUMN_VAL)));
        ser.setDef(c.getInt(c.getColumnIndex(SERVICES_COLUMN_DEF)));
        ser.setSpe(c.getInt(c.getColumnIndex(SERVICES_COLUMN_SPE)));
        ser.setEna(c.getInt(c.getColumnIndex(SERVICES_COLUMN_ENA)));

        c.close();
        db.close();
        return ser;
    }

    /**
     * Change an existing service
     * @param ser The Service to update to
     * @return 1 if successfull
     */
    public int updateService(Service ser) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SERVICES_COLUMN_DESC, ser.getDesc());
        values.put(SERVICES_COLUMN_VAL, ser.getVal());
        values.put(SERVICES_COLUMN_DEF, ser.getDef());
        values.put(SERVICES_COLUMN_SPE, ser.getSpe() ? 1 : 0);
        values.put(SERVICES_COLUMN_ENA, ser.getEna());

        // updating row
        return db.update(TABLE_SERVICE, values, SERVICES_COLUMN_ID + " = ?",
                new String[] { String.valueOf(ser.getId()) });
    }

    /**
     * Delete a service from the db
     * @param service_id the id for the service to delete
     */
    public void deleteService(int service_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SERVICE, SERVICES_COLUMN_ID + " = ?",
                new String[] { String.valueOf(service_id) });
    }

    /**
     * Get all services from db
     * @return returns all Services from DB
     */
    public SparseArray<Service> getAllServices() {
        SparseArray<Service> services = new SparseArray<>();
        String selectQuery = "SELECT * FROM " + TABLE_SERVICE
                + " WHERE " + SERVICES_COLUMN_ENA + " = 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Service ser = new Service();
                ser.setId(c.getInt(c.getColumnIndex(SERVICES_COLUMN_ID)));
                ser.setDesc(c.getString(c.getColumnIndex(SERVICES_COLUMN_DESC)));
                ser.setVal(c.getFloat(c.getColumnIndex(SERVICES_COLUMN_VAL)));
                ser.setDef(c.getInt(c.getColumnIndex(SERVICES_COLUMN_DEF)));
                ser.setSpe(c.getInt(c.getColumnIndex(SERVICES_COLUMN_SPE)));

                // adding to contacts list
                services.put(ser.getId(), ser);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return services;
    }

    // ---------------------- month_contact table methods ------------------------//

    /**
     * Inserting a month_service into db
     * @param month the month id for the relation
     * @param contact the contact id for the relation
     * @param req the required weekly work hours
     */
    public void insertMC(int month, int contact, float req) {

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MC_COLUMN_MONTH, month);
            contentValues.put(MC_COLUMN_CONTACT, contact);
            contentValues.put(MC_COLUMN_REQ, req);
            // insert row
            db.insert(TABLE_MONTH_CONTACT, null, contentValues);

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            db.close();
        }
        finally {
            db.endTransaction();
        }

        db.close();
    }

    /**
     * Get all contacts for a certain month
     * @param month_id ID required for finding all contacts
     * @param req the required hours per person
     * @return returns all the contacts for this month
     */
    public SparseArray<Contact> getAllContactsForMonth(int month_id, SparseArray<Float> req) {
        SQLiteDatabase db = this.getReadableDatabase();
        SparseArray<Contact> contacts = new SparseArray<>();

        String selectQuery = "SELECT " + TABLE_CONTACT + "."
                + CONTACTS_COLUMN_ID + ", " + TABLE_CONTACT + "."
                + CONTACTS_COLUMN_WH + ", " + TABLE_CONTACT + "."
                + CONTACTS_COLUMN_NAME + ", " + TABLE_MONTH_CONTACT + "."
                + MC_COLUMN_REQ + " FROM " + TABLE_CONTACT
                + " JOIN " + TABLE_MONTH_CONTACT + " ON " + TABLE_CONTACT
                + "." + CONTACTS_COLUMN_ID + " = " + TABLE_MONTH_CONTACT
                + "." + MC_COLUMN_CONTACT + " JOIN " + TABLE_MONTH
                + " ON " + TABLE_MONTH_CONTACT + "." + MC_COLUMN_MONTH
                + " = " + TABLE_MONTH + "." + MONTH_COLUMN_ID
                + " WHERE " + TABLE_MONTH + "." + MONTH_COLUMN_ID
                + " = " + month_id;

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Contact con = new Contact();
                con.setId(c.getInt(c.getColumnIndex(CONTACTS_COLUMN_ID)));
                con.setName(c.getString(c.getColumnIndex(CONTACTS_COLUMN_NAME)));
                con.setWh(c.getFloat(c.getColumnIndex(CONTACTS_COLUMN_WH)));

                if (req != null) req.put(con.getId(), c.getFloat(c.getColumnIndex(MC_COLUMN_REQ)));
                // adding to contacts list
                contacts.put(con.getId(), con);
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return contacts;
    }

    // ---------------------- month_service table methods ------------------------//

    /**
     * Inserting a month_service into db
     * @param month the month id for the relation
     * @param service the service id for the relation
     */
    public void insertMS(int month, int service) {

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MS_COLUMN_MONTH, month);
            contentValues.put(MS_COLUMN_SERVICE, service);
            // insert row
            db.insert(TABLE_MONTH_SERVICE, null, contentValues);

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            db.close();
        }
        finally {
            db.endTransaction();
        }

        db.close();
    }

    /**
     * Get all services for a certain month
     * @param month_id ID required for finding all services
     * @return returns all the services for this month
     */
    public SparseArray<Service> getAllServicesForMonth(int month_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        SparseArray<Service> services = new SparseArray<>();

        String selectQuery = "SELECT " + TABLE_SERVICE + "."
                + SERVICES_COLUMN_ID + ", " + TABLE_SERVICE + "."
                + SERVICES_COLUMN_DESC + ", " + TABLE_SERVICE + "."
                + SERVICES_COLUMN_VAL + ", " + TABLE_SERVICE + "."
                + SERVICES_COLUMN_DEF + ", " + TABLE_SERVICE + "."
                + SERVICES_COLUMN_SPE + " FROM " + TABLE_SERVICE
                + " JOIN " + TABLE_MONTH_SERVICE + " ON " + TABLE_SERVICE
                + "." + SERVICES_COLUMN_ID + " = " + TABLE_MONTH_SERVICE
                + "." + MS_COLUMN_SERVICE + " JOIN " + TABLE_MONTH
                + " ON " + TABLE_MONTH_SERVICE + "." + MC_COLUMN_MONTH
                + " = " + TABLE_MONTH + "." + MONTH_COLUMN_ID
                + " WHERE " + TABLE_MONTH + "." + MONTH_COLUMN_ID
                + " = " + month_id;

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Service ser = new Service();
                ser.setId(c.getInt(c.getColumnIndex(SERVICES_COLUMN_ID)));
                ser.setDesc(c.getString(c.getColumnIndex(SERVICES_COLUMN_DESC)));
                ser.setVal(c.getFloat(c.getColumnIndex(SERVICES_COLUMN_VAL)));
                ser.setDef(c.getInt(c.getColumnIndex(SERVICES_COLUMN_DEF)));
                ser.setSpe(c.getInt(c.getColumnIndex(SERVICES_COLUMN_SPE)));

                // adding to contacts list
                services.put(ser.getId(), ser);
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return services;
    }

    // ---------------------- day_service table methods ------------------------//

    /**
     * Inserting a day_service into db
     * @param day the day id for the relation
     * @param service the service id for the relation
     * @param contact the contact id for the relation
     */
    public void insertDS(int day, int service, int contact) {

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DS_COLUMN_DAY, day);
            contentValues.put(DS_COLUMN_CONTACT, contact);
            contentValues.put(DS_COLUMN_SERVICE, service);

            // insert row
            db.insert(TABLE_DAY_SERVICE, null, contentValues);

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            db.close();
        }
        finally {
            db.endTransaction();
        }

        db.close();
    }

    /**
     * Delete a day_service from the db
     * @param day_id the day id for the relation
     * @param user_id the user id for the relation
     */
    public void deleteDS(int day_id, int user_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_DAY_SERVICE, DS_COLUMN_DAY + " = ? and " + DS_COLUMN_CONTACT + " = ?",
                new String[] { String.valueOf(day_id), String.valueOf(user_id) });
    }

    /**
     * Get all services for a certain day
     * @param day_id ID required for finding all services
     * @return return all the contacts and services for the specific day
     */
    public List<ContactService> getAllContactsForDay(int day_id) {
            SQLiteDatabase db = this.getReadableDatabase();
            List<ContactService> contactServices = new ArrayList<>();

            String selectQuery = "SELECT " + TABLE_CONTACT + "."
                    + CONTACTS_COLUMN_ID + " as a, " +  TABLE_SERVICE + "."
                    + SERVICES_COLUMN_ID + " as b FROM " + TABLE_DAY
                    + " JOIN " + TABLE_DAY_SERVICE + " ON " + TABLE_DAY
                    + "." + DAY_COLUMN_ID + " = " + TABLE_DAY_SERVICE
                    + "." + DS_COLUMN_DAY + " JOIN " + TABLE_CONTACT
                    + " ON " + TABLE_DAY_SERVICE + "." + DS_COLUMN_CONTACT
                    + " = " + TABLE_CONTACT + "." + CONTACTS_COLUMN_ID
                    + " JOIN " + TABLE_SERVICE + " ON " + TABLE_DAY_SERVICE
                    + "." + DS_COLUMN_SERVICE + " = " + TABLE_SERVICE
                    + "." + SERVICES_COLUMN_ID + " WHERE " + TABLE_DAY
                    + "." + DAY_COLUMN_ID + " = " + day_id;

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                ContactService contactService = new ContactService();

                contactService.contact_id = c.getInt(c.getColumnIndex("a"));
                contactService.service_id = c.getInt(c.getColumnIndex("b"));
                // adding to list
                contactServices.add(contactService);
            } while (c.moveToNext());
        }
        c.close();
        db.close();

        return contactServices;
    }
}