package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout;
import closer.vlllage.com.closer.ui.RevealAnimator;
import io.objectbox.query.QueryBuilder;

public class GroupMessageMentionHandler extends PoolMember {
    private RevealAnimator animator;
    private MentionAdapter adapter;
    private MaxSizeFrameLayout container;

    public void attach(MaxSizeFrameLayout container, RecyclerView recyclerView, MentionAdapter.OnMentionClickListener onMentionClickListener) {
        this.container = container;
        animator = new RevealAnimator(container, (int) ($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f));
        adapter = new MentionAdapter(this, onMentionClickListener);

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    public void showSuggestionsForName(CharSequence name) {
        if (name == null) {
            show(false);
        } else {
            if (name.charAt(0) == '@') {
                name = name.subSequence(1, name.length());
            }
            List<Phone> phones = $(StoreHandler.class).getStore().box(Phone.class).query().
                    contains(Phone_.name, name.toString(), QueryBuilder.StringOrder.CASE_INSENSITIVE)
                    .build().find();

            if (phones.isEmpty()) {
                show(false);
            } else {
                show(true);
                setItems(phones);
            }
        }
    }

    public void setItems(List<Phone> phones) {
        adapter.setItems(phones);
    }

    public void show(boolean show) {
        if (animator == null) {
            return;
        }

        if (adapter.getItemCount() < 1) {
            show = false;
        }

        if (!show && container.getVisibility() == View.VISIBLE) {
            animator.show(false, true);
        } else if (show && container.getVisibility() == View.GONE) {
            animator.show(true, true);
        }
    }
}
