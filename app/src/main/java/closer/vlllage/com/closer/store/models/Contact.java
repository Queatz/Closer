package closer.vlllage.com.closer.store.models;

public class Contact extends BaseObject {
    private String name;
    private String phoneNumber;
    private String closerAccountId;

    public String getName() {
        return name;
    }

    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Contact setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getCloserAccountId() {
        return closerAccountId;
    }

    public Contact setCloserAccountId(String closerAccountId) {
        this.closerAccountId = closerAccountId;
        return this;
    }
}
