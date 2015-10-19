package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    PrepareDeviceCommunication prepareDeviceCommunication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup mainRelLay = (ViewGroup)findViewById(R.id.mainBackgroundRelLay);
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.speed_layout, mainRelLay);
        inflater.inflate(R.layout.temp_layout, mainRelLay);
        inflater.inflate(R.layout.battery_layout, mainRelLay);
        inflater.inflate(R.layout.economic_layout, mainRelLay);
        inflater.inflate(R.layout.performance_layout, mainRelLay);

        prepareDeviceCommunication = new PrepareDeviceCommunication(this,
                BluetoothAdapter.getDefaultAdapter(), new IntentFilter());
        prepareDeviceCommunication.establishBluetoothConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(prepareDeviceCommunication.isReceiverRegistered) {
            unregisterReceiver(prepareDeviceCommunication.getBtActionReceiver());
        }
    }
}
