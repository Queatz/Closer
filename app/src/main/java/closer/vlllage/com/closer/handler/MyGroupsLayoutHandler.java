package closer.vlllage.com.closer.handler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import io.objectbox.android.AndroidScheduler;

public class MyGroupsLayoutHandler extends PoolMember {
    private ViewGroup myGroupsLayout;
    private MyGroupsAdapter myGroupsAdapter;

    public void attach(ViewGroup myGroupsLayout) {
        this.myGroupsLayout = myGroupsLayout;
        RecyclerView myGroupsRecyclerView = myGroupsLayout.findViewById(R.id.myGroupsRecyclerView);
        myGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                myGroupsRecyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        myGroupsAdapter = new MyGroupsAdapter(this);
        myGroupsRecyclerView.setAdapter(myGroupsAdapter);
        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(Group.class).query()
                .sort($(SortHandler.class).sortGroups())
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroups));
    }

    private void setGroups(List<Group> groups) {
        $(AppShortcutsHandler.class).setGroupShortcuts(groups);
        myGroupsAdapter.setGroups(groups);
    }

    public int getHeight() {
        return myGroupsLayout.getMeasuredHeight();
    }

    public void showVerifyMyNumber(boolean show) {
        List<String> actions = new ArrayList<>();

        if (show) {
            actions.add($(ResourcesHandler.class).getResources().getString(R.string.verify_your_number));
        }

        myGroupsAdapter.setActions(actions);
    }
}
