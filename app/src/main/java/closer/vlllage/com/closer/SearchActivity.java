package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;

import closer.vlllage.com.closer.handler.AccountHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.search.SearchHandler;

public class SearchActivity extends CircularRevealActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        findViewById(R.id.closeButton).setOnClickListener(view -> finish());

        $(SearchHandler.class).attach(
                findViewById(R.id.searchGroups),
                findViewById(R.id.groupsRecyclerView)
        );
    }

    @Override
    protected int getBackgroundId() {
        return R.id.background;
    }
}
