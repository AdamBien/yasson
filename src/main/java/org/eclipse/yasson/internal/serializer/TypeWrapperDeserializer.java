/*******************************************************************************
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Roman Grigoriadi
 ******************************************************************************/

package org.eclipse.yasson.internal.serializer;

import org.eclipse.yasson.internal.Unmarshaller;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.internal.model.PropertyModel;
import org.eclipse.yasson.internal.model.TypeWrapper;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Handler for a {@link TypeWrapper} type.
 * Reads a class property first during deserialization and instantiates correct polymorphic type after.
 *
 * TODO ensuring class property will be first which is not the case for now!
 *
 * @author Roman Grigoriadi
 */
public class TypeWrapperDeserializer<T> extends ObjectDeserializer<TypeWrapper<T>> {

    /**
     * Parsed class from JSON to load by name.
     */
    private Class<?> concreteWrappedClass;

    /**
     * Enumeration of allowed classes to be more defensive.
     */
    private String[] allowedClassNames;

    /**
     * Creates instance of an item.
     *
     * @param builder builder to build from
     * @param allowedClassNames Array containing allowed class names.
     */
    protected TypeWrapperDeserializer(DeserializerBuilder builder, String[] allowedClassNames) {
        super(builder);
        this.allowedClassNames = allowedClassNames;
    }

    @Override
    protected void deserializeNext(JsonParser parser, Unmarshaller context) {
        final String lastKeyName = parserContext.getLastKeyName();

        if (parserContext.getLastKeyName().equals("className")) {
            PropertyModel newPropertyModel = getClassModel().findPropertyModelByJsonReadName(lastKeyName);
            final JsonbDeserializer<?> deserializer = new StringTypeDeserializer(newPropertyModel);

            String className = (String) deserializer.deserialize(parser, context, String.class);
            if (allowedClassNames.length > 0 && Stream.of(allowedClassNames).noneMatch(Predicate.isEqual(className))) {
                throw new JsonbException(Messages.getMessage(MessageKeys.CLASS_LOAD_NOT_ALLOWED, className));
            }

            try {
                concreteWrappedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new JsonbException("Cannot load class for ", e);
            }
        } else if (concreteWrappedClass != null && parserContext.getLastKeyName().equals("instance")) {
            deserializeInstance(parser, context);
        }
    }

    private void deserializeInstance(JsonParser parser, Unmarshaller context) {
        PropertyModel newPropertyModel = getClassModel().findPropertyModelByJsonReadName("instance");
        final JsonbDeserializer<?> deserializer = newUnmarshallerItemBuilder(context.getJsonbContext())
                .withType(concreteWrappedClass).withModel(newPropertyModel)
                .build();

        appendResult(deserializer.deserialize(parser, context, null));
    }
}
