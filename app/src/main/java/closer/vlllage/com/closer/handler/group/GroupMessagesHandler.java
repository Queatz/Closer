package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ActivityHandler;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class GroupMessagesHandler extends PoolMember {

    private GroupMessagesAdapter groupMessagesAdapter;

    public void attach(RecyclerView recyclerView, EditText replyMessage) {
        recyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.VERTICAL,
                true
        ));

        groupMessagesAdapter = new GroupMessagesAdapter(this);
        groupMessagesAdapter.setOnMessageClickListener(message -> {
            $(AlertHandler.class).make()
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.call))
                    .show();
        });
        recyclerView.setAdapter(groupMessagesAdapter);
    }
}
