package sensor.information.collector;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "SensorsInformation.db";
    // Contacts table name
    private static final String TABLE_CONTACTS = "Informations";
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    private static final String GPS_Location = "location";
    private static final String GPS_Longitude = "longitude";
    private static final String GPS_Latitude = "latitude";
    private static final String X_Text = "xtext";
    private static final String Y_Text = "ytext";
    private static final String Z_Text = "ztext";
    private static final String LightReading = "lightreading";
    private static final String TouchScreen = "touchscreen";
    private static final String UserIDtrue= "useridtrue";
    private static final String UserIDwindvane="useridwindvane";
    private static final String Time_Stamp = "time";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT," + GPS_Longitude + " TEXT,"
                + GPS_Latitude + " TEXT," + GPS_Location + " TEXT," + X_Text
                + " TEXT," + Y_Text + " TEXT," + Z_Text + " TEXT,"
                + LightReading + " TEXT," + TouchScreen + " TEXT," + UserIDtrue
                + " TEXT," + UserIDwindvane + " TEXT," + Time_Stamp
                + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public void addContact(Contact contact) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName()); // Contact Name
        values.put(KEY_PH_NO, contact.getPhoneNumber()); // Contact Phone Number
        values.put(GPS_Longitude, contact.getLongitude()); //gps longitude
        values.put(GPS_Latitude, contact.getLatitude()); //gps latitude
        values.put(GPS_Location, contact.getAddress());// gps fine location with
        // street name
        values.put(X_Text, contact.getXText());// get x axis accelerometer
        // information
        values.put(Y_Text, contact.getYText());// get y axis accelerometer
        // information
        values.put(Z_Text, contact.getZText());// get z axis accelerometer
        // information
        values.put(LightReading, contact.getLightReading());// get light reading
        // information
        values.put(TouchScreen, contact.getTouchScreen());// get touch screen
        // information
        values.put(UserIDtrue, contact.getUserIDTrue());// get true user id
        // information
        values.put(UserIDwindvane,contact.getUserIDWindVane());//get wind vane user id
        // information
        values.put(Time_Stamp, contact.getTimeStamp());// current time stamp of


        // server



        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    public Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID,
                        KEY_NAME, KEY_PH_NO, GPS_Longitude, GPS_Latitude, GPS_Location, X_Text, Y_Text, Z_Text,
                        LightReading, TouchScreen, UserIDtrue, UserIDwindvane, Time_Stamp}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getString(6),
                cursor.getString(7), cursor.getString(8), cursor.getString(9),
                cursor.getString(10), cursor.getString(11), cursor.getString(12), cursor.getString(13));

        // return contact
        return contact;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setPhoneNumber(cursor.getString(2));
                contact.set_Longitude(cursor.getString(3));
                contact.set_Latitude(cursor.getString(4));
                contact.setAddress(cursor.getString(5));
                contact.setXText(cursor.getString(6));
                contact.setYText(cursor.getString(7));
                contact.setZText(cursor.getString(8));
                contact.setLightReading(cursor.getString(9));
                contact.setTouchScreen(cursor.getString(10));
                contact.setUserIDTrue(cursor.getString(11));
                contact.setUserIDWindVane(cursor.getString(12));
                contact.setTimeStamp(cursor.getString(13));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return contactList;
    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        // return count
        return count;
    }

    // Updating single contact
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PH_NO, contact.getPhoneNumber());
        values.put(GPS_Longitude,contact.getLongitude());
        values.put(GPS_Latitude,contact.getLatitude());
        values.put(GPS_Location, contact.getAddress());
        values.put(X_Text, contact.getXText());
        values.put(Y_Text, contact.getYText());
        values.put(Z_Text, contact.getZText());
        values.put(LightReading, contact.getLightReading());
        values.put(TouchScreen, contact.getTouchScreen());
        values.put(UserIDtrue,contact.getUserIDTrue());
        values.put(UserIDwindvane,contact.getUserIDWindVane());
        values.put(Time_Stamp, contact.getTimeStamp());


        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getID())});
    }

    // Deleting single contact
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getID())});
        db.close();
    }

    // Trunk database
    public void trunkContact() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, null, null);
        db.close();
    }

}
