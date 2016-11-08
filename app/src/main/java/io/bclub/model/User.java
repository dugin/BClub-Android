package io.bclub.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class User implements Parcelable {

    public final String objectId;

    public final String plan;
    public final String voucher;

    public final Date validUntil;
    public final Date subscriptionDate;

    public final boolean paymentSucceeded;

    public User(String objectId, String plan, String voucher, Date validUntil, Date subscriptionDate, boolean paymentSucceeded) {
        this.objectId = objectId;
        this.plan = plan;
        this.subscriptionDate = subscriptionDate;
        this.voucher = voucher;
        this.validUntil = validUntil;
        this.paymentSucceeded = paymentSucceeded;
    }

    protected User(Parcel in) {
        objectId = in.readString();
        plan = in.readString();
        voucher = in.readString();
        paymentSucceeded = (boolean) in.readValue(User.class.getClassLoader());

        long timestamp = in.readLong();

        if (timestamp != -1L) {
            validUntil = new Date();
            validUntil.setTime(timestamp);
        } else {
            validUntil = null;
        }

        timestamp = in.readLong();

        if (timestamp != -1L) {
            subscriptionDate = new Date();
            subscriptionDate.setTime(timestamp);
        } else {
            subscriptionDate = null;
        }
    }

    public boolean mustSubscribePlan() {
        Date now = new Date();

        return validUntil == null
                || (!validUntil.after(now) && plan != null)
                || (!validUntil.after(now) && voucher != null);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(objectId);
        dest.writeString(plan);
        dest.writeString(voucher);
        dest.writeValue(paymentSucceeded);

        if (validUntil != null) {
            dest.writeLong(validUntil.getTime());
        } else {
            dest.writeLong(-1L);
        }

        if (subscriptionDate != null) {
            dest.writeLong(subscriptionDate.getTime());
        } else {
            dest.writeLong(-1L);
        }
    }
}
