package io.bclub.controller.model;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import io.bclub.model.EstablishmentPromotion;

public class FetchPromotionsResponse {

    public final List<EstablishmentPromotion> promotions;
    public final boolean hasMore;
    public final Location location;

    public FetchPromotionsResponse(List<EstablishmentPromotion> promotions, EstablishmentPromotion featured, Location location, boolean hasMore) {
        this.hasMore = hasMore;
        this.location = location;

        if (promotions == null) {
            this.promotions = new ArrayList<>();
        } else {
            this.promotions = promotions;
        }

        if (featured != null) {
            this.promotions.add(0, featured);
        }
    }
}
