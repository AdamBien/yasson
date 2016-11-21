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
package org.eclipse.yasson.internal;

import org.eclipse.yasson.model.ClassCustomization;
import org.eclipse.yasson.model.ClassModel;
import org.eclipse.yasson.model.JsonbAnnotatedElement;

import javax.json.JsonValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSONB mappingContext. Created once per {@link javax.json.bind.Jsonb} instance. Represents a global scope.
 * Holds internal model.
 *
 * TODO make mapping context be shared cache between threads working with same payload classes
 *
 * Thread safe
 *
 * @author Dmitry Kornilov
 * @author Roman Grigoriadi
 */
public class MappingContext {
    private final ConcurrentHashMap<Class<?>, ClassModel> classes = new ConcurrentHashMap<>();
    private final ClassParser classParser = new ClassParser();
    private static final Class<?>[] supportedTypes;

    static {
        supportedTypes = new Class[]{Collection.class, Map.class, JsonValue.class};
    }

    /**
     * Search for class model.
     * Parse class and create one if not found.
     * @param clazz clazz to search by or parse, not null.
     */
    public ClassModel getOrCreateClassModel(Class<?> clazz) {
        final AnnotationIntrospector introspector = AnnotationIntrospector.getInstance();
        ClassModel classModel = classes.get(clazz);
        if (classModel != null) {
            return classModel;
        }
        final Stack<Class> newClassModels = new Stack<>();
        for (Class classToParse = clazz; classToParse != Object.class; classToParse = classToParse.getSuperclass()) {
            newClassModels.push(classToParse);
        }

        while (!newClassModels.empty()) {
            Class toParse = newClassModels.pop();
            classes.computeIfAbsent(toParse, aClass -> {
                final JsonbAnnotatedElement<Class<?>> clsElement = introspector.collectAnnotations(aClass);
                final ClassCustomization customization = introspector.introspectCustomization(clsElement);
                final ClassModel newClassModel = new ClassModel(aClass, customization);
                classParser.parseProperties(newClassModel, clsElement);
                return  newClassModel;
            });
        }
        return classes.get(clazz);
    }

    /**
     * Provided class class model is returned first by iterator.
     * Following class models are sorted by hierarchy from provided class up to the Object.class.
     *
     * @param clazz class to start iteration of class models from
     * @return iterator of class models
     */
    public Iterator<ClassModel> classModelIterator(final Class<?> clazz) {
        return new Iterator<ClassModel>() {
            private Class<?> next = clazz;

            @Override
            public boolean hasNext() {
                return next != Object.class;
            }

            @Override
            public ClassModel next() {
                final ClassModel result = classes.get(next);
                next = next.getSuperclass();
                return result;
            }
        };
    }

    /**
     * Search for class model, without parsing if not found.
     * @param clazz clazz to search by or parse, not null.
     * @return model of a class if found.
     */
    public ClassModel getClassModel(Class<?> clazz) {
        return classes.get(clazz);
    }

}
