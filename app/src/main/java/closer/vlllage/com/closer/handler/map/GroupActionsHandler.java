package closer.vlllage.com.closer.handler.map;

import android.support.v7.widget.RecyclerView;

import java.util.List;
import java.util.Random;

import closer.vlllage.com.closer.handler.group.GroupActionAdapter;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.search.GroupActionRecyclerViewHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupAction;

public class GroupActionsHandler extends PoolMember {

    public void attach(RecyclerView groupActionsRecyclerView) {
        $(GroupActionRecyclerViewHandler.class).attach(groupActionsRecyclerView, GroupActionAdapter.Layout.PHOTO);
        List<GroupAction> groupActions = $(StoreHandler.class).getStore().box(GroupAction.class).query().sort((a, b) -> a == b ? 0 : new Random().nextInt(2) > 0 ? -1 : 1).build().find();
        $(GroupActionRecyclerViewHandler.class).getAdapter().setGroupActions(groupActions);
        $(GroupActionRecyclerViewHandler.class).setOnGroupActionRepliedListener(groupAction -> $(GroupActivityTransitionHandler.class).showGroupMessages(null, groupAction.getGroup()));
    }
}
