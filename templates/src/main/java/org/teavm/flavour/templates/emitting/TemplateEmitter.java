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
package org.teavm.flavour.templates.emitting;

import java.util.ArrayList;
import java.util.List;
import org.teavm.flavour.mp.Emitter;
import org.teavm.flavour.mp.ReflectValue;
import org.teavm.flavour.mp.Value;
import org.teavm.flavour.templates.Fragment;
import org.teavm.flavour.templates.tree.TemplateNode;

/**
 *
 * @author Alexey Andreev
 */
public class TemplateEmitter {
    private Emitter<Fragment> em;
    private OffsetToLineMapper locationMapper;

    public TemplateEmitter(Emitter<Fragment> em, OffsetToLineMapper locationMapper) {
        this.em = em;
        this.locationMapper = locationMapper;
    }

    public Value<Fragment> emitTemplate(ReflectValue<Object> model, String sourceFileName,
            List<TemplateNode> fragment) {
        Value<Fragment> innerFragment = emitInnerFragment(sourceFileName, model, fragment);
        return emitWorker(innerFragment);
    }

    private Value<Fragment> emitInnerFragment(String sourceFileName, ReflectValue<Object> model,
            List<TemplateNode> fragment) {
        EmitContext context = new EmitContext(locationMapper);
        context.sourceFileName = sourceFileName;
        context.addVariable("this", lem -> lem.emit(() -> model));
        context.model = model;
        return new FragmentEmitter(context).emitTemplate(em, null, fragment, new ArrayList<>());
    }

    private Value<Fragment> emitWorker(Value<Fragment> innerFragment) {
        return em.proxy(Fragment.class, (proxyEm, instance, method, args) -> {
            int argCount = args.length;
            Value<Object[]> innerArgs = proxyEm.emit(() -> new Object[argCount]);
            for (int i = 0; i < argCount; ++i) {
                int index = i;
                proxyEm.emit(() -> innerArgs.get()[index] = args[index]);
            }
            Value<Object> result = proxyEm.emit(() -> method.invoke(innerFragment, innerArgs.get()));
            proxyEm.returnValue(result);
        });
    }
}
