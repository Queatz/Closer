package closer.vlllage.com.closer.handler;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.group.GroupHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.map.SetNameHandler;
import closer.vlllage.com.closer.handler.search.SearchGroupsAdapter;
import closer.vlllage.com.closer.handler.search.SearchHandler;
import closer.vlllage.com.closer.pool.PoolFragment;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMember;
import closer.vlllage.com.closer.store.models.GroupMember_;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;

public class PersonalSlideFragment extends PoolFragment {

    private EditText yourCurrentStatus;
    private TextView yourName;
    private Switch shareYourLocationSwitch;
    private String previousStatus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        View view = inflater.inflate(R.layout.activity_personal, container, false);

        RecyclerView subscribedGroupsRecyclerView = view.findViewById(R.id.subscribedGroupsRecyclerView);
        TextView youveSubscribedEmpty = view.findViewById(R.id.youveSubscribedEmpty);

        SearchGroupsAdapter searchGroupsAdapter = new SearchGroupsAdapter($(GroupHandler.class), (group, v) -> {
            $(SearchHandler.class).openGroup(group.getId(), v);
        }, null);

        searchGroupsAdapter.setActionText($(ResourcesHandler.class).getResources().getString(R.string.open_group));
        searchGroupsAdapter.setIsSmall(true);
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_large_padding);

        subscribedGroupsRecyclerView.setAdapter(searchGroupsAdapter);
        subscribedGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(subscribedGroupsRecyclerView.getContext()));

        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupMember.class).query()
                .equal(GroupMember_.phone, $(Val.class).of($(PersistenceHandler.class).getPhoneId()))
                .equal(GroupMember_.subscribed, true)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(groupMembers -> {
                    if (groupMembers.isEmpty()) {
                        youveSubscribedEmpty.setVisibility(View.VISIBLE);
                        searchGroupsAdapter.setGroups(new ArrayList<>());
                    } else {
                        youveSubscribedEmpty.setVisibility(View.GONE);

                        Set<String> ids = new HashSet<>();
                        for (GroupMember groupMember : groupMembers) {
                            ids.add(groupMember.getGroup());
                        }

                        $(StoreHandler.class).findAll(Group.class, Group_.id, ids, $(SortHandler.class).sortGroups()).observer(searchGroupsAdapter::setGroups);
                    }
                }));

        yourCurrentStatus = view.findViewById(R.id.currentStatus);
        shareYourLocationSwitch = view.findViewById(R.id.shareYourLocationSwitch);
        yourName = view.findViewById(R.id.yourName);

        updateLocationInfo();

        shareYourLocationSwitch.setOnCheckedChangeListener((switchView, isChecked) -> {
            $(AccountHandler.class).updateActive(isChecked);
            updateLocationInfo();
        });

        shareYourLocationSwitch.setChecked($(AccountHandler.class).getActive());
        previousStatus = $(AccountHandler.class).getStatus();
        yourCurrentStatus.setText(previousStatus);

        yourCurrentStatus.setOnFocusChangeListener((editTextView, isFocused) -> {
            if (yourCurrentStatus.getText().toString().equals(previousStatus)) {
                return;
            }

            $(AccountHandler.class).updateStatus(yourCurrentStatus.getText().toString());
            $(KeyboardHandler.class).showKeyboard(yourCurrentStatus, false);
        });

        yourName.setText($(AccountHandler.class).getName());

        $(DisposableHandler.class).add($(AccountHandler.class).changes().subscribe(
                accountChange -> {
                    if (accountChange.prop.equals(AccountHandler.ACCOUNT_FIELD_NAME)) {
                        yourName.setText($(AccountHandler.class).getName());
                    }
                },
                throwable -> $(DefaultAlerts.class).thatDidntWork()
        ));

        yourName.setOnClickListener(v -> $(SetNameHandler.class).modifyName());

        yourName.requestFocus();

        return view;
    }

    private void updateLocationInfo() {
        if (shareYourLocationSwitch.isChecked()) {
            yourCurrentStatus.setVisibility(View.VISIBLE);
        } else {
            yourCurrentStatus.setVisibility(View.GONE);
        }
    }

}
