// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly;

import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import static com.oblong.jelly.Slaw.*;
import static com.oblong.jelly.SlawTests.*;

/**
 * Unit Test for class Slaw: conses.
 *
 * Created: Wed May 12 04:23:10 2010
 *
 * @author jao
 */
public class SlawConsTest {

    @Test public void conses() {
        testCons(cons(vector(int8(2), float64(-23421341.234)),
                      string("foo")));
        testCons(cons(array(SlawIlk.VECTOR_ARRAY, NumericIlk.INT32, 2),
                      cons(int8(2), map())));
        testCons(cons(int16(0), int16(0)));
    }

    private void testCons(Slaw c) {
        assertEquals(c, c);
        assertEquals(2, c.count());
        assertEquals(c, cons(c.car(), c.cdr()));
        assertNull(c.numericIlk());
        testAtomicEmissions(c);
        testPairiness(c);
        testListness(c);
        Map<Slaw,Slaw> m = c.emitMap();
        assertEquals(c.cdr(), m.get(c.car()));
        assertEquals(c.cdr(), c.find(c.car()));
    }
}
