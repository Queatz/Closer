package closer.vlllage.com.closer.api.models;

import java.util.List;

import closer.vlllage.com.closer.store.models.Phone;

public class PhoneResult extends ModelResult {
    public List<Double> geo;
    public String name;
    public String photo;
    public String status;
    public Boolean active;

    public static Phone from(PhoneResult phoneResult) {
        return updateFrom(new Phone(), phoneResult);
    }

    public static Phone updateFrom(Phone phone, PhoneResult phoneResult) {
        phone.setId(phoneResult.id);
        phone.setUpdated(phoneResult.updated);

        if (phoneResult.geo != null && phoneResult.geo.size() == 2) {
            phone.setLatitude(phoneResult.geo.get(0));
            phone.setLongitude(phoneResult.geo.get(1));
        }

        phone.setName(phoneResult.name);
        phone.setStatus(phoneResult.status);
        phone.setPhoto(phoneResult.photo);

        return phone;
    }
}
