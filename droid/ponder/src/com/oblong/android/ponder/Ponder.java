// Copyright (c) 2010 Oblong Industries

package com.oblong.android.ponder;

import android.app.ListActivity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.oblong.jelly.PoolServer;

/**
 *
 * Created: Tue Nov 23 15:57:08 2010
 *
 * @author jao
 */
public class Ponder extends ListActivity {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serverDialog = new ServerDialog(this);

        final WifiManager wifi =
            (WifiManager)getSystemService(Context.WIFI_SERVICE);
        table = new ServerTable(this, wifi);

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        registerForContextMenu(lv);
    }

    @Override public void onResume() {
        super.onResume();
        table.activate();
    }

    @Override public void onStop() {
        super.onStop();
        table.deactivate();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.server_list, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.add_server:
            serverDialog.show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onCreateContextMenu(ContextMenu menu,
                                              View v,
                                              ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.server_context, menu);
    }

    @Override public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info =
            (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.delete_server:
            table.delServer(info.position);
            return true;
        case R.id.refresh_server:
            table.refreshServer(info.position);
            return true;
        case R.id.show_server:
            // deleteNote(info.id);
            return true;
        case R.id.close_menu:
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    void registerServer(PoolServer server, String name) {
        table.registerServer(new RowInfo(server, name));
    }

    private ServerTable table;
    private ServerDialog serverDialog;
}
