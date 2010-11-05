// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.pool.net;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.jcip.annotations.NotThreadSafe;

import com.oblong.jelly.Hose;
import com.oblong.jelly.InOutException;
import com.oblong.jelly.NoSuchProteinException;
import com.oblong.jelly.NumericIlk;
import com.oblong.jelly.Pool;
import com.oblong.jelly.PoolAddress;
import com.oblong.jelly.PoolException;
import com.oblong.jelly.Protein;
import com.oblong.jelly.ProteinMetadata;
import com.oblong.jelly.Slaw;
import com.oblong.jelly.pool.PoolProtein;
import com.oblong.jelly.slaw.SlawFactory;

@NotThreadSafe
final class NetHose implements Hose {

    @Override public int version() {
        return connection.version();
    }

    @Override public Slaw info() {
        try {
            final Slaw res = Request.INFO.send(connection, indexSlaw(-1));
            return res.nth(1).toProtein().ingests();
        } catch (Throwable e) {
            return factory.map();
        }
    }

    @Override public String name() {
        return name;
    }

    @Override public void setName(String n) {
        name = n == null ? poolAddress.toString() : n;
    }

    @Override public PoolAddress poolAddress() {
        return poolAddress;
    }

    @Override public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    @Override public void withdraw() {
        try {
            if (isConnected()) Request.WITHDRAW.sendAndClose(connection);
        } catch (PoolException e) {
            final Logger log = Logger.getLogger(Hose.class.getName());
            log.info("Error withdrawing Hose '" + name() + "':\n\t" + e);
        }
    }

    @Override public long index() {
        return index;
    }

    @Override public long newestIndex() throws PoolException {
        try {
            final Slaw res = Request.NEWEST_INDEX.send(connection).nth(0);
            return res.emitLong();
        } catch (NoSuchProteinException e) {
            return Protein.NO_INDEX;
        }
    }

    @Override public long oldestIndex() throws PoolException {
        try {
            final Slaw res = Request.OLDEST_INDEX.send(connection).nth(0);
            return res.emitLong();
        } catch (NoSuchProteinException e) {
            return Protein.NO_INDEX;
        }
    }

    @Override public void seekTo(long idx) {
        index = idx;
        dirtyIndex = true;
        if (isConnected()) connection.resetPolled();
    }

    @Override public void seekBy(long offset) {
        seekTo(index + offset);
    }

    @Override public void toLast() throws PoolException {
        seekTo(newestIndex());
    }

    @Override public void runOut() throws PoolException {
        seekTo(1 + newestIndex());
    }

    @Override public void rewind() throws PoolException {
        seekTo(oldestIndex());
    }

    @Override public Protein deposit(Protein p) throws PoolException {
        final Slaw res = Request.DEPOSIT.send(connection, p);
        final long index = res.nth(0).emitLong();
        final double stamp = res.nth(2).emitDouble();
        return new PoolProtein(p, index, stamp, this);
    }

    @Override public Protein current() throws PoolException {
        return nth(index);
    }

    @Override public Protein next(Slaw... descrips) throws PoolException {
        final Protein p = maybePolled(descrips);
        if (p != null) return p;
        if (descrips.length == 0) return next();
        final Slaw res = Request.PROBE_FWD.send(connection,
                                                indexSlaw(index),
                                                matcher(descrips));
        return new PoolProtein(res.nth(0).toProtein(),
                               cleanIndex(res.nth(2).emitLong() + 1) - 1,
                               res.nth(1).emitDouble(),
                               this);
    }

    @Override public Protein awaitNext(long t, TimeUnit unit)
        throws PoolException, TimeoutException {
        if (t == 0) return next();
        if (dirtyIndex)
            try {
                return next();
            } catch (NoSuchProteinException e) {
                cleanIndex(index);
            }
        return await(t, unit);
    }

    @Override public Protein awaitNext() throws PoolException {
        try {
            return awaitNext(-1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            assert false : "Timeout while waiting forever";
            return null;
        }
    }

    @Override public Protein previous(Slaw... descrips) throws PoolException {
        if (descrips.length == 0) return previous();
        final Slaw res = Request.PROBE_BACK.send(connection,
                                                 indexSlaw(index),
                                                 matcher(descrips));
        return new PoolProtein(res.nth(0).toProtein(),
                               cleanIndex(res.nth(2).emitLong()),
                               res.nth(1).emitDouble(),
                               this);
    }

    @Override public Protein nth(long idx) throws PoolException {
        final Slaw sidx = indexSlaw(idx);
        final Slaw res = Request.NTH_PROTEIN.send(connection, sidx);
        return new PoolProtein(res.nth(0).toProtein(),
                               sidx.emitLong(),
                               res.nth(1).emitDouble(),
                               this);
    }

    @Override public Protein nth(long idx, boolean d, boolean i, boolean dt)
        throws PoolException {
        return nth(idx, d, i, dt ? 0 : -1, -1);
    }

    @Override public Protein nth(long idx, boolean d, boolean i,
                                 long s, long l) throws PoolException {
        final Metadata md = firstFetch(subFetch(idx, idx + 1, d, i, s, l));
        final Protein p = md.protein(this);
        if (p == null) throw new NoSuchProteinException(0);
        return p;
    }

    @Override public List<Protein> range(long from, long to)
        throws PoolException {
        return range(from, to, true, true, true);
    }

    @Override public List<Protein> range(long f, long t,
                                         boolean d, boolean i, boolean dt)
        throws PoolException {
        return range(f, t, d, i, dt ? 0 : -1, -1);
    }

    @Override public List<Protein> range(long f, long t, boolean d, boolean i,
                                         long s, long l)
        throws PoolException {
        return Metadata.parseProteins(subFetch(f, t, d, i, s, l), this);
    }

    @Override public ProteinMetadata metadata(long idx) throws PoolException {
        return firstFetch(subFetch(idx, idx + 1, false, false, -1, -1));
    }

    @Override public List<ProteinMetadata> metadata(long from, long to)
        throws PoolException {
        return Metadata.parseMeta(subFetch(from, to, false, false, -1, -1));
    }

    @Override public boolean poll(Slaw... descrips) throws PoolException {
        if (checkPolled(connection.polled(), descrips)) return true;
        connection.resetPolled();
        final Slaw m = descrips.length == 0
            ? factory.nil() : factory.list(descrips);
        try {
            Request.FANCY_ADD_AWAITER.send(connection, indexSlaw(index), m);
        } catch (NoSuchProteinException e) {
            return false;
        }
        return true;
    }

    @Override public Protein peek() {
        return connection.polled();
    }

    @Override public Hose dup() throws PoolException {
        final Hose result = Pool.participate(poolAddress);
        result.setName(name);
        result.seekTo(index);
        return result;
    }

    @Override public Hose dupAndClose() throws PoolException {
        if (!isConnected()) return dup();
        final NetConnection c = connection;
        connection = null;
        return new NetHose(c, poolAddress, name, index);
    }

    NetHose(NetConnection con, String pn) throws PoolException {
        this(con, new PoolAddress(con.address(), pn), null, 0);
        cleanIndex(newestIndex());
    }

    NetHose(NetConnection conn, PoolAddress addr, String name, long idx) {
        connection = conn;
        factory = connection.factory();
        poolAddress = addr;
        setName(name);
        cleanIndex(idx);
        connection.setHose(this);
    }

    private Slaw subFetch(long from, long to,
                          boolean d, boolean i, long beg, long len)
        throws PoolException {
        if (from < 0) from = 0;
        if (from >= to) return factory.list();
        final List<Slaw> req = new ArrayList<Slaw>((int)(to - from));
        for (long k = from; k < to; ++k) {
            req.add(factory.map(factory.string("idx"), indexSlaw(k),
                                factory.string("des"), factory.bool(d),
                                factory.string("ing"), factory.bool(i),
                                factory.string("roff"), indexSlaw(beg),
                                factory.string("rbytes"), indexSlaw(len)));
        }
        return Request.SUB_FETCH.send(connection, factory.list(req)).nth(0);
    }

    private Metadata firstFetch(Slaw f) throws NoSuchProteinException {
        if (f.count() == 0) throw new NoSuchProteinException(0);
        final Metadata md = new Metadata(f.nth(0));
        if (md.retort() != 0) throw new NoSuchProteinException(md.retort());
        return md;
    }

    private Protein next() throws PoolException {
        final Protein p = maybePolled();
        if (p != null) return p;
        final Slaw res = Request.NEXT.send(connection, indexSlaw(index));
        return new PoolProtein(res.nth(0).toProtein(),
                               cleanIndex(res.nth(2).emitLong() + 1) - 1,
                               res.nth(1).emitDouble(),
                               this);
    }

    private boolean checkPolled(PoolProtein p, Slaw... descrips) {
        return !dirtyIndex
            && p != null
            && p.index() >= index
            && p.matches(descrips);
    }

    private Protein maybePolled(Slaw... descrips) {
        if (!isConnected()) return null;
        final PoolProtein p = connection.resetPolled();
        if (checkPolled(p)) {
            return new PoolProtein(p.bareProtein(),
                                   cleanIndex(p.index() + 1) - 1,
                                   p.timestamp(), this);
        }
        return null;
    }

    private Protein previous() throws PoolException {
        connection.resetPolled();
        final Slaw res = Request.PREV.send(connection, indexSlaw(index));
        return new PoolProtein(res.nth(0).toProtein(),
                               cleanIndex(res.nth(2).emitLong()),
                               res.nth(1).emitDouble(),
                               this);
    }

    private Protein await(long t, TimeUnit u)
        throws PoolException, TimeoutException {
        checkConnection();
        if (t > 0) connection.setTimeout(u.toMillis(t) + 100,
                                         TimeUnit.MILLISECONDS);
        try {
            final Protein p = maybePolled();
            if (p != null) return p;
            final Slaw res =
                Request.AWAIT_NEXT.send(connection, timeSlaw(t, u));
            if (res == null) throw new TimeoutException();
            return new PoolProtein(res.nth(1).toProtein(),
                                   cleanIndex(res.nth(3).emitLong() + 1) - 1,
                                   res.nth(2).emitDouble(),
                                   this);
        } finally {
            connection.setTimeout(0, u);
        }
    }

    private Slaw matcher(Slaw... descrips) {
        return factory.list(descrips);
    }

    private Slaw timeSlaw(long timeout, TimeUnit unit) {
        double poolTimeout =
            timeout < 0 ? WAIT_FOREVER : ((double)unit.toNanos(timeout))/1e9;
        if (version() < FIRST_NEW_WAIT_V) {
            if (poolTimeout == WAIT_FOREVER) poolTimeout = OLD_WAIT;
            else if (poolTimeout == NO_WAIT) poolTimeout = OLD_NO_WAIT;
        }
        return factory.number(NumericIlk.FLOAT64, poolTimeout);
    }

    private Slaw indexSlaw(long idx) {
        return factory.number(NumericIlk.INT64, Math.max(0, idx));
    }

    private long cleanIndex(long idx) {
        dirtyIndex = false;
        return index = idx;
    }

    private void checkConnection() throws InOutException {
        if (!isConnected()) throw new InOutException("Connection closed");
    }

    private static final double WAIT_FOREVER = -1;
    private static final double NO_WAIT = 0;
    private static final double OLD_WAIT = NO_WAIT;
    private static final double OLD_NO_WAIT = WAIT_FOREVER;
    private static final int FIRST_NEW_WAIT_V = 2;

    private NetConnection connection;
    private final SlawFactory factory;
    private final PoolAddress poolAddress;
    private String name;
    private volatile long index;
    private volatile boolean dirtyIndex;
}
