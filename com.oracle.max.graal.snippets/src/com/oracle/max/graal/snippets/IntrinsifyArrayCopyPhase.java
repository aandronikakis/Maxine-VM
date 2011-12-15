/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.max.graal.snippets;

import java.lang.reflect.*;

import com.oracle.max.graal.compiler.graphbuilder.*;
import com.oracle.max.graal.compiler.phases.*;
import com.oracle.max.graal.compiler.util.*;
import com.oracle.max.graal.graph.*;
import com.oracle.max.graal.nodes.*;
import com.oracle.max.graal.nodes.java.*;
import com.sun.cri.ri.*;

public class IntrinsifyArrayCopyPhase extends Phase {
    private final RiRuntime runtime;
    private RiResolvedMethod intArrayCopy;
    private RiResolvedMethod arrayCopy;
    private RiResolvedMethod charArrayCopy;
    private RiResolvedMethod longArrayCopy;


    public IntrinsifyArrayCopyPhase(RiRuntime runtime) {
        this.runtime = runtime;
        try {
            intArrayCopy = getArrayCopySnippet(runtime, int.class);
            charArrayCopy = getArrayCopySnippet(runtime, char.class);
            longArrayCopy = getArrayCopySnippet(runtime, long.class);
            arrayCopy = runtime.getRiMethod(System.class.getDeclaredMethod("arraycopy", Object.class, int.class, Object.class, int.class, int.class));
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static RiResolvedMethod getArrayCopySnippet(RiRuntime runtime, Class<?> componentClass) throws NoSuchMethodException {
        Class<?> arrayClass = Array.newInstance(componentClass, 0).getClass();
        return runtime.getRiMethod(ArrayCopySnippets.class.getDeclaredMethod("arraycopy", arrayClass, int.class, arrayClass, int.class, int.class));
    }

    @Override
    protected void run(StructuredGraph graph) {
        for (MethodCallTargetNode methodCallTarget : graph.getNodes(MethodCallTargetNode.class)) {
            RiResolvedMethod targetMethod = methodCallTarget.targetMethod();
            RiResolvedMethod snippetMethod = null;
            if (targetMethod == arrayCopy) {
                ValueNode src = methodCallTarget.arguments().get(0);
                ValueNode dest = methodCallTarget.arguments().get(2);
                if (src == null || dest == null) {
                    return;
                }
                RiResolvedType srcDeclaredType = src.declaredType();
                RiResolvedType destDeclaredType = dest.declaredType();
                if (srcDeclaredType != null
                                && srcDeclaredType.isArrayClass()
                                && destDeclaredType != null
                                && destDeclaredType.isArrayClass()
                                && srcDeclaredType.componentType() == destDeclaredType.componentType()) {
                    Class<?> componentType = srcDeclaredType.componentType().toJava();
                    if (componentType.equals(int.class)) {
                        snippetMethod = intArrayCopy;
                    } else if (componentType.equals(char.class)) {
                        snippetMethod = charArrayCopy;
                    } else if (componentType.equals(long.class)) {
                        snippetMethod = longArrayCopy;
                    }
                }
            }

            if (snippetMethod != null) {
                StructuredGraph snippetGraph = (StructuredGraph) snippetMethod.compilerStorage().get(Graph.class);
                if (snippetGraph == null) {
                    snippetGraph = new StructuredGraph();
                    new GraphBuilderPhase(runtime, snippetMethod).apply(snippetGraph);
                    snippetMethod.compilerStorage().put(Graph.class, snippetGraph);
                }
                InliningUtil.inline(methodCallTarget.invoke(), snippetGraph, false);
            }
        }
        new CanonicalizerPhase(null, runtime, null).apply(graph);
    }

}