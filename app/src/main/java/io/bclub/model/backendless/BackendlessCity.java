package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class BackendlessCity {

    private java.util.Date created;
    private String ownerId;
    private String objectId;
    private java.util.Date updated;
    private String name;

    public static BackendlessCity findById(String id) {
        return Backendless.Data.of(BackendlessCity.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessCity> callback) {
        Backendless.Data.of(BackendlessCity.class).findById(id, callback);
    }

    public static BackendlessCity findFirst() {
        return Backendless.Data.of(BackendlessCity.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessCity> callback) {
        Backendless.Data.of(BackendlessCity.class).findFirst(callback);
    }

    public static BackendlessCity findLast() {
        return Backendless.Data.of(BackendlessCity.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessCity> callback) {
        Backendless.Data.of(BackendlessCity.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessCity> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessCity.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessCity>> callback) {
        Backendless.Data.of(BackendlessCity.class).find(query, callback);
    }

    public java.util.Date getCreated() {
        return created;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getObjectId() {
        return objectId;
    }

    public java.util.Date getUpdated() {
        return updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BackendlessCity save() {
        return Backendless.Data.of(BackendlessCity.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessCity> callback) {
        Backendless.Data.of(BackendlessCity.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessCity.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessCity.class).remove(this, callback);
    }
}