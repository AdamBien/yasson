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

import org.eclipse.yasson.model.JsonBindingModel;

import javax.json.stream.JsonGenerator;
import java.math.BigInteger;

/**
 * Serialize {@link BigInteger}
 * @author David Kral
 */
public class BigIntegerTypeSerializer extends AbstractNumberSerializer<BigInteger> {

    public BigIntegerTypeSerializer(JsonBindingModel model) {
        super(BigInteger.class, model);
    }

    @Override
    protected void serializeNonFormatted(BigInteger obj, JsonGenerator generator, String key) {
        generator.write(key, obj);
    }

    @Override
    protected void serializeNonFormatted(BigInteger obj, JsonGenerator generator) {
        generator.write(obj);
    }
}
