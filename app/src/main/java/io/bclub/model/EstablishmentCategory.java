package io.bclub.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EstablishmentCategory implements Parcelable {

    public final String id;
    public final String name;
    public final String imageUrl;

    public boolean checked;

    public EstablishmentCategory(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    protected EstablishmentCategory(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();

        checked = (boolean) in.readValue(EstablishmentCategory.class.getClassLoader());
    }

    public static final Creator<EstablishmentCategory> CREATOR = new Creator<EstablishmentCategory>() {
        @Override
        public EstablishmentCategory createFromParcel(Parcel in) {
            return new EstablishmentCategory(in);
        }

        @Override
        public EstablishmentCategory[] newArray(int size) {
            return new EstablishmentCategory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);

        dest.writeValue(checked);
    }
}
