package closer.vlllage.com.closer.handler.phone;

import java.util.Date;
import java.util.Random;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.Phone;

import static android.text.format.DateUtils.DAY_IN_MILLIS;

public class NameHandler extends PoolMember {

    private static final String[] fallbackNames = {
        "Random Armadillo",
        "Random Aardvark",
        "Random Kangaroo",
        "Random Gorilla",
        "Random Chimpanzee",
        "Random Anaconda",
        "Random Parakeet",
        "Random Rhino",
        "Random Muskrat",
        "Random Bumblebee",
        "Random Tiger",
        "Random Ocelot",
        "Random Capybara",
        "Random Sloth",
        "Random Lemur",
        "Random Baboon"
    };

    public String getName(Phone phone) {
        if (phone == null) {
            return noName();
        }

        return $(Val.class).isEmpty(phone.getName()) ?
                fallbackName(phone.getId()) : phone.getName();
    }

    public String getName(GroupContact groupContact) {
        if (groupContact == null) {
            return noName();
        }

        String name = $(Val.class).isEmpty(groupContact.getContactName()) ?
                fallbackName(groupContact.getContactId()) : groupContact.getContactName();

        if (isInactive(groupContact)) {
            return $(ResourcesHandler.class).getResources().getString(R.string.contact_inactive_inline, name);
        } else {
            return name;
        }
    }

    public String getName(GroupInvite groupInvite) {
        if (groupInvite == null) {
            return noName();
        }

        return $(Val.class).isEmpty(groupInvite.getName()) ? noName() : groupInvite.getName();
    }

    private String fallbackName(String phoneId) {
        if (phoneId == null) {
           return noName();
        }

        int index = new Random(phoneId.hashCode()).nextInt(fallbackNames.length);
        return fallbackNames[index];
    }

    private String noName() {
        return $(ResourcesHandler.class).getResources().getString(R.string.no_name);
    }

    private boolean isInactive(GroupContact groupContact) {
        Date fifteenDaysAgo = new Date();
        fifteenDaysAgo.setTime(fifteenDaysAgo.getTime() - 15 * DAY_IN_MILLIS);
        return groupContact.getContactActive().before(fifteenDaysAgo);
    }
}
