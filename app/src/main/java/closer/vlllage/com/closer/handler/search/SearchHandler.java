package closer.vlllage.com.closer.handler.search;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import closer.vlllage.com.closer.pool.PoolMember;

public class SearchHandler extends PoolMember {

    private SearchGroupsAdapter searchGroupsAdapter;

    public void attach(EditText searchGroups, RecyclerView groupsRecyclerView) {
        searchGroupsAdapter = new SearchGroupsAdapter(this,
                group -> {},
                groupName -> {});

        groupsRecyclerView.setAdapter(searchGroupsAdapter);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                groupsRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));

        searchGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showGroups(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void showGroups(String searchQuery) {
        searchGroupsAdapter.setCreatePublicGroupName(searchQuery.trim().isEmpty() ? null : searchQuery);
    }
}
