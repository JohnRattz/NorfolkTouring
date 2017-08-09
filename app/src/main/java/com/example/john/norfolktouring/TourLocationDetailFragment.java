package com.example.john.norfolktouring;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.john.norfolktouring.NavigationIconClickListeners.DirectionsIconClickListener;
import com.example.john.norfolktouring.NavigationIconClickListeners.MapIconClickListener;
import com.example.john.norfolktouring.Utils.AttributedPhoto;
import com.example.john.norfolktouring.Utils.PlacesUtils;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.john.norfolktouring.NorfolkTouring.setActionBarTitle;
import static com.example.john.norfolktouring.Utils.AttributedPhoto.getAttributionText;

/**
 * Created by John on 6/1/2017.
 */

public class TourLocationDetailFragment extends Fragment {
    /*** Member Variables ***/
    MainActivity mActivity;
    TourLocation mTourLocation;
    ArrayList<TourLocation.LocationFeature> mFeatures;
    View mRootView;
    @BindView(R.id.location_details_google_image_powered_by_google_image_view)
    ImageView mSmallViewPoweredByGoogle;
    @BindView(R.id.expanded_image_powered_by_google_image_view)
    ImageView mEnlargedViewPoweredByGoogle;
    @BindView(R.id.expanded_image_attribution_text_view)
    TextView mEnlargedViewImageAttribution;
    @BindView(R.id.feature_list)
    LinearLayout mFeaturesListView;

    /** ButterKnife **/
    // Unbind views for this `Fragment` in `onDestroyView()`
    private Unbinder mButterKnifeUnbinder;

    /** Image Cycling **/
    // Resource images.
    ArrayList<Integer> mResourceImages;
    @BindView(R.id.location_details_resource_image_view)
    ImageView mLocationResourcesImageView;
    // The index into an array of image resources to cycle through
    // in this detailed view.
    int mImageResourceIndx = 0;
    // Google images (acquired with Google Places API).
    // This needs to be a synchronized List.
    ArrayList<AttributedPhoto> mGoogleImages;
    @BindView(R.id.location_details_google_image_view)
    ImageView mLocationGoogleImageView;
    @BindView(R.id.location_details_google_image_attribution_text_view)
    TextView mLocationGoogleImageAttributionView;
    int mGoogleImageIndx = 0;
    // All image cyclers.
    ArrayList<Cycler> mImageCyclers;
    // Milliseconds between cycling images.
    final int mMillisBetweenCycles = 6000;
    /** Image Zoom Variables **/
    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;
    // The system "short" animation time duration, in milliseconds.
    private int mShortAnimationDuration;
    // The arrows used to select the image in the enlarged view.
    @BindView(R.id.expanded_image)                  ImageView mEnlargedImageView;
    @BindView(R.id.expanded_image_animation_target) View mEnlargedImageViewAnimTarget;
    @BindView(R.id.prev_image_arrow)                ImageView mBackArrowImageView;
    @BindView(R.id.next_image_arrow)                ImageView mForwardArrowImageView;

    // Used to determine which set of images are currently in the enlarged image view.
    private enum EnlargedImageSet {
        RESOURCE_IMAGES, GOOGLE_IMAGES
    }
    private EnlargedImageSet mEnlargedImageSet;

    /** Remaining Views (below resource and Google images) **/
    @BindView(R.id.location_details_name_view)            TextView mNameTextView;
    @BindView(R.id.location_rating_detail_view)           LinearLayout mRatingView;
    @BindView(R.id.location_hours_detail_view_container)  LinearLayout mHoursContainerView;
    @BindView(R.id.location_open_status_detail_view)      TextView mOpenStatusView;
    @BindView(R.id.location_website_detail_view)          TextView mWebsiteView;
    @BindView(R.id.location_details_address_view)         TextView mAddressTextView;
    @BindView(R.id.location_details_contact_info_view)    TextView mContactInfoTextView;
    @BindView(R.id.location_details_features_header_view) TextView mFeaturesHeaderView;
    @BindView(R.id.location_details_description_view)     TextView mDescriptionTextView;
    @BindView(R.id.location_details_distance_view)        TextView mLocationDistanceView;
    @BindView(R.id.google_maps_detail_view)               View mGoogleMapsView;
    @BindView(R.id.google_maps_route_plan_detail_view)    View mGoogleMapsRoutePlanView;

    /** Constants **/
    private static final int NUM_FEATURES_COLUMNS = 2;

    // Strings for storing state information.
    private static final String TOUR_LOCATION = "mTourLocation";
    private static final String RESOURCE_IMAGES = "mResourceImages";
    private static final String IMAGE_RESOURCE_INDX = "mImageResourceIndx";
    private static final String GOOGLE_IMAGES = "mGoogleImages";
    private static final String GOOGLE_IMAGE_INDX = "mGoogleImageIndx";
    private static final String SHORT_ANIMATION_DURATION = "mShortAnimationDuration";

    private static final String LOG_TAG = TourLocationDetailFragment.class.getCanonicalName();

    public static final String FRAGMENT_LABEL = "detail";

    /*** Nested Classes ***/

    /**
     * Groups a `Handler` together with its `Runnable` for `Handler`s with only one `Runnable`.
     */
    class Cycler {
        Handler mHandler;
        Runnable mRunnable;

        Cycler(Handler handler, Runnable runnable) {
            mHandler = handler;
            mRunnable = runnable;
        }

        public void start() {
            mHandler.post(mRunnable);
        }

        public void restart() {
            mHandler.postDelayed(mRunnable, mMillisBetweenCycles);
        }

        public void stop() {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Click listener for features.
     */
    private static class FeatureClickListener implements View.OnClickListener {
        private final boolean mHasImages;
        private final boolean mHasDescription;
        private ImageView mExpandedStateCaretView;
        private ExpandableRelativeLayout mFeatureContentContainer;
        private ImageView mFeatureImageView;
        private TextView mFeatureDescriptionView;
        private Cycler mFeatureImageCycler;
        private boolean mHasFeatureImageCycler;

        FeatureClickListener(boolean hasImages, boolean hasDescription,
                             ImageView expandedStateCaretView,
                             ExpandableRelativeLayout featureContentContainer,
                             ImageView featureImageView, TextView featureDescriptionView,
                             Cycler featureImageCycler) {
            mHasImages = hasImages;
            mHasDescription = hasDescription;
            mExpandedStateCaretView = expandedStateCaretView;
            mFeatureContentContainer = featureContentContainer;
            mFeatureImageView = featureImageView;
            mFeatureDescriptionView = featureDescriptionView;
            mFeatureImageCycler = featureImageCycler;
            mHasFeatureImageCycler = (featureImageCycler != null);
        }

        private void stopFeatureImageCycler() {
            mFeatureImageCycler.stop();
        }

        private void startFeatureImageCycler() {
            mFeatureImageCycler.start();
        }

        @Override
        public void onClick(View v) {
            // Ensure that the content is visible (this is necessary AFAIK - another
            // peculiarity of the library `AAkira/ExpandableLayout`).
            mFeatureContentContainer.setVisibility(View.VISIBLE);
            // Toggle the visibility of the feature image and description.
            boolean featureDetailsShowing = mFeatureContentContainer.isExpanded();
            if (!featureDetailsShowing) {
                // Show an up caret to indicate that this content can be hidden.
                mExpandedStateCaretView.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                mFeatureContentContainer.expand();
                // Only show what needs to be shown.
                if (mHasImages && mHasFeatureImageCycler)
                    startFeatureImageCycler();
            } else {
                // Show a down caret to indicate that this content can be shown.
                mExpandedStateCaretView.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                // Only hide what needs to be hidden.
                if (mHasImages && mHasFeatureImageCycler)
                    stopFeatureImageCycler();
                mFeatureContentContainer.collapse();
            }
        }
    }

    /*** Methods ***/

    /**
     * Creates and adds feature `View`s to the list of features (done "statically").
     */
    private void addFeatures() {
        for (int featureIndx = 0; featureIndx < mFeatures.size(); featureIndx++) {
            TourLocation.LocationFeature feature = mFeatures.get(featureIndx);
            // Create a `View` for this feature.
            View featureView = getFeatureView(mFeaturesListView, feature);
            mFeaturesListView.addView(featureView);
        }
    }

    /**
     * Creates a `View` for a location feature (`LocationFeature`).
     * @param parent  The containing `ViewGroup`.
     * @param feature The `LocationFeature` to create a `View` for.
     */
    private View getFeatureView(ViewGroup parent, TourLocation.LocationFeature feature) {
        View featureView = LayoutInflater.from(mActivity).inflate(
                R.layout.feature, parent, false);

        // Get a reference to the `ImageView` for carets denoting expandability of this view.
        ImageView expandedStateCaretView =
                (ImageView) featureView.findViewById(R.id.expanded_state_caret_view);

        // Get the {@link LocationFeature} object located at this position in the list.
        String featureHeaderText = feature.getName();
        final ArrayList<Integer> images = feature.getImages();
        String description = feature.getDescription();

        // Set the feature header.
        TextView featureHeader = (TextView) featureView.findViewById(R.id.feature_header);
        featureHeader.setText(featureHeaderText);

        // Get the the image and description (the "content") as well as the container `View`.
        final ExpandableRelativeLayout featureContentContainer =
                (ExpandableRelativeLayout) featureView.findViewById(R.id.feature_content);
        final ImageView featureImageView = (ImageView) featureView.findViewById(R.id.feature_image);
        TextView featureDescriptionView = (TextView) featureView.findViewById(R.id.feature_description);

        // Set the initial image resource if one exists.
        boolean hasImages;
        if (images != null) {
            if (images.size() > 0) {
                hasImages = true;
                featureImageView.setImageResource(feature.getImages().get(0));
            } else {
                hasImages = false;
            }
        } else {
            hasImages = false;
        }

        // Set feature description.
        boolean hasDescription;
        if (description != null) {
            hasDescription = true;
            featureDescriptionView.setText(description);
        } else {
            hasDescription = false;
        }

        // If there is no image nor description for this feature, do not display a caret.
        if (!hasImages && !hasDescription)
            expandedStateCaretView.setVisibility(View.GONE);

        // Create an image cycler for this feature.
        Cycler featureImageCycler = null;
        if (hasImages) {
            final Handler featureImageCyclerHandler = new Handler();
            Runnable featureImageCyclerRunnable = new Runnable() {
                private ArrayList<Integer> mFeatureImages = images;
                private ImageView mFeatureImageView = featureImageView;
                private int mImageIndx = 0;

                public void run() {
                    // Go to the next image.
                    mImageIndx = (mImageIndx + 1) % mFeatureImages.size();
                    // Set the image.
                    mFeatureImageView.setImageResource(mFeatureImages.get(mImageIndx));
                    // Schedule this function to run again (cycling images).
                    featureImageCyclerHandler.postDelayed(this, mMillisBetweenCycles);
                }
            };
            // Start the image cycler.
            featureImageCyclerHandler.post(featureImageCyclerRunnable);
            // Add this feature image cycler to the list of image cyclers.
            featureImageCycler =
                    new Cycler(featureImageCyclerHandler, featureImageCyclerRunnable);
            mImageCyclers.add(featureImageCycler);
        }

        // Set a click listener on this feature to show and hide the image and description.
        featureView.setOnClickListener(new FeatureClickListener(hasImages, hasDescription,
                expandedStateCaretView, featureContentContainer, featureImageView,
                featureDescriptionView, featureImageCycler));
        // Trigger a click artificially to get the views working correctly
        // (this is just a peculiarity of the library `AAkira/ExpandableLayout`).
        featureView.performClick();

        return featureView;
    }

    private Bitmap getBitmapFromImageView(ImageView imageView) {
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }

    /**
     * Lifecycle Methods
     **/

    @Override
    public void onStart() {
        super.onStart();
        // Start the image cycling Handlers.
        restartCyclers();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        // Record that this Fragment is the currently displayed one in `MainActivity`.
        mActivity.setCurrentFragment(this);

        // Create a list of image cyclers to start and stop along with this fragment.
        mImageCyclers = new ArrayList<>();

        mRootView = inflater.inflate(R.layout.location_detail_view, container, false);
        // Set up the ButterKnife unbinder.
        mButterKnifeUnbinder = ButterKnife.bind(this, mRootView);

        // Load saved variable values.
        if (savedInstanceState != null) {
            mTourLocation = savedInstanceState.getParcelable(TOUR_LOCATION);
            mResourceImages = savedInstanceState.getIntegerArrayList(RESOURCE_IMAGES);
            mImageResourceIndx = savedInstanceState.getInt(IMAGE_RESOURCE_INDX);
            mGoogleImages = savedInstanceState.getParcelableArrayList(GOOGLE_IMAGES);
            mGoogleImageIndx = savedInstanceState.getInt(GOOGLE_IMAGE_INDX);
            mShortAnimationDuration = savedInstanceState.getInt(SHORT_ANIMATION_DURATION);
        } else { // Extract the Fragment bundle arguments.
            Bundle bundle = getArguments();
            mTourLocation = bundle.getParcelable("location");
            mResourceImages = mTourLocation.getResourceImages();
            mImageResourceIndx = 0;
            mGoogleImages = mTourLocation.getGoogleImages();
            mGoogleImageIndx = 0;
            // Retrieve and cache the system's default "short" animation time.
            mShortAnimationDuration = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
        }
        String name = mTourLocation.getLocationName();

        setActionBarTitle(mActivity, name);

        // Set the first images in the Resources and Google image views.
        updateResourceImage();
        updateGoogleImage();

        // Set up click listeners for the image selection arrows for the enlarged view.
        mBackArrowImageView.setOnClickListener(new OnImageBackNavigationClickListener());
        mForwardArrowImageView.setOnClickListener(new OnImageForwardNavigationClickListener());

        // Start acquiring the Google images in the background.
        PlacesUtils.getPlaceImagesInBackground(name, mGoogleImages);

        // Set the resource images in the view (cycle through them automatically, one at a time).
        final Handler resourceImageCyclingHandler = new Handler();
        Runnable resourceImageCycler = new Runnable() {
            public void run() {
                // Go to the next image.
                setResourceImageIndexNext();
                // Set the image.
                updateResourceImage();
                // Schedule this function to run again (cycling images).
                resourceImageCyclingHandler.postDelayed(this, mMillisBetweenCycles);
            }
        };
        // Add the resource image cycler to the list of image cyclers.
        mImageCyclers.add(new Cycler(resourceImageCyclingHandler, resourceImageCycler));

        // Set the Google images in the view (cycle through them automatically, one at a time).
        final Handler googleImageCyclingHandler = new Handler();
        Runnable googleImageCycler = new Runnable() {
            public void run() {
                // Wait for there to be at least one image.
                waitForGoogleImages();
                // Go to the next image.
                setGoogleImageIndexNext();
                // Set the image and attribution.
                updateGoogleImage();
                // Schedule this function to run again (cycling images).
                googleImageCyclingHandler.postDelayed(this, mMillisBetweenCycles);
            }
        };
        // Add the Google image cycler to the list of image cyclers.
        mImageCyclers.add(new Cycler(googleImageCyclingHandler, googleImageCycler));

        // Schedule the cyclers' first runs.
        startCyclers();

        // Set click listeners on the ImageViews to show larger, interactive versions of them.
        mLocationResourcesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the cycling handlers.
                stopCyclers();
                // Note that we are using the resource images in the enlarged image view.
                mEnlargedImageSet = EnlargedImageSet.RESOURCE_IMAGES;
                zoomImageFromThumb(mRootView, mLocationResourcesImageView,
                        mEnlargedImageView, mEnlargedImageViewAnimTarget,
                        getBitmapFromImageView(mLocationResourcesImageView));
            }
        });
        mLocationGoogleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the cycling handlers.
                stopCyclers();
                // Note that we are using the Google images in the enlarged image view.
                mEnlargedImageSet = EnlargedImageSet.GOOGLE_IMAGES;
                zoomImageFromThumb(mRootView, mLocationGoogleImageView,
                        mEnlargedImageView, mEnlargedImageViewAnimTarget,
                        getBitmapFromImageView(mLocationGoogleImageView));
            }
        });

        String address = mTourLocation.getAddress();
        String contactInfo = mTourLocation.getContactInfo();
        mFeatures = mTourLocation.getFeatures();
        String description = mTourLocation.getDescription();

        // Set the name in the view.
        mNameTextView.setText(name);

        // Set the rating in the view.
        int rating = mTourLocation.getRating();
        for (int starIndx = 0; starIndx < 5; starIndx++) {
            ImageView starView = (ImageView) mRatingView.getChildAt(starIndx);
            if (starIndx < rating)
                starView.setImageResource(R.drawable.ic_star_black_24dp);
            else
                starView.setImageResource(R.drawable.ic_star_border_black_24dp);
        }

        // Set the hours of operation in the view if there are any.
        for (int day = 0; day < 7; day++) {
            TourLocation.DailyHours dailyHours = mTourLocation.getDailyHours(day);
            if (dailyHours != null) {
                int openingHours = dailyHours.getOpenTime();
                int closingHours = dailyHours.getCloseTime();
                boolean openingPM = openingHours >= 1200;
                boolean closingPM = closingHours >= 1200;
                if (openingHours >= 1300) openingHours -= 1200;
                if (closingHours >= 1300) closingHours -= 1200;
                // Determine where to put the colons in the formatted text.
                int openingHoursColonIndx = openingHours >= 1000 ? 2 : 1;
                int closingHoursColonIndx = closingHours >= 1000 ? 2 : 1;
                String openingHoursFormatted = new StringBuilder(Integer.toString(openingHours))
                        .insert(openingHoursColonIndx, ':')
                        .append(openingPM ? "PM" : "AM").toString();
                String closingHoursFormatted = new StringBuilder(Integer.toString(closingHours))
                        .insert(closingHoursColonIndx, ':')
                        .append(closingPM ? "PM" : "AM").toString();
                // Acquire a reference to the `View` and set the text.
                TextView hoursView =
                        (TextView) ((LinearLayout) mHoursContainerView.getChildAt(day)).getChildAt(1);
                hoursView.setText(openingHoursFormatted + " - \n" + closingHoursFormatted);
            }
        }

        // Set the open status (whether this location is currently open).
        Boolean locationIsOpen = mTourLocation.getOpenNow();
        if (locationIsOpen == null)
            mOpenStatusView.setText(R.string.open_status_unavailable);
        else if (locationIsOpen)
            mOpenStatusView.setText(R.string.location_open);
        else
            mOpenStatusView.setText(R.string.location_closed);

        // Set the website in the view.
        String websiteURL = mTourLocation.getWebsite();
        if (websiteURL != null) {
            mWebsiteView.setText(websiteURL);
        } else {
            TextView websiteHeaderView = (TextView) mRootView.findViewById(R.id.location_website_header_detail_view);
            websiteHeaderView.setVisibility(View.GONE);
            mWebsiteView.setVisibility(View.GONE);
        }

        // Set the address in the view.
        mAddressTextView.setText(address);

        // Set the contact info in the view if there is any.
        if (contactInfo != null) {
            mContactInfoTextView.setText(contactInfo);
        } else {
            // Remove the "Contact Info:" header and contact info.
            TextView contactInfoHeaderView =
                    (TextView) mRootView.findViewById(R.id.location_details_contact_info_header_view);
            contactInfoHeaderView.setVisibility(View.GONE);
            mContactInfoTextView.setVisibility(View.GONE);
        }

        // Set the features in the view.
        if (mFeatures.size() > 0) {
            addFeatures();
        } else {
            // Remove the "Features:" header and the list.
            mFeaturesHeaderView.setVisibility(View.GONE);
            mFeaturesListView.setVisibility(View.GONE);
        }

        // Set the detailed description in the view.
        mDescriptionTextView.setText(description);

        // Set the distance text for this location.
        updateUILocationDistance();

        // Set a click listener on the Google Maps icon and text.
        mGoogleMapsView.setOnClickListener(new MapIconClickListener(mActivity, mTourLocation));

        // Set a click listener on the Google Maps Route Plan icon and text.
        mGoogleMapsRoutePlanView.setOnClickListener(
                new DirectionsIconClickListener(mActivity,
                        mTourLocation, mActivity.getCurrentLocation()));

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(TOUR_LOCATION, mTourLocation);
        savedInstanceState.putIntegerArrayList(RESOURCE_IMAGES, mResourceImages);
        savedInstanceState.putInt(IMAGE_RESOURCE_INDX, mImageResourceIndx);
        savedInstanceState.putParcelableArrayList(GOOGLE_IMAGES, mGoogleImages);
        savedInstanceState.putInt(GOOGLE_IMAGE_INDX, mGoogleImageIndx);
        savedInstanceState.putInt(SHORT_ANIMATION_DURATION, mShortAnimationDuration);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop the image cycling Handlers.
        stopCyclers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mButterKnifeUnbinder.unbind();
    }

    /** Animation Methods **/

    /**
     * The following function and its documentation is courtesy of
     * <a href="https://developer.android.com/training/animation/zoom.html">Google</a>
     * Many modifications were added, such as arrows used for changing the image displayed.
     * <p>
     * "Zooms" in a thumbnail view by assigning the high resolution image to a hidden "zoomed-in"
     * image view and animating its bounds to fit the entire activity content area. More
     * specifically:
     * <p>
     * <ol>
     * <li>Assign the high-res image to the hidden "zoomed-in" (expanded) image view.</li>
     * <li>Calculate the starting and ending bounds for the expanded view.</li>
     * <li>Animate each of four positioning/sizing properties (X, Y, SCALE_X, SCALE_Y)
     * simultaneously, from the starting bounds to the ending bounds.</li>
     * <li>Zoom back out by running the reverse animation on click.</li>
     * </ol>
     *
     * @param thumbView        The thumbnail view to zoom in.
     * @param largeImageBitmap The high-resolution version of the image represented by the thumbnail.
     */
    private void zoomImageFromThumb(View rootView, final View thumbView,
                                    final ImageView expandedView,
                                    final View expandedViewAnimTarget, Bitmap largeImageBitmap) {
        // If there's an animation in progress, cancel it immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Set the enlarged image view to show the image (but the view is currently invisible).
        updateEnlargedImageView();

        // Calculate the starting and ending bounds for the zoomed-in image. This step
        // involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail, and the
        // final bounds are the global visible rectangle of the container view. Also
        // set the container view's offset as the origin for the bounds, since that's
        // the origin for the positioning animation properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getActivity().findViewById(R.id.content_frame).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final bounds using the
        // "center crop" technique. This prevents undesirable stretching during the animation.
        // Also calculate the start scaling factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
        // the zoomed-in view (the default is the center of the view).
        expandedViewAnimTarget.setPivotX(0f);
        expandedViewAnimTarget.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedViewAnimTarget, View.X, startBounds.left,
                        finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedViewAnimTarget, View.Y, startBounds.top,
                        finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedViewAnimTarget, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedViewAnimTarget, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Hide the thumbnail and show the zoomed-in view. When the animation begins,
                // it will position the zoomed-in view in the place of the thumbnail.
                // Also handle the cases of clicking on the view not enlarged
                // while the enlarged view is visible.
                thumbView.setAlpha(0f);
                if (thumbView == mLocationResourcesImageView) {
                    setEnlargedViewAttributionsInvisible();
                    // If the Google image view was previously
                    // being viewed, it is still transparent.
                    setGoogleImageViewOpaque();
                    setSmallViewAttributionsVisible();
                } else {
                    setSmallViewAttributionsInvisible();
                    // If the Resource image view was previously
                    // being viewed, it is still transparent.
                    setResourceImageViewOpaque();
                }
                expandedView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                terminateAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                terminateAnimation();
            }

            private void terminateAnimation() {
                // Set the image selection arrows to be visible.
                setImageSelectArrowsVisible();
                if (thumbView == mLocationGoogleImageView)
                    setEnlargedViewAttributionsVisible();
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
        // and show the thumbnail instead of the expanded image.
        final float startScaleFinal = startScale;
        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel, back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(expandedViewAnimTarget, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedViewAnimTarget, View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedViewAnimTarget, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedViewAnimTarget, View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setImageSelectArrowsInvisible();
                        if (thumbView == mLocationGoogleImageView)
                            setEnlargedViewAttributionsInvisible();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        terminateAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        terminateAnimation();
                    }

                    private void terminateAnimation() {
                        thumbView.setAlpha(1f);
                        if (thumbView == mLocationGoogleImageView)
                            setSmallViewAttributionsVisible();
                        expandedView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                        // Restart the image cycling handlers.
                        restartCyclers();
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    /**
     * Methods for enlarged view
     **/

    private void updateEnlargedImageView() {
        if (mEnlargedImageSet == EnlargedImageSet.RESOURCE_IMAGES) {
            updateResourceImageEnlargedView();
        } else {
            updateGoogleImageEnlargedView();
        }
    }

    private void updateEnlargedImageViewAttribution() {
        String fullAttributionString =
                getAttributionText(getActivity(), getCurrentGooglePhotoAttribution());
        mEnlargedViewImageAttribution.setText(fullAttributionString);
        mEnlargedViewImageAttribution.setText(getAttributionText(getActivity(), getCurrentGooglePhotoAttribution()));
    }

    /**
     * Methods for Google image attributions ("Powered by Google" and image attributions)
     **/

    private void setSmallViewAttributionsVisible() {
        mSmallViewPoweredByGoogle.setVisibility(View.VISIBLE);
        mLocationGoogleImageAttributionView.setVisibility(View.VISIBLE);
    }

    private void setSmallViewAttributionsInvisible() {
        mSmallViewPoweredByGoogle.setVisibility(View.INVISIBLE);
        mLocationGoogleImageAttributionView.setVisibility(View.INVISIBLE);
    }

    private void setEnlargedViewAttributionsVisible() {
        mEnlargedViewPoweredByGoogle.setVisibility(View.VISIBLE);
        mEnlargedViewImageAttribution.setVisibility(View.VISIBLE);
    }

    private void setEnlargedViewAttributionsInvisible() {
        mEnlargedViewPoweredByGoogle.setVisibility(View.INVISIBLE);
        mEnlargedViewImageAttribution.setVisibility(View.INVISIBLE);
    }

    /**
     * Methods for the Resource images
     **/

    private int getCurrentResourceImageIndex() {
        return mImageResourceIndx;
    }

    /**
     * Sets the resource image view to be the image specified by `index`.
     *
     * @param index
     */
    private void setResourceImageByIndex(int index) {
        mLocationResourcesImageView.setImageResource(mResourceImages.get(index));
    }

    /**
     * Updates the Resource image in the Resource image view.
     */
    private void updateResourceImage() {
        if (mResourceImages.size() == 0)
            return;
        int currentResourceImage = getCurrentResourceImage();
        mLocationResourcesImageView.setImageResource(currentResourceImage);
    }

    /**
     * Updates the Resource image in the enlarged image view.
     */
    private void updateResourceImageEnlargedView() {
        if (mResourceImages.size() == 0)
            return;
        int currentResourceImage = getCurrentResourceImage();
        mEnlargedImageView.setImageResource(currentResourceImage);
    }

    private int getCurrentResourceImage() {
        return mResourceImages.get(getCurrentResourceImageIndex());
    }

    private Bitmap getCurrentResourceImageBitmap() {
        return BitmapFactory.decodeResource(getResources(),
                getCurrentResourceImage());
    }

    private int getResourceImageIndexPrev() {
        if (mImageResourceIndx < 1)
            return mResourceImages.size() - 1;
        else
            return mImageResourceIndx - 1;
    }

    private void setResourceImageIndexPrev() {
        mImageResourceIndx = getResourceImageIndexPrev();
    }

    private int getResourceImageIndexNext() {
        return (mImageResourceIndx + 1) % mResourceImages.size();
    }

    private void setResourceImageIndexNext() {
        mImageResourceIndx = getResourceImageIndexNext();
    }

    private void setResourceImageViewOpaque() {
        mLocationResourcesImageView.setAlpha(1f);
    }

    /**
     * Methods for the Google images
     **/

    private int getCurrentGoogleImageIndex() {
        return mGoogleImageIndx;
    }

    private AttributedPhoto getGoogleImageByIndex(int index) {
        return mGoogleImages.get(index);
    }

    /**
     * Updates the Google image and attribution text in the Google image view.
     */
    private void updateGoogleImage() {
        if (mGoogleImages.isEmpty())
            return;
        AttributedPhoto attributedGooglePhoto = mGoogleImages.get(getCurrentGoogleImageIndex());
        // Set the image and attribution in the Google image view...
        mLocationGoogleImageView.setImageBitmap(attributedGooglePhoto.bitmap);
        String fullAttributionString =
                getAttributionText(getActivity(), attributedGooglePhoto.attribution);
        mLocationGoogleImageAttributionView.setText(fullAttributionString);
    }

    /**
     * Updates the Google image and attribution text in the enlarged image view.
     */
    private void updateGoogleImageEnlargedView() {
        if (mGoogleImages.isEmpty())
            return;
        AttributedPhoto attributedGooglePhoto = mGoogleImages.get(getCurrentGoogleImageIndex());
        mEnlargedImageView.setImageBitmap(attributedGooglePhoto.bitmap);
        updateEnlargedImageViewAttribution();
    }

    private String getCurrentGooglePhotoAttribution() {
        return mGoogleImages.get(getCurrentGoogleImageIndex()).attribution.toString();
    }

    private Bitmap getCurrentGoogleImageBitmap() {
        return mGoogleImages.get(getCurrentGoogleImageIndex()).bitmap;
    }

    private int getGoogleImageIndexPrev() {
        if (mGoogleImageIndx < 1)
            return mGoogleImages.size() - 1;
        else
            return mGoogleImageIndx - 1;
    }

    private void setGoogleImageIndexPrev() {
        mGoogleImageIndx = getGoogleImageIndexPrev();
    }

    private int getGoogleImageIndexNext() {
        return (mGoogleImageIndx + 1) % mGoogleImages.size();
    }

    private void setGoogleImageIndexNext() {
        mGoogleImageIndx = getGoogleImageIndexNext();
    }

    private void setGoogleImageViewOpaque() {
        mLocationGoogleImageView.setAlpha(1f);
    }

    /**
     * Blocks until at least one Google image has been retrieved.
     */
    private void waitForGoogleImages() {
        while (mGoogleImages.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Methods for the enlarged view
     **/

    private void setEnlargedBitmap(Bitmap bitmap) {
        mEnlargedImageView.setImageBitmap(bitmap);
    }

    /** Methods for the image cycling Handlers **/

    /**
     * Starts the image cycling Handlers.
     */
    private void startCyclers() {
        for (Cycler cycler : mImageCyclers)
            cycler.start();
    }

    /**
     * Restarts the image cycling Handlers.
     */
    private void restartCyclers() {
        for (Cycler cycler : mImageCyclers)
            cycler.restart();
    }

    /**
     * Stops the image cycling Handlers.
     */
    private void stopCyclers() {
        for (Cycler cycler : mImageCyclers)
            cycler.stop();
    }

    /** Methods for the image selection arrows for the enlarged view **/

    /**
     * Makes the image selection arrows for the enlarged image view visible.
     */
    private void setImageSelectArrowsVisible() {
        mBackArrowImageView.setVisibility(View.VISIBLE);
        mForwardArrowImageView.setVisibility(View.VISIBLE);
    }

    /**
     * Makes the image selection arrows for the enlarged image view invisible.
     */
    private void setImageSelectArrowsInvisible() {
        mBackArrowImageView.setVisibility(View.INVISIBLE);
        mForwardArrowImageView.setVisibility(View.INVISIBLE);
    }

    /** Click listeners for the image selection arrows for the enlarged view **/

    /**
     * The click listener for the left arrow (changes to previous image).
     */
    private class OnImageBackNavigationClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mEnlargedImageSet == EnlargedImageSet.RESOURCE_IMAGES) {
                setResourceImageIndexPrev();
                setResourceImageByIndex(mImageResourceIndx);
                setEnlargedBitmap(getCurrentResourceImageBitmap());
            } else {
                setGoogleImageIndexPrev();
                updateGoogleImage();
                setEnlargedBitmap(getCurrentGoogleImageBitmap());
                updateEnlargedImageViewAttribution();
            }
        }
    }

    /**
     * The click listener for the right arrow (changes to next image).
     */
    private class OnImageForwardNavigationClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mEnlargedImageSet == EnlargedImageSet.RESOURCE_IMAGES) {
                setResourceImageIndexNext();
                setResourceImageByIndex(mImageResourceIndx);
                setEnlargedBitmap(getCurrentResourceImageBitmap());
            } else {
                setGoogleImageIndexNext();
                updateGoogleImage();
                setEnlargedBitmap(getCurrentGoogleImageBitmap());
                updateEnlargedImageViewAttribution();
            }
        }
    }

    /**
     * Location Updates Methods
     **/

    public void locationCallback() {
        updateUILocationDistance();
    }

    /**
     * Updates the relevant portion of the UI regarding the distance
     * of this `TourLocation` from the device.
     *
     * @return `true` if the `TourLocation` has a `Location`, and `false` otherwise.
     */
    private boolean updateUILocationDistance() {
        // Set the distance text for this location.
        Location location = mTourLocation.getLocation();
        Location deviceLocation = mActivity.getCurrentLocation();
        // If the `Location`s for both this `TourLocation` and the device have been determined...
        if (location != null && deviceLocation != null) {
            // Set the appropriate text in the corresponding `View` in the list.
            String formatString = getActivity().getString(R.string.location_distance_detail_view);
            mLocationDistanceView.setText(
                    String.format(formatString, (int) deviceLocation.distanceTo(location)));
        }
        return location != null;
    }
}
