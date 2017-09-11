package com.example.john.norfolktouring.Data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.R;
import com.example.john.norfolktouring.TourLocation;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.john.norfolktouring.Data.TourLocationContract.LocationFeatureEntry;
import static com.example.john.norfolktouring.Data.TourLocationContract.LocationFeatureResourceImage;
import static com.example.john.norfolktouring.Data.TourLocationContract.TourLocationEntry;
import static com.example.john.norfolktouring.Data.TourLocationContract.TourLocationResourceImage;

/**
 * Created by John on 9/2/2017.
 */

public class TourLocationDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = TourLocationDbHelper.class.getCanonicalName();
    private static final String DATABASE_NAME = "tourlocation.db";
    private static final int DATABASE_VERSION = 1;

    public TourLocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO: Find out how to update the database without losing data.
        // Drop any existing instance of the TourLocationEntry table.
//        final String SQL_DROP_TOURLOCATION_TABLE = "DROP TABLE IF EXISTS " + TourLocationEntry.TABLE_NAME;
//        db.execSQL(SQL_DROP_TOURLOCATION_TABLE);
        // Create TourLocationEntry table.
        final String SQL_CREATE_TOURLOCATION_TABLE = "CREATE TABLE " +
                TourLocationEntry.TABLE_NAME + " (" +
                TourLocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TourLocationEntry.COLUMN_CATEGORY + " TEXT NOT NULL, " +
                TourLocationEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                TourLocationEntry.COLUMN_LOCATION_DESCRIPTION + " TEXT, " +
                TourLocationEntry.COLUMN_LOCATION_ADDRESS + " TEXT NOT NULL, " +
                TourLocationEntry.COLUMN_LOCATION_CONTACT_INFO + " TEXT" +
                ");";
        try {
            db.execSQL(SQL_CREATE_TOURLOCATION_TABLE);
            // TODO: Remove this when done debugging.
            Cursor cursor = db.rawQuery("SELECT * FROM " + TourLocationEntry.TABLE_NAME, null);
            int numElems = cursor.getCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Drop any existing instance of the TourLocationResourceImage table.
//        final String SQL_DROP_TOURLOCATION_RESOURCE_IMAGE_TABLE = "DROP TABLE IF EXISTS " + TourLocationResourceImage.TABLE_NAME;
//        db.execSQL(SQL_DROP_TOURLOCATION_RESOURCE_IMAGE_TABLE);
        // Create TourLocationResourceImage table.
        final String SQL_CREATE_TOURLOCATION_RESOURCE_IMAGE_TABLE = "CREATE TABLE " +
                TourLocationResourceImage.TABLE_NAME + " (" +
                TourLocationResourceImage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TourLocationResourceImage.COLUMN_LOCATION_ID + " INTEGER NOT NULL, " +
                TourLocationResourceImage.COLUMN_RESOURCE_IMAGE + " INTEGER, " +
                // COLUMN_LOCATION_ID is a foreign key to TourLocationEntry._ID.
                "FOREIGN KEY (" + TourLocationResourceImage.COLUMN_LOCATION_ID + ") REFERENCES "
                + TourLocationEntry.TABLE_NAME + " (" + TourLocationEntry._ID + ") " +
                ");";
        try {
            db.execSQL(SQL_CREATE_TOURLOCATION_RESOURCE_IMAGE_TABLE);
            // TODO: Remove this when done debugging.
            Cursor cursor = db.rawQuery("SELECT * FROM " + TourLocationResourceImage.TABLE_NAME, null);
            int numElems = cursor.getCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Drop any existing instance of the LocationFeatureEntry table.
//        final String SQL_DROP_LOCATIONFEATURE_TABLE = "DROP TABLE IF EXISTS " + LocationFeatureEntry.TABLE_NAME;
//        db.execSQL(SQL_DROP_LOCATIONFEATURE_TABLE);
        // Create LocationFeatureEntry table.
        final String SQL_CREATE_LOCATIONFEATURE_TABLE = "CREATE TABLE " +
                LocationFeatureEntry.TABLE_NAME + " (" +
                LocationFeatureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationFeatureEntry.COLUMN_LOCATION_ID + " INTEGER NOT NULL, " +
                LocationFeatureEntry.COLUMN_FEATURE_NAME + " TEXT NOT NULL, " +
                LocationFeatureEntry.COLUMN_FEATURE_DESCRIPTION + " TEXT, " +
                // COLUMN_LOCATION_ID is a foreign key to TourLocationEntry._ID.
                "FOREIGN KEY (" + LocationFeatureEntry.COLUMN_LOCATION_ID + ") REFERENCES "
                + TourLocationEntry.TABLE_NAME + " (" + TourLocationEntry._ID + ") " +
                ");";
        try {
            db.execSQL(SQL_CREATE_LOCATIONFEATURE_TABLE);
            // TODO: Remove this when done debugging.
            Cursor cursor = db.rawQuery("SELECT * FROM " + LocationFeatureEntry.TABLE_NAME, null);
            int numElems = cursor.getCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Drop any existing instance of the LocationFeatureResourceImage table.
//        final String SQL_DROP_LOCATIONFEATURE_RESOURCE_IMAGE_TABLE = "DROP TABLE IF EXISTS " + LocationFeatureResourceImage.TABLE_NAME;
//        db.execSQL(SQL_DROP_LOCATIONFEATURE_RESOURCE_IMAGE_TABLE);
        // Create LocationFeatureResourceImage table.
        final String SQL_CREATE_LOCATIONFEATURE_RESOURCE_IMAGE_TABLE = "CREATE TABLE " +
                LocationFeatureResourceImage.TABLE_NAME + " (" +
                LocationFeatureResourceImage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationFeatureResourceImage.COLUMN_FEATURE_ID + " INTEGER NOT NULL, " +
                LocationFeatureResourceImage.COLUMN_FEATURE_IMAGE + " INTEGER, " +
                // COLUMN_FEATURE_ID is a foreign key to LocationFeatureEntry._ID.
                "FOREIGN KEY (" + LocationFeatureResourceImage.COLUMN_FEATURE_ID + ") REFERENCES "
                + LocationFeatureEntry.TABLE_NAME + " (" + LocationFeatureEntry._ID + ") " +
                ");";
        try {
            db.execSQL(SQL_CREATE_LOCATIONFEATURE_RESOURCE_IMAGE_TABLE);
            // TODO: Remove this when done debugging.
            Cursor cursor = db.rawQuery("SELECT * FROM " + LocationFeatureResourceImage.TABLE_NAME, null);
            int numElems = cursor.getCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert TourLocation data.
        ContentValues tourLocationEntryValues = new ContentValues();
        ContentValues tourLocationResourceImageValues = new ContentValues();
        ContentValues locationFeatureEntryValues = new ContentValues();
        ContentValues locationFeatureResourceImageValues = new ContentValues();
        Context appContext = NorfolkTouring.getContext();
        Resources resources = appContext.getResources();
        // Insert Parks
        String category = appContext.getResources().getString(R.string.parks_category_label);
        // Town Point Park
        insertTourLocation(db, category, tourLocationEntryValues, tourLocationResourceImageValues,
                locationFeatureEntryValues, locationFeatureResourceImageValues,
                "Town Point Park",
                resources.getString(R.string.parks_town_point_park_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.parks_town_point_park1,
                        R.drawable.parks_town_point_park2
                )),
                "Waterside Drive, Norfolk, VA 23510",
                "(757) 664-6880",
                new ArrayList<TourLocation.LocationFeature>(Arrays.asList(
                        new TourLocation.LocationFeature("Public Parking Lots", null, null),
                        new TourLocation.LocationFeature("Stage / Auditorium", null, null),
                        new TourLocation.LocationFeature("Tables", null, null),
                        new TourLocation.LocationFeature("Trash Cans", null, null),
                        new TourLocation.LocationFeature("Water Splash Pad", null, null),
                        new TourLocation.LocationFeature("Waterfront", null, null)))
        );
        // Ocean View Beach Park
        insertTourLocation(db, category, tourLocationEntryValues, tourLocationResourceImageValues,
                locationFeatureEntryValues, locationFeatureResourceImageValues,
                "Ocean View Beach Park",
                resources.getString(R.string.parks_ocean_view_beach_park_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.parks_ocean_view_beach_park1,
                        R.drawable.parks_ocean_view_beach_park2,
                        R.drawable.parks_ocean_view_beach_park3
                )),
                "100 W. Ocean View Ave., Norfolk, VA 23503",
                null,
                null
        );
        // Community Beach Park
        insertTourLocation(db, category, tourLocationEntryValues, tourLocationResourceImageValues,
                locationFeatureEntryValues, locationFeatureResourceImageValues,
                "Community Beach Park",
                null,
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.parks_community_beach_park1,
                        R.drawable.parks_community_beach_park2,
                        R.drawable.parks_community_beach_park3
                )),
                "700 E. Ocean View Ave, Norfolk, VA 23503",
                null,
                null
        );
        // Sarah Constant Beach Park
        insertTourLocation(db, category, tourLocationEntryValues, tourLocationResourceImageValues,
                locationFeatureEntryValues, locationFeatureResourceImageValues,
                "Sarah Constant Beach Park",
                null,
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.parks_sarah_constant_beach_park1,
                        R.drawable.parks_sarah_constant_beach_park2,
                        R.drawable.parks_sarah_constant_beach_park3
                )),
                "300 W. Ocean View Avenue, Norfolk, VA 23510",
                null,
                null
        );

        // TODO: Insert Museums.
        category = appContext.getResources().getString(R.string.museums_category_label);
        // Chrysler Museum of Art
        insertTourLocation(db, category, tourLocationEntryValues, tourLocationResourceImageValues,
                locationFeatureEntryValues, locationFeatureResourceImageValues,
                "Chrysler Museum of Art",
                resources.getString(R.string.museum_chrysler_museum_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.museums_chrysler_museum_of_art1,
                        R.drawable.museums_chrysler_museum_of_art2,
                        R.drawable.museums_chrysler_museum_of_art3,
                        R.drawable.museums_chrysler_museum_of_art4,
                        R.drawable.museums_chrysler_museum_of_art5,
                        R.drawable.museums_chrysler_museum_of_art6,
                        R.drawable.museums_chrysler_museum_of_art7,
                        R.drawable.museums_chrysler_museum_of_art8
                )),
                "1 Memorial Pl, Norfolk, VA 23510",
                "(757) 664-6200",
                null);
        // Hampton Roads Naval Museum
        insertTourLocation(db, category, tourLocationEntryValues, tourLocationResourceImageValues,
                locationFeatureEntryValues, locationFeatureResourceImageValues,
                "Hampton Roads Naval Museum",
                resources.getString(R.string.museum_hampton_roads_naval_museum_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.military_hampton_roads_naval_museum1,
                        R.drawable.military_hampton_roads_naval_museum2
                )),
                "1 Waterside Dr, Norfolk, VA 23510",
                "(757) 322-2987",
                null);
        // Hermitage Museum & Gardens
        insertTourLocation(db, category, tourLocationEntryValues, tourLocationResourceImageValues,
                locationFeatureEntryValues, locationFeatureResourceImageValues,
                "Hermitage Museum & Gardens",
                resources.getString(R.string.museum_hermitage_museum_and_gardens_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.museums_hermitage_museum_and_gardens1,
                        R.drawable.museums_hermitage_museum_and_gardens1
                )),
                "7637 N Shore Rd, Norfolk, VA 23505",
                "(757) 423-2052",
                null);

        // TODO: Insert Military.

        // TODO: Insert Restaurants.

        // TODO: Insert Other.

    }

    void insertTourLocation(SQLiteDatabase db, String category,
                            ContentValues tourLocationEntryValues,
                            ContentValues tourLocationResourceImageValues,
                            ContentValues locationFeatureEntryValues,
                            ContentValues locationFeatureResourceImageValues,
                            String locationName, String description,
                            ArrayList<Integer> resourceImages, String address, String contactInfo,
                            ArrayList<TourLocation.LocationFeature> features) {
        db.beginTransaction();
        try {
            // Insert rows into TourLocationEntry table.
            tourLocationEntryValues.put(TourLocationEntry.COLUMN_CATEGORY, category);
            tourLocationEntryValues.put(TourLocationEntry.COLUMN_LOCATION_NAME, locationName);
            tourLocationEntryValues.put(TourLocationEntry.COLUMN_LOCATION_DESCRIPTION, description);
            tourLocationEntryValues.put(TourLocationEntry.COLUMN_LOCATION_ADDRESS, address);
            tourLocationEntryValues.put(TourLocationEntry.COLUMN_LOCATION_CONTACT_INFO, contactInfo);
            long tourLocationId = db.insert(TourLocationEntry.TABLE_NAME, null, tourLocationEntryValues);
            tourLocationEntryValues.clear();
            // Insert rows into TourLocationResourceImage table.
            if (resourceImages != null) {
                for (int resImgId : resourceImages) {
                    tourLocationResourceImageValues.put(TourLocationResourceImage.COLUMN_LOCATION_ID, tourLocationId);
                    tourLocationResourceImageValues.put(TourLocationResourceImage.COLUMN_RESOURCE_IMAGE, resImgId);
                    db.insert(TourLocationResourceImage.TABLE_NAME, null, tourLocationResourceImageValues);
                    tourLocationResourceImageValues.clear();
                }
            }
            // Insert rows into LocationFeatureEntry table.
            if (features != null) {
                for (TourLocation.LocationFeature feature : features) {
                    locationFeatureEntryValues.put(LocationFeatureEntry.COLUMN_LOCATION_ID, tourLocationId);
                    locationFeatureEntryValues.put(LocationFeatureEntry.COLUMN_FEATURE_NAME, feature.getName());
                    locationFeatureEntryValues.put(LocationFeatureEntry.COLUMN_FEATURE_DESCRIPTION, feature.getDescription());
                    long featureId = db.insert(LocationFeatureEntry.TABLE_NAME, null, locationFeatureEntryValues);
                    locationFeatureEntryValues.clear();
                    // Insert rows into LocationFeatureResourceImage table.
                    ArrayList<Integer> featureImgIds = feature.getImages();
                    if (featureImgIds != null) {
                        for (int featureResImgId : featureImgIds) {
                            locationFeatureResourceImageValues.put(LocationFeatureResourceImage.COLUMN_FEATURE_ID, featureId);
                            locationFeatureResourceImageValues.put(LocationFeatureResourceImage.COLUMN_FEATURE_IMAGE, featureResImgId);
                            db.insert(LocationFeatureResourceImage.TABLE_NAME, null, locationFeatureResourceImageValues);
                            locationFeatureResourceImageValues.clear();
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to insert TourLocation \"" +
                    locationName + "\" in category \"" + category + "\".");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * This is called when the version becomes larger than the database on the device.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Find out how to update the database without losing data.
        db.execSQL("DROP TABLE IF EXISTS " + TourLocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TourLocationResourceImage.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LocationFeatureEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LocationFeatureResourceImage.TABLE_NAME);
        onCreate(db);
    }
}
