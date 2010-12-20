// Copyright (c) 2010 Oblong Industries

package com.oblong.android.ponder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ListActivity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;

import com.oblong.jelly.PoolServer;
import com.oblong.jelly.PoolServers;
import com.oblong.jelly.pool.PoolServerCache;
import com.oblong.jelly.pool.net.TCPServerFactory;

/**
 *
 * Created: Mon Nov 29 16:38:43 2010
 *
 * @author jao
 */
final class ServerTable {

    ServerTable(ListActivity la, WifiManager wifi) {
        localInfos = new HashSet<ServerInfo>();
        servers = new PoolServerCache();
        adapter = new ServerListAdapter(la);
        la.setListAdapter(adapter);
        wifiMngr = wifi;
        mcLock = setupMulticastLock();
        setupListener();
    }

    void activate() {
        mcLock.acquire();
        rescan();
    }

    void deactivate() {
        mcLock.release();
    }

    void rescan() {
        final Thread th = new Thread (new Runnable () {
                @Override public void run() {
                    for (PoolServer s : PoolServers.remoteServers())
                        if (!servers.contains(s)) notifyNewServer(s);
                }
            });
        th.start();
    }

    void reset() {
        for (ServerInfo i : adapter) i.clearPools();
        adapter.notifyDataSetChanged();
        new Thread (new Runnable () {
                @Override public void run() {
                    TCPServerFactory.reset();
                    for (PoolServer s : PoolServers.remoteServers()) {
                        notifyNewServer(s);
                    }
                    for (ServerInfo i : adapter) updatePoolNumber(i);
                    setupListener();
                }
            }).start();
    }

    void deleteUnreachable() {
        for (ServerInfo i : adapter) if (i.connectionError()) delServer(i);
    }

    void registerServer(ServerInfo info) {
        int k = 1;
        info.clearPools();
        while (localInfos.contains(info)) info.nextName(k++);
        localInfos.add(info);
        adapter.add(info);
        adapter.notifyDataSetChanged();
        checkInfo(info);
    }

    void refreshServer(int position) {
        final ServerInfo info = adapter.getItem(position);
        if (info != null) {
            info.clearPools();
            adapter.notifyDataSetChanged();
            updatePoolNumber(info);
        }
    }

    void delServer(int position) {
        delServer(adapter.getItem(position));
    }

    void delServer(ServerInfo info) {
        if (info != null) {
            localInfos.remove(info);
            servers.remove(info.server());
            adapter.remove(info);
            adapter.notifyDataSetChanged();
        }
    }

    ServerInfo getItem(int position) {
        return adapter.getItem(position);
    }

    private MulticastLock setupMulticastLock () {
        final int address = wifiMngr.getDhcpInfo().ipAddress;
        System.setProperty("net.mdns.interface",
                           Formatter.formatIpAddress(address));
        MulticastLock lk = wifiMngr.createMulticastLock("_ponder-lock");
        lk.setReferenceCounted(false);
        lk.acquire();
        return lk;
    }

    private static boolean isGeneric(PoolServer s) {
        return s.subtype().length() == 0;
    }

    private void updatePoolNumber(ServerInfo info) {
        info.clearPools();
        info.updatePoolNumber(handler, UPD_MSG);
    }

    private void sendMessage(int msg, Object arg) {
        handler.sendMessage(Message.obtain(handler, msg, arg));
    }

    private void notifyNewServer(PoolServer server) {
        sendMessage(ADD_MSG, server);
    }

    private void notifyGoneServer(PoolServer server) {
        sendMessage(DEL_MSG, server);
    }

    private void setupListener() {
        PoolServers.addRemoteListener(new PoolServers.Listener() {
                public void serverAdded(PoolServer s) {
                    notifyNewServer(s);
                }
                public void serverRemoved(PoolServer s) {
                    notifyGoneServer(s);
                }
            });
    }

    private void checkInfo(ServerInfo info) {
        if (info != null) {
            info.clearPools();
            adapter.notifyDataSetChanged();
            info.updatePoolNumber(handler, CHK_MSG);
            if (!isGeneric(info.server()) && !localInfos.contains(info)) {
                final PoolServer gs = servers.get(info.server().name(), "");
                final ServerInfo gi = adapter.getItem(gs);
                if (gi != null) adapter.remove(gi);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void addServer(PoolServer server) {
        if (!servers.contains(server)) {
            final boolean c =
                servers.contains(server.address(), server.name(), null);
            servers.add(server);
            if (!(isGeneric(server) && c)) {
                final ServerInfo info = new ServerInfo(server);
                adapter.add(info);
                checkInfo(info);
            }
        }
    }

    private void delServer(PoolServer server) {
        checkInfo(adapter.getItem(server));
    }

    private void onCheckServer(ServerInfo info) {
        if (info.connectionError() && !localInfos.contains(info)) {
            for (PoolServer s :
                     servers.get(null, info.server().name(), null)) {
                ServerInfo i = adapter.getItem(s);
                if (i != null && i.connectionError()) {
                    servers.remove(s);
                    adapter.remove(i);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateServer(ServerInfo info) {
        adapter.notifyDataSetChanged();
    }

    private static final int ADD_MSG = 0;
    private static final int DEL_MSG = 2;
    private static final int UPD_MSG = 4;
    private static final int CHK_MSG = 6;

    private final Handler handler = new Handler () {
        public void handleMessage(Message m) {
            switch (m.what) {
            case ADD_MSG: addServer((PoolServer)m.obj); break;
            case DEL_MSG: delServer((PoolServer)m.obj); break;
            case UPD_MSG: updateServer((ServerInfo)m.obj); break;
            case CHK_MSG: onCheckServer((ServerInfo)m.obj); break;
            default:
                Ponder.logger().warning(
                    "Unexpected message to ServerTable: " + m.what);
                break;
            }
        }
    };

    private final Set<ServerInfo> localInfos;
    private final PoolServerCache servers;
    private final ServerListAdapter adapter;
    private final MulticastLock mcLock;
    private final WifiManager wifiMngr;
}
