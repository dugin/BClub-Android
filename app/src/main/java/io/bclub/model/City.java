package io.bclub.model;

import android.os.Parcel;
import android.os.Parcelable;

public class City implements Parcelable {

    public final String objectId;
    public final String name;

    public City(String objectId, String name) {
        this.objectId = objectId;
        this.name = name;
    }

    protected City(Parcel in) {
        objectId = in.readString();
        name = in.readString();
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(objectId);
        dest.writeString(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof City)) {
            return false;
        }

        return objectId != null && objectId.equals(((City) o).objectId);
    }
}
