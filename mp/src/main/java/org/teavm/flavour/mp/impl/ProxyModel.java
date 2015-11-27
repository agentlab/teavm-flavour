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
package org.teavm.flavour.mp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.teavm.model.MethodReference;

/**
 *
 * @author Alexey Andreev
 */
public class ProxyModel {
    private MethodReference method;
    private List<ProxyParameter> parameters = new ArrayList<>();
    private List<ProxyParameter> readonlyParameters = Collections.unmodifiableList(parameters);
    private List<ProxyParameter> dynamicParameters = new ArrayList<>();
    private List<ProxyParameter> readonlyDynamicParameters = Collections.unmodifiableList(dynamicParameters);

    ProxyModel(MethodReference method) {
        this.method = method;
    }

    public MethodReference getMethod() {
        return method;
    }

    public List<ProxyParameter> getParameters() {
        return readonlyParameters;
    }

    public List<ProxyParameter> getDynamicParameters() {
        return readonlyDynamicParameters;
    }
}