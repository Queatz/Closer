package closer.vlllage.com.closer.handler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import io.objectbox.android.AndroidScheduler;

public class MyGroupsLayoutHandler extends PoolMember {
    private ViewGroup myGroupsLayout;

    public void attach(ViewGroup myGroupsLayout) {
        this.myGroupsLayout = myGroupsLayout;
        RecyclerView myGroupsRecyclerView = myGroupsLayout.findViewById(R.id.myGroupsRecyclerView);
        myGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                myGroupsRecyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        MyGroupsAdapter myGroupsAdapter = new MyGroupsAdapter(this);
        myGroupsRecyclerView.setAdapter(myGroupsAdapter);
        $(StoreHandler.class).getStore().box(Group.class).query().build()
                .subscribe().on(AndroidScheduler.mainThread())
                .observer(myGroupsAdapter::setGroups);
    }

    public int getHeight() {
        return myGroupsLayout.getMeasuredHeight();
    }
}
