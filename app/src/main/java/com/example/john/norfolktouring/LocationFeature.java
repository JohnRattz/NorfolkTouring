package com.example.john.norfolktouring;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by John on 7/6/2017.
 */
public class LocationFeature implements Parcelable {
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

    protected LocationFeature(Parcel in) {
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
