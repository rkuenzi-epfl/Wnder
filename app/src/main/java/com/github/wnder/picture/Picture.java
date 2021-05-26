package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class describing a locally stored picture Parcelable
 */
public class Picture implements Parcelable {
    private final String uniqueId;
    private final Bitmap bmp;
    private final Location picLocation;

    /**
     * Constructor for a locally stored picture
     * @param uniqueId unique ID
     * @param bmp picture
     */
    public Picture(String uniqueId, Bitmap bmp, Location loc) {
        this.uniqueId = uniqueId;
        this.bmp = bmp;
        this.picLocation = loc;
    }

    /**
     * Constructor for the the Parcel
     * @param in parcel for the Parcel constructor
     */
    protected Picture(Parcel in) {
        uniqueId = in.readString();
        bmp = in.readParcelable(Bitmap.class.getClassLoader());
        picLocation = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator<Picture> CREATOR = new Creator<Picture>() {
        /**
         * Create a Picture from a parcel
         * @param in parcel to create from
         * @return the Picture created with the parcel in
         */
        @Override
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        /**
         * Create a new Picture array
         * @param size of the Picture Array
         * @return the Picture Array
         */
        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };

    /**
     * Get id of local picture
     * @return unique ID
     */
    public String getUniqueId(){
        return this.uniqueId;
    }

    /**
     * Get bitmap of local picture
     * @return bitmap
     */
    public Bitmap getBitmap(){
        return this.bmp;
    }

    /**
     * Get real location of picture
     * @return real location
     */
    public Location getPicLocation(){
        return this.picLocation;
    }

    /**
     * @return 0 by default
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten the Picture object into a Parcel
     * @param dest parcel to write on
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uniqueId);
        dest.writeValue(bmp);
        dest.writeValue(picLocation);
    }
}
