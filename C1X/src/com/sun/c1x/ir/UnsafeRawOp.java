/*
 * Copyright (c) 2009 Sun Microsystems, Inc.  All rights reserved.
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
package com.sun.c1x.ir;

import com.sun.c1x.util.InstructionClosure;
import com.sun.c1x.value.BasicType;
import com.sun.c1x.value.ValueType;

/**
 * The <code>UnsafeRawOp</code> class is the base class of all unsafe raw operations.
 *
 * @author Ben L. Titzer
 */
public abstract class UnsafeRawOp extends UnsafeOp {

    Instruction _base;
    Instruction _index;
    int _log2Scale;

    /**
     * Creates a new UnsafeRawOp instruction.
     * @param basicType the basic type of the operation
     * @param addr the instruction generating the base address (a long)
     * @param isStore <code>true</code> if this operation is a store
     */
    public UnsafeRawOp(BasicType basicType, Instruction addr, boolean isStore) {
        super(basicType, isStore);
        assert addr == null || addr.type().base() == ValueType.LONG_TYPE;
        _base = addr;
    }

    /**
     * Creates a new UnsafeRawOp instruction.
     * @param basicType the basic type of the operation
     * @param addr the instruction generating the base address (a long)
     * @param index the instruction generating the index
     * @param log2scale the log base 2 of the scaling factor
     * @param isStore <code>true</code> if this operation is a store
     */
    public UnsafeRawOp(BasicType basicType, Instruction addr, Instruction index, int log2scale, boolean isStore) {
        this(basicType, addr, isStore);
        _base = addr;
        _index = index;
        _log2Scale = log2scale;
    }

    /**
     * Gets the instruction generating the base address for this operation.
     * @return the instruction generating the base
     */
    public Instruction base() {
        return _base;
    }

    /**
     * Gets the instruction generating the index for this operation.
     * @return the instruction generating the index
     */
    public Instruction index() {
        return _index;
    }

    /**
     * Checks whether this instruction has an index.
     * @return <code>true</code> if this instruction has an index
     */
    public boolean hasIndex() {
        return _index != null;
    }

    /**
     * Gets the log base 2 of the scaling factor for the index of this instruction.
     * @return the log base 2 of the scaling factor
     */
    public int log2Scale() {
        return _log2Scale;
    }

    /**
     * Sets the instruction that generates the base address for this instruction.
     * @param base the instruction generating the base address
     */
    public void setBase(Instruction base) {
        _base = base;
    }

    /**
     * Sets the instruction generating the base address for this instruction.
     * @param index the instruction generating the index
     */
    public void setIndex(Instruction index) {
        _index = index;
    }

    /**
     * Sets the scaling factor for the index of this instruction.
     * @param log2scale the log base 2 of the scaling factor for this instruction
     */
    public void setLog2Scale(int log2scale) {
        _log2Scale = log2scale;
    }

    /**
     * Iterates over the input values to this instruction.
     * @param closure the closure to apply
     */
    public void inputValuesDo(InstructionClosure closure) {
        super.inputValuesDo(closure);
        _base = closure.apply(_base);
        if (_index != null) {
            _index = closure.apply(_index);
        }
    }
}