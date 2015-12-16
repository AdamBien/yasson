package org.eclipse.persistence.json.bind.internal.conversion;

import java.lang.reflect.Type;

/**
 * @author David Král
 */
public class BooleanTypeConverter extends AbstractTypeConverter<Boolean> {

    public BooleanTypeConverter() {
        super(Boolean.class);
    }

    @Override
    public Boolean fromJson(String jsonValue, Type type) {
        return Boolean.parseBoolean(jsonValue);
    }

    @Override
    public String toJson(Boolean object) {
        return String.valueOf(object);
    }

    @Override
    public boolean supportsToJson(Class<?> type) {
        return super.supportsToJson(type)
                || type == boolean.class;
    }

    @Override
    public boolean supportsFromJson(Class<?> type) {
        return super.supportsFromJson(type)
                || type == boolean.class;
    }
}
