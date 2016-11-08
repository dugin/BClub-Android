package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class BackendlessNeighborhood {

    private String name;
    private String ownerId;
    private java.util.Date updated;
    private java.util.Date created;
    private String objectId;

    private BackendlessCity city;

    public static BackendlessNeighborhood findById(String id) {
        return Backendless.Data.of(BackendlessNeighborhood.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessNeighborhood> callback) {
        Backendless.Data.of(BackendlessNeighborhood.class).findById(id, callback);
    }

    public static BackendlessNeighborhood findFirst() {
        return Backendless.Data.of(BackendlessNeighborhood.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessNeighborhood> callback) {
        Backendless.Data.of(BackendlessNeighborhood.class).findFirst(callback);
    }

    public static BackendlessNeighborhood findLast() {
        return Backendless.Data.of(BackendlessNeighborhood.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessNeighborhood> callback) {
        Backendless.Data.of(BackendlessNeighborhood.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessNeighborhood> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessNeighborhood.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessNeighborhood>> callback) {
        Backendless.Data.of(BackendlessNeighborhood.class).find(query, callback);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public java.util.Date getUpdated() {
        return updated;
    }

    public java.util.Date getCreated() {
        return created;
    }

    public String getObjectId() {
        return objectId;
    }

    public BackendlessCity getCity() {
        return city;
    }

    public void setCity(BackendlessCity city) {
        this.city = city;
    }

    public BackendlessNeighborhood save() {
        return Backendless.Data.of(BackendlessNeighborhood.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessNeighborhood> callback) {
        Backendless.Data.of(BackendlessNeighborhood.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessNeighborhood.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessNeighborhood.class).remove(this, callback);
    }
}