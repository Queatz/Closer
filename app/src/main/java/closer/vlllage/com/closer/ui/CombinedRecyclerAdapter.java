package closer.vlllage.com.closer.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

public class CombinedRecyclerAdapter extends PoolRecyclerAdapter<RecyclerView.ViewHolder> {

    private final List<RecyclerView.Adapter> adapters = new ArrayList<>();
    private final List<Integer> adapterCursors = new ArrayList<>();
    private final List<PriorityAdapterItem> adapterItems = new ArrayList<>();

    public CombinedRecyclerAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    public void addAdapter(RecyclerView.Adapter adapter) {
        adapters.add(adapter);
        reset();
    }

    public void removeAdapter(RecyclerView.Adapter adapter) {
        adapters.remove(adapter);
        reset();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return adapters.get(viewType).onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        advanceCursorToPosition(position);
        adapterItems.get(position).adapter.onBindViewHolder(holder, adapterItems.get(position).localPosition);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        adapters.get(holder.getItemViewType()).onViewRecycled(holder);
    }

    @Override
    public int getItemViewType(int position) {
        advanceCursorToPosition(position);
        return adapterItems.get(position).viewType;
    }

    @Override
    public int getItemCount() {
        int count = 0;

        for (RecyclerView.Adapter adapter : adapters) {
            count += adapter.getItemCount();
        }

        return count;
    }

    private void advanceCursorToPosition(int position) {
        if (adapters.isEmpty()) {
            return;
        }

        while (adapterItems.size() <= position) {
            adapterItems.add(findNextPriorityItem());
        }
    }

    private PriorityAdapterItem findNextPriorityItem() {
        PriorityAdapterItem priorityAdapterItem = new PriorityAdapterItem();

        int priority = -1;

        for (int i = 0; i < adapters.size(); i++) {
            RecyclerView.Adapter adapter = adapters.get(i);

            if (adapterCursors.get(i) >= adapter.getItemCount()) {
                continue;
            }

            int adapterPriority;
            if (adapter instanceof PrioritizedAdapter) {
                adapterPriority = ((PrioritizedAdapter) adapter).getItemPriority(adapterCursors.get(i));
            } else {
                adapterPriority = adapterCursors.get(i);
            }

            if (adapterPriority < 0) {
                throw new IllegalStateException("Priority cannot be sub-zero");
            }

            if (adapterPriority < priority || priority == -1) {
                priorityAdapterItem.viewType = i;
                priorityAdapterItem.adapter = adapter;
                priorityAdapterItem.localPosition = adapterCursors.get(i);
                priority = adapterPriority;
            }
        }

        adapterCursors.set(priorityAdapterItem.viewType, adapterCursors.get(priorityAdapterItem.viewType) + 1);

        return priorityAdapterItem;
    }

    private void reset() {
        adapterCursors.clear();
        for (RecyclerView.Adapter ignored : adapters) adapterCursors.add(0);
        adapterItems.clear();
        notifyDataSetChanged();
    }

    public void notifyAdapterChanged(RecyclerView.Adapter adapter) {
        reset();
    }

    public interface PrioritizedAdapter {
        int getItemPriority(int position);
    }

    private static class PriorityAdapterItem {
        int viewType;
        RecyclerView.Adapter adapter;
        int localPosition;
    }
}
