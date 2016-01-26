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
package com.sun.max.vm.runtime.arm;

import com.sun.cri.ci.CiCalleeSaveLayout;
import com.sun.cri.ci.CiRegister;
import com.sun.max.unsafe.Address;
import com.sun.max.unsafe.Pointer;
import com.sun.max.unsafe.Word;
import com.sun.max.vm.Log;
import com.sun.max.vm.runtime.Trap;
import com.sun.max.vm.runtime.TrapFrameAccess;

import static com.oracle.max.asm.target.armv7.ARMV7.*;
import static com.sun.max.vm.MaxineVM.vm;
import static com.sun.max.vm.runtime.arm.ARMSafepointPoll.LATCH_REGISTER;

/* APN this will need to be rewritten
 ARM architecture traps are going to be different to X86 ...
The description below is for X86 and ARMV7 appears because of the use of a global find and replace when instantiating the ARM architecture. 


*/
/**
 * The trap frame on ARMV7 contains the {@linkplain com.sun.max.vm.runtime.Trap.Number trap number} and the values of the
 * processor's registers when a trap occurs. The trap frame is as follows:
 *
 * <pre>
 *   Base       Contents
 *
 *          :                                :
 *          |                                | Trapped frame
 *   -------+--------------------------------+----------
 *          | trapped PC                     | Trap frame
 *          +--------------------------------+     ---
 *          | flags register                 |      ^
 *          +--------------------------------+      |
 *          | trap number                    |      |
 *          +--------------------------------+      |
 *          |                                |    frame
 *          : XMM0 - XMM15  save area        :    size
 *          |                                |      |
 *          +--------------------------------+      |
 *          |                                |      |
 *          : GPR (rax - r15)  save area     :      |
 *    %sp   |                                |      v
 *   -------+--------------------------------+----------
 * </pre>
 *
 * Or, alternatively, the trap frame is described by the following C-like struct declaration:
 *
 * <pre>
 * trap_state {
 *     Word generalPurposeRegisters[16];
 *     Word xmmRegisters[16];
 *     Word trapNumber;
 *     Word flagsRegister;
 * }
 * </pre>
 *
 * The fault address (i.e. trapped PC) is stored in the return address slot, making the
 * trap frame appear as if the trapped method called the trap stub directly
 *
 * ARMV7
 * r14_xxx contains the return address.
 * r15_xxx contians the exception vector address
 * SPSR_xxx contains copy of the CPSR at exception.
 * NOTHING PUSHED ONTO STACK BY HARDWARE?
 *
 */
public final class ARMTrapFrameAccess extends TrapFrameAccess {

    public static final int TRAP_NUMBER_OFFSET;
    public static final int FLAGS_OFFSET;

    public static final CiCalleeSaveLayout CSL;
    static {
        CiRegister[] csaRegs = {r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, /*r13,*/ r14,
		s0, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15};
        //int size = 4 * 14;
	int size = 4*14 + 8 * 16;
        TRAP_NUMBER_OFFSET = size;
        size += 4;
        FLAGS_OFFSET = size;
        size += 4;
	/*struct sigcontext {
        unsigned long trap_no;
        unsigned long error_code;
        unsigned long oldmask;
        unsigned long arm_r0;
        unsigned long arm_r1;
        unsigned long arm_r2;
        unsigned long arm_r3;
        unsigned long arm_r4;
        unsigned long arm_r5;
        unsigned long arm_r6;
        unsigned long arm_r7;
        unsigned long arm_r8;
        unsigned long arm_r9;
        unsigned long arm_r10;
        unsigned long arm_fp;
        unsigned long arm_ip;
        unsigned long arm_sp;
        unsigned long arm_lr;
        unsigned long arm_pc;
        unsigned long arm_cpsr;
        unsigned long fault_address;
};
	*/
	
        CSL = new CiCalleeSaveLayout(0, size, 4, csaRegs);
    }

    /* APN
        I guess we need to provide registers here rather than pointers.
     */
    @Override
    public Pointer getPCPointer(Pointer trapFrame) {
	
	//Used to experimentally determine where the PC was saved.
	/*for(int i = -12; i < 32;i+=4) {
        com.sun.max.vm.Log.print(i);
	com.sun.max.vm.Log.print(" PC  ");
	com.sun.max.vm.Log.println(trapFrame.plus(vm().stubs.trapStub().frameSize()+i).readWord(0));
	}
	*/
	return trapFrame.plus(vm().stubs.trapStub().frameSize());

    }

    @Override
    public Pointer getSP(Pointer trapFrame) {
        return trapFrame.plus(vm().stubs.trapStub().frameSize() +  0x4);
    }

    @Override
    public Pointer getFP(Pointer trapFrame) {
        return trapFrame.readWord(CSL.offsetOf(r11)).asPointer();
    }

    @Override
    public Pointer getSafepointLatch(Pointer trapFrame) {
        Pointer csa = getCalleeSaveArea(trapFrame);
        int offset = CSL.offsetOf(LATCH_REGISTER);
        return csa.readWord(offset).asPointer();
    }

    @Override
    public void setSafepointLatch(Pointer trapFrame, Pointer value) {
        Pointer csa = getCalleeSaveArea(trapFrame);
        int offset = CSL.offsetOf(LATCH_REGISTER);
        csa.writeWord(offset, value);
    }

    @Override
    public Pointer getCalleeSaveArea(Pointer trapFrame) {
        return trapFrame.plus(CSL.frameOffsetToCSA);
    }

    @Override
    public int getTrapNumber(Pointer trapFrame) {
        return trapFrame.readWord(TRAP_NUMBER_OFFSET).asAddress().toInt();
    }

    @Override
    public void setTrapNumber(Pointer trapFrame, int trapNumber) {
        trapFrame.writeWord(TRAP_NUMBER_OFFSET, Address.fromInt(trapNumber));
    }

    @Override
    public void logTrapFrame(Pointer trapFrame) {
        final Pointer csa = getCalleeSaveArea(trapFrame);
        Log.println("Non-zero registers:");

        for (CiRegister reg : CSL.registers) {
            if (reg.isCpu()) {
                int offset = CSL.offsetOf(reg);
                final Word value = csa.readWord(offset);
                if (!value.isZero()) {
                    Log.print("  ");
                    Log.print(reg.name);
                    Log.print("=");
                    Log.println(value);
                }
            }
        }
        Log.print("  rip=");
        Log.println(getPC(trapFrame));
        Log.print("  rflags=");
        final Word flags = csa.readWord(FLAGS_OFFSET);
        Log.print(flags);
        Log.print(' ');
        logFlags(flags.asAddress().toInt());
        Log.println();
        if (false) {
            boolean seenNonZeroXMM = false;
            for (CiRegister reg : CSL.registers) {
                if (reg.isFpu()) {
                    int offset = CSL.offsetOf(reg);
                    final double value = csa.readDouble(offset);
                    if (value != 0) {
                        if (!seenNonZeroXMM) {
                            Log.println("Non-zero XMM registers:");
                            seenNonZeroXMM = true;
                        }
                        Log.print("  ");
                        Log.print(reg.name);
                        Log.print("=");
                        Log.print(value);
                        Log.print("  {bits: ");
                        Log.print(Address.fromLong(Double.doubleToRawLongBits(value)));
                        Log.println("}");
                    }
                }
            }
        }
        final int trapNumber = getTrapNumber(trapFrame);
        Log.print("Trap number: ");
        Log.print(trapNumber);
        Log.print(" == ");
        Log.println(Trap.Number.toExceptionName(trapNumber));
    }

    private static final String[] rflags = {
        "CF", // 0
        null, // 1
        "PF", // 2
        null, // 3
        "AF", // 4
        null, // 5
        "ZF", // 6
        "SF", // 7
        "TF", // 8
        "IF", // 9
        "DF", // 10
        "OF", // 11
        "IO", // 12
        "PL", // 13
        "NT", // 14
        null, // 15
        "RF", // 16
        "VM", // 17
        "AC", // 18
        "VIF", // 19
        "VIP", // 20
        "ID" // 21
    };

    private static void logFlags(int flags) {
        Log.print('{');
        boolean first = true;
        for (int i = rflags.length - 1; i >= 0; i--) {
            int mask = 1 << i;
            if ((flags & mask) != 0) {
                final String flag = rflags[i];
                if (flag != null) {
                    if (!first) {
                        Log.print(", ");
                    } else {
                        first = false;
                    }
                    Log.print(flag);
                }
            }
        }
        Log.print('}');
    }
}
