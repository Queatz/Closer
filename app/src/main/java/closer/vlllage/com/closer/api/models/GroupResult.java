package closer.vlllage.com.closer.api.models;

import com.google.gson.annotations.SerializedName;

public class GroupResult extends ModelResult {
    public String name;
    public String about;
    @SerializedName("public") public Boolean isPublic;
}
