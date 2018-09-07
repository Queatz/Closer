package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.MiniWindowHandler;
import closer.vlllage.com.closer.handler.search.SearchHandler;
import closer.vlllage.com.closer.ui.CircularRevealActivity;

public class SearchActivity extends CircularRevealActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        findViewById(R.id.closeButton).setOnClickListener(view -> finish());

        EditText searchGroups = findViewById(R.id.searchGroups);

        searchGroups.requestFocus();

        $(SearchHandler.class).attach(
                searchGroups,
                findViewById(R.id.groupsRecyclerView)
        );

        $(MiniWindowHandler.class).attach(findViewById(R.id.titleText), findViewById(R.id.backgroundColor));

        findViewById(R.id.closeButton).requestFocus();
    }

    @Override
    protected int getBackgroundId() {
        return R.id.background;
    }
}
