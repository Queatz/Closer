package closer.vlllage.com.closer.handler;

import android.view.View;

import closer.vlllage.com.closer.pool.PoolMember;

public class SuggestionHandler extends PoolMember {

    public void shuffle() {

    }

    public void attach(View shuffleButton) {
        shuffleButton.setOnClickListener(view -> this.shuffle());
    }
}
