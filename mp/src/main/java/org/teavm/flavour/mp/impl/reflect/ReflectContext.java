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
package org.teavm.flavour.mp.impl.reflect;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.teavm.flavour.mp.impl.EmitterContextImpl;
import org.teavm.model.ClassReaderSource;
import org.teavm.model.ElementModifier;
import org.teavm.model.ElementReader;
import org.teavm.model.ValueType;

/**
 *
 * @author Alexey Andreev
 */
public class ReflectContext {
    private ClassReaderSource classSource;
    private EmitterContextImpl emitterContext;
    private Map<ValueType, ReflectClassImpl<?>> classes = new HashMap<>();
    private ClassLoader classLoader;

    public ReflectContext(ClassReaderSource classSource, ClassLoader classLoader) {
        this.classSource = classSource;
        this.classLoader = classLoader;
    }

    public void setEmitterContext(EmitterContextImpl emitterContext) {
        this.emitterContext = emitterContext;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ClassReaderSource getClassSource() {
        return classSource;
    }

    public ReflectClassImpl<?> getClass(ValueType type) {
        return classes.computeIfAbsent(type, t -> new ReflectClassImpl<>(type, emitterContext));
    }

    public static int getModifiers(ElementReader element) {
        int modifiers = 0;
        switch (element.getLevel()) {
            case PUBLIC:
                modifiers |= Modifier.PUBLIC;
                break;
            case PROTECTED:
                modifiers |= Modifier.PROTECTED;
                break;
            case PRIVATE:
                modifiers |= Modifier.PRIVATE;
                break;
            case PACKAGE_PRIVATE:
                break;
        }
        Set<ElementModifier> modifierSet = element.readModifiers();
        if (modifierSet.contains(ElementModifier.ABSTRACT)) {
            modifiers |= Modifier.ABSTRACT;
        }
        if (modifierSet.contains(ElementModifier.FINAL)) {
            modifiers |= Modifier.FINAL;
        }
        if (modifierSet.contains(ElementModifier.INTERFACE)) {
            modifiers |= Modifier.INTERFACE;
        }
        if (modifierSet.contains(ElementModifier.NATIVE)) {
            modifiers |= Modifier.NATIVE;
        }
        if (modifierSet.contains(ElementModifier.STATIC)) {
            modifiers |= Modifier.STATIC;
        }
        if (modifierSet.contains(ElementModifier.STRICT)) {
            modifiers |= Modifier.STRICT;
        }
        if (modifierSet.contains(ElementModifier.SYNCHRONIZED)) {
            modifiers |= Modifier.SYNCHRONIZED;
        }
        if (modifierSet.contains(ElementModifier.TRANSIENT)) {
            modifiers |= Modifier.TRANSIENT;
        }
        if (modifierSet.contains(ElementModifier.VOLATILE)) {
            modifiers |= Modifier.VOLATILE;
        }
        return modifiers;
    }
}
