package com.example.john.norfolktouring.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.InputFilter;

import com.example.john.norfolktouring.TourLocation;

import static com.example.john.norfolktouring.Data.TourLocationContract.*;

/**
 * Created by John on 9/2/2017.
 */

public class TourLocationContentProvider extends ContentProvider {
    // TODO: Add support for more paths?
    // Constants corresponding to paths.
    public static final int LOCATIONS = 100;
    public static final int LOCATION_WITH_NAME = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Note which URI structures to recognize and the integer constants they match with.
        // Directory
        uriMatcher.addURI(AUTHORITY, PATH_LOCATIONS, LOCATIONS);
        // Single item
        uriMatcher.addURI(AUTHORITY, PATH_LOCATIONS + "/#", LOCATION_WITH_NAME);

        return uriMatcher;
    }

    // TODO: To support concurrency, this probably needs to have synchronized methods.
    private static TourLocationDbHelper sTourLocationDbHelper;
    public static TourLocationDbHelper getTourLocationHelper() {return sTourLocationDbHelper;}

    @Override
    public boolean onCreate() {
        sTourLocationDbHelper = new TourLocationDbHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = sTourLocationDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch(match) {
            case LOCATIONS:
                // Insert values into the tour locations table.
                long id = db.insert(TourLocationEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(TourLocationEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            // We can't insert data into just one row for the LOCATION_WITH_NAME case.
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }
        // Notify the resolver if the URI has been changed, and return the newly inserted URI.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = sTourLocationDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor returnCursor;
        switch (match) {
            case LOCATIONS:
                String category = selectionArgs[0];
                // Ensure that the _ID column from TourLocationEntry is the _ID column in the result cursor.
                final String columnsToSelectTLE =
                        TourLocationEntry.QUALIFIED_ID + " AS " + TourLocationEntry._ID + ", " +
                        /*TourLocationEntry.QUALIFIED_COLUMN_CATEGORY + ", " +*/
                        TourLocationEntry.QUALIFIED_COLUMN_LOCATION_NAME + ", " +
                        TourLocationEntry.QUALIFIED_COLUMN_LOCATION_DESCRIPTION + ", " +
                        TourLocationEntry.QUALIFIED_COLUMN_LOCATION_ADDRESS + ", " +
                        TourLocationEntry.QUALIFIED_COLUMN_LOCATION_CONTACT_INFO + ", ";
                final String columnsToSelectTLRI =
                        TourLocationResourceImage.QUALIFIED_ID + " AS " + TourLocationResourceImage.UNIQUE_ID + ", " +
                        /*TourLocationResourceImage.QUALIFIED_COLUMN_LOCATION_ID + ", " +*/
                        TourLocationResourceImage.QUALIFIED_COLUMN_RESOURCE_IMAGE + ", ";
                final String columnsToSelectLFE =
                        LocationFeatureEntry.QUALIFIED_ID + " AS " + LocationFeatureEntry.UNIQUE_ID + ", " +
                        /*LocationFeatureEntry.QUALIFIED_COLUMN_LOCATION_ID + ", " +*/
                        LocationFeatureEntry.QUALIFIED_COLUMN_FEATURE_NAME + ", " +
                        LocationFeatureEntry.QUALIFIED_COLUMN_FEATURE_DESCRIPTION + ", ";
                final String columnsToSelectLFRI =
                        /*LocationFeatureResourceImage.QUALIFIED_ID + " AS " + LocationFeatureResourceImage.UNIQUE_ID + ", " +*/
                        /*LocationFeatureResourceImage.QUALIFIED_COLUMN_FEATURE_ID + ", " +*/
                        LocationFeatureResourceImage.QUALIFIED_COLUMN_FEATURE_IMAGE;
                final String columnsToSelect = columnsToSelectTLE + columnsToSelectTLRI +
                        columnsToSelectLFE + columnsToSelectLFRI;
                // Join tables.
                // Join TourLocation table with the TourLocation resource images table.
                // Rename the _ID field of the TourLocationEntry table in the join.
                /*final String renamedTLECols = TourLocationEntry.QUALIFIED_ID + " AS " + TourLocationEntry.UNIQUE_ID + ", " +
                        TourLocationEntry.COLUMN_CATEGORY + ", " + TourLocationEntry.COLUMN_LOCATION_NAME + ", " +
                        TourLocationEntry.COLUMN_LOCATION_DESCRIPTION + ", " + TourLocationEntry.COLUMN_LOCATION_ADDRESS + ", " +
                        TourLocationEntry.COLUMN_LOCATION_CONTACT_INFO;*/
//                final String renamedTLRICols = ;
//                final String joinCondition =
//                        TourLocationEntry.UNIQUE_ID + "=" + TourLocationResourceImage.QUALIFIED_COLUMN_LOCATION_ID;
                final String firstJoin = /*"( SELECT * " +
                        "FROM " + TourLocationEntry.TABLE_NAME + " " +
                        "LEFT JOIN " + TourLocationResourceImage.TABLE_NAME + " ON " +
                        TourLocationEntry.QUALIFIED_ID + "=" + TourLocationResourceImage.QUALIFIED_COLUMN_LOCATION_ID + " " +
                        "UNION ALL " +
                        "SELECT * " +
                        "FROM " + TourLocationResourceImage.TABLE_NAME + " " +
                        "LEFT JOIN " + TourLocationEntry.TABLE_NAME + " ON " +
                        TourLocationEntry.QUALIFIED_ID + "=" + TourLocationResourceImage.QUALIFIED_COLUMN_LOCATION_ID + " )";*/
                "(" + TourLocationEntry.TABLE_NAME + " LEFT JOIN " +
                        TourLocationResourceImage.TABLE_NAME + " ON " +
                        TourLocationEntry.QUALIFIED_ID + "=" + TourLocationResourceImage.QUALIFIED_COLUMN_LOCATION_ID + ")";
//                String.format("(%s LEFT JOIN ((SELECT %s FROM %s) %s) ON %s)",
//                        TourLocationEntry.TABLE_NAME,
//                        renamedTLRICols, TourLocationResourceImage.TABLE_NAME, TourLocationResourceImage.ALT_TABLE_NAME,
//                        joinCondition);
                // Join the table just constructed with the LocationFeature table.
                final String secondJoinFormat = "(%s LEFT JOIN " +
                        LocationFeatureEntry.TABLE_NAME + " ON " +
                        TourLocationEntry.QUALIFIED_ID + "=" + LocationFeatureEntry.QUALIFIED_COLUMN_LOCATION_ID + ")";
                final String secondJoin = String.format(secondJoinFormat, firstJoin);
                // Join the table just constructed with the LocationFeature resource images table.
                final String thirdJoinFormat = "(%s LEFT JOIN " +
                        LocationFeatureResourceImage.TABLE_NAME + " ON " +
                        LocationFeatureEntry.QUALIFIED_ID + "=" + LocationFeatureResourceImage.QUALIFIED_COLUMN_FEATURE_ID + ")";
                final String tables = String.format(thirdJoinFormat, secondJoin);
                final String whereClause = " WHERE " + TourLocationEntry.QUALIFIED_COLUMN_CATEGORY + "='" + category + "'";
                final String query = /*"SELECT * FROM " + TourLocationEntry.TABLE_NAME;*/
                /*"SELECT * FROM " + TourLocationResourceImage.TABLE_NAME;*/
                /*"SELECT * FROM " + LocationFeatureEntry.TABLE_NAME;*/
                /*"SELECT * FROM " + LocationFeatureResourceImage.TABLE_NAME;*/
                "SELECT " + columnsToSelect + " FROM " + tables + whereClause;
                /*"SELECT " + columnsToSelect + " FROM " + secondJoin;*/
                // TODO: Remove these when done debugging.
                int count;
                int colcount;
                try {
                    // TODO: Remove these four Strings and test queries when done debugging.
                    final String SQLTLETest = "SELECT * FROM " + TourLocationEntry.TABLE_NAME;
                    db.rawQuery(SQLTLETest, null);
                    final String SQLTLRITest = "SELECT * FROM " + TourLocationResourceImage.TABLE_NAME;
                    db.rawQuery(SQLTLRITest, null);
                    final String SQLLFETest = "SELECT * FROM " + LocationFeatureEntry.TABLE_NAME;
                    db.rawQuery(SQLLFETest, null);
                    final String SQLLFRITest = "SELECT * FROM " + LocationFeatureResourceImage.TABLE_NAME;
                    db.rawQuery(SQLLFRITest, null);
                    returnCursor = db.rawQuery(query, null);
                    count = returnCursor.getCount();
                    colcount = returnCursor.getColumnCount();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
//                returnCursor = db.query(TourLocationContract.TourLocationEntry.TABLE_NAME,
//                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Register the ContentResolver as a listener for changes in this URI
        // to update the cursor's data accordingly.
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
