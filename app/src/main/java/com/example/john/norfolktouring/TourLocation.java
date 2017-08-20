package com.example.john.norfolktouring;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.john.norfolktouring.Utils.AttributedPhoto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by John on 5/17/2017.
 */

public class TourLocation implements Parcelable {
    /*** Member Variables ***/
    private String mLocationName;
    private String mDescription;
    private ArrayList<Integer> mResourceImages;
    // Must be synchronized for obtaining images off of the main thread while cycling.
    private List<AttributedPhoto> mGoogleImages = Collections.synchronizedList(new ArrayList<AttributedPhoto>());
    private String mAddress;
    private String mContactInfo;
    private ArrayList<LocationFeature> mFeatures;

    // Geographic coordinates of this location.
    private Location mLocation;
    // Denotes whether this location is currently open.
    private Boolean mOpenNow;
    // The opening and closing hours for this location per day of the week.
    private DailyHours[] mWeeklyHours = new DailyHours[7];
    // The aggregated rating for this location based on Google reviews.
    private int mRating = RATING_NOT_DETERMINED;
    // The URL for this location's website.
    private String mWebsite;

    // Constants
    private static final String LOG_TAG = TourLocation.class.getCanonicalName();

    public static final int RATING_NOT_DETERMINED = -1;

    /*** Nested Classes ***/
    public class DailyHours {
        private int openTime = UNKNOWN_TIME;
        private int closeTime = UNKNOWN_TIME;

        public static final int UNKNOWN_TIME = -1;

        DailyHours(int openTime, int closeTime) {
            setOpenTime(openTime);
            setCloseTime(closeTime);
        }

        public int getOpenTime() {
            return openTime;
        }
        public int getCloseTime() {
            return closeTime;
        }

        public void setOpenTime(int openTime) {
            this.openTime = openTime;
        }
        public void setCloseTime(int closeTime) {
            this.closeTime = closeTime;
        }
    }

    /*** Methods ***/

    public TourLocation(String locationName, String description,
                        ArrayList<Integer> resourceImages,
                        String address, String contactInfo, ArrayList<LocationFeature> features) {
        this.mLocationName = locationName;
        this.mDescription = description;
        this.mResourceImages = resourceImages;
        this.mAddress = address;
        this.mContactInfo = contactInfo;
        this.mFeatures = (features != null) ? features : new ArrayList<LocationFeature>(0);
    }

    /** Getters and Setters **/

    public String getLocationName() {return mLocationName;}

    public String getDescription() {return mDescription;}

    public ArrayList<Integer> getResourceImages() {
        return mResourceImages;
    }

    public ArrayList<AttributedPhoto> getGoogleImages() {
        return new ArrayList<>(mGoogleImages);
    }

    public String getAddress() {
        return mAddress;
    }

    public String getContactInfo() {
        return mContactInfo;
    }

    public ArrayList<LocationFeature> getFeatures() {
        return mFeatures;
    }

    public Location getLocation() {
        return mLocation;
    }
    public void setLocation(Location location) {
        mLocation = location;
    }


    public Boolean getOpenNow() {return mOpenNow;}
    public void setOpenNow(Boolean openNow) {mOpenNow = openNow;}


    public DailyHours[] getWeeklyHours() {return mWeeklyHours;}
    public DailyHours getDailyHours(int day) {return getWeeklyHours()[day];}
    public void setHours(int day, int openTime, int closeTime) {
        mWeeklyHours[day] = new DailyHours(openTime, closeTime);
    }

    public int getRating() {return mRating;}
    public void setRating(int rating) {mRating = rating;}

    public String getWebsite() {return mWebsite;}
    public void setWebsite(String website) {mWebsite = website;}

    /**
     * Methods to implement Parcelable
     **/

    protected TourLocation(Parcel in) {
        mLocationName = in.readString();
        mDescription = in.readString();
        if (in.readByte() == 0x01) {
            mResourceImages = new ArrayList<Integer>();
            in.readList(mResourceImages, Integer.class.getClassLoader());
        } else {
            mResourceImages = null;
        }
        if (in.readByte() == 0x01) {
            mGoogleImages = new ArrayList<AttributedPhoto>();
            in.readList(mGoogleImages, AttributedPhoto.class.getClassLoader());
        } else {
            mGoogleImages = null;
        }
        mAddress = in.readString();
        mContactInfo = in.readString();
        if (in.readByte() == 0x01) {
            mFeatures = new ArrayList<LocationFeature>();
            in.readList(mFeatures, LocationFeature.class.getClassLoader());
        } else {
            mFeatures = null;
        }
        mLocation = (Location) in.readValue(Location.class.getClassLoader());
        byte mOpenNowVal = in.readByte();
        mOpenNow = mOpenNowVal == 0x02 ? null : mOpenNowVal != 0x00;
        mRating = in.readInt();
        mWebsite = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLocationName);
        dest.writeString(mDescription);
        if (mResourceImages == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mResourceImages);
        }
        if (mGoogleImages == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mGoogleImages);
        }
        dest.writeString(mAddress);
        dest.writeString(mContactInfo);
        if (mFeatures == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mFeatures);
        }
        dest.writeValue(mLocation);
        if (mOpenNow == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (mOpenNow ? 0x01 : 0x00));
        }
        dest.writeInt(mRating);
        dest.writeString(mWebsite);
    }

    @SuppressWarnings("unused")
    public static final Creator<TourLocation> CREATOR = new Creator<TourLocation>() {
        @Override
        public TourLocation createFromParcel(Parcel in) {
            return new TourLocation(in);
        }

        @Override
        public TourLocation[] newArray(int size) {
            return new TourLocation[size];
        }
    };

    /**
     * Describes a feature of a TourLocation.
     */
    public static class LocationFeature implements Parcelable {
        private String mName;
        private String mDescription;
        private ArrayList<Integer> mImages;

        public LocationFeature(String name, String description, ArrayList<Integer> images) {
            mName = name;
            mDescription = description;
            mImages = images;
        }

        public String getName() {return mName;}
        public String getDescription() {return mDescription;}
        public ArrayList<Integer> getImages() {return mImages;}

        /** Methods to implement Parcelable **/

        LocationFeature(Parcel in) {
            mName = in.readString();
            mDescription = in.readString();
            if (in.readByte() == 0x01) {
                mImages = new ArrayList<Integer>();
                in.readList(mImages, Integer.class.getClassLoader());
            } else {
                mImages = null;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mName);
            dest.writeString(mDescription);
            if (mImages == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeList(mImages);
            }
        }

        @SuppressWarnings("unused")
        public static final Creator<LocationFeature> CREATOR = new Creator<LocationFeature>() {
            @Override
            public LocationFeature createFromParcel(Parcel in) {
                return new LocationFeature(in);
            }

            @Override
            public LocationFeature[] newArray(int size) {
                return new LocationFeature[size];
            }
        };
    }
}
