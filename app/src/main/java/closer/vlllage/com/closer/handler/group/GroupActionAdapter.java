package closer.vlllage.com.closer.handler.group;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.Group_;


public class GroupActionAdapter extends PoolRecyclerAdapter<GroupActionAdapter.GroupActionViewHolder> {

    public enum Layout {
        TEXT,
        PHOTO
    }

    private final List<GroupAction> groupActions = new ArrayList<>();
    private OnGroupActionClickListener onGroupActionClickListener;
    private OnGroupActionLongClickListener onGroupActionLongClickListener;
    private final Layout layout;

    public GroupActionAdapter(PoolMember poolMember,
                              Layout layout,
                              OnGroupActionClickListener onGroupActionClickListener,
                              OnGroupActionLongClickListener onGroupActionLongClickListener) {
        super(poolMember);
        this.layout = layout;
        this.onGroupActionClickListener = onGroupActionClickListener;
        this.onGroupActionLongClickListener = onGroupActionLongClickListener;
    }

    @NonNull
    @Override
    public GroupActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupActionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layout == Layout.TEXT ? R.layout.group_action_item : R.layout.group_action_photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupActionViewHolder holder, int position) {
        GroupAction groupAction = groupActions.get(position);
        holder.actionName.setText(groupActions.get(position).getName());

        View target;

        switch (layout) {
            case PHOTO:
                target = holder.itemView;
                break;
            case TEXT:
            default:
                target = holder.actionName;
                break;
        }

        target.setOnClickListener(view -> {
            if (onGroupActionClickListener != null) {
                onGroupActionClickListener.onGroupActionClick(groupAction);
            }
        });

        target.setOnLongClickListener(view -> {
            if (onGroupActionLongClickListener != null) {
                onGroupActionLongClickListener.onGroupActionLongClick(groupAction);
                return true;
            }

            return false;
        });

        if (layout == Layout.PHOTO) {
            Group group = $(StoreHandler.class).getStore().box(Group.class).query()
                    .equal(Group_.id, groupActions.get(position).getGroup())
                    .build()
                    .findFirst();
            holder.groupName.setText(group == null ? "" : group.getName());

            switch (getRandom(groupAction).nextInt(4)) {
                case 1: holder.itemView.setBackgroundResource(R.drawable.clickable_blue_8dp); break;
                case 2: holder.itemView.setBackgroundResource(R.drawable.clickable_accent_8dp); break;
                case 3: holder.itemView.setBackgroundResource(R.drawable.clickable_green_8dp); break;
                default: holder.itemView.setBackgroundResource(R.drawable.clickable_red_8dp); break;
            }

            if (groupAction.getPhoto() != null) {
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, $(ResourcesHandler.class).getResources().getDimension(R.dimen.groupActionSmallTextSize));
                holder.actionName.setBackgroundResource(R.color.black_25);
                holder.photo.setImageDrawable(null);
                Picasso.get().load(groupAction.getPhoto().split("\\?")[0] + "?s=256")
                        .noPlaceholder()
                        .into(holder.photo);
            } else {
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, $(ResourcesHandler.class).getResources().getDimension(R.dimen.groupActionLargeTextSize));
                holder.actionName.setBackground(null);
                holder.photo.setImageResource(getRandomBubbleBackgroundResource(groupAction));
            }
        }
    }

    private Random getRandom(GroupAction groupAction) {
        return new Random(groupAction.getId() == null ?
                groupAction.getObjectBoxId() :
                groupAction.getId().hashCode());
    }

    @Override
    public int getItemCount() {
        return groupActions.size();
    }

    public void setGroupActions(List<GroupAction> groupActions) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return GroupActionAdapter.this.groupActions.size();
            }

            @Override
            public int getNewListSize() {
                return groupActions.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                return GroupActionAdapter.this.groupActions.get(oldPosition).getId()
                        .equals(groupActions.get(newPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                return GroupActionAdapter.this.groupActions.get(oldPosition).getName()
                        .equals(groupActions.get(newPosition).getName());
            }
        }, true);
        this.groupActions.clear();
        this.groupActions.addAll(groupActions);
        diffResult.dispatchUpdatesTo(this);
    }

    private @DrawableRes int getRandomBubbleBackgroundResource(GroupAction groupAction) {
        switch (getRandom(groupAction).nextInt(3)) {
            case 0: return R.drawable.bkg_bubbles;
            case 1: return R.drawable.bkg_bubbles_2;
            default: return R.drawable.bkg_bubbles_3;
        }
    }

    class GroupActionViewHolder extends RecyclerView.ViewHolder {

        ImageView photo;
        TextView actionName;
        TextView groupName;

        public GroupActionViewHolder(View itemView) {
            super(itemView);
            itemView.setClipToOutline(true);
            actionName = itemView.findViewById(R.id.actionName);
            groupName = itemView.findViewById(R.id.groupName);
            photo = itemView.findViewById(R.id.photo);
        }
    }

    public interface OnGroupActionClickListener {
        void onGroupActionClick(GroupAction groupAction);
    }

    public interface OnGroupActionLongClickListener {
        void onGroupActionLongClick(GroupAction groupAction);
    }
}
