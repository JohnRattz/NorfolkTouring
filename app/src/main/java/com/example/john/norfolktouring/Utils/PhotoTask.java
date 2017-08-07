package com.example.john.norfolktouring.Utils;

/**
 * Created by John on 6/20/2017.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import java.util.List;

/**
 * Based on PhotoTask class presented
 * <a href="https://developers.google.com/places/android-api/photos">here</a>
 */
class PhotoTask extends AsyncTask<String, Void, Void> {
    private Activity mActivity;
    private int[] mWidths;
    private int[] mHeights;
    private GoogleApiClient mGoogleApiClient;
    // Images are inserted into this `List` as they are obtained.
    // This needs to be a synchronized `List`.
    private List<AttributedPhoto> mPlaceImages;

    // Constants
    private static final String LOG_TAG = PhotoTask.class.getCanonicalName();

    PhotoTask(List<AttributedPhoto> googleImages) {
        // Initialize mWidths and mHeights.
        initPhotoDims();
        mGoogleApiClient = PlacesUtils.getGoogleApiClient();
        mPlaceImages = googleImages;
    }

    /**
     * Determines the dimensions (mWidths and mHeights) of the photos to be obtained.
     * Dimensions are obtained from values for layout files
     * (2 files - location layout and detailed view). Only detail views are used.
     */
    private void initPhotoDims() {
        // I could not obtain these values from the corresponding View in the inflated layout.
        mWidths = new int[]{602};
        mHeights = new int[]{400};
    }

    /**
     * Loads the first 10 photos for a place from the Geo Data API.
     * The place ID must be the first (and only) parameter.
     */
    @Override
    protected Void doInBackground(String... params) {
        // Terminate if the parameters are invalid.
        if (params.length != 1) {
            Log.e(LOG_TAG, "doInBackground - only one place ID may be passed as a parameter!");
        } else if (mWidths.length != mHeights.length) {
            Log.e(LOG_TAG, "doInBackground - the number of widths and heights must be equal!");
        }

        // Retrieve the metadata for 10 photos associated with this location.
        final String placeID = params[0];
        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(mGoogleApiClient, placeID).await();

        // Extract bitmaps from the metadata.
        if (result.getStatus().isSuccess()) {
            PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
            int numPhotos = photoMetadataBuffer.getCount();
            int numBitmapsPerPhoto = mWidths.length;
            for (int photoIndx = 0; photoIndx < numPhotos && !isCancelled(); photoIndx++) {
                // Get the bitmap and its attributions.
                PlacePhotoMetadata photo = photoMetadataBuffer.get(photoIndx);
                CharSequence attribution = photo.getAttributions();
                for(int sizedBitmapIndx = 0;
                    sizedBitmapIndx < numBitmapsPerPhoto; sizedBitmapIndx++) {
                    int width = mWidths[sizedBitmapIndx];
                    int height = mHeights[sizedBitmapIndx];
                    // Load a scaled bitmap for this photo.
                    Bitmap photoBitmap = photo.getScaledPhoto(mGoogleApiClient, width, height)
                            .await().getBitmap();
                    AttributedPhoto attributedPhoto = new AttributedPhoto(attribution, photoBitmap);
                    mPlaceImages.add(attributedPhoto);
                }
            }
            // Release the PlacePhotoMetadataBuffer.
            photoMetadataBuffer.release();
        }
        return null;
    }
}