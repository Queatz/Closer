package closer.vlllage.com.closer.handler.group;

public class GroupActionBarButton {

    private String name;
    private Runnable onClick;
    private Runnable onLongClick;

    public GroupActionBarButton(String name, Runnable onClick) {
        this.name = name;
        this.onClick = onClick;
    }

    public GroupActionBarButton(String name, Runnable onClick, Runnable onLongClick) {
        this.name = name;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    public String getName() {
        return name;
    }

    public GroupActionBarButton setName(String name) {
        this.name = name;
        return this;
    }

    public Runnable getOnClick() {
        return onClick;
    }

    public GroupActionBarButton setOnClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public Runnable getOnLongClick() {
        return onLongClick;
    }

    public GroupActionBarButton setOnLongClick(Runnable onLongClick) {
        this.onLongClick = onLongClick;
        return this;
    }
}
