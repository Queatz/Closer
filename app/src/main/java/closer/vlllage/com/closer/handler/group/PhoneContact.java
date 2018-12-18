package closer.vlllage.com.closer.handler.group;

public class PhoneContact {
    private String name;
    private String phoneNumber;
    private String phoneId;

    public PhoneContact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public PhoneContact setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public PhoneContact setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getFirstName() {
        int firstWhiteSpace = name.indexOf(" ");

        if (firstWhiteSpace == -1) {
            return name;
        }

        return name.substring(0, firstWhiteSpace);
    }

    public String getPhoneId() {
        return phoneId;
    }

    public PhoneContact setPhoneId(String phoneId) {
        this.phoneId = phoneId;
        return this;
    }
}
