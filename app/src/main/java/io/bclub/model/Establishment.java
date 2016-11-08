package io.bclub.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import io.bclub.util.Constants;

public class Establishment implements Parcelable {

    public final String objectId;
    public final String name;
    public final String description;
    public final String categoryName;
    public final String categoryIcon;
    public final String email;
    public final String addressLine;
    public final String neighborhood;
    public final ArrayList<String> telephones;
    public final double distance;
    public final Location location;

    public Establishment(String objectId, String name, String description,
                         String categoryName, String categoryIcon, String email,
                         String addressLine, String neighborhood, ArrayList<String> telephones,
                         double distance, Location location) {

        this.objectId = objectId;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.email = email;
        this.addressLine = addressLine;
        this.neighborhood = neighborhood;
        this.telephones = telephones;
        this.distance = distance;
        this.location = location;
    }

    protected Establishment(Parcel in) {
        objectId = in.readString();
        name = in.readString();
        description = in.readString();
        categoryName = in.readString();
        categoryIcon = in.readString();
        email = in.readString();
        addressLine = in.readString();
        neighborhood = in.readString();
        telephones = in.createStringArrayList();
        distance = in.readDouble();
        location = in.readParcelable(Location.class.getClassLoader());
    }

    @NonNull
    public String getFormattedDistance() {
        if (distance == -1D) {
            return "";
        }

        return Constants.DECIMAL_FORMAT.format(distance).concat(" km");
    }

    public static final Creator<Establishment> CREATOR = new Creator<Establishment>() {
        @Override
        public Establishment createFromParcel(Parcel in) {
            return new Establishment(in);
        }

        @Override
        public Establishment[] newArray(int size) {
            return new Establishment[size];
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
        dest.writeString(description);
        dest.writeString(categoryName);
        dest.writeString(categoryIcon);
        dest.writeString(email);
        dest.writeString(addressLine);
        dest.writeString(neighborhood);
        dest.writeStringList(telephones);
        dest.writeDouble(distance);
        dest.writeParcelable(location, flags);
    }
}
