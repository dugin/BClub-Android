package io.bclub.controller.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import io.bclub.model.City;

public class CitySearchResponse implements Parcelable {

    public final int currentPage;
    public final List<City> list;

    public CitySearchResponse(int currentPage, List<City> list) {
        this.currentPage = currentPage;
        this.list = list;
    }

    protected CitySearchResponse(Parcel in) {
        currentPage = in.readInt();
        list = null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(currentPage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CitySearchResponse> CREATOR = new Creator<CitySearchResponse>() {
        @Override
        public CitySearchResponse createFromParcel(Parcel in) {
            return new CitySearchResponse(in);
        }

        @Override
        public CitySearchResponse[] newArray(int size) {
            return new CitySearchResponse[size];
        }
    };
}
