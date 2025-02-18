// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.pool.net;


import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import com.oblong.jelly.Hose;
import com.oblong.jelly.PoolServerAddress;
import com.oblong.jelly.PoolException;
import com.oblong.jelly.PoolOptions;
import com.oblong.jelly.PoolServer;
import com.oblong.jelly.Slaw;
import com.oblong.jelly.slaw.SlawFactory;

/**
 *
 * Created: Mon Jun 14 16:22:00 2010
 *
 * @author jao
 */
@ThreadSafe
public final class Server implements PoolServer {

    public Server(NetConnectionFactory cf, PoolServerAddress addr) {
        this(cf, addr, addr.toString(), "");
    }

    public Server(NetConnectionFactory cf,
                  PoolServerAddress addr,
                  String sn,
                  String st) {
        address = addr;
        connectionFactory = cf;
        name = sn == null ? addr.host() : sn;
        subtype = st == null ? "" : st;
        qname = qualifiedName(address, name, subtype, connectionFactory);
    }

    @Override public String qualifiedName() { return qname; }

    @Override public PoolServerAddress address() { return address; }

    @Override public String name() { return name; }

    @Override public String subtype() { return subtype; }

    @Override public void create(String name, PoolOptions opts)
        throws PoolException {
        final NetConnection connection = connectionFactory.get(this);
        final SlawFactory factory = connection.factory();
        Request.CREATE.sendAndClose(connection,
                                    factory.string(name),
                                    factory.string("mmap"),
                                    optSlaw(opts));
    }

    @Override public void dispose(String name) throws PoolException {
        final NetConnection connection = connectionFactory.get(this);
        final SlawFactory factory = connection.factory();
        Request.DISPOSE.sendAndClose(connection, factory.string(name));
    }

    @Override public Set<String> pools() throws PoolException {
        final NetConnection connection = connectionFactory.get(this);
        final Slaw list = Request.LIST.sendAndClose(connection);
        Set<String> result = new HashSet<String>(list.count());
        for (Slaw s : list.nth(1).emitList()) {
            if (s.isString()) result.add(s.emitString());
        }
        return result;
    }

    @Override public Hose participate(String name) throws PoolException {
        final NetConnection connection = connectionFactory.get(this);
        final SlawFactory factory = connection.factory();
        Request.PARTICIPATE.send(connection,
                                 factory.string(name),
                                 factory.protein(null, null, null));
        return new NetHose(connection, name);
    }

    @Override public Hose participate(String name, PoolOptions opts)
        throws PoolException {
        final NetConnection connection = connectionFactory.get(this);
        final SlawFactory factory = connection.factory();
        Request.PARTICIPATE_C.send(connection,
                                   factory.string(name),
                                   factory.string("mmap"),
                                   optSlaw(opts),
                                   factory.protein(null, null, null));
        return new NetHose(connection, name);
     }

    @Override public String toString() {
        return
            "<Server: " + name + " @ " + address + ", sub: " + subtype + ">";
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Server)) return false;
        final Server s = (Server)o;
        return qualifiedName().equals(s.qualifiedName());
    }

    @Override public int hashCode() {
        return qualifiedName().hashCode();
    }

    public static String qualifiedName(PoolServerAddress addr,
                                       String name,
                                       String subtype,
                                       NetConnectionFactory factory) {
        final String st = subtype == null || subtype.length() == 0 ?
            "" : String.format("_%s._sub.", subtype);
        return String.format("'%s'.%s%s.%s:%d",
                             name == null ? addr.host() : name,
                             st,
                             factory.serviceName(),
                             addr.host(),
                             addr.port());
    }

    private static final Slaw optSlaw(PoolOptions opts) {
        return opts == null ? NULL_OPTS : opts.toProtein();
    }

    private static final Slaw NULL_OPTS = new PoolOptions(null).toProtein();

    private final PoolServerAddress address;
    private final NetConnectionFactory connectionFactory;
    private final String name;
    private final String subtype;
    private volatile String qname;
}
