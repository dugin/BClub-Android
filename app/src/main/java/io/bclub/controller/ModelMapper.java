package io.bclub.controller;

import android.location.Location;
import android.support.annotation.Nullable;

import com.backendless.BackendlessUser;
import com.backendless.geo.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.bclub.model.Establishment;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.model.Promotion;
import io.bclub.model.User;
import io.bclub.model.backendless.BackendlessAddress;
import io.bclub.model.backendless.BackendlessEstablishment;
import io.bclub.model.backendless.BackendlessEstablishmentCategory;
import io.bclub.model.backendless.BackendlessEstablishmentPromotion;
import io.bclub.model.backendless.BackendlessPromotion;
import io.bclub.model.backendless.BackendlessVoucher;

public abstract class ModelMapper {
    private ModelMapper() { }

    public static Establishment buildEstablishment(BackendlessEstablishment object, @Nullable Location location) {
        String id = object.getObjectId();
        String name = object.getName();
        String description = object.getDetail();
        String email = object.getEmail();

        String categoryName = null;
        String categoryLabel = null;
        String categoryIcon = null;

        String addressLine = null;
        String neighborhood = null;

        ArrayList<String> telephones = null;
        double distance;

        Location establishmentLocation = null;

        if (object.getCategory() != null) {
            BackendlessEstablishmentCategory category = object.getCategory();

            categoryName = category.getName();
            categoryIcon = category.getIcon();
        }

        if (object.getAddress() != null) {
            BackendlessAddress address = object.getAddress();

            addressLine = address.toString();
            neighborhood = address.getNeighborhood().getName();

            if (address.getGeolocation() != null) {
                GeoPoint geoPoint = address.getGeolocation();

                establishmentLocation = new Location("Backendless");

                establishmentLocation.setLatitude(geoPoint.getLatitude());
                establishmentLocation.setLongitude(geoPoint.getLongitude());
            }

            telephones = object.getTelephoneNumbers();
        }

        if (establishmentLocation != null && location != null) {
            distance = establishmentLocation.distanceTo(location) / 1000D;
        } else {
            distance = -1D;
        }

        return new Establishment(id, name, description, categoryName, categoryIcon, email, addressLine, neighborhood, telephones, distance, establishmentLocation);
    }

    public static List<Promotion> buildPromotions(BackendlessEstablishmentPromotion object) {
        List<BackendlessPromotion> list = object.getPromotions();
        ArrayList<Promotion> promotions = new ArrayList<>(list.size());

        for (int i = 0, size = list.size(); i < size; ++i) {
            BackendlessPromotion backendlessPromotion = list.get(i);
            promotions.add(buildPromotion(backendlessPromotion));
        }

        return promotions;
    }

    public static EstablishmentPromotion buildEstablishmentPromotion(BackendlessEstablishmentPromotion object, @Nullable Location location) {
        Establishment establishment = ModelMapper.buildEstablishment(object.getEstablishment(), location);
        List<Promotion> promotions = sortPromotions(ModelMapper.buildPromotions(object));

        return new EstablishmentPromotion(establishment, promotions, object.getObjectId(), object.getFeaturedDate(), object.getRestriction(), null, object.getImageUrl(), object.getMorning(), object.getAfternoon(), object.getNight(), object.getActive());
    }

    public static Promotion buildPromotion(BackendlessPromotion object) {
        double percent = object.getPercent() == null ? -1D : object.getPercent();
        String[] workdays = new String[]{object.getMonday(), object.getTuesday(), object.getWednesday(), object.getThursday(), object.getFriday(), object.getSaturday(), object.getSunday()};

        return new Promotion(object.getObjectId(), percent, workdays);
    }

    public static List<Promotion> sortPromotions(List<Promotion> list) {
        Collections.sort(list, (lhs, rhs) -> Double.compare(lhs.percent, rhs.percent));
        return list;
    }

    public static List<EstablishmentPromotion> buildEstablishmentsPromotions(List<BackendlessEstablishmentPromotion> establishments, @Nullable Location location) {
        List<EstablishmentPromotion> list = new ArrayList<>(establishments.size());

        for (int i = 0, size = establishments.size(); i < size; ++i) {
            BackendlessEstablishmentPromotion promotion = establishments.get(i);

            if (promotion.getEstablishment() == null)
                continue;

            list.add(buildEstablishmentPromotion(promotion, location));
        }

        return list;
    }

    public static User from(BackendlessUser backendlessUser) {
        Date validUntil = (Date) backendlessUser.getProperty("validUntil");
        Date subscriptionDate = (Date) backendlessUser.getProperty("subscriptionDate");
        String plan = (String) backendlessUser.getProperty("plan");
        Boolean paymentSucceeded = (Boolean) backendlessUser.getProperty("paymentSucceeded");

        String voucher = null;

        BackendlessVoucher voucherObject = (BackendlessVoucher) backendlessUser.getProperty("voucher");

        if (voucherObject != null) {
            voucher = voucherObject.getName();
        }

        if (paymentSucceeded == null) {
            paymentSucceeded = false;
        }

        return new User(backendlessUser.getObjectId(), plan, voucher, validUntil, subscriptionDate, paymentSucceeded);
    }
}
