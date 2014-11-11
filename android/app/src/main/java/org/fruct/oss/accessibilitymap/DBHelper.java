package org.fruct.oss.accessibilitymap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.fruct.oss.accessibilitymap.Model.Accessibility;
import org.fruct.oss.accessibilitymap.Model.Passport;

/**
 * Created by alexander on 21.07.14.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "accessibility_map_db6";
    private static final int DB_VERSION = 2;

    Context context;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    /**
     * Creates any of our tables.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        try {

            //database.setLocale(context.getResources().getConfiguration().locale);

            // Scopes
            Log.d("accmap db", "scopes");
            database.execSQL("" +
                    "create table scopes" +
                    "(" +
                    "id integer primary key,"+
                    "name text" +
                    ");");

            // List of disabilities
            Log.d("accmap db", "2");
            database.execSQL("" +
                    "create table disabilities" +
                    "(" +
                    "id integer primary key,"+
                    "name text," +
                    "sign text" +
                    ");");

            // List of accessibilities
            Log.d("accmap db", "3");
            database.execSQL("" +
                    "create table accessibilities" +
                    "(" +
                    "id integer primary key,"+
                    "name text," +
                    "sign text" +
                    ");");

            // Maintenance form
            Log.d("accmap db", "4");
            database.execSQL("" +
                    "create table maintenances" +
                    "(" +
                    "id integer primary key,"+
                    "name text," +
                    "sign text" +
                    ");");

            // Functional areas
            Log.d("accmap db", "5");
            database.execSQL("" +
                    "create table functionals" +
                    "(" +
                    "id integer primary key,"+
                    "name text" +
                    ");");


            // All the points with references to other tables
            Log.d("accmap db", "6");
            database.execSQL("" +
                    "create table points " +
                    "(" +
                    "id integer primary key autoincrement," +
                    "passid integer," + // Passport id from server
                    "name text," +
                    "name_uppercase text," + // Only for search purposes. SQLite does not support like operator for non-english lang
                    "objectname text," +
                    "objectname_uppercase text," + // Only for search purposes
                    "address text," +
                    "route text," +
                    "longitude real," +
                    "latitude real," +
                    "site text," +
                    "phone text," +
                    "scope integer not null references scopes(id)," +
                    "disability integer not null references disabilities(id)," + // ref
                    "maintenance integer not null references maintenances(id)," + // ref
                    "functional integer not null references functionals(id)," + // ref
                    "accessibility integer not null references accessibilities(id)" + // ref
                    ");");
            Log.d("accmap db", "7");

/*



 */

        } catch(Exception e){
            Log.e("accmap", "Error creating database" + e.toString());
        }
    }

    public void deleteAll() {
        Log.d("accmap db", "Deleting all the data...");

        SQLiteDatabase db = getWritableDatabase();

        db.delete("points", null, null);
        db.delete("scopes", null, null);
        db.delete("accessibilities", null, null);
        db.delete("disabilities", null, null);
        db.delete("functionals", null, null);
        db.delete("maintenances", null, null);

        db.close();
    }

    public String getScopeNameById(int id) {
        return getAnyNameById(id, "scopes");
    }

    public String getDisabilityNameById(int id) {
        return getAnyNameById(id, "disabilities");
    }

    public String getAccessibilityNameById(int id) {
        return getAnyNameById(id, "accessibilities");
    }

    public String getMaintentanceNamByid(int id) {
        return getAnyNameById(id, "maintenances");
    }

    public String getFunctionalNamByid(int id) {
        return getAnyNameById(id, "functionals");
    }



    private String getAnyNameById(int id, String tableName) {
        String scopeName = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(true, tableName, new String[]{}, "id = " + id, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            scopeName = cursor.getString(cursor.getColumnIndex("name"));
        }

        cursor.close();
        //db.close();

        return scopeName;
    }

    public void addPassport(Passport passport) {

        for (int i = 0; i < passport.scopes.size(); i++) {
            for (int j = 0; j < passport.accessibilities.size(); j++) {
                for (int k = 0; k < passport.accessibilities.get(j).functionalAreas.size(); k++) {
                    try {
                        addPoint(passport.id,
                                passport.name,
                                passport.objectName,
                                passport.address,
                                passport.latitude,
                                passport.longitude,
                                passport.scopes.get(i).id,
                                passport.accessibilities.get(j).category.id,
                                passport.accessibilities.get(j).maintenanceForm.id,
                                passport.accessibilities.get(j).functionalAreas.get(k).functionalArea.id,
                                passport.accessibilities.get(j).functionalAreas.get(k).accessibilityType.id,
                                passport.route,
                                passport.contacts,
                                passport.site
                        );
                    } catch (NullPointerException e) {
                        Log.e("accmap", passport.id + " " + passport.name);
                    }
                }
            }
        }
    }

    public void addPoint(long passportId, String name, String objectName, String address, double latitude, double longitude, long scopeId,
                         long disabilityId, long maintenanceId, long functionalId, long accessibilityId, String route,
                         String phone, String site) {

        //Log.d("accmap", name + " " + objectName + address);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("passid", passportId);
        cv.put("name", name);
        cv.put("name_uppercase", name.toUpperCase());
        cv.put("objectname", objectName);
        cv.put("objectname_uppercase", objectName.toUpperCase());
        cv.put("address", address);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("scope", scopeId);
        cv.put("disability", disabilityId);
        cv.put("maintenance", maintenanceId);
        cv.put("functional", functionalId);
        cv.put("accessibility", accessibilityId);
        cv.put("route", route);
        cv.put("phone", phone);
        cv.put("site", site);

        if (db != null) {
            db.insert("points", null, cv);
            db.close();
        }

    }


    public int getScopeIdByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(true, "scopes", new String[]{}, "name='" + name + "'", null, null, null, null, null);

        int id = -1; // FIXME: or -1?
        Log.d("accmap", "getScopeIdByName: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }

        cursor.close();
        db.close();

        return id;
    }

    // Add new scope
    public void addScope(int id, String name) {
        addAnyItem("scopes", id, null, name);
    }

    // Add new disability
    public void addDisability(int id, String sign, String name) {
        addAnyItem("disabilities", id, sign, name);
    }

    // Add new accessibility
    public void addAccessibility(int id, String sign, String name) {
        addAnyItem("accessibilities", id, sign, name);
    }

    // Add new maintenance
    public void addMaintenance(int id, String sign, String name) {
        addAnyItem("maintenances", id, sign, name);
    }

    // Add new functional
    public void addFunctional(int id, String name) {
        addAnyItem("functionals", id, null, name);
    }

    // Function for adding simple items
    private void addAnyItem(String tableName, int id, String sign, String name) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("id", id);

        if (sign != null)
            cv.put("sign", sign);

        if (db != null) {
            db.insert(tableName, null, cv);
            db.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * On open we want to make sure we get rid of the stupid setLocale error
     */
    @Override
    public void onOpen(SQLiteDatabase database) {
        if(!database.isOpen()) {
            SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS |
                    SQLiteDatabase.CREATE_IF_NECESSARY);
        }
    }


}
