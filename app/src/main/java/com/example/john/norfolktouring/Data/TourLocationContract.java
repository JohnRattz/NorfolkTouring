package com.example.john.norfolktouring.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by John on 9/2/2017.
 */

public class TourLocationContract {

    public static final String AUTHORITY = "com.example.john.norfolktouring";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    // Define the possible paths for accessing data in this contract.
    public static final String PATH_LOCATIONS = "locations";

    /**
     * Stores the primary information associated with TourLocations.
     */
    public static final class TourLocationEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build();

        public static final String TABLE_NAME = "tour_location";
        public static final String ALT_TABLE_NAME = "TLE";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_LOCATION_NAME = "location_name";
        public static final String COLUMN_LOCATION_DESCRIPTION = "location_description";
        public static final String COLUMN_LOCATION_ADDRESS = "location_address";
        public static final String COLUMN_LOCATION_CONTACT_INFO = "location_contact_info";

        /* Qualified versions of column names */
        public static final String QUALIFIED_ID = TABLE_NAME + "." + _ID;
        public static final String UNIQUE_ID = "tour_location_id";
        public static final String QUALIFIED_COLUMN_CATEGORY = TABLE_NAME + "." + COLUMN_CATEGORY;
        public static final String QUALIFIED_COLUMN_LOCATION_NAME = TABLE_NAME + "." + COLUMN_LOCATION_NAME;
        public static final String QUALIFIED_COLUMN_LOCATION_DESCRIPTION = TABLE_NAME + "." + COLUMN_LOCATION_DESCRIPTION;
        public static final String QUALIFIED_COLUMN_LOCATION_ADDRESS = TABLE_NAME + "." + COLUMN_LOCATION_ADDRESS;
        public static final String QUALIFIED_COLUMN_LOCATION_CONTACT_INFO = TABLE_NAME + "." + COLUMN_LOCATION_CONTACT_INFO;
    }

    /**
     * Stores integer values corresponding to resource images for TourLocations.
     */
    public static final class TourLocationResourceImage implements BaseColumns {
        public static final String TABLE_NAME = "location_resource_image";
        public static final String ALT_TABLE_NAME = "TLRI";
        // Foreign key to TourLocationEntry table.
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_RESOURCE_IMAGE = "location_image";

        /* Qualified versions of column names */
        public static final String QUALIFIED_ID = TABLE_NAME + "." + _ID;
        // Used to maintain BaseColumns._ID as the _ID from another table in a join.
        public static final String UNIQUE_ID = "TLRI_ID";
        public static final String QUALIFIED_COLUMN_LOCATION_ID = TABLE_NAME + "." + COLUMN_LOCATION_ID;
        public static final String QUALIFIED_COLUMN_RESOURCE_IMAGE = TABLE_NAME + "." + COLUMN_RESOURCE_IMAGE;
    }

    /**
     * Stores the primary information associated with LocationFeatures.
     */
    public static final class LocationFeatureEntry implements BaseColumns {
        public static final String TABLE_NAME = "location_feature";
        // Foreign key to TourLocationEntry table.
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_FEATURE_NAME = "feature_name";
        public static final String COLUMN_FEATURE_DESCRIPTION = "feature_description";

        /* Qualified versions of column names */
        public static final String QUALIFIED_ID = TABLE_NAME + "." + _ID;
        public static final String UNIQUE_ID = "LFE_ID";
        public static final String QUALIFIED_COLUMN_LOCATION_ID = TABLE_NAME + "." + COLUMN_LOCATION_ID;
        public static final String QUALIFIED_COLUMN_FEATURE_NAME = TABLE_NAME + "." + COLUMN_FEATURE_NAME;
        public static final String QUALIFIED_COLUMN_FEATURE_DESCRIPTION = TABLE_NAME + "." + COLUMN_FEATURE_DESCRIPTION;
    }

    /**
     * Stores integer values corresponding to resource images for LocationFeatures.
     */
    public static final class LocationFeatureResourceImage implements BaseColumns {
        public static final String TABLE_NAME = "location_feature_image";
        // Foreign key to LocationFeatureEntry table.
        public static final String COLUMN_FEATURE_ID = "feature_id";
        public static final String COLUMN_FEATURE_IMAGE = "feature_image";

        /* Qualified versions of column names */
        public static final String QUALIFIED_ID = TABLE_NAME + "." + _ID;
        public static final String UNIQUE_ID = "LFRI_ID";
        public static final String QUALIFIED_COLUMN_FEATURE_ID = TABLE_NAME + "." + COLUMN_FEATURE_ID;
        public static final String QUALIFIED_COLUMN_FEATURE_IMAGE = TABLE_NAME + "." + COLUMN_FEATURE_IMAGE;
    }
}
