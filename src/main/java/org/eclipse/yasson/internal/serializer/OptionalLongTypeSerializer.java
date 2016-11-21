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
import java.util.OptionalLong;

/**
 * @author David Král
 */
public class OptionalLongTypeSerializer extends AbstractValueTypeSerializer<OptionalLong> {

    public OptionalLongTypeSerializer(SerializerBindingModel model) {
        super(OptionalLong.class, model);
    }

    @Override
    protected void serialize(OptionalLong obj, JsonGenerator generator, String key) {
        if (obj.isPresent()) {
            generator.write(key, obj.getAsLong());
        } else {
            generator.writeNull(key);
        }
    }

    @Override
    protected void serialize(OptionalLong obj, JsonGenerator generator) {
        if (obj.isPresent()) {
            generator.write(obj.getAsLong());
        } else {
            generator.writeNull();
        }
    }
}
