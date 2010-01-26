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
package com.sun.max.vm.cps.dir;

import com.sun.max.vm.cps.dir.transform.*;

/**
 * Explicit exception throwing.
 *
 * @author Hiroshi Yamauchi
 * @author Bernd Mathiske
 */
public class DirThrow extends DirInstruction {

    final DirValue throwable;

    public DirThrow(DirValue throwable) {
        this.throwable = throwable;
    }

    public DirValue throwable() {
        return throwable;
    }

    @Override
    public boolean isEquivalentTo(DirInstruction other, DirBlockEquivalence dirBlockEquivalence) {
        if (other instanceof DirThrow) {
            final DirThrow dirThrow = (DirThrow) other;
            return throwable.equals(dirThrow.throwable);
        }
        return false;
    }

    @Override
    public int hashCodeForBlock() {
        return super.hashCodeForBlock() ^ throwable.hashCodeForBlock();
    }

    @Override
    public String toString() {
        return "throw " + throwable;
    }

    @Override
    public void acceptVisitor(DirVisitor visitor) {
        visitor.visitThrow(this);
    }
}