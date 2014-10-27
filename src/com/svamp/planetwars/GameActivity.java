package com.svamp.planetwars;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.svamp.planetwars.network.DataPacketListener;
import com.svamp.planetwars.network.GameClient;
import com.svamp.planetwars.network.GameEvent;
import com.svamp.planetwars.network.GameHost;
import com.svamp.planetwars.network.PackageHeader;
import com.svamp.planetwars.network.Player;
import com.svamp.planetwars.opengl.TextureTool;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GameActivity extends Activity implements DataPacketListener,View.OnClickListener {
    private ViewFlipper flipper;
    private GameHost host;
    private GameClient client;
    private ArrayAdapter<Player> adapter;
    private Handler refreshHandler;
    private StarView starView;

    private static final String TAG = GameActivity.class.getCanonicalName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize the TextureTool
        TextureTool.getInstance().initialize(getResources());
        ShaderTool.init(getResources());

        setContentView(R.layout.game);
        Button startGame = (Button) findViewById(R.id.start_game_button);
        startGame.setOnClickListener(this);
        flipper = (ViewFlipper) findViewById(R.id.vfShow);

        //Init listView (user list)
        ListView userList = (ListView) findViewById(R.id.playerList);
        adapter = new ArrayAdapter<Player>(this,R.layout.user_list_item,R.id.user_list_item_name);
        userList.setAdapter(adapter);
        //Check if we are host or client...
        Bundle b = getIntent().getExtras();
        if(b.getBoolean("is_host")) {
            //Make host client..
            host = new GameHost(b.getInt("port"),4);
            //Start client..
            try {
                host.start();
            } catch (IOException e) {
                Log.e(TAG,"Error occurred while starting host.",e);
            }
        }
        //Show the chosen host IP...
        String hostIpString = getResources().getString(R.string.host_ip_text);
        TextView ipText = (TextView) findViewById(R.id.host_ip);
        ipText.setText(hostIpString+" "+b.getString("ip_address")+":"+b.getInt("port"));
        //Connect to host.
        connectClient();

        //We are connected! Periodically update user list..
        refreshHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ( msg.what == 1337 ) {
                            refreshUserList();
                        }
                        if (msg.what == 60 ) {
                            startGame();
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Destroyed. Shutting down comm");
        shutDownNetworking();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(starView!=null) starView.onPause();
        Log.d(TAG,"Paused. Shutting down comm");
        //shutDownNetworking();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(starView!=null) starView.onResume();
        //connectClient();
    }

    private void shutDownNetworking() {
        if(client !=null) {
            GameEvent event = new GameEvent(PackageHeader.DISCONNECTED, client.getPlayer());
            client.sendData(event.toByteArray());
            client.stop(); //Kill all communication
        }
        if(host!=null) {
            host.stop();
        }
    }

    private void connectClient() {
        new ClientConnect(this).execute(getIntent().getExtras());
    }

    private void connectionComplete() {
        client.registerListener(this);
        refreshUserList();
    }

    private void refreshUserList() {
        Log.d(TAG,"Refreshing user list..");
        adapter.clear();
        adapter.add(client.getPlayer()); //Add this player.
        for(Player p : client.getPeers())
            adapter.add(p);
        adapter.sort(client.getPlayer()); //Player class is comparator for itself.
        Log.d(TAG, "Refreshed.");
    }

    private void startGame() {
        client.unregisterListener(this);
        flipper.showNext();
        starView = (StarView) findViewById(R.id.starView);
        starView.initView(client); //Initialize starView with the client.
    }

    @Override
    public void receive(GameEvent packet) {
        Message m = new Message();
        switch (packet.getHeader()) {
            case SUBMITTED_PLAYER_DATA: case DISCONNECTED: //Send message to update UI.
                m.what=1337;
                refreshHandler.dispatchMessage(m);
                break;
            case GAME_START:
                m.what=60;
                refreshHandler.dispatchMessage(m);
        }
    }

    @Override
    public void onClick(View v) {
        GameEvent event = new GameEvent(PackageHeader.REQUEST_GAME_START,client.getPlayer());
        client.sendData(event.toByteArray());
    }

    /**
     * AsyncTask showing a progressdialog as it tries to connect to the host specified in the provided Bundle,
     * showing an error dialog on failure, with retry button and a return button transferring the user to the
     * LocalNetwork activity. On completion, the client field is set with a GameCommunicator connected to host.
     */
    private class ClientConnect extends AsyncTask<Bundle,Void,String> implements DialogInterface.OnClickListener,DataPacketListener {
        private final GameActivity gameActivity;
        private ProgressDialog dialog;
        private PackageHeader connectionResponse;

        private ClientConnect(GameActivity c) {
            this.gameActivity =c;
        }

        @Override
        protected void onPreExecute() {
            //Make & show process dialog while attempting to connect..
            dialog = new ProgressDialog(gameActivity);
            dialog.setMessage(getResources().getString(R.string.connecting_to_host_string));
            dialog.show();
        }

        @Override
        protected String doInBackground(Bundle... params) {
            Bundle b = params[0];
            //Make address to host.
            InetSocketAddress address = new InetSocketAddress(b.getString("ip_address"),b.getInt("port"));
            try {
                //Connect. Random port.
                client = new GameClient(GameHost.getFreePort(),address);
                client.registerListener(this);
                client.start(); //Start listening on port.
            } catch (IOException e) { //Thrown on unreachable host.
                return getResources().getString(R.string.conn_failed_unreachable_string)+address.getHostName();
            }
            //Initiate connection attempt:
            GameEvent event = new GameEvent(PackageHeader.REQUEST_CONNECTION, client.getPlayer());
            client.sendData(event.toByteArray());
            while(connectionResponse==null) { //Wait for connectionResponse.
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
            }
            client.unregisterListener(this); //We don't need data packets anymore.
            switch (connectionResponse) {
                case CONNECTION_REFUSED:
                    return getResources().getString(R.string.conn_failed_string)+address.getHostName();
                case CONNECTION_REFUSED_SERVER_FULL:
                    return getResources().getString(R.string.conn_failed_full_string)+address.getHostName();
                default:
                    return null; //Any package not "refused" is a good package.
            }
        }

        @Override
        protected void onPostExecute(String error) {
            if(dialog.isShowing())
                dialog.dismiss();
            if(error!=null) {
                Dialog d = new AlertDialog.Builder(gameActivity)
                        .setMessage(error)
                        .setPositiveButton(R.string.retry_button, this)
                        .setNeutralButton(R.string.return_button, this).create();
                d.show();
            } else { //No error to show.
                connectionComplete();
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which==Dialog.BUTTON_POSITIVE) {
                connectClient();
            }
            else if(which==Dialog.BUTTON_NEUTRAL) {
                Intent i = new Intent(gameActivity,LocalNetworkActivity.class);
                startActivity(i);
            }
        }
        @Override
        public void receive(GameEvent packet) {
            connectionResponse = packet.getHeader();
        }
    }
}
