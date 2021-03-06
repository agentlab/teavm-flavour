/*
 *  Copyright 2016 Alexey Andreev.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 *
 * @author Alexey Andreev
 */
public class TypeInference {
    private GenericTypeNavigator typeNavigator;
    private Map<TypeVar, InferenceVar> inferenceVars = new WeakHashMap<>();

    public TypeInference(GenericTypeNavigator typeNavigator) {
        this.typeNavigator = typeNavigator;
    }

    public boolean equalConstraint(GenericType a, GenericType b) {
        a = eliminateWildcard(a);
        b = eliminateWildcard(b);
        if (a instanceof GenericReference && b instanceof GenericReference) {
            InferenceVar x = var(((GenericReference) a).getVar());
            InferenceVar y = var(((GenericReference) b).getVar());
            return equalConstraintTwoVars(x, y);
        } else if (a instanceof GenericReference) {
            InferenceVar x = var(((GenericReference) a).getVar());
            return equalConstraintVarType(x, b);
        } else if (b instanceof GenericReference) {
            InferenceVar x = var(((GenericReference) b).getVar());
            return equalConstraintVarType(x, a);
        } else if (a instanceof GenericClass && b instanceof GenericClass) {
            GenericClass s = (GenericClass) a;
            GenericClass t = (GenericClass) b;
            if (!s.getName().equals(t.getName()) || s.getArguments().size() != t.getArguments().size()) {
                return false;
            }
            for (int i = 0; i < s.getArguments().size(); ++i) {
                if (!equalConstraint(s.getArguments().get(i), t.getArguments().get(i))) {
                    return false;
                }
            }
            return true;
        } else if (a instanceof GenericArray && b instanceof GenericArray) {
            GenericArray s = (GenericArray) a;
            GenericArray t = (GenericArray) b;
            if (s.getElementType() instanceof GenericType && t.getElementType() instanceof GenericType) {
                return equalConstraint((GenericType) s.getElementType(), (GenericType) t.getElementType());
            } else {
                return s.getElementType().equals(t.getElementType());
            }
        }
        return false;
    }

    private GenericType eliminateWildcard(GenericType type) {
        if (!(type instanceof GenericWildcard)) {
            return type;
        }
        GenericWildcard wildcard = (GenericWildcard) type;
        TypeVar var = new TypeVar();
        if (!wildcard.getLowerBound().isEmpty()) {
            var.withLowerBound(wildcard.getLowerBound().toArray(new GenericType[0]));
        }
        if (!wildcard.getUpperBound().isEmpty()) {
            var.withUpperBound(wildcard.getUpperBound().toArray(new GenericType[0]));
        }
        return new GenericReference(var);
    }

    private boolean equalConstraintTwoVars(InferenceVar x, InferenceVar y) {
        InferenceVar common = x.union(y);
        InferenceVar remaining = common == x ? y : x;
        common.upperDependencies.addAll(remaining.upperDependencies);
        common.lowerDependencies.addAll(remaining.lowerDependencies);
        if (common.boundType == null) {
            common.boundType = remaining.boundType;
            common.bounds.addAll(remaining.bounds);
            return true;
        } else if (remaining.boundType == null) {
            return true;
        } else if (common.boundType == BoundType.EXACT && remaining.boundType == BoundType.EXACT) {
            return equalConstraint(common.bounds.iterator().next(), remaining.bounds.iterator().next());
        } else {
            return false;
        }
    }

    private boolean equalConstraintVarType(InferenceVar x, GenericType t) {
        x = x.find();
        if (x.boundType == null) {
            x.boundType = BoundType.EXACT;
            x.bounds.add(t);
            for (InferenceVar dep : x.lowerDependencies) {
                if (!subtypeConstraint(new GenericReference(dep.anyTypeVar()), t)) {
                    return false;
                }
            }
            for (InferenceVar dep : x.upperDependencies) {
                if (!subtypeConstraint(t, new GenericReference(dep.anyTypeVar()))) {
                    return false;
                }
            }
            return true;
        } else if (x.boundType == BoundType.EXACT) {
            return equalConstraint(x.bounds.iterator().next(), t);
        } else if (x.boundType == BoundType.LOWER || x.boundType == BoundType.UPPER) {
            if (x.complexBound) {
                return false;
            }
            x.boundType = BoundType.EXACT;
            GenericType oldBound = x.bounds.iterator().next();
            x.bounds.clear();
            x.bounds.add(t);
            for (InferenceVar dep : x.lowerDependencies) {
                if (!subtypeConstraint(new GenericReference(dep.anyTypeVar()), t)) {
                    return false;
                }
            }
            for (InferenceVar dep : x.upperDependencies) {
                if (!subtypeConstraint(t, new GenericReference(dep.anyTypeVar()))) {
                    return false;
                }
            }
            return subtypeConstraint(oldBound, t);
        } else {
            return false;
        }
    }

    public boolean subtypeConstraint(GenericType a, GenericType b) {
        a = eliminateWildcard(a);
        b = eliminateWildcard(b);
        if (a instanceof GenericReference || b instanceof GenericReference) {
            if (a instanceof GenericReference) {
                InferenceVar x = var(((GenericReference) a).getVar());
                if (!addUpperBound(x, b)) {
                    return false;
                }
            }
            if (b instanceof GenericReference) {
                InferenceVar x = var(((GenericReference) b).getVar());
                if (!addLowerBound(x, a)) {
                    return false;
                }
            }
            return true;
        } else if (a instanceof GenericClass && b instanceof GenericClass) {
            return subtypeConstraintClasses((GenericClass) a, (GenericClass) b);
        } else if (a instanceof GenericArray) {
            if (b instanceof GenericArray) {
                GenericArray p = (GenericArray) a;
                GenericArray q = (GenericArray) b;
                if (p.getElementType() instanceof GenericType && q.getElementType() instanceof GenericType) {
                    return subtypeConstraint((GenericType) p.getElementType(), (GenericType) q.getElementType());
                } else {
                    return p.getElementType().equals(q.getElementType());
                }
            } else if (b instanceof GenericClass && ((GenericClass) b).getName().equals("java.lang.Object")) {
                return true;
            }
        }
        return false;
    }

    private boolean subtypeConstraintClasses(GenericClass s, GenericClass t) {
        if (t.getName().equals("java.lang.Object")) {
            return true;
        }
        List<GenericClass> path = typeNavigator.sublassPath(s, t.getName());
        if (path == null) {
            return false;
        }
        GenericClass pattern = path.get(path.size() - 1);
        for (int i = 0; i < pattern.getArguments().size(); ++i) {
            if (!equalConstraint(pattern.getArguments().get(i), t.getArguments().get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean addUpperBound(InferenceVar x, GenericType t) {
        x = x.find();

        if (t instanceof GenericReference) {
            InferenceVar y = var(((GenericReference) t).getVar());
            return addLowerDependency(y, x);
        }

        if (x.boundType == null) {
            x.boundType = BoundType.UPPER;
            x.bounds.add(t);
            for (InferenceVar dependency : x.lowerDependencies) {
                if (!subtypeConstraint(new GenericReference(dependency.anyTypeVar()), t)) {
                    return false;
                }
            }
            return true;
        } else if (x.boundType == BoundType.UPPER) {
            return equalConstraint(x.bounds.iterator().next(), t);
        } else if (x.boundType == BoundType.EXACT) {
            return subtypeConstraint(x.bounds.iterator().next(), t);
        } else if (x.boundType == BoundType.LOWER && !x.complexBound) {
            x.boundType = BoundType.EXACT;
            return subtypeConstraint(x.bounds.iterator().next(), t);
        }

        return false;
    }

    private boolean addLowerBound(InferenceVar x, GenericType t) {
        x = x.find();

        if (t instanceof GenericReference) {
            InferenceVar y = var(((GenericReference) t).getVar());
            return addLowerDependency(x, y);
        }

        if (x.visited) {
            x.recursive = true;
            return true;
        }
        x.visited = true;
        try {
            if (x.boundType == null) {
                x.boundType = BoundType.LOWER;
                if (t instanceof GenericClass) {
                    GenericClass cls = (GenericClass) t;
                    GenericType[] args = new GenericType[cls.getArguments().size()];
                    for (int i = 0; i < args.length; ++i) {
                        args[i] = new GenericReference(new TypeVar());
                    }
                    GenericClass bound = new GenericClass(cls.getName(), args);
                    for (int i = 0; i < args.length; ++i) {
                        if (!subtypeConstraint(cls.getArguments().get(i), args[i])) {
                            return false;
                        }
                    }
                    x.bounds.add(bound);
                } else if (t instanceof GenericArray) {
                    GenericArray array = (GenericArray) t;
                    if (array.getElementType() instanceof GenericType) {
                        GenericReference arg = new GenericReference(new TypeVar());
                        x.bounds.add(new GenericArray(arg));
                        if (!subtypeConstraint((GenericType) array.getElementType(), arg)) {
                            return false;
                        }
                    } else {
                        x.bounds.add(array);
                    }
                } else {
                    x.bounds.add(t);
                }
                for (InferenceVar dependency : x.upperDependencies) {
                    if (!subtypeConstraint(t, new GenericReference(dependency.anyTypeVar()))) {
                        return false;
                    }
                }
                return true;
            } else if (x.boundType == BoundType.LOWER) {
                if (!extendLowerBound(x, t)) {
                    return false;
                }
                for (InferenceVar dependency : x.upperDependencies) {
                    if (!subtypeConstraint(t, new GenericReference(dependency.anyTypeVar()))) {
                        return false;
                    }
                }
                return true;
            } else if (x.boundType == BoundType.EXACT) {
                return subtypeConstraint(t, x.bounds.iterator().next());
            } else if (x.boundType == BoundType.UPPER && !x.complexBound) {
                if (!equalConstraint(x.bounds.iterator().next(), t)) {
                    return false;
                }
                x.boundType = BoundType.EXACT;
                return true;
            }
            return false;
        } finally {
            x.visited = false;
        }
    }

    private boolean addLowerDependency(InferenceVar x, InferenceVar y) {
        if (x == y) {
            return true;
        }
        if (!x.lowerDependencies.add(y)) {
            return true;
        }
        y.upperDependencies.add(x);

        if (x.boundType != null) {
            switch (x.boundType) {
                case EXACT:
                case UPPER:
                    if (x.boundType == BoundType.LOWER && x.complexBound) {
                        return false;
                    }
                    for (GenericType type : x.bounds) {
                        if (!subtypeConstraint(new GenericReference(y.anyTypeVar()), type)) {
                            return false;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if (y.boundType != null) {
            switch (y.boundType) {
                case EXACT:
                case LOWER:
                    if (x.boundType == BoundType.UPPER && x.complexBound) {
                        return false;
                    }
                    for (GenericType type : y.bounds) {
                        if (!subtypeConstraint(type, new GenericReference(x.anyTypeVar()))) {
                            return false;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private boolean extendLowerBound(InferenceVar x, GenericType t) {
        if (t instanceof GenericClass) {
            extendLowerBoundClass(x, (GenericClass) t);
            return true;
        } else if (t instanceof GenericArray) {
            GenericArray array = (GenericArray) t;
            for (GenericType lowerBound : x.bounds.toArray(new GenericType[0])) {
                if (lowerBound instanceof GenericArray) {
                    GenericArray other = (GenericArray) lowerBound;
                    if (other.getElementType() instanceof GenericType
                            && array.getElementType() instanceof GenericType) {
                        GenericReference ref = new GenericReference(new TypeVar());
                        x.bounds.clear();
                        x.bounds.add(new GenericArray(ref));
                        return subtypeConstraint((GenericType) array.getElementType(), ref)
                                && subtypeConstraint((GenericType) other.getElementType(), ref);
                    } else {
                        if (other.getElementType().equals(array.getElementType())) {
                            return true;
                        } else {
                            x.bounds.clear();
                            x.bounds.add(new GenericClass("java.lang.Object"));
                            x.complexBound = true;
                            return true;
                        }
                    }
                } else {
                    x.bounds.clear();
                    x.bounds.add(new GenericClass("java.lang.Object"));
                    x.complexBound = true;
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean extendLowerBoundClass(InferenceVar x, GenericClass cls) {
        List<GenericType> newLowerBounds = new ArrayList<>();
        List<GenericClass> newClasses = new ArrayList<>();
        Set<String> newErasure = new HashSet<>();
        Map<String, GenericClass> erasureMap = new HashMap<>();
        for (GenericType lowerBound : x.bounds.toArray(new GenericType[0])) {
            if (lowerBound instanceof GenericClass) {
                GenericClass lowerBoundCls = (GenericClass) lowerBound;
                newErasure.addAll(typeNavigator.commonSupertypes(Collections.singleton(cls.getName()),
                        Collections.singleton(lowerBoundCls.getName())));
                erasureMap.put(lowerBoundCls.getName(), lowerBoundCls);
            } else if (lowerBound instanceof GenericArray) {
                x.complexBound = true;
                x.bounds.clear();
                x.bounds.add(new GenericClass("java.lang.Object"));
                return true;
            }
        }
        if (newErasure.size() > 1) {
            newErasure.remove("java.lang.Object");
        }
        if (newErasure.size() != 1 || erasureMap.size() != 1
                || !erasureMap.keySet().containsAll(newErasure)) {
            x.complexBound = true;
        }

        for (String erasure : newErasure) {
            GenericClass existing = erasureMap.get(erasure);
            if (existing == null) {
                existing = typeNavigator.getGenericClass(erasure);
                newClasses.add(existing);
                erasureMap.put(erasure, existing);
            }
            newLowerBounds.add(existing);
        }

        x.bounds.clear();
        x.bounds.addAll(newLowerBounds);

        for (String erasure : newErasure) {
            GenericClass existing = erasureMap.get(erasure);
            List<GenericClass> path = typeNavigator.sublassPath(cls, erasure);
            GenericClass newType = path.get(path.size() - 1);
            for (int i = 0; i < existing.getArguments().size(); ++i) {
                if (!subtypeConstraint(newType.getArguments().get(i), existing.getArguments().get(i))) {
                    return false;
                }
            }
        }

        for (GenericClass existing : erasureMap.values()) {
            if (newErasure.contains(existing)) {
                continue;
            }
            for (GenericClass newClass : newClasses) {
                List<GenericClass> path = typeNavigator.sublassPath(existing, newClass.getName());
                if (path == null) {
                    continue;
                }
                GenericClass oldClass = path.get(path.size() - 1);
                for (int i = 0; i < oldClass.getArguments().size(); ++i) {
                    if (!subtypeConstraint(oldClass.getArguments().get(i), newClass.getArguments().get(i))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private InferenceVar var(TypeVar typeVar) {
        return inferenceVars.computeIfAbsent(typeVar, InferenceVar::new).find();
    }

    public Substitutions getSubstitutions() {
        return substitutions;
    }

    private Substitutions substitutions = new Substitutions() {
        @Override
        public GenericType get(TypeVar var) {
            InferenceVar inferenceVar = var(var);

            if (inferenceVar.recursive) {
                return null;
            }
            if (inferenceVar.boundType == null) {
                return GenericWildcard.unbounded();
            }
            switch (inferenceVar.boundType) {
                case EXACT:
                    return inferenceVar.bounds.iterator().next();
                case UPPER: {
                    if (!inferenceVar.complexBound) {
                        return inferenceVar.bounds.iterator().next();
                    }
                    return GenericWildcard.upperBounded(new ArrayList<>(inferenceVar.bounds));
                }
                case LOWER: {
                    if (!inferenceVar.complexBound) {
                        return inferenceVar.bounds.iterator().next();
                    }
                    return GenericWildcard.upperBounded(new ArrayList<>(inferenceVar.bounds));
                }
            }

            return null;
        }
    };

    class InferenceVar {
        InferenceVar parent;
        int rank;
        Set<TypeVar> variables = new HashSet<>();
        Set<GenericType> bounds = new HashSet<>();
        Set<InferenceVar> lowerDependencies = new HashSet<>();
        Set<InferenceVar> upperDependencies = new HashSet<>();
        BoundType boundType;
        boolean complexBound;
        boolean visited;
        boolean recursive;

        InferenceVar(TypeVar var) {
            variables.add(var);
            if (!var.getUpperBound().isEmpty()) {
                if (var.getUpperBound().size() != 1
                        || !var.getUpperBound().contains(new GenericClass("java.lang.Object"))) {
                    for (GenericType bound : var.getUpperBound()) {
                        if (bound instanceof GenericReference) {
                            InferenceVar other = var(((GenericReference) bound).getVar());
                            upperDependencies.add(other);
                            other.lowerDependencies.add(this);
                        } else {
                            boundType = BoundType.UPPER;
                            bounds.add(bound);
                        }
                    }
                }
            } else if (!var.getLowerBound().isEmpty()) {
                for (GenericType bound : var.getLowerBound()) {
                    if (bound instanceof GenericReference) {
                        InferenceVar other = var(((GenericReference) bound).getVar());
                        lowerDependencies.add(other);
                        other.upperDependencies.add(this);
                    } else {
                        boundType = BoundType.LOWER;
                        bounds.add(bound);
                    }
                }
            }
            complexBound = bounds.size() > 1;
        }

        public TypeVar anyTypeVar() {
            return variables.iterator().next();
        }

        public InferenceVar find() {
            if (parent == null) {
                return this;
            }
            if (parent.parent == null) {
                return parent;
            }
            List<InferenceVar> path = new ArrayList<>();
            InferenceVar v = this;
            while (v.parent != null) {
                path.add(v);
                v = v.parent;
            }
            for (InferenceVar u : path) {
                u.parent = v;
            }
            return v;
        }

        public InferenceVar union(InferenceVar other) {
            return this.find().unionImpl(other.find());
        }

        private InferenceVar unionImpl(InferenceVar other) {
            if (this == other) {
                return this;
            }
            if (rank > other.rank) {
                other.parent = this;
                unionData(other);
                return this;
            } else if (rank < other.rank) {
                parent = other;
                other.unionData(this);
                return other;
            } else {
                other.parent = this;
                ++rank;
                unionData(other);
                return this;
            }
        }

        private void unionData(InferenceVar other) {
            variables.addAll(other.variables);
            recursive |= other.recursive;
        }
    }

    enum BoundType {
        UPPER,
        LOWER,
        EXACT
    }
}
