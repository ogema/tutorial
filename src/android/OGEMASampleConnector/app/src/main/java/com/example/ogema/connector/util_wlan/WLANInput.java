package com.example.ogema.connector.util_wlan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dnestle on 31.08.2015.
 */
public class WLANInput implements Parcelable {
    public String configFileDirectory;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(configFileDirectory);
    }

    public static final Parcelable.Creator<WLANInput> CREATOR = new Creator<WLANInput>() {
         public WLANInput createFromParcel(Parcel source) {
            WLANInput mObj = new WLANInput();
            mObj.configFileDirectory = source.readString();
            return mObj;
        }
        public WLANInput[] newArray(int size) {
            return new WLANInput[size];
        }
    };

}
