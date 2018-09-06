package closer.vlllage.com.closer.handler.group;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
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
import closer.vlllage.com.closer.store.models.GroupAction;


public class GroupActionAdapter extends PoolRecyclerAdapter<GroupActionAdapter.GroupActionViewHolder> {

    public enum Layout {
        TEXT,
        PHOTO
    }

    private List<GroupAction> groupActions = new ArrayList<>();
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
            switch (new Random(groupAction.getId().hashCode()).nextInt(4)) {
                case 0: holder.itemView.setBackgroundResource(R.drawable.clickable_red_8dp); break;
                case 1: holder.itemView.setBackgroundResource(R.drawable.clickable_blue_8dp); break;
                case 2: holder.itemView.setBackgroundResource(R.drawable.clickable_accent_8dp); break;
                case 3: holder.itemView.setBackgroundResource(R.drawable.clickable_green_8dp); break;
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

    @Override
    public int getItemCount() {
        return groupActions.size();
    }

    public void setGroupActions(List<GroupAction> groupActions) {
        this.groupActions = groupActions;
        notifyDataSetChanged();
    }

    private @DrawableRes int getRandomBubbleBackgroundResource(GroupAction groupAction) {
        switch (new Random(groupAction.getId().hashCode()).nextInt(3)) {
            case 0: return R.drawable.bkg_bubbles;
            case 1: return R.drawable.bkg_bubbles_2;
            default: return R.drawable.bkg_bubbles_3;
        }
    }

    class GroupActionViewHolder extends RecyclerView.ViewHolder {

        ImageView photo;
        TextView actionName;

        public GroupActionViewHolder(View itemView) {
            super(itemView);
            itemView.setClipToOutline(true);
            actionName = itemView.findViewById(R.id.actionName);
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
