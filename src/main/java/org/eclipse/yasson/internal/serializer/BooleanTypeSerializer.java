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

import org.eclipse.yasson.model.SerializerBindingModel;

import javax.json.stream.JsonGenerator;

/**
 * @author David Král
 */
public class BooleanTypeSerializer extends AbstractValueTypeSerializer<Boolean> {

    public BooleanTypeSerializer(SerializerBindingModel model) {
        super(Boolean.class, model);
    }

    @Override
    protected void serialize(Boolean obj, JsonGenerator generator, String key) {
        generator.write(key, String.valueOf(obj));
    }

    @Override
    protected void serialize(Boolean obj, JsonGenerator generator) {
        generator.write(String.valueOf(obj));
    }
}
