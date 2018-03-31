package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.pool.PoolMember;

public class RefreshHandler extends PoolMember {
    public void refreshMyMessages() {
        $(DisposableHandler.class).add($(ApiHandler.class).myMessages().subscribe( messages -> {
            // Insert all new messages
        }));
    }

    public void refreshMyGroups() {
        $(DisposableHandler.class).add($(ApiHandler.class).myGroups().subscribe( groups -> {
            // Clear all my groups that don't match
            // Update group names and contacts
        }));
    }
}
