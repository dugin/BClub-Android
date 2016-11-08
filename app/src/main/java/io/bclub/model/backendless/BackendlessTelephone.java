package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class BackendlessTelephone {

    private String number;
    private java.util.Date created;
    private String objectId;
    private java.util.Date updated;
    private String ownerId;

    public static BackendlessTelephone findById(String id) {
        return Backendless.Data.of(BackendlessTelephone.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessTelephone> callback) {
        Backendless.Data.of(BackendlessTelephone.class).findById(id, callback);
    }

    public static BackendlessTelephone findFirst() {
        return Backendless.Data.of(BackendlessTelephone.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessTelephone> callback) {
        Backendless.Data.of(BackendlessTelephone.class).findFirst(callback);
    }

    public static BackendlessTelephone findLast() {
        return Backendless.Data.of(BackendlessTelephone.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessTelephone> callback) {
        Backendless.Data.of(BackendlessTelephone.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessTelephone> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessTelephone.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessTelephone>> callback) {
        Backendless.Data.of(BackendlessTelephone.class).find(query, callback);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public java.util.Date getCreated() {
        return created;
    }

    public String getObjectId() {
        return objectId;
    }

    public java.util.Date getUpdated() {
        return updated;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public BackendlessTelephone save() {
        return Backendless.Data.of(BackendlessTelephone.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessTelephone> callback) {
        Backendless.Data.of(BackendlessTelephone.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessTelephone.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessTelephone.class).remove(this, callback);
    }
}