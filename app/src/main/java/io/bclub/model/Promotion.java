package io.bclub.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Promotion implements Parcelable {

    public final String objectId;
    public final double percent;
    public final String[] weekDays;

    public Promotion(String objectId, double percent, String[] weekDays) {
        this.objectId = objectId;
        this.percent = percent;
        this.weekDays = weekDays;
    }

    protected Promotion(Parcel in) {
        objectId = in.readString();
        percent = in.readDouble();
        weekDays = in.createStringArray();
    }

    public static final Creator<Promotion> CREATOR = new Creator<Promotion>() {
        @Override
        public Promotion createFromParcel(Parcel in) {
            return new Promotion(in);
        }

        @Override
        public Promotion[] newArray(int size) {
            return new Promotion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(objectId);
        dest.writeDouble(percent);
        dest.writeStringArray(weekDays);
    }
}
