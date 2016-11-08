package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

public class BackendlessAddress {

    private String objectId;
    private String complement;
    private java.util.Date created;
    private String ownerId;
    private String number;
    private String street;
    private java.util.Date updated;
    private BackendlessNeighborhood neighborhood;
    private GeoPoint geolocation;

    public static BackendlessAddress findById(String id) {
        return Backendless.Data.of(BackendlessAddress.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessAddress> callback) {
        Backendless.Data.of(BackendlessAddress.class).findById(id, callback);
    }

    public static BackendlessAddress findFirst() {
        return Backendless.Data.of(BackendlessAddress.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessAddress> callback) {
        Backendless.Data.of(BackendlessAddress.class).findFirst(callback);
    }

    public static BackendlessAddress findLast() {
        return Backendless.Data.of(BackendlessAddress.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessAddress> callback) {
        Backendless.Data.of(BackendlessAddress.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessAddress> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessAddress.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessAddress>> callback) {
        Backendless.Data.of(BackendlessAddress.class).find(query, callback);
    }

    public String getObjectId() {
        return objectId;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public java.util.Date getCreated() {
        return created;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public java.util.Date getUpdated() {
        return updated;
    }

    public BackendlessNeighborhood getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(BackendlessNeighborhood neighborhood) {
        this.neighborhood = neighborhood;
    }

    public GeoPoint getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(GeoPoint geolocation) {
        this.geolocation = geolocation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);

        sb.append(street);

        if (number != null) {
            sb.append(", ").append(number);
        }

        if (complement != null) {
            sb.append(" ").append(complement);
        }

        return sb.toString();
    }

    public BackendlessAddress save() {
        return Backendless.Data.of(BackendlessAddress.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessAddress> callback) {
        Backendless.Data.of(BackendlessAddress.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessAddress.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessAddress.class).remove(this, callback);
    }
}