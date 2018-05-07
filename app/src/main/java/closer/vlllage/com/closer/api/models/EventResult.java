package closer.vlllage.com.closer.api.models;

import java.util.Date;
import java.util.List;

public class EventResult extends ModelResult {
    public List<Double> geo;
    public String name;
    public String about;
    public Date startsAt;
    public Date endsAt;
    public boolean cancelled;
}
