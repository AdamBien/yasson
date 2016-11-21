/*******************************************************************************
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Dmitry Kornilov - initial implementation
 ******************************************************************************/
package org.eclipse.yasson.internal;

import org.eclipse.yasson.model.ClassModel;
import org.eclipse.yasson.model.JsonbAnnotatedElement;
import org.eclipse.yasson.model.Property;
import org.eclipse.yasson.model.PropertyModel;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created a class internal model.
 *
 * @author Dmitry Kornilov
 */
class ClassParser {

    public static final String IS_PREFIX = "is";

    public static final String GET_PREFIX = "get";

    public static final String SET_PREFIX = "set";

    public static final String GENERATED_PREFIX = "this$";

    /**
     * Parse class fields and getters setters. Merge to java bean like properties.
     *
     * @return model of a class
     */
    public void parseProperties(ClassModel classModel, JsonbAnnotatedElement<Class<?>> classElement) {

        final Map<String, Property> classProperties = new HashMap<>();

        parseFields(classElement, classProperties);

        parseClassAndInterfaceMethods(classElement, classProperties);

        classProperties.values().stream().forEach((property)->{
            PropertyModel propertyModel = new PropertyModel(classModel, property);
            classModel.addProperty(propertyModel);
        });

    }

    private void parseClassAndInterfaceMethods(JsonbAnnotatedElement<Class<?>> classElement, Map<String, Property> classProperties) {
        Class<?> concreteClass = classElement.getElement();
        parseMethods(concreteClass, classElement, classProperties);
        for (Class<?> ifc : AnnotationIntrospector.getInstance().collectInterfaces(concreteClass)) {
            parseIfaceMethodAnnotations(ifc, classProperties);
        }
    }

    private void parseIfaceMethodAnnotations(Class<?> ifc, Map<String, Property> classProperties) {
        for(Method method : ifc.getDeclaredMethods()) {
            final String methodName = method.getName();
            if (!isPropertyMethod(methodName)) {
                continue;
            }
            String propertyName = toPropertyMethod(methodName);
            final Property property = classProperties.get(propertyName);
            if (property == null) {
                //May happen for classes which both extend a class with some method and implement interface with same method.
                continue;
            }
            JsonbAnnotatedElement<Method> methodElement = isGetter(methodName) ?
                    property.getGetterElement() : property.getSetterElement();
            //Only push iface annotations if not overridden on impl classes
            for (Annotation ann : method.getDeclaredAnnotations()) {
                if (methodElement.getAnnotation(ann.annotationType()) == null) {
                    methodElement.putAnnotation(ann);
                }
            }
        }
    }

    private void parseMethods(Class<?> clazz, JsonbAnnotatedElement<Class<?>> classElement, Map<String, Property> classProperties) {
        for (Method method : clazz.getDeclaredMethods()) {
            String name = method.getName();
            if (!isPropertyMethod(name)) {
                continue;
            }
            final String propertyName = toPropertyMethod(name);

            Property property = classProperties.get(propertyName);
            if (property == null) {
                property= new Property(propertyName, classElement);
                classProperties.put(propertyName, property);
            }

            if (isSetter(name)) {
                property.setSetter(method);
            } else {
                property.setGetter(method);
            }
        }
    }

    private boolean isGetter(String methodName) {
        return methodName.startsWith(GET_PREFIX) || methodName.startsWith(IS_PREFIX);
    }

    private boolean isSetter(String methodName) {
        return methodName.startsWith(SET_PREFIX);
    }

    private String toPropertyMethod(String name) {
        return Introspector.decapitalize(name.substring(name.startsWith(IS_PREFIX) ? 2 : 3, name.length()));
    }

    private boolean isPropertyMethod(String name) {
        return name.startsWith(GET_PREFIX) || name.startsWith(SET_PREFIX) || name.startsWith(IS_PREFIX);
    }

    private void parseFields(JsonbAnnotatedElement<Class<?>> classElement, Map<String, Property> classProperties) {
        for (Field field : classElement.getElement().getDeclaredFields()) {
            final String name = field.getName();
            if (field.getName().startsWith(GENERATED_PREFIX)) {
                continue;
            }
            final Property property = new Property(name, classElement);
            property.setField(field);
            classProperties.put(name, property);
        }
    }

}
