package closer.vlllage.com.closer.handler;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;

public class SearchGroupHandler extends PoolMember {

    private String searchQuery = "";
    private SearchGroupsAdapter searchGroupsAdapter;
    private List<Group> groupsCache;
    private boolean hideCreateGroupOption;

    public void showGroupsForQuery(SearchGroupsAdapter searchGroupsAdapter, String searchQuery) {
        this.searchQuery = searchQuery.toLowerCase();
        this.searchGroupsAdapter = searchGroupsAdapter;

        if (!hideCreateGroupOption) {
            searchGroupsAdapter.setCreatePublicGroupName(searchQuery.trim().isEmpty() ? null : searchQuery);
        }

        if (this.groupsCache != null) {
            setGroups(this.groupsCache);
        }
    }

    public void setGroups(List<Group> allGroups) {
        this.groupsCache = allGroups;

        List<Group> groups = new ArrayList<>();
        for(Group group : allGroups) {
            if (group.getName() != null) {
                if (group.getName().toLowerCase().contains(searchQuery) ||
                        $(Val.class).of(group.getAbout(), "").toLowerCase().contains(searchQuery) ||
                        groupActionNamesContains(group, searchQuery)) {
                    groups.add(group);
                }
            }
        }

        searchGroupsAdapter.setGroups(groups);
    }

    private boolean groupActionNamesContains(Group group, String searchQuery) {
        List<GroupAction> groupActions = $(StoreHandler.class).getStore().box(GroupAction.class).query()
                .equal(GroupAction_.group, group.getId()).build().find();

        for (GroupAction groupAction : groupActions) {
            if (groupAction.getName().toLowerCase().contains(searchQuery)) {
                return true;
            }
        }

        return false;
    }

    public void hideCreateGroupOption() {
        this.hideCreateGroupOption = true;
    }
}
