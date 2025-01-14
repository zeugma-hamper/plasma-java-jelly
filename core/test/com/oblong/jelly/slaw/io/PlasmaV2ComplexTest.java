// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.slaw.io;

import org.junit.Test;

import com.oblong.jelly.*;
import com.oblong.jelly.slaw.*;
import com.oblong.jelly.slaw.io.BinaryExternalizer;
import com.oblong.jelly.slaw.io.BinaryInternalizer;

import static com.oblong.jelly.Slaw.*;

/**
 * Unit test for class Externalizer: complex numbers.
 *
 * Created: Tue May 18 15:43:23 2010
 *
 * @author jao
 */
public class PlasmaV2ComplexTest extends SerializationTestBase {

    public PlasmaV2ComplexTest() {
        super(new BinaryExternalizer(), new BinaryInternalizer());
    }

    @Test public void weeComplexes() {
        short[][] bs = {{0x82, 0x00, 0x40, 0x00, 0x00, 0x00, 0x02, 0x2a},
                        {0x96, 0x00, 0xc0, 0x00, 0x12, 0x34, 0x56, 0x78}};
        Slaw[] sx = {complex(int8(2), int8(42)),
                     complex(unt16(0x1234), unt16(0x5678))};
        check(sx, bs);
    }

    @Test public void complex32() {
        short[][] bs = {{0x8a, 0x01, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x2a},
                        {0x9a, 0x01, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08},
                        {0xaa, 0x01, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x40, 0x46, 0x66, 0x66, 0xc1, 0x94, 0x7a, 0xe1}};
        Slaw[] sx = {complex(int32(2), int32(42)),
                     complex(unt32(0x01020304), unt32(0x05060708)),
                     complex(float32(3.1f), float32(-18.56f))};
        check(sx, bs);
    }

    @Test public void complex64() {
        short[][] bs = {{0x8e, 0x03, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                         0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10},
                        {0x9e, 0x03, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01},
                        {0xae, 0x03, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x40, 0x08, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcd,
                         0xc0, 0x32, 0x8f, 0x5c, 0x28, 0xf5, 0xc2, 0x8f}};
        Slaw[] sx = {complex(int64(0x0102030405060708L),
                             int64(0x090a0b0c0d0e0f10L)),
                     complex(unt64(0), int64(1)),
                     complex(float64(3.1), float64(-18.56))};
        check(sx, bs);
    }
}
