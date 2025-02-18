// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.slaw.io;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

import com.oblong.util.ExceptionHandler;
import org.junit.Test;

import com.oblong.jelly.*;
import static com.oblong.jelly.Slaw.*;
import com.oblong.jelly.slaw.*;

/**
 * Unit test for PlasmaV2 serialization: proteins.
 *
 * Created: Mon May 31 14:29:13 2010
 *
 * @author jao
 */
public class PlasmaV2ProteinsTest extends SerializationTestBase {

    public PlasmaV2ProteinsTest() {
        super(new BinaryExternalizer(), new BinaryInternalizer());
    }

    @Test public void empty() {
        final byte[] bs = {0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02,
                           0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        check("Empty protein", protein(null, null, null), bs);

        final byte[] ws = {0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02,
                           0x05, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        check("Wee empty protein", protein(null, null, makeData(5)), ws);

        final byte[] bd = {0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06,
                           0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x19,
                           0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                           0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10,
                           0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
                           0x19, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        check("Data protein", protein(null, null, makeData(25)), bd);
    }

    @Test public void littleEndianIntern() {
        final short[] p0 = {0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10,
                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        checkIntern(protein(null, null, null), p0);

        final short[] p1 = {0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10,
                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40,
                            0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x43,
                            0x01, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88,
                            0x02, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88,
                            0x03, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88};
        checkIntern(protein(list(int32(1), int32(2), int32(3)), null, null),
                    p1);

        final short[] p2 = {0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10,
                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20,
                            0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x52,
                            0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x62,
                            0x61, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x32,
                            0x08, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88,
                            0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x62,
                            0x62, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x32,
                            0x05, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88};
        checkIntern(protein(null,
                            map(string("a"), int32(8), string("b"), int32(5)),
                            null),
                    p2);

        final short[] p3 = {0x0d, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10,
                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x60,
                            0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x43,
                            0x01, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88,
                            0x02, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88,
                            0x03, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88,
                            0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x52,
                            0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x62,
                            0x61, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x32,
                            0x01, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88,
                            0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x62,
                            0x62, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x32,
                            0x02, 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x88};
        checkIntern(protein(list(int32(1), int32(2), int32(3)),
                            map(string("a"), int32(1), string("b"), int32(2)),
                            null),
                    p3);

        final short[] p4 = {0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10,
                            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x00, 0x06};
        checkIntern(protein(null, null, makeData(6)), p4);

    }
    
    @Test public void circular() {
        final Protein[] ps = {
                protein(string("eightelt"), null, null)
        };
        for (Protein p : ps) circularCheck("", p);
    }

    private void checkIntern(Protein s, short[] b) {
        circularCheck("Part of intern checks", s);
        try {
            final InputStream is = new ByteArrayInputStream(asBytes(b));
            final Protein s2 = internalizer.internProtein(is, factory);
            assertEquals(s, s2);
        } catch (Exception e) {
	        ExceptionHandler.handleException(e);
            fail(e.toString());
        }
    }

    private static byte[] makeData(int len) {
        byte[] d = new byte[len];
        for (int i = 0; i < len; ++i) d[i] = (byte)(i+1);
        return d;
    }
}
