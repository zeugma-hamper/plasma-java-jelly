// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.pool.tcp;

import com.oblong.jelly.HoseTestBase;
import com.oblong.jelly.PoolException;

/**
 * Tests for Hose operations against an external TCP pool server.
 *
 * Created: Wed Jul 21 12:29:22 2010
 *
 * @author jao
 */
public class ExternalTCPHoseTest extends HoseTestBase {

    public ExternalTCPHoseTest() throws PoolException {
        super(externalServer());
    }

}
