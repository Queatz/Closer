package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


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
            String photoUrl = new String[]{
                    "http://closer-files.vlllage.com/-7089523882354415894087138496463966086-8036632908012253855?s=500",
                    "http://closer-files.vlllage.com/-796028576968279690120195844113056218564147041864003206309?s=500",
                    "http://closer-files.vlllage.com/-6017237096331884528-35284271276821013372831657250643073785?s=500"
            }[new Random().nextInt(2)];
            Picasso.get().load(photoUrl.split("\\?")[0] + "?s=1600")
                    .noPlaceholder()
                    .transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.imageCorners), 0))
                    .into(holder.photo);
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
