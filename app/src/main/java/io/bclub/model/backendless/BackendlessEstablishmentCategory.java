package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class BackendlessEstablishmentCategory {

    private String objectId;
    private String ownerId;
    private java.util.Date created;
    private String icon;
    private String name;
    private java.util.Date updated;
    private String label;

    public static BackendlessEstablishmentCategory findById(String id) {
        return Backendless.Data.of(BackendlessEstablishmentCategory.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessEstablishmentCategory> callback) {
        Backendless.Data.of(BackendlessEstablishmentCategory.class).findById(id, callback);
    }

    public static BackendlessEstablishmentCategory findFirst() {
        return Backendless.Data.of(BackendlessEstablishmentCategory.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessEstablishmentCategory> callback) {
        Backendless.Data.of(BackendlessEstablishmentCategory.class).findFirst(callback);
    }

    public static BackendlessEstablishmentCategory findLast() {
        return Backendless.Data.of(BackendlessEstablishmentCategory.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessEstablishmentCategory> callback) {
        Backendless.Data.of(BackendlessEstablishmentCategory.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessEstablishmentCategory> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessEstablishmentCategory.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessEstablishmentCategory>> callback) {
        Backendless.Data.of(BackendlessEstablishmentCategory.class).find(query, callback);
    }

    public String getObjectId() {
        return objectId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public java.util.Date getCreated() {
        return created;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public java.util.Date getUpdated() {
        return updated;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BackendlessEstablishmentCategory save() {
        return Backendless.Data.of(BackendlessEstablishmentCategory.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessEstablishmentCategory> callback) {
        Backendless.Data.of(BackendlessEstablishmentCategory.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessEstablishmentCategory.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessEstablishmentCategory.class).remove(this, callback);
    }
}