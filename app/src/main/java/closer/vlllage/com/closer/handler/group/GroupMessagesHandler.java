package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import closer.vlllage.com.closer.handler.ActivityHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class GroupMessagesHandler extends PoolMember {
    public void attach(RecyclerView recyclerView, EditText replyMessage) {
        recyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.VERTICAL,
                true
        ));

        recyclerView.setAdapter(new GroupMessagesAdapter(this));
    }
}
