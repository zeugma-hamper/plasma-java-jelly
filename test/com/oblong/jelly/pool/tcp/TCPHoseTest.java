// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.pool.tcp;

import static org.junit.Assert.*;

import org.junit.AfterClass;

import com.oblong.jelly.HoseTestBase;
import com.oblong.jelly.PoolException;
import com.oblong.jelly.pool.mem.TCPMemProxy;

/**
 * Unit Test for hose operations on TCP pools, using a MemPool-base
 * TCPProxy as the target PoolServer.
 *
 * Created: Tue Jul  6 15:25:02 2010
 *
 * @author jao
 */
public class TCPHoseTest extends HoseTestBase {

    public TCPHoseTest() throws PoolException {
        super(proxy.tcpAddress());
    }

    @AfterClass public static void close() {
        proxy.exit();
        try { proxyThread.join(10); } catch (Exception e) {}
    }

    private static TCPMemProxy proxy;
    private static Thread proxyThread;

    static {
        try {
            proxy = new TCPMemProxy(60005);
            proxyThread = new Thread(proxy);
            proxyThread.start();
        } catch (Exception e) {
            fail("Initialization error: " + e);
        }
    }

    public static void main(String[] args) throws PoolException {
        TCPHoseTest test = new TCPHoseTest();
        test.openDefault();
        test.deposit();
        test.clean();
        close();
    }
}
