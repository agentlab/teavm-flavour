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
package org.teavm.flavour.directives.html;

import org.teavm.flavour.templates.BindAttributeDirective;
import org.teavm.flavour.templates.BindContent;
import org.teavm.flavour.templates.Renderable;
import org.teavm.flavour.templates.ValueChangeListener;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;

/**
 *
 * @author Alexey Andreev
 */
@BindAttributeDirective(name = "change")
public class ValueChangeBinder implements Renderable {
    private HTMLInputElement element;
    private ValueChangeListener<String> listener;
    private boolean bound;

    public ValueChangeBinder(HTMLElement element) {
        this.element = (HTMLInputElement) element;
    }

    @BindContent
    public void setListener(ValueChangeListener<String> listener) {
        this.listener = listener;
    }

    @Override
    public void render() {
        if (!bound) {
            bound = true;
            element.addEventListener("change", eventListener);
        }
    }

    @Override
    public void destroy() {
        if (bound) {
            bound = false;
            element.removeEventListener("change", eventListener);
        }
    }

    private EventListener<Event> eventListener = evt -> listener.changed(element.getValue());
}
