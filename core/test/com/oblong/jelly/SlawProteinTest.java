// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly;

import static org.junit.Assert.*;
import org.junit.Test;

import static com.oblong.jelly.Slaw.*;

/**
 * Unit Test for class SlawProtein
 *
 * Created: Mon May 31 15:12:32 2010
 *
 * @author jao
 */
public class SlawProteinTest {

    @Test public void empty() {
        Protein p =  protein(null, null, null);
        assertNull(p.ingests());
        assertNull(p.descrips());
        assertEquals(0, p.dataLength());
        assertEquals(protein(null, null, null), p);
    }

}