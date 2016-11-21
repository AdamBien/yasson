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
 * Roman Grigoriadi
 ******************************************************************************/

package org.eclipse.yasson.defaultmapping.inheritance.model.generics;

/**
 * @author Roman Grigoriadi
 */
public class SecondLevelGeneric<S,SF extends S,Z> extends FirstLevelGeneric<SF,Z> {

    private S inSecondLevel;

    public S getInSecondLevel() {
        return inSecondLevel;
    }

    public void setInSecondLevel(S inSecondLevel) {
        this.inSecondLevel = inSecondLevel;
    }

}
