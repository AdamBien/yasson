/*******************************************************************************
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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

import org.eclipse.yasson.internal.AbstractContainerSerializer;
import org.eclipse.yasson.internal.AnnotationIntrospector;
import org.eclipse.yasson.internal.MappingContext;
import org.eclipse.yasson.internal.ProcessingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.internalOrdering.AnnotationOrderStrategy;
import org.eclipse.yasson.internal.internalOrdering.PropOrderStrategy;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.model.ClassModel;
import org.eclipse.yasson.model.PropertyModel;

import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.config.PropertyOrderStrategy;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Serializes arbitrary object by reading its properties.
 *
 * @author Roman Grigoriadi
 */
public class ObjectSerializer<T> extends AbstractContainerSerializer<T> {


    public ObjectSerializer(SerializerBuilder builder) {
        super(builder);
    }

    @Override
    protected void serializeInternal(T object, JsonGenerator generator, SerializationContext ctx) {
        // Deal with inheritance
        final List<PropertyModel> allProperties = new LinkedList<>();
        final MappingContext mappingContext = ProcessingContext.getMappingContext();
        for (Class clazz = object.getClass(); clazz.getSuperclass() != null; clazz = clazz.getSuperclass()) {
            ClassModel classModel = mappingContext.getOrCreateClassModel(clazz);
            final List<PropertyModel> properties = new ArrayList<>(classModel.getProperties().values());
            Optional<JsonbPropertyOrder> jsonbPropertyOrder = AnnotationIntrospector.getInstance().getJsonbPropertyOrderAnnotation(clazz);
            List<PropertyModel> filteredAndSorted;
            //Check if the class has JsonbPropertyOrder annotation defined
            //TODO check if implementation of ordering is sound.
            if (!jsonbPropertyOrder.isPresent()) {
                final Map<String, PropOrderStrategy> orderStrategies = ProcessingContext.getJsonbContext().getOrderStrategies();
                //Sorting fields according to selected or default order
                String propertyOrderStrategy = ProcessingContext.getJsonbContext().getConfig().getProperty(JsonbConfig.PROPERTY_ORDER_STRATEGY).isPresent() ? (String) ProcessingContext.getJsonbContext().getConfig().getProperty(JsonbConfig.PROPERTY_ORDER_STRATEGY).get() : PropertyOrderStrategy.LEXICOGRAPHICAL;
                if (!orderStrategies.containsKey(propertyOrderStrategy)) {
                    throw new JsonbException(Messages.getMessage(MessageKeys.PROPERTY_ORDER, propertyOrderStrategy));
                }
                filteredAndSorted = orderStrategies.get(propertyOrderStrategy).sortProperties(properties);
            } else {
                filteredAndSorted = new AnnotationOrderStrategy(jsonbPropertyOrder.get().value()).sortProperties(properties);
            }
            filteredAndSorted = filteredAndSorted.stream().filter(propertyModel -> !allProperties.contains(propertyModel)).collect(toList());
            allProperties.addAll(0, filteredAndSorted);
        }

        allProperties.stream()
                .forEach((propertyModel) -> marshallProperty(object, generator, ctx, propertyModel));
    }

    @Override
    protected void writeStart(JsonGenerator generator) {
        generator.writeStartObject();
    }

    @Override
    protected void writeStart(String key, JsonGenerator generator) {
        generator.writeStartObject(key);
    }

    @SuppressWarnings("unchecked")
    private void marshallProperty(T object, JsonGenerator generator, SerializationContext ctx, PropertyModel propertyModel) {
        final Object propertyValue = propertyModel.getValue(object);
        if (propertyValue == null || isEmptyOptional(propertyValue)) {
            if (propertyModel.getCustomization().isNillable()) {
                generator.writeNull(propertyModel.getJsonWriteName());
            }
            return;
        }
        Type genericType = ReflectionUtils.resolveType(this, propertyModel.getType());
//        Type genericType = propertyModel.getType();
        final JsonbSerializer<?> serializer = new SerializerBuilder().withWrapper(this)
                .withObjectClass(propertyValue.getClass()).withModel(propertyModel)
                .withType(genericType).build();
        serializerCaptor(serializer, propertyValue, generator, ctx);
    }

}
