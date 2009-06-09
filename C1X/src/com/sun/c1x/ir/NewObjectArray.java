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

import com.sun.c1x.ci.CiType;
import com.sun.c1x.util.InstructionVisitor;
import com.sun.c1x.value.ValueStack;

/**
 * The <code>NewObjectArray</code> instruction represents an allocation of an object array.
 *
 * @author Ben L. Titzer
 */
public class NewObjectArray extends NewArray {

    final CiType _elementClass;

    /**
     * Constructs a new NewObjectArray instruction.
     * @param elementClass the class of elements in this array
     * @param length the instruction producing the length of the array
     * @param stateBefore the state before the allocation
     */
    public NewObjectArray(CiType elementClass, Instruction length, ValueStack stateBefore) {
        super(length, stateBefore);
        _elementClass = elementClass;
    }

    /**
     * Gets the type of the elements of the array.
     * @return the element type of the array
     */
    public CiType elementClass() {
        return _elementClass;
    }

    /**
     * Gets the exact type of this instruction.
     * @return the exact type of this instruction
     */
    public CiType exactType() {
        return _elementClass.arrayOf();
    }

    /**
     * Implements this instruction's half of the visitor pattern.
     * @param v the visitor to accept
     */
    public void accept(InstructionVisitor v) {
        v.visitNewObjectArray(this);
    }
}