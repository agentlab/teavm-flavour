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
package org.teavm.flavour.expr.plan;

/**
 *
 * @author Alexey Andreev
 */
public class ConditionalPlan extends Plan {
    private Plan condition;
    private Plan consequent;
    private Plan alternative;

    public ConditionalPlan(Plan condition, Plan consequent, Plan alternative) {
        this.condition = condition;
        this.consequent = consequent;
        this.alternative = alternative;
    }

    public Plan getCondition() {
        return condition;
    }

    public void setCondition(Plan condition) {
        this.condition = condition;
    }

    public Plan getConsequent() {
        return consequent;
    }

    public void setConsequent(Plan consequent) {
        this.consequent = consequent;
    }

    public Plan getAlternative() {
        return alternative;
    }

    public void setAlternative(Plan alternative) {
        this.alternative = alternative;
    }

    @Override
    public void acceptVisitor(PlanVisitor visitor) {
        visitor.visit(this);
    }
}
