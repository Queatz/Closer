package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class GroupMember extends BaseObject {
    private String phone;
    private String group;
    private boolean muted;
    private boolean subscribed;

    public String getPhone() {
        return phone;
    }

    public GroupMember setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public GroupMember setGroup(String group) {
        this.group = group;
        return this;
    }

    public boolean isMuted() {
        return muted;
    }

    public GroupMember setMuted(boolean muted) {
        this.muted = muted;
        return this;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public GroupMember setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
        return this;
    }
}
