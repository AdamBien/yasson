/*******************************************************************************
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * <p>
 * Contributors:
 * Roman Grigoriadi
 ******************************************************************************/

package org.eclipse.yasson.internal.unmarshaller;

import org.eclipse.yasson.internal.JsonbParser;
import org.eclipse.yasson.internal.JsonbRiParser;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.Unmarshaller;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.model.JsonBindingModel;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

/**
 * @author Roman Grigoriadi
 */
public abstract class AbstractContainerDeserializer<T> extends AbstractDeserializer<T> implements JsonbDeserializer<T> {

    protected JsonbRiParser.LevelContext parserContext;

    /**
     * Create instance of current item with its builder.
     *
     * @param builder
     */
    protected AbstractContainerDeserializer(DeserializerBuilder builder) {
        super(builder);
    }

    /**
     * Drives JSONP {@link JsonParser} to deserialize json document.
     *
     * @return instance of a type for this item
     */
    @Override
    public final T deserialize(JsonParser parser, DeserializationContext context, Type rtType) {
        Unmarshaller ctx = (Unmarshaller) context;
        ctx.setCurrent(this);
        deserializeInternal((JsonbParser) parser, ctx);
        ctx.setCurrent(getWrapper());
        return getInstance();
    }

    protected abstract T getInstance();


    protected void deserializeInternal(JsonbParser parser, Unmarshaller context) {
        parserContext = moveToFirst(parser);
        while (parser.hasNext()) {
            final JsonParser.Event event = parser.next();
            switch (event) {
                case START_OBJECT:
                case START_ARRAY:
                case VALUE_STRING:
                case VALUE_NUMBER:
                case VALUE_FALSE:
                case VALUE_TRUE:
                    deserializeNext(parser, context);
                    break;
                case KEY_NAME:
                    break;
                case VALUE_NULL:
                    appendResult(null);
                    break;
                case END_OBJECT:
                case END_ARRAY:
                    return;
                default:
                    throw new JsonbException(Messages.getMessage(MessageKeys.NOT_VALUE_TYPE, event));
            }
        }
    }

    /**
     * Determine class mappings and create an instance of a new deserializer.
     * Currently processed deserializer is pushed to stack, for waiting till new object is finished.
     */
    protected abstract void deserializeNext(JsonParser parser, Unmarshaller context);

    /**
     * Move to first event for current deserializer structure.
     * @param parser parser
     * @return first event
     */
    protected abstract JsonbRiParser.LevelContext moveToFirst(JsonbParser parser);

    /**
     * Binding model for current deserializer.
     * @return model
     */
    protected abstract JsonBindingModel getModel();


    protected DeserializerBuilder newUnmarshallerItemBuilder() {
        return new DeserializerBuilder().withWrapper(this).withJsonValueType(JsonValueType.of(parserContext.getLastEvent()));
    }

    protected JsonbDeserializer<?> newCollectionOrMapItem(Type valueType) {
        Type actualValueType = ReflectionUtils.resolveType(this, valueType);
        return newUnmarshallerItemBuilder().withType(actualValueType).withModel(getModel()).build();
    }

    /**
     * After object is transitively deserialized from JSON, "append" it to its wrapper.
     * In case of a field set value to field, in case of collections
     * or other embedded objects use methods provided.
     * @param result instance result of an item
     */
    public abstract void appendResult(Object result);
}
