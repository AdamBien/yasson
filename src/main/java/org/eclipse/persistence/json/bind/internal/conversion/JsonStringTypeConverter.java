package org.eclipse.persistence.json.bind.internal.conversion;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.lang.reflect.Type;

/**
 * @author David Král
 */
public class JsonStringTypeConverter extends AbstractTypeConverter<JsonString> {

    public JsonStringTypeConverter() {
        super(JsonString.class);
    }

    @Override
    public JsonString fromJson(String jsonValue, Type type) {
        final JsonBuilderFactory factory = Json.createBuilderFactory(null);
        final JsonObject jsonObject = factory.createObjectBuilder()
                .add("json", jsonValue)
                .build();
        return jsonObject.getJsonString("json");
    }

    @Override
    public String toJson(JsonString object) {
        return object.toString();
    }

}
