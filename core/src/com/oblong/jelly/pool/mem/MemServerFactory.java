// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.pool.mem;


import net.jcip.annotations.Immutable;

import com.oblong.jelly.PoolServer;
import com.oblong.jelly.PoolServerAddress;
import com.oblong.jelly.PoolServers;
import com.oblong.jelly.pool.PoolConnectionFactory;
import com.oblong.jelly.pool.Server;

/**
 *
 *
 * Created: Tue Jun 29 19:37:16 2010
 *
 * @author jao
 */
@Immutable
public final class MemServerFactory implements PoolServers.Factory {

    @Override public PoolServer get(PoolServerAddress address) {
        return new Server(factory, address);
    }

    public static boolean register() {
        return register("mem");
    }

    public static boolean register(String scheme) {
        return PoolServers.register(scheme, new MemServerFactory());
    }

    private static final PoolConnectionFactory factory =
        new MemPoolConnection.Factory();
}