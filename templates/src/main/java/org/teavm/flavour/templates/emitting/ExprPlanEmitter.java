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
import org.teavm.flavour.expr.plan.ArithmeticCastPlan;
import org.teavm.flavour.expr.plan.ArrayConstructionPlan;
import org.teavm.flavour.expr.plan.ArrayLengthPlan;
import org.teavm.flavour.expr.plan.BinaryPlan;
import org.teavm.flavour.expr.plan.CastFromIntegerPlan;
import org.teavm.flavour.expr.plan.CastPlan;
import org.teavm.flavour.expr.plan.CastToIntegerPlan;
import org.teavm.flavour.expr.plan.ConditionalPlan;
import org.teavm.flavour.expr.plan.ConstantPlan;
import org.teavm.flavour.expr.plan.ConstructionPlan;
import org.teavm.flavour.expr.plan.FieldAssignmentPlan;
import org.teavm.flavour.expr.plan.FieldPlan;
import org.teavm.flavour.expr.plan.GetArrayElementPlan;
import org.teavm.flavour.expr.plan.InstanceOfPlan;
import org.teavm.flavour.expr.plan.InvocationPlan;
import org.teavm.flavour.expr.plan.LambdaPlan;
import org.teavm.flavour.expr.plan.LogicalBinaryPlan;
import org.teavm.flavour.expr.plan.NegatePlan;
import org.teavm.flavour.expr.plan.NotPlan;
import org.teavm.flavour.expr.plan.Plan;
import org.teavm.flavour.expr.plan.PlanVisitor;
import org.teavm.flavour.expr.plan.ReferenceEqualityPlan;
import org.teavm.flavour.expr.plan.ThisPlan;
import org.teavm.flavour.expr.plan.VariablePlan;
import org.teavm.flavour.mp.Emitter;
import org.teavm.flavour.mp.EmitterContext;
import org.teavm.flavour.mp.ReflectClass;
import org.teavm.flavour.mp.Value;
import org.teavm.flavour.mp.reflect.ReflectField;
import org.teavm.flavour.mp.reflect.ReflectMethod;
import org.teavm.flavour.templates.Templates;
import org.teavm.model.emit.ValueEmitter;

/**
 *
 * @author Alexey Andreev
 */
class ExprPlanEmitter implements PlanVisitor {
    private EmitContext context;
    Emitter<?> em;
    Value<Object> var;
    ValueEmitter thisVar;

    ExprPlanEmitter(EmitContext context, Emitter<?> em) {
        this.context = context;
        this.em = em;
    }

    private void location(Plan plan) {
        context.location(em, plan.getLocation());
    }

    @Override
    public void visit(ConstantPlan plan) {
        location(plan);
        Object value = plan.getValue();
        if (value == null) {
            var = em.lazy(() -> null);
        } else {
            var = em.lazy(() -> value);
        }
    }

    @Override
    public void visit(VariablePlan plan) {
        location(plan);
        emitVariable(plan.getName());
    }

    @Override
    public void visit(ThisPlan plan) {
        location(plan);
        emitVariable("this");
    }

    private void emitVariable(String name) {
        var = context.getVariable(name).emit(em);
    }

    @Override
    public void visit(BinaryPlan plan) {
        plan.getFirstOperand().acceptVisitor(this);
        Value<Object> first = var;
        plan.getSecondOperand().acceptVisitor(this);
        Value<Object> second = var;

        location(plan);
        switch (plan.getType()) {
            case ADD:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() + (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() + (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() + (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() + (Double) second.get());
                        break;
                }
                break;
            case SUBTRACT:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() - (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() - (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() - (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() - (Double) second.get());
                        break;
                }
                break;
            case MULTIPLY:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() * (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() * (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() * (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() * (Double) second.get());
                        break;
                }
                break;
            case DIVIDE:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() / (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() / (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() / (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() / (Double) second.get());
                        break;
                }
                break;
            case REMAINDER:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() % (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() % (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() % (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() % (Double) second.get());
                        break;
                }
                break;
            case EQUAL:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> ((Integer) first.get()).intValue() == (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> ((Long) first.get()).longValue() == (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> ((Float) first.get()).floatValue() == (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> ((Double) first.get()).doubleValue() == (Double) second.get());
                        break;
                }
                break;
            case NOT_EQUAL:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> ((Integer) first.get()).intValue() != (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> ((Long) first.get()).longValue() != (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> ((Float) first.get()).floatValue() != (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> ((Double) first.get()).doubleValue() != (Double) second.get());
                        break;
                }
                break;
            case GREATER:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() > (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() > (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() > (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() > (Double) second.get());
                        break;
                }
                break;
            case GREATER_OR_EQUAL:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() >= (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() >= (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() >= (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() >= (Double) second.get());
                        break;
                }
                break;
            case LESS:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() < (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() < (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() < (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() < (Double) second.get());
                        break;
                }
                break;
            case LESS_OR_EQUAL:
                switch (plan.getValueType()) {
                    case INT:
                        var = em.lazy(() -> (Integer) first.get() <= (Integer) second.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (Long) first.get() <= (Long) second.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (Float) first.get() <= (Float) second.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (Double) first.get() <= (Double) second.get());
                        break;
                }
                break;
        }
    }

    @Override
    public void visit(NegatePlan plan) {
        plan.getOperand().acceptVisitor(this);
        location(plan);
        Value<Object> operand = var;
        switch (plan.getValueType()) {
            case INT:
                var = em.lazy(() -> -(Integer) operand.get());
                break;
            case LONG:
                var = em.lazy(() -> -(Long) operand.get());
                break;
            case FLOAT:
                var = em.lazy(() -> -(Float) operand.get());
                break;
            case DOUBLE:
                var = em.lazy(() -> -(Double) operand.get());
                break;
        }
    }

    @Override
    public void visit(ReferenceEqualityPlan plan) {
        plan.getFirstOperand().acceptVisitor(this);
        Value<Object> first = var;
        plan.getSecondOperand().acceptVisitor(this);
        Value<Object> second = var;

        location(plan);
        switch (plan.getType()) {
            case EQUAL:
                var = em.lazy(() -> first == second);
                break;
            case NOT_EQUAL:
                var = em.lazy(() -> first != second);
                break;
        }
    }

    @Override
    public void visit(LogicalBinaryPlan plan) {
        plan.getFirstOperand().acceptVisitor(this);
        Value<Object> first = var;
        plan.getSecondOperand().acceptVisitor(this);
        Value<Object> second = var;

        location(plan);
        switch (plan.getType()) {
            case AND:
                var = em.lazy(() -> (Boolean) first.get() && (Boolean) second.get());
                break;
            case OR:
                var = em.lazy(() -> (Boolean) first.get() || (Boolean) second.get());
                break;
        }
    }

    @Override
    public void visit(NotPlan plan) {
        plan.getOperand().acceptVisitor(this);
        Value<Object> operand = var;

        location(plan);
        var = em.lazy(() -> !(Boolean) operand.get());
    }

    @Override
    public void visit(CastPlan plan) {
        plan.getOperand().acceptVisitor(this);
        Value<Object> operand = var;
        TypeParser typeParser = new TypeParser(plan.getTargetType());
        ReflectClass<Object> cls = typeParser.parse().asSubclass(Object.class);

        location(plan);
        var = em.lazy(() -> cls.cast(operand.get()));
    }

    @Override
    public void visit(ArithmeticCastPlan plan) {
        plan.getOperand().acceptVisitor(this);
        Value<Object> operand = var;

        location(plan);
        switch (plan.getSourceType()) {
            case INT:
                switch (plan.getTargetType()) {
                    case INT:
                        break;
                    case LONG:
                        var = em.lazy(() -> (long) (Integer) operand.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (float) (Integer) operand.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (double) (Integer) operand.get());
                        break;
                }
                break;
            case LONG:
                switch (plan.getTargetType()) {
                    case INT:
                        var = em.lazy(() -> (int) (long) (Long) operand.get());
                        break;
                    case LONG:
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (float) (Long) operand.get());
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (double) (Long) operand.get());
                        break;
                }
                break;
            case FLOAT:
                switch (plan.getTargetType()) {
                    case INT:
                        var = em.lazy(() -> (int) (float) (Float) operand.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (long) (float) (Float) operand.get());
                        break;
                    case FLOAT:
                        break;
                    case DOUBLE:
                        var = em.lazy(() -> (double) (Float) operand.get());
                        break;
                }
                break;
            case DOUBLE:
                switch (plan.getTargetType()) {
                    case INT:
                        var = em.lazy(() -> (int) (double) (Double) operand.get());
                        break;
                    case LONG:
                        var = em.lazy(() -> (long) (double) (Double) operand.get());
                        break;
                    case FLOAT:
                        var = em.lazy(() -> (float) (double) (Double) operand.get());
                        break;
                    case DOUBLE:
                        break;
                }
                break;
        }
    }

    @Override
    public void visit(CastFromIntegerPlan plan) {
        plan.getOperand().acceptVisitor(this);
        Value<Object> value = var;
        location(plan);
        Value<Integer> intValue = em.lazy(() -> (Integer) value.get());
        switch (plan.getType()) {
            case BYTE:
                var = em.lazy(() -> (byte) (int) intValue.get());
                break;
            case SHORT:
                var = em.lazy(() -> (short) (int) intValue.get());
                break;
            case CHAR:
                var = em.lazy(() -> (char) (int) intValue.get());
                break;
        }
    }

    @Override
    public void visit(CastToIntegerPlan plan) {
        plan.getOperand().acceptVisitor(this);
        Value<Object> value = var;

        location(plan);
        switch (plan.getType()) {
            case BYTE:
                var = em.lazy(() -> (int) (Byte) value.get());
                break;
            case SHORT:
                var = em.lazy(() -> (int) (Short) value.get());
                break;
            case CHAR:
                var = em.lazy(() -> (int) (Character) value.get());
                break;
        }
    }

    @Override
    public void visit(GetArrayElementPlan plan) {
        plan.getArray().acceptVisitor(this);
        Value<Object> array = var;
        plan.getIndex().acceptVisitor(this);
        Value<Object> index = var;

        location(plan);
        var = em.lazy(() -> ((Object[]) array.get())[(Integer) index.get()]);
    }

    @Override
    public void visit(ArrayLengthPlan plan) {
        plan.getArray().acceptVisitor(this);
        Value<Object> array = var;

        location(plan);
        var = em.lazy(() -> ((Object[]) array.get()).length);
    }

    @Override
    public void visit(FieldPlan plan) {
        ReflectClass<?> cls = em.getContext().findClass(plan.getClassName());
        ReflectField field = cls.getField(plan.getFieldName());

        if (plan.getInstance() != null) {
            plan.getInstance().acceptVisitor(this);
            Value<Object> instance = var;
            location(plan);
            var = em.lazy(() -> field.get(instance.get()));
        } else {
            location(plan);
            var = em.lazy(() -> field.get(null));
        }
    }

    @Override
    public void visit(FieldAssignmentPlan plan) {
        ReflectClass<?> cls = em.getContext().findClass(plan.getClassName());
        ReflectField field = cls.getField(plan.getFieldName());

        plan.getValue().acceptVisitor(this);
        Value<Object> value = var;
        if (plan.getInstance() != null) {
            plan.getInstance().acceptVisitor(this);
            Value<Object> instance = var;
            location(plan);
            var = em.lazy(() -> {
                field.set(instance.get(), value.get());
                return null;
            });
        } else {
            location(plan);
            var = em.lazy(() -> {
                field.set(null, value.get());
                return null;
            });
        }
    }

    @Override
    public void visit(InstanceOfPlan plan) {
        ReflectClass<?> cls = em.getContext().findClass(plan.getClassName());
        plan.getOperand().acceptVisitor(this);
        Value<Object> value = var;

        location(plan);
        var = em.lazy(() -> cls.isInstance(value.get()));
    }

    @Override
    public void visit(InvocationPlan plan) {
        var = em.lazyFragment(Object.class, lem -> {
            Emitter<?> oldEm = em;
            em = lem;
            try {
                Value<Object> instance;
                if (plan.getInstance() != null) {
                    plan.getInstance().acceptVisitor(this);
                    instance = var;
                } else {
                    instance = null;
                }

                location(plan);
                ReflectClass<?> cls = lem.getContext().findClass(plan.getClassName());
                ReflectMethod method = findMethod(cls, plan.getMethodName(), plan.getMethodDesc());
                int argCount = method.getParameterCount();
                Value<Object[]> arguments = lem.emit(() -> new Object[argCount]);
                for (int i = 0; i < plan.getArguments().size(); ++i) {
                    int index = i;
                    plan.getArguments().get(i).acceptVisitor(this);
                    Value<Object> argValue = var;
                    lem.emit(() -> arguments.get()[index] = argValue.get());
                }

                lem.returnValue(() -> method.invoke(instance, arguments.get()));
            } finally {
                em = oldEm;
            }
        });
    }

    @Override
    public void visit(ConstructionPlan plan) {
        var = em.lazyFragment(Object.class, lem -> {
            Emitter<?> oldEm = em;
            em = lem;
            try {
                location(plan);
                ReflectClass<?> cls = lem.getContext().findClass(plan.getClassName());
                ReflectMethod method = findMethod(cls, "<init>", plan.getMethodDesc());
                int argCount = method.getParameterCount();
                Value<Object[]> arguments = lem.emit(() -> new Object[argCount]);
                for (int i = 0; i < plan.getArguments().size(); ++i) {
                    int index = i;
                    plan.getArguments().get(i).acceptVisitor(this);
                    Value<Object> argValue = var;
                    lem.emit(() -> arguments.get()[index] = argValue.get());
                }

                lem.returnValue(() -> method.construct(arguments.get()));
            } finally {
                em = oldEm;
            }
        });
    }

    @Override
    public void visit(ArrayConstructionPlan plan) {
        List<Value<Object>> elements = new ArrayList<>();
        for (Plan elemPlan : plan.getElements()) {
            elemPlan.acceptVisitor(this);
            elements.add(var);
        }
        ReflectClass<Object> cls = new TypeParser(plan.getElementType()).parse().asSubclass(Object.class);
        var = em.lazyFragment(Object.class, lem -> {
            location(plan);
            int sz = elements.size();
            Value<Object[]> array = lem.emit(() -> cls.createArray(sz));
            for (int i = 0; i < sz; ++i) {
                Value<Object> elem = elements.get(i);
                int index = i;
                lem.emit(() -> array.get()[index] = elem.get());
            }
            lem.returnValue(array);
        });
    }

    private ReflectMethod findMethod(ReflectClass<?> owner, String name, String desc) {
        TypeParser parser = new TypeParser(desc);
        parser.index++;
        List<ReflectClass<?>> argumentTypes = new ArrayList<>();
        while (parser.text.charAt(parser.index) != ')') {
            ReflectClass<?> argumentType = parser.parse();
            if (argumentType == null) {
                return null;
            }
            argumentTypes.add(argumentType);
        }
        return owner.getMethod(name, argumentTypes.toArray(new ReflectClass<?>[0]));
    }

    @Override
    public void visit(ConditionalPlan plan) {
        plan.getCondition().acceptVisitor(this);
        Value<Object> condition = var;
        plan.getConsequent().acceptVisitor(this);
        Value<Object> consequent = var;
        plan.getAlternative().acceptVisitor(this);
        Value<Object> alternative = var;

        location(plan);
        var = em.lazy(() -> (Boolean) condition.get() ? consequent.get() : alternative.get());
    }

    @Override
    public void visit(LambdaPlan plan) {
        emit(plan, false);
    }

    public void emit(LambdaPlan plan, boolean updateTemplates) {
        location(plan);
        ReflectClass<Object> cls = em.getContext().findClass(plan.getClassName()).asSubclass(Object.class);
        var = em.proxy(cls, (bodyEm, instance, method, args) -> {
            context.pushBoundVars();
            for (int i = 0; i < args.length; ++i) {
                int argIndex = i;
                context.addVariable(plan.getBoundVars().get(i), innerEm -> innerEm.emit(args[argIndex]));
            }

            Emitter<?> oldEm = em;
            em = bodyEm;
            location(plan);
            plan.getBody().acceptVisitor(this);
            Value<Object> result = var;
            Value<Object> valueToReturn = em.emit(() -> result.get());
            if (updateTemplates) {
                em.emit(() -> Templates.update());
            }
            if (method.getReturnType() != bodyEm.getContext().findClass(void.class)) {
                bodyEm.returnValue(valueToReturn);
            }
            em = oldEm;

            context.popBoundVars();
        });
    }

    class TypeParser {
        int index;
        String text;
        EmitterContext context;

        TypeParser(String text) {
            this.text = text;
            this.context = em.getContext();
        }

        ReflectClass<?> parse() {
            if (index >= text.length()) {
                return null;
            }
            char c = text.charAt(index);
            switch (c) {
                case 'V':
                    ++index;
                    return context.findClass(void.class);
                case 'Z':
                    ++index;
                    return context.findClass(boolean.class);
                case 'B':
                    ++index;
                    return context.findClass(byte.class);
                case 'S':
                    ++index;
                    return context.findClass(short.class);
                case 'I':
                    ++index;
                    return context.findClass(int.class);
                case 'J':
                    ++index;
                    return context.findClass(long.class);
                case 'F':
                    ++index;
                    return context.findClass(float.class);
                case 'D':
                    ++index;
                    return context.findClass(double.class);
                case 'L': {
                    int next = text.indexOf(';', ++index);
                    if (next < 0) {
                        return null;
                    }
                    ReflectClass<?> cls = context.findClass(text.substring(index, next).replace('/', '.'));
                    index = next + 1;
                    return cls;
                }
                case '[': {
                    ++index;
                    ReflectClass<?> component = parse();
                    return component != null ? context.arrayClass(component) : null;
                }
                default:
                    return null;
            }
        }
    }
}
