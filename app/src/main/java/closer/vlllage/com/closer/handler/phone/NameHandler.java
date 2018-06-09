package closer.vlllage.com.closer.handler.phone;

import java.util.Random;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.Phone;

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

        return $(Val.class).isEmpty(groupContact.getContactName()) ?
                fallbackName(groupContact.getContactId()) : groupContact.getContactName();
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
}
