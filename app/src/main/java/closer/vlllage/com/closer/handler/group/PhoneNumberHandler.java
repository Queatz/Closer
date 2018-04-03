package closer.vlllage.com.closer.handler.group;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

import closer.vlllage.com.closer.pool.PoolMember;

public class PhoneNumberHandler extends PoolMember {

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public boolean isValidNumber(String phoneNumber) {
        try {
            return phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(phoneNumber, Locale.US.getCountry()));
        } catch (NumberParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String normalize(String phoneNumber) {
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, Locale.US.getCountry());
            return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
