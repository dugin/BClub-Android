package io.bclub.model.backendless;

import com.backendless.Backendless;

import java.util.Date;

public class BackendlessVoucher {

    private java.util.Date created;
    private String ownerId;
    private String objectId;
    private java.util.Date updated;
    private String name;

    private Boolean used;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public BackendlessVoucher save() {
        return Backendless.Data.of(BackendlessVoucher.class).save(this);
    }
}