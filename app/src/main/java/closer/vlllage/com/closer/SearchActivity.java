package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class SearchActivity extends CircularRevealActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    protected int getBackgroundId() {
        return R.id.background;
    }
}
