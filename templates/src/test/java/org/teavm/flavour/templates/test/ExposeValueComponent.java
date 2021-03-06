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
package org.teavm.flavour.templates.test;

import org.teavm.flavour.templates.AbstractComponent;
import org.teavm.flavour.templates.BindAttribute;
import org.teavm.flavour.templates.BindContent;
import org.teavm.flavour.templates.BindDirective;
import org.teavm.flavour.templates.Component;
import org.teavm.flavour.templates.Fragment;
import org.teavm.flavour.templates.Slot;

/**
 *
 * @author Alexey Andreev
 */
@BindDirective(name = "expose")
public class ExposeValueComponent extends AbstractComponent {
    private Fragment body;
    private Component bodyComponent;
    public static String value = "";

    public ExposeValueComponent(Slot slot) {
        super(slot);
    }

    @BindAttribute(name = "var")
    public String getValue() {
        return value;
    }

    @BindContent
    public void setBody(Fragment body) {
        this.body = body;
    }

    @Override
    public void render() {
        if (bodyComponent == null) {
            bodyComponent = body.create();
            getSlot().append(bodyComponent.getSlot());
        }
        bodyComponent.render();
    }
}
