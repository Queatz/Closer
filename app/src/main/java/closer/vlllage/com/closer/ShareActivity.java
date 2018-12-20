package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.share.SearchGroupsHeaderAdapter;
import closer.vlllage.com.closer.pool.PoolActivity;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

public class ShareActivity extends PoolActivity {

    private RecyclerView shareRecyclerView;
    private SearchGroupsHeaderAdapter searchGroupsAdapter;
    private TranslateAnimation finishAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        setContentView(R.layout.activity_share);

        View background = findViewById(R.id.background);
        shareRecyclerView = findViewById(R.id.shareRecyclerView);

        ViewTreeObserver viewTreeObserver = background.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                background.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                reveal();
            }

            private void reveal() {
                TranslateAnimation animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0
                );
                animation.setDuration(500);
                animation.setInterpolator(new DecelerateInterpolator());
                shareRecyclerView.startAnimation(animation);
            }
        });

        background.setOnTouchListener((view, motionEvent) -> {
            background.setOnTouchListener(null);
            finish();
            return true;
        });

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        searchGroupsAdapter = new SearchGroupsHeaderAdapter($(PoolMember.class), (group, view) -> {

        }, null);

        searchGroupsAdapter.setActionText($(ResourcesHandler.class).getResources().getString(R.string.share));
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_light);
        searchGroupsAdapter.setBackgroundResId(R.drawable.clickable_green_flat);

        shareRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        shareRecyclerView.setAdapter(searchGroupsAdapter);

        QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query()
                .notEqual(Group_.name, "");

        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroups())
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .observer(searchGroupsAdapter::setGroups));
    }

    @Override
    public void finish() {
        if (finishAnimator != null) {
            return;
        }

        finishAnimator = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1
        );;
        finishAnimator.setDuration(225);
        finishAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        finishAnimator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ShareActivity.super.finish();
                getWindow().getDecorView().setVisibility(View.GONE);
                overridePendingTransition(0, 0);
                finishAnimator = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        shareRecyclerView.startAnimation(finishAnimator);
    }
}
