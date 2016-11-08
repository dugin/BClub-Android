package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class BackendlessEstablishmentPromotion {

    private Boolean night;
    private String ownerId;
    private String objectId;
    private Boolean active;
    private String restriction;
    private java.util.Date featuredDate;
    private Boolean afternoon;
    private String imageUrl;
    private java.util.Date created;
    private Boolean morning;
    private java.util.Date updated;
    private java.util.List<BackendlessPromotion> promotions;
    private BackendlessEstablishment establishment;

    public static BackendlessEstablishmentPromotion findById(String id, int relationDepth) {
        return Backendless.Data.of(BackendlessEstablishmentPromotion.class).findById(id, relationDepth);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessEstablishmentPromotion> callback) {
        Backendless.Data.of(BackendlessEstablishmentPromotion.class).findById(id, callback);
    }

    public static BackendlessEstablishmentPromotion findFirst() {
        return Backendless.Data.of(BackendlessEstablishmentPromotion.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessEstablishmentPromotion> callback) {
        Backendless.Data.of(BackendlessEstablishmentPromotion.class).findFirst(callback);
    }

    public static BackendlessEstablishmentPromotion findLast() {
        return Backendless.Data.of(BackendlessEstablishmentPromotion.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessEstablishmentPromotion> callback) {
        Backendless.Data.of(BackendlessEstablishmentPromotion.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessEstablishmentPromotion> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessEstablishmentPromotion.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessEstablishmentPromotion>> callback) {
        Backendless.Data.of(BackendlessEstablishmentPromotion.class).find(query, callback);
    }

    public Boolean getNight() {
        return night == null ? false : night;
    }

    public void setNight(Boolean night) {
        this.night = night;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getObjectId() {
        return objectId;
    }

    public boolean getActive() {
        return active == null ? false : active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    public java.util.Date getFeaturedDate() {
        return featuredDate;
    }

    public void setFeaturedDate(java.util.Date featuredDate) {
        this.featuredDate = featuredDate;
    }

    public Boolean getAfternoon() {
        return afternoon == null ? false : afternoon;
    }

    public void setAfternoon(Boolean afternoon) {
        this.afternoon = afternoon;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public java.util.Date getCreated() {
        return created;
    }

    public Boolean getMorning() {
        return morning == null ? false : morning;
    }

    public void setMorning(Boolean morning) {
        this.morning = morning;
    }

    public java.util.Date getUpdated() {
        return updated;
    }

    public java.util.List<BackendlessPromotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(java.util.List<BackendlessPromotion> promotions) {
        this.promotions = promotions;
    }

    public BackendlessEstablishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(BackendlessEstablishment establishment) {
        this.establishment = establishment;
    }

    public BackendlessEstablishmentPromotion save() {
        return Backendless.Data.of(BackendlessEstablishmentPromotion.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessEstablishmentPromotion> callback) {
        Backendless.Data.of(BackendlessEstablishmentPromotion.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessEstablishmentPromotion.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessEstablishmentPromotion.class).remove(this, callback);
    }
}