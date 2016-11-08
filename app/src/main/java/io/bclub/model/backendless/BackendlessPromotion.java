package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class BackendlessPromotion {

    private String objectId;
    private String friday;
    private String tuesday;
    private String monday;
    private String ownerId;
    private String saturday;
    private String thursday;
    private java.util.Date created;
    private String wednesday;
    private Double percent;
    private java.util.Date updated;
    private String sunday;

    public static BackendlessPromotion findById(String id) {
        return Backendless.Data.of(BackendlessPromotion.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessPromotion> callback) {
        Backendless.Data.of(BackendlessPromotion.class).findById(id, callback);
    }

    public static BackendlessPromotion findFirst() {
        return Backendless.Data.of(BackendlessPromotion.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessPromotion> callback) {
        Backendless.Data.of(BackendlessPromotion.class).findFirst(callback);
    }

    public static BackendlessPromotion findLast() {
        return Backendless.Data.of(BackendlessPromotion.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessPromotion> callback) {
        Backendless.Data.of(BackendlessPromotion.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessPromotion> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessPromotion.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessPromotion>> callback) {
        Backendless.Data.of(BackendlessPromotion.class).find(query, callback);
    }

    public String getObjectId() {
        return objectId;
    }

    public String getFriday() {
        return friday;
    }

    public void setFriday(String friday) {
        this.friday = friday;
    }

    public String getTuesday() {
        return tuesday;
    }

    public void setTuesday(String tuesday) {
        this.tuesday = tuesday;
    }

    public String getMonday() {
        return monday;
    }

    public void setMonday(String monday) {
        this.monday = monday;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getSaturday() {
        return saturday;
    }

    public void setSaturday(String saturday) {
        this.saturday = saturday;
    }

    public String getThursday() {
        return thursday;
    }

    public void setThursday(String thursday) {
        this.thursday = thursday;
    }

    public java.util.Date getCreated() {
        return created;
    }

    public String getWednesday() {
        return wednesday;
    }

    public void setWednesday(String wednesday) {
        this.wednesday = wednesday;
    }

    public Double getPercent() {
        return percent == null ? 0 : percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public java.util.Date getUpdated() {
        return updated;
    }

    public String getSunday() {
        return sunday;
    }

    public void setSunday(String sunday) {
        this.sunday = sunday;
    }

    public BackendlessPromotion save() {
        return Backendless.Data.of(BackendlessPromotion.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessPromotion> callback) {
        Backendless.Data.of(BackendlessPromotion.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessPromotion.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessPromotion.class).remove(this, callback);
    }
}