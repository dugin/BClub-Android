package io.bclub.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

public class EstablishmentPromotion implements Parcelable {

    public final Establishment establishment;
    public final List<Promotion> promotions;

    public final String objectId;
    public final Date featuredDate;
    public final String restriction;
    public final String detail;
    public final String imageUrl;

    public final boolean morning;
    public final boolean afternoon;
    public final boolean night;

    public final boolean active;

    public final boolean[] weekDays;

    public EstablishmentPromotion(Establishment establishment, List<Promotion> promotions, String objectId, Date featuredDate, String restriction, String detail, String imageUrl, boolean morning, boolean afternoon, boolean night, boolean active) {
        this.establishment = establishment;
        this.promotions = promotions;

        this.objectId = objectId;
        this.featuredDate = featuredDate;
        this.restriction = restriction;
        this.detail = detail;
        this.imageUrl = imageUrl;
        this.morning = morning;
        this.afternoon = afternoon;
        this.night = night;

        this.active = active;

        weekDays = new boolean[7];

        prepareWorkdays();
    }

    protected EstablishmentPromotion(Parcel in) {
        ClassLoader cl = EstablishmentPromotion.class.getClassLoader();

        establishment = in.readParcelable(Establishment.class.getClassLoader());
        promotions = in.createTypedArrayList(Promotion.CREATOR);

        objectId = in.readString();
        restriction = in.readString();
        detail = in.readString();
        imageUrl = in.readString();
        morning = (boolean) in.readValue(cl);
        afternoon = (boolean) in.readValue(cl);
        night = (boolean) in.readValue(cl);

        active = (boolean) in.readValue(cl);

        weekDays = in.createBooleanArray();

        long timestamp = in.readLong();

        if (timestamp == -1L) {
            featuredDate = null;
        } else {
            featuredDate = new Date();
            featuredDate.setTime(timestamp);
        }
    }

    void prepareWorkdays() {
        for (int i = 0, size = promotions.size(); i < size; ++i) {
            Promotion promotion = promotions.get(i);

            weekDays[0] |= promotion.weekDays[0] != null;
            weekDays[1] |= promotion.weekDays[1] != null;
            weekDays[2] |= promotion.weekDays[2] != null;
            weekDays[3] |= promotion.weekDays[3] != null;
            weekDays[4] |= promotion.weekDays[4] != null;
            weekDays[5] |= promotion.weekDays[5] != null;
            weekDays[6] |= promotion.weekDays[6] != null;
        }
    }

    public static final Creator<EstablishmentPromotion> CREATOR = new Creator<EstablishmentPromotion>() {
        @Override
        public EstablishmentPromotion createFromParcel(Parcel in) {
            return new EstablishmentPromotion(in);
        }

        @Override
        public EstablishmentPromotion[] newArray(int size) {
            return new EstablishmentPromotion[size];
        }
    };

    @Nullable
    public Promotion getFirst() {
        if (promotions == null || promotions.isEmpty()) {
            return null;
        }

        return promotions.get(0);
    }

    @Nullable
    public Promotion getLast() {
        if (promotions == null || promotions.isEmpty()) {
            return null;
        }

        return promotions.get(promotions.size() - 1);
    }

    public boolean isSinglePromotion() {
        return promotions != null && promotions.size() == 1;
    }

    public boolean hasDistanceCalculated() {
        return establishment.distance != -1D;
    }

    public EstablishmentPromotion copyPromotionsAndOwnProperties(EstablishmentPromotion other) {
        if (other.promotions != null) {
            return new EstablishmentPromotion(establishment, other.promotions, objectId, other.featuredDate, other.restriction, other.detail, other.imageUrl, other.morning, other.afternoon, other.night, other.active);
        }

        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(establishment, flags);
        dest.writeTypedList(promotions);

        dest.writeString(objectId);
        dest.writeString(restriction);
        dest.writeString(detail);
        dest.writeString(imageUrl);
        dest.writeValue(morning);
        dest.writeValue(afternoon);
        dest.writeValue(night);
        dest.writeValue(active);

        dest.writeBooleanArray(weekDays);

        if (featuredDate != null) {
            dest.writeLong(featuredDate.getTime());
        } else {
            dest.writeLong(-1L);
        }
    }
}
