/*******************************************************************************
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * <p>
 * Contributors:
 * Dmitry Kornilov - initial implementation
 ******************************************************************************/
package org.eclipse.yasson.model;

import org.eclipse.yasson.internal.ProcessingContext;
import org.eclipse.yasson.internal.naming.CaseInsensitiveStrategy;

import javax.json.bind.JsonbException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A model for Java class.
 *
 * @author Dmitry Kornilov
 */
public class ClassModel {

    private final Class<?> clazz;

    private final ClassCustomization classCustomization;

    /**
     * A list of class properties.
     */
    private final Map<String, PropertyModel> properties = new HashMap<>();

    /**
     * Gets a property model by default (non customized) name
     * @param name a name as parsed from field / getter / setter without annotation customizing
     * @return property model
     */
    public PropertyModel getPropertyModel(String name) {
        return properties.get(name);
    }

    /**
     * Create instance of class model.
     * @param clazz class to model onto
     */
    public ClassModel(Class<?> clazz, ClassCustomization customization) {
        this.clazz = clazz;
        this.classCustomization = customization;
    }

    /**
     * Search for field in this class model and superclasses of its class.
     * @param jsonReadName name as it appears in JSON during reading.
     * @return PropertyModel if found.
     */
    public PropertyModel findPropertyModelByJsonReadName(String jsonReadName) {
        Objects.requireNonNull(jsonReadName);
        return searchProperty(this, jsonReadName);
    }

    private PropertyModel searchProperty(ClassModel classModel, String jsonReadName) {
        final PropertyModel result = classModel.getPropertyModel(jsonReadName);
        if (result != null) {
            return result;
        }
        for (PropertyModel propertyModel : properties.values()) {
            if (equalsReadName(jsonReadName, propertyModel)) {
                return propertyModel;
            }
        }
        final ClassModel parent =
                ProcessingContext.getMappingContext().getClassModel(classModel.getType().getSuperclass());
        return parent == null ? null : searchProperty(parent, jsonReadName);
    }

    /**
     * Check if name is equal according to property strategy. In case of {@link CaseInsensitiveStrategy} ignore case.
     * User can provide own strategy implementation, cast to custom interface is not an option.
     */
    private boolean equalsReadName(String jsonName, PropertyModel propertyModel) {
        final String propertyReadName = propertyModel.getReadName();
        if (ProcessingContext.getJsonbContext().getPropertyNamingStrategy() instanceof CaseInsensitiveStrategy) {
            return jsonName.equalsIgnoreCase(propertyReadName);
        }
        return jsonName.equalsIgnoreCase(propertyReadName);
    }

    public Customization getCustomization() {
        return classCustomization;
    }

    public Class<?> getType() {
        return clazz;
    }

    /**
     * Get class properties copy, combination of field and its getter / setter, javabeans alike.
     * @return class properties.
     */
    public Map<String, PropertyModel> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Adds a property model to a class model. Checks if a property with same name exists.
     * @param propertyModel initialise model of a property not null
     */
    public void addProperty(PropertyModel propertyModel) {
        Objects.requireNonNull(propertyModel);
        for (PropertyModel existing : properties.values()) {
            if (propertyModel.getReadName().equals(existing.getReadName()) ||
                    propertyModel.getJsonWriteName().equals(existing.getJsonWriteName())) {
                throw new JsonbException(String.format("Property %s clashes with property %s by read or write name in class %s.",
                        propertyModel.getPropertyName(), existing.getPropertyName(), getType().getName()));
            }
        }
        properties.put(propertyModel.getPropertyName(), propertyModel);
    }

    /**
     * Introspected customization for a class.
     * @return immutable class customization.
     */
    public ClassCustomization getClassCustomization() {
        return classCustomization;
    }


}
