package com.example.john.norfolktouring.Utils;

/**
 * Created by John on 6/21/2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;

import com.example.john.norfolktouring.R;

/**
 * Holder for an image and its attribution.
 */
public class AttributedPhoto implements Parcelable {

    public final CharSequence attribution;

    public final Bitmap bitmap;

    AttributedPhoto( CharSequence attribution, Bitmap bitmap) {
        this.attribution = attribution;
        this.bitmap = bitmap;
    }

    static public String getAttributionText(Context context, CharSequence attribution) {
        return String.format(context.getString(R.string.google_image_attribution),
                getAttributedName(attribution));
    }

    static public String getAttributedName(CharSequence attribution) {
        return Html.fromHtml(attribution.toString()).toString();
    }

    /********** Methods to implement Parcelable **********/

    private AttributedPhoto(Parcel in) {
        attribution = (CharSequence) in.readValue(CharSequence.class.getClassLoader());
        bitmap = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(attribution);
        dest.writeValue(bitmap);
    }

    @SuppressWarnings("unused")
    public static final Creator<AttributedPhoto> CREATOR = new Creator<AttributedPhoto>() {
        @Override
        public AttributedPhoto createFromParcel(Parcel in) {
            return new AttributedPhoto(in);
        }

        @Override
        public AttributedPhoto[] newArray(int size) {
            return new AttributedPhoto[size];
        }
    };
}