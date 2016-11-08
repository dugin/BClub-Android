package io.bclub.model.backendless;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;

public class BackendlessEstablishment {

    private java.util.Date created;
    private String objectId;
    private java.util.Date updated;
    private String email;
    private String detail;
    private String name;
    private String ownerId;
    private BackendlessAddress address;
    private BackendlessEstablishmentCategory category;
    private java.util.List<BackendlessTelephone> telephones;

    public static BackendlessEstablishment findById(String id) {
        return Backendless.Data.of(BackendlessEstablishment.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<BackendlessEstablishment> callback) {
        Backendless.Data.of(BackendlessEstablishment.class).findById(id, callback);
    }

    public static BackendlessEstablishment findFirst() {
        return Backendless.Data.of(BackendlessEstablishment.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<BackendlessEstablishment> callback) {
        Backendless.Data.of(BackendlessEstablishment.class).findFirst(callback);
    }

    public static BackendlessEstablishment findLast() {
        return Backendless.Data.of(BackendlessEstablishment.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<BackendlessEstablishment> callback) {
        Backendless.Data.of(BackendlessEstablishment.class).findLast(callback);
    }

    public static BackendlessCollection<BackendlessEstablishment> find(BackendlessDataQuery query) {
        return Backendless.Data.of(BackendlessEstablishment.class).find(query);
    }

    public static void findAsync(BackendlessDataQuery query, AsyncCallback<BackendlessCollection<BackendlessEstablishment>> callback) {
        Backendless.Data.of(BackendlessEstablishment.class).find(query, callback);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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

    public BackendlessAddress getAddress() {
        return address;
    }

    public void setAddress(BackendlessAddress address) {
        this.address = address;
    }

    public BackendlessEstablishmentCategory getCategory() {
        return category;
    }

    public void setCategory(BackendlessEstablishmentCategory category) {
        this.category = category;
    }

    public java.util.List<BackendlessTelephone> getTelephones() {
        return telephones;
    }

    public ArrayList<String> getTelephoneNumbers() {
        if (telephones == null) {
            return null;
        }

        ArrayList<String> numbers = new ArrayList<>(telephones.size());

        for (int i = 0, size = telephones.size(); i < size; ++i) {
            numbers.add(telephones.get(i).getNumber());
        }

        return numbers;
    }

    public void setTelephones(java.util.List<BackendlessTelephone> telephones) {
        this.telephones = telephones;
    }

    public BackendlessEstablishment save() {
        return Backendless.Data.of(BackendlessEstablishment.class).save(this);
    }

    public void saveAsync(AsyncCallback<BackendlessEstablishment> callback) {
        Backendless.Data.of(BackendlessEstablishment.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(BackendlessEstablishment.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(BackendlessEstablishment.class).remove(this, callback);
    }
}