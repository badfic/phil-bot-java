package com.badfic.philbot.data.phil;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "fire_drill_permission")
public class FireDrillPermission {

    @Id
    private UUID id;

    @Column
    private long channelId;

    @Column
    private long roleId;

    @Column
    private long allowPermission;

    @Column
    private long denyPermission;

    @Column
    private long inheritPermission;

    public FireDrillPermission(long channelId, long roleId, long allowPermission, long denyPermission, long inheritPermission) {
        this.id = UUID.randomUUID();
        this.channelId = channelId;
        this.roleId = roleId;
        this.allowPermission = allowPermission;
        this.denyPermission = denyPermission;
        this.inheritPermission = inheritPermission;
    }

    public FireDrillPermission() {
    }

    public UUID getId() {
        return id;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getAllowPermission() {
        return allowPermission;
    }

    public void setAllowPermission(long allowPermission) {
        this.allowPermission = allowPermission;
    }

    public long getDenyPermission() {
        return denyPermission;
    }

    public void setDenyPermission(long denyPermission) {
        this.denyPermission = denyPermission;
    }

    public long getInheritPermission() {
        return inheritPermission;
    }

    public void setInheritPermission(long inheritPermission) {
        this.inheritPermission = inheritPermission;
    }
}
