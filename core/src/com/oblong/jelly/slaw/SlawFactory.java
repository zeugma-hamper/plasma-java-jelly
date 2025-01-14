// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.slaw;

import com.oblong.jelly.NumericIlk;
import com.oblong.jelly.Protein;
import com.oblong.jelly.Slaw;
import com.oblong.jelly.SlawIlk;
import com.oblong.jelly.schema.HasToSlaw;
import com.oblong.jelly.slaw.java.SlawList;
import com.oblong.jelly.slaw.java.SlawMap;
import com.oblong.jelly.slaw.java.SlawString;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created: Mon Apr 26 01:28:28 2010
 *
 * @author jao
 */
public interface SlawFactory {

    Slaw nil();
    Slaw bool(boolean v);
	SlawString string(String s);

    Slaw number(NumericIlk ilk, long n);
    Slaw number(NumericIlk ilk, double n);
    Slaw number(BigInteger n);
    Slaw complex(Slaw re, Slaw im);

    Slaw vector(Slaw x, Slaw y);
    Slaw vector(Slaw x, Slaw y, Slaw z);
    Slaw vector(Slaw x, Slaw y, Slaw z, Slaw w);
    Slaw vector(Slaw[] cmps);

    Slaw multivector(Slaw... cmps);

    Slaw array(byte[] e, boolean s);
    Slaw array(Slaw[] sx);
    Slaw array(Slaw s, Slaw... sx);
    Slaw array(SlawIlk ilk, NumericIlk ni, int dimension);

    Slaw cons(Slaw car, Slaw cdr);

    Slaw list(List<? extends Slaw> s);
    SlawList list(Slaw... s);
    SlawList list(Collection<? extends HasToSlaw> collection);

    SlawMap map(Map<Slaw,Slaw> m);
    Slaw map(Slaw... kvs);
    Slaw map(List<Slaw> s);

    Protein protein(Slaw ingests, Slaw descrips, byte[] data);
}
