package com.oblong.jelly.pool;

import net.jcip.annotations.Immutable;

import com.oblong.jelly.PoolException;

@Immutable
public class PoolNoSuchProteinException extends PoolException {

    public PoolNoSuchProteinException(long sc) {
        super(Code.NO_SUCH_PROTEIN, sc, "Server code was " + sc);
    }

    private static final long serialVersionUID = -7648502586498969925L;
}