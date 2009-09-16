/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package test.com.sun.max.vm.compiler.c1x.amd64;

import junit.framework.*;
import test.com.sun.max.vm.compiler.*;

import com.sun.max.asm.*;
import com.sun.max.platform.*;
import com.sun.max.vm.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.compiler.target.*;

/**
 * @author
 */
public class C1XTranslatorTestSetup extends CompilerTestSetup<TargetMethod> {

    public C1XTranslatorTestSetup(Test test) {
        super(test);
    }

    @Override
    public TargetMethod translate(ClassMethodActor classMethodActor) {
        return  javaPrototype().vmConfiguration().jitScheme().compile(classMethodActor);
    }

    private boolean isCompilable(MethodActor method) {
        return method instanceof ClassMethodActor && !method.isAbstract() && !method.isNative() && !method.isBuiltin() && !method.isUnsafeCast();
    }
    @Override
    protected VMConfiguration createVMConfiguration() {
        return VMConfigurations.createStandardJit(BuildLevel.DEBUG, Platform.host().constrainedByInstructionSet(InstructionSet.AMD64), new com.sun.max.vm.compiler.c1x.Package());
    }
}