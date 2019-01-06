package closer.vlllage.com.closer.store.models;

public class ReactionCount {
    public String reaction;
    public long count;

    @Override
    public boolean equals(Object other) {
        return other instanceof ReactionCount && reaction.equals(((ReactionCount) other).reaction) && count == ((ReactionCount) other).count;
    }
}

