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
package org.teavm.flavour.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alexey Andreev
 */
public class PathReader {
    private Map<Class<?>, List<Object>> pathSets = new HashMap<>();

    public boolean read(String path) {
        return false;
    }

    public void addPathSet(Object pathSet) {
        for (Class<?> cls : extractTypes(pathSet)) {
            pathSets.computeIfAbsent(cls, key -> new ArrayList<>()).add(pathSet);
        }
    }

    Collection<Object> getListeners(Class<?> type) {
        return pathSets.getOrDefault(type, Collections.emptyList());
    }

    private static native Class<?>[] extractTypes(Object pathSet);
}
