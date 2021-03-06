/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.flavour.expr.type;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Alexey Andreev
 */
public abstract class ValueType {
    ValueType() {
    }

    @Override
    public String toString() {
        return new ValueTypeFormatter().format(this);
    }

    public ValueType substitute(Substitutions substitutions) {
        return substitute(substitutions, new HashSet<>());
    }

    abstract ValueType substitute(Substitutions substitutions, Set<TypeVar> visited);
}
