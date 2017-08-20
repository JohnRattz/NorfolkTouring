package com.example.john.norfolktouring.Utils;

import android.app.Activity;

import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.TourLocation;
import com.example.john.norfolktouring.TourLocationListFragment.TourLocationListFragment;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.john.norfolktouring.TourLocation.RATING_NOT_DETERMINED;

/**
 * Created by John on 6/20/2017.
 */

public class PlacesUtils {
    private static GoogleApiClient mGoogleApiClient;

    // Parks
    private static final String TOWN_POINT_PARK_ID = "ChIJXZz9rnSYuokRIlNgL39X8GM";
    private static final String OCEAN_VIEW_BEACH_PARK_ID = "ChIJQdx-U4yQuokR-DRvZRAbGO0";
    private static final String COMMUNITY_BEACH_PARK_ID = "ChIJwad7me2QuokRs6YBguwYQz4";
    private static final String SARAH_CONSTANT_BEACH_PARK_ID = "ChIJk7hLz4iQuokRD3WmA4t1hpY";

    // Museums
    private static final String CHRYSLER_MUSEUM_ID = "ChIJ-fHgXBOYuokRIcua3IJjjHI";
    private static final String HAMPTON_ROADS_NAVAL_MUSEUM_ID = "ChIJiQ2IUnOYuokRty1UExbaBOY";
    private static final String HERMITAGE_MUSEUM_AND_GARDENS_ID = "ChIJRX9T556ZuokRpSCB63_ooBE";

    // Military
    private static final String NAUTICUS_ID = "ChIJ1zaUT3OYuokROVBWHBE5tG0";
    private static final String MACARTHUR_MEMORIAL_ID = "ChIJi66QIQyYuokRps2pupEVr0k";

    // Restaurants
    private static final String FRANCOS_ID = "ChIJAf8GdeCWuokREy1j3b8azqg";
    private static final String AZALEA_INN_ID = "ChIJ71EWatOWuokRsEDmD59AARI";
    private static final String PARADISE_PIZZERA_ID = "ChIJn4BmvBCXuokRryB9CDHl8Fk";
    private static final String REGINOS_ID = "ChIJ9R2Bs0uRuokRurUjjaI3L9g";
    private static final String DOUMARS_ID = "ChIJu_kvkDyYuokRRLq_fM_PsUs";
    private static final String HANDSOME_BISCUIT_ID = "ChIJBbQLYzeYuokRLNHpW2YgiSY";

    // Other
    private static final String NORFOLK_BOTANICAL_GARDEN_ID = "ChIJETolPM-WuokR7LPd_NPbPXA";
    private static final String VIRIGNIA_ZOO_ID = "ChIJoXYXhiuYuokRQMe7LLQSdRc";
    private static final String AMERICAN_ROVER_ID = "ChIJqcRevAuYuokRerkT09cYkCA";

    private static final Map<String, String> PLACE_NAME_TO_ID =
            new HashMap<String, String>() {{
                // Parks
                put("Town Point Park", TOWN_POINT_PARK_ID);
                put("Ocean View Beach Park", OCEAN_VIEW_BEACH_PARK_ID);
                put("Community Beach Park", COMMUNITY_BEACH_PARK_ID);
                put("Sarah Constant Beach Park", SARAH_CONSTANT_BEACH_PARK_ID);

                // Museums
                put("Chrysler Museum of Art", CHRYSLER_MUSEUM_ID);
                put("Hampton Roads Naval Museum", HAMPTON_ROADS_NAVAL_MUSEUM_ID);
                put("Hermitage Museum & Gardens", HERMITAGE_MUSEUM_AND_GARDENS_ID);

                // Military
                put("Nauticus", NAUTICUS_ID);
                put("MacArthur Memorial", MACARTHUR_MEMORIAL_ID);

                // Restaurants
                put("Franco's", FRANCOS_ID);
                put("Azalea Inn & Time Out Sports Bar", AZALEA_INN_ID);
                put("Paradise Pizzeria", PARADISE_PIZZERA_ID);
                put("Regino's Italian Restaurant", REGINOS_ID);
                put("Doumar's Cones & Barbecue", DOUMARS_ID);
                put("Handsome Biscuit", HANDSOME_BISCUIT_ID);

                // Other
                put("Norfolk Botanical Garden", NORFOLK_BOTANICAL_GARDEN_ID);
                put("The Virginia Zoo", VIRIGNIA_ZOO_ID);
                put("American Rover", AMERICAN_ROVER_ID);
            }};

    /*** Methods ***/

    public static String getPlaceIdByName(String placeName) {
        return PLACE_NAME_TO_ID.get(placeName);
    }

    /**
     * Get the `Location`, hours of operation, rating, and website for a `TourLocation`
     * if appropriate.
     */
    public static void getInfoForTourLocationIfNeeded(Activity activity,
                                                      TourLocation tourLocation) {
        List<TourLocation> tourLocations = new ArrayList<>(Arrays.asList(tourLocation));
        getInfoForTourLocations(activity, tourLocations);
    }

    /**
     * Get the `Location`, hours of operation, rating, and website for the `TourLocation`s
     * if appropriate.
     */
    public static void getInfoForTourLocationsIfNeeded(Activity activity,
                                                       ArrayList<TourLocation> locations) {
        boolean infoNeeded = false;
        // No checks for hours of operation or website because they are not consistently available.
        for (TourLocation tourLocation : locations) {
            // Check the location.
            if (tourLocation.getLocation() == null) {
                infoNeeded = true;
                break;
            }
            // Check the rating.
            if (tourLocation.getRating() == RATING_NOT_DETERMINED) {
                infoNeeded = true;
                break;
            }
        }
        if (infoNeeded)
            getInfoForTourLocations(activity, locations);
    }

    /**
     * Retrieves information for `tourLocation` asynchronously.
     * See `getInfoForTourLocations()` documentation for information acquired.
     * @param tourLocation A `TourLocation` to acquire information for.
     */
    public static void getInfoForTourLocation(Activity activity, TourLocation tourLocation) {
        List<TourLocation> tourLocations = new ArrayList<>(Arrays.asList(tourLocation));
        getInfoForTourLocations(activity, tourLocations);
    }

    /**
     * Acquires location, hours of operation, rating, and the website for `tourLocations`.
     */
    private static void getInfoForTourLocations(Activity activity,
                                               List<TourLocation> tourLocations) {
        List<String> placeNames = new ArrayList<>(tourLocations.size());
        // Get all `TourLocation` names.
        for (TourLocation tourLocation : tourLocations)
            placeNames.add(tourLocation.getLocationName());
        ArrayList<String> placeIDs = getPlaceIdsByNames(placeNames);
        // Extract the underlying array.
        String[] placeIDarr = new String[placeIDs.size()];
        placeIDarr = placeIDs.toArray(placeIDarr);
        // Acquire the desired data asynchronously.
        InfoByIdsTask infoByIdsTask = new InfoByIdsTask(activity, tourLocations);
        infoByIdsTask.execute(placeIDarr);
    }

    private static ArrayList<String> getPlaceIdsByNames(List<String> placeNames) {
        ArrayList<String> placeIDs = new ArrayList<>(placeNames.size());
        for(String placeName : placeNames)
            placeIDs.add(getPlaceIdByName(placeName));
        return placeIDs;
    }

    /** Methods for Images **/

    /**
     * Uses the Google Places API (within Geo Data API) to acquire 10 photos for a location.
     * @param placeName The name of the place for which more images are desired.
     */
    public static void getPlaceImagesInBackground(String placeName,
                                                  List<AttributedPhoto> googleImages) {
        String placeID = getPlaceIdByName(placeName);
        PhotoTask photoTask = new PhotoTask(googleImages);
        photoTask.execute(placeID);
    }

    /** Methods for the Google API Client **/

    public static void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    public static GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
