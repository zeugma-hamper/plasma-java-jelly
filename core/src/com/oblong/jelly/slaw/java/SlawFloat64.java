// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.slaw.java;

import net.jcip.annotations.Immutable;

import com.oblong.jelly.NumericIlk;

@Immutable
final class SlawFloat64 extends SlawNumber {
    SlawFloat64(double v) {
        value = v;
    }

    @Override public NumericIlk numericIlk() { return NumericIlk.FLOAT64; }
    @Override public double emitDouble() { return value; }

    @Override public float emitFloat() {
        return value > Float.MAX_VALUE ? Float.MAX_VALUE : (float)value;
    }

    @Override public long emitLong() { return (long)value; }

    private double value;
}