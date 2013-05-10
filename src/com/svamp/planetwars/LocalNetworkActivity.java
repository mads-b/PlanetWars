package com.svamp.planetwars;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.svamp.planetwars.network.GameHost;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalNetworkActivity extends Activity implements View.OnClickListener {


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localnetwork);

        Button hostButton = (Button) findViewById(R.id.host_button);
        hostButton.setOnClickListener(this);
        Button joinButton = (Button) findViewById(R.id.join_button);
        joinButton.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        Bundle contextData = new Bundle();

        switch (v.getId()) {
            case R.id.host_button:
                contextData.putBoolean("is_host",true);
                contextData.putString("ip_address",getWifiIp());
                contextData.putInt("port", GameHost.getFreePort());
                break;
            case R.id.join_button:
                EditText ip = (EditText) findViewById(R.id.join_field);
                contextData.putBoolean("is_host",false);
                String[] host = ip.getText().toString().split(":");
                if(host.length!=2) {
                    showIpError();
                    return;
                }
                int port;
                try {
                    InetAddress.getByName(host[0]);
                    port = Integer.parseInt(host[1]);
                } catch (NumberFormatException e) { showIpError(); return;
                } catch (UnknownHostException e)  { showIpError(); return; }

                contextData.putString("ip_address",host[0]);
                contextData.putInt("port",port);
                break;
            default:
                return;
        }
        Intent i = new Intent(getBaseContext(),GameActivity.class);
        i.putExtras(contextData);
        startActivity(i);
    }

    private void showIpError() {
        Toast.makeText(this,"Error! IP on invalid format!",Toast.LENGTH_LONG).show();
    }

    private String getWifiIp() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }
}
