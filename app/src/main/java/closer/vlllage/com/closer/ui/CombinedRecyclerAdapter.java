package closer.vlllage.com.closer.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.handler.helpers.TimerHandler;
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
        return adapterFromViewType(viewType).onCreateViewHolder(parent, (viewType / 10000));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        advanceCursorToPosition(position);
        adapterItems.get(position).adapter.onBindViewHolder(holder, adapterItems.get(position).localPosition);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        adapterFromViewType(holder.getItemViewType()).onViewRecycled(holder);
    }

    @Override
    public int getItemViewType(int position) {
        advanceCursorToPosition(position);
        return adapterItems.get(position).adapterIndex +
                10000 * adapterItems.get(position).adapter.getItemViewType(adapterItems.get(position).localPosition);
    }

    @Override
    public int getItemCount() {
        int count = 0;

        for (RecyclerView.Adapter adapter : adapters) {
            count += adapter.getItemCount();
        }

        return count;
    }

    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapterFromViewType(int viewType) {
        return adapters.get(viewType - (viewType / 10000) * 10000);
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

        for (int currentAdapterIndex = 0; currentAdapterIndex < adapters.size(); currentAdapterIndex++) {
            RecyclerView.Adapter adapter = adapters.get(currentAdapterIndex);

            if (adapterCursors.get(currentAdapterIndex) >= adapter.getItemCount()) {
                continue;
            }

            int adapterPriority;
            if (adapter instanceof PrioritizedAdapter) {
                adapterPriority = ((PrioritizedAdapter) adapter).getItemPriority(adapterCursors.get(currentAdapterIndex));
            } else {
                adapterPriority = adapterCursors.get(currentAdapterIndex);
            }

            if (adapterPriority < 0) {
                throw new IllegalStateException("Priority cannot be sub-zero");
            }

            if (adapterPriority < priority || priority == -1) {
                priorityAdapterItem.adapterIndex = currentAdapterIndex;
                priorityAdapterItem.adapter = adapter;
                priorityAdapterItem.localPosition = adapterCursors.get(currentAdapterIndex);
                priority = adapterPriority;
            }
        }

        adapterCursors.set(priorityAdapterItem.adapterIndex, adapterCursors.get(priorityAdapterItem.adapterIndex) + 1);

        return priorityAdapterItem;
    }

    private void reset() {
        adapterCursors.clear();
        for (RecyclerView.Adapter ignored : adapters) adapterCursors.add(0);
        adapterItems.clear();
        $(TimerHandler.class).post(this::notifyDataSetChanged);
    }

    public void notifyAdapterChanged(RecyclerView.Adapter adapter) {
        reset();
    }

    public interface PrioritizedAdapter {
        int getItemPriority(int position);
    }

    private static class PriorityAdapterItem {
        int adapterIndex;
        RecyclerView.Adapter adapter;
        int localPosition;
    }
}
