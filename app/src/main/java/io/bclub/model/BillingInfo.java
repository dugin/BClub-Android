package io.bclub.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BillingInfo implements Parcelable {

    String stripeToken;

    public BillingInfo(String stripeToken) {
        this.stripeToken = stripeToken;
    }

    protected BillingInfo(Parcel in) {
        stripeToken = in.readString();
    }

    public static final Creator<BillingInfo> CREATOR = new Creator<BillingInfo>() {
        @Override
        public BillingInfo createFromParcel(Parcel in) {
            return new BillingInfo(in);
        }

        @Override
        public BillingInfo[] newArray(int size) {
            return new BillingInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stripeToken);
    }
}
