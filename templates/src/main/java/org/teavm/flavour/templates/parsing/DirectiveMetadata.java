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
package org.teavm.flavour.templates.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.teavm.flavour.expr.type.meta.MethodDescriber;

/**
 *
 * @author Alexey Andreev
 */
class DirectiveMetadata extends BaseDirectiveMetadata {
    MethodDescriber constructor;
    MethodDescriber contentSetter;
    boolean ignoreContent;
    final Map<String, DirectiveAttributeMetadata> attributes = new HashMap<>();
    final List<NestedDirective> nestedDirectives = new ArrayList<>();
}
