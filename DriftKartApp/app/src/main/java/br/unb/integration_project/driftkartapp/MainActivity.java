package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    BluetoothMonitor btMonitor;
    SensorDataHanddling sensorDataHanddling;

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

        sensorDataHanddling = new SensorDataHanddling(this,
                BluetoothAdapter.getDefaultAdapter(), new IntentFilter());
        validateBluetoothConnection();
    }

    public void validateBluetoothConnection() {
        String btOfflineMessage = "Bluetooth Desligado!";
        String notHaveBluetooth = "Não possue Bluetooth!";

        int connectionStatus = sensorDataHanddling.establishBluetoothConnection();
        switch (connectionStatus) {
            case SensorDataHanddling.BLUETOOTH_OFFLINE:
                Toast.makeText(getApplicationContext(), btOfflineMessage, Toast.LENGTH_LONG)
                        .show();
                break;
            case SensorDataHanddling.NOT_HAVE_BLUETOOTH:
                Toast.makeText(getApplicationContext(), notHaveBluetooth, Toast.LENGTH_LONG)
                        .show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO: Verify if BroadcastReceiver was setted.
        unregisterReceiver(sensorDataHanddling.getBtActionReceiver());

    }
}
