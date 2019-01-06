package closer.vlllage.com.closer.store;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import closer.vlllage.com.closer.store.models.ReactionCount;
import io.objectbox.converter.PropertyConverter;

public class ReactionCountListJsonConverter implements PropertyConverter<List<ReactionCount>, String> {

    private static final Gson gson = new Gson();

    @Override
    public List<ReactionCount> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return gson.fromJson(databaseValue, new TypeToken<List<ReactionCount>>() {}.getType());
    }

    @Override
    public String convertToDatabaseValue(List<ReactionCount> entityProperty) {
        return gson.toJson(entityProperty);
    }
}
