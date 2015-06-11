/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
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
package test.com.sun.max.vm.jtrun;

import com.sun.max.annotate.HOSTED_ONLY;
import com.sun.max.vm.MaxineVM;
import com.sun.max.vm.VMIntOption;
import com.sun.max.vm.VMOptions;
import com.sun.max.vm.hosted.BootImageGenerator;
import com.sun.max.vm.run.java.JavaRunScheme;
import test.com.sun.max.vm.run.AbstractTestRunScheme;

import static com.sun.max.vm.VMOptions.register;

/**
 * This abstract run scheme is shared by all the concrete run schemes generated by the {@link JTGenerator}.
 * It behaves as the standard {@link JavaRunScheme} if a main class is specified on the command.
 * If no main class is specified, then the tests will be run and the VM will exit.
 */
public abstract class JTAbstractRunScheme extends AbstractTestRunScheme {

    @HOSTED_ONLY
    public JTAbstractRunScheme() {
        super("test");
    }

    protected static boolean nativeTests;
    protected static int testStart;
    protected static int testEnd;
    protected static int testCount;

    private static VMIntOption startOption = register(new VMIntOption("-XX:TesterStart=", -1,
                    "The number of the first test to run."), MaxineVM.Phase.STARTING);
    private static VMIntOption endOption  = register(new VMIntOption("-XX:TesterEnd=", -1,
                    "The number of the last test to run. Specify 0 to run exactly one test."), MaxineVM.Phase.STARTING);
    private boolean classesRegistered;

    @HOSTED_ONLY
    private void registerClasses() {
        if (!classesRegistered) {
            classesRegistered = true;
            Class[] list = getClassList();
            for (Class<?> testClass : list) {
                addClassToImage(testClass);
            }
            testCount = list.length;
        }
    }

    protected abstract void runTests();

    @Override
    public void initialize(MaxineVM.Phase phase) {
        super.initialize(phase);
        if (phase == MaxineVM.Phase.STARTING) {
            noTests = VMOptions.parseMain(false);
            if (!noTests) {
                testStart = startOption.getValue();
                if (testStart < 0) {
                    testStart = 0;
                }
                testStart = -1; // APN HACK FOR NOW
                testEnd = endOption.getValue();
                if (testEnd < testStart || testEnd > testCount) {
                    testEnd = testCount;
                } else if (testEnd == testStart) {
                    testEnd = testStart + 1;
                }
                if (nativeTests) {
                    System.loadLibrary("javatest");
                }
                runTests();
            }
        }
        JTUtil.verbose = 3;
        if (MaxineVM.isHosted()) {
            registerClasses();
            nativeTests = BootImageGenerator.nativeTests;
            super.initialize(phase);
        }
    }

    @HOSTED_ONLY
    public abstract Class[] getClassList();
}
