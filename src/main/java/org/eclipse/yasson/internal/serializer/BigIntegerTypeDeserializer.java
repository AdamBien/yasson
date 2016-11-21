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

import org.eclipse.yasson.internal.Unmarshaller;
import org.eclipse.yasson.model.JsonBindingModel;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Deserialize {@link BigInteger}.
 * @author David Král
 */
public class BigIntegerTypeDeserializer extends AbstractNumberDeserializer<BigInteger> {

    public BigIntegerTypeDeserializer(JsonBindingModel model) {
        super(BigInteger.class, model);
    }

    @Override
    public BigInteger deserialize(String jsonValue, Unmarshaller unmarshaller, Type rtType) {
        return deserializeForamtted(jsonValue, true).map(num->new BigInteger(num.toString()))
                .orElseGet(()->new BigInteger(jsonValue));
    }
}
