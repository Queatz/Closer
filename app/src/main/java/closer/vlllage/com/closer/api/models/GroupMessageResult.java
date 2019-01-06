package closer.vlllage.com.closer.api.models;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.store.models.ReactionCount;

public class GroupMessageResult extends ModelResult {
    public String from;
    public String to;
    public String text;
    public String attachment;
    public Date created;
    public PhoneResult phone;
    public List<ReactionCount> reactions;
}
