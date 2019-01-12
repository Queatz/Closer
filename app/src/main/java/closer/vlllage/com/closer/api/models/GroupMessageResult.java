package closer.vlllage.com.closer.api.models;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.ReactionCount;

public class GroupMessageResult extends ModelResult {
    public String from;
    public String to;
    public String text;
    public String attachment;
    public Date created;
    public PhoneResult phone;
    public List<ReactionCount> reactions;


    public static GroupMessage from(GroupMessageResult result) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setId(result.id);
        groupMessage.setFrom(result.from);
        groupMessage.setTo(result.to);
        groupMessage.setText(result.text);
        groupMessage.setTime(result.created);
        groupMessage.setUpdated(result.updated);
        groupMessage.setAttachment(result.attachment);
        groupMessage.setReactions(result.reactions);
        return groupMessage;
    }
}
