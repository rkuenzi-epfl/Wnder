package com.github.wnder.picture;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class describing a parcelable picture
 */
public class Picture implements Parcelable {
    private final String uniqueId;
    private final double picLat;
    private final double picLng;

    /**
     * Constructor for a locally stored picture
     * @param uniqueId unique ID
     */
    public Picture(String uniqueId, double lat, double lng) {
        this.uniqueId = uniqueId;
        this.picLat = lat;
        this.picLng = lng;
    }

    /**
     * Constructor for the the Parcel
     * @param in parcel for the Parcel constructor
     */
    protected Picture(Parcel in) {
        uniqueId = in.readString();
        picLat = in.readDouble();
        picLng = in.readDouble();
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
     * Get latitude of the picture
     * @return real location
     */
    public double getPicLat(){
        return this.picLat;
    }

    /**
     * Get longitude of the picture
     * @return real location
     */
    public double getPicLng(){
        return this.picLng;
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
        dest.writeDouble(picLat);
        dest.writeDouble(picLng);
    }
}
