package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    PrepareDeviceCommunication prepareDeviceCommunication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareDeviceCommunication = new PrepareDeviceCommunication(this,
                BluetoothAdapter.getDefaultAdapter(), new IntentFilter());
        prepareDeviceCommunication.establishBluetoothConnection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the user accepts to enable bluetooth.
        if(resultCode == RESULT_OK) {
            prepareDeviceCommunication.establishBluetoothConnection();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        //TODO: Destroy all BT allocated resources.
        super.onDestroy();
        if(prepareDeviceCommunication.isReceiverRegistered) {
            unregisterReceiver(prepareDeviceCommunication.getBtActionReceiver());
        }
    }

    public void showEnableBluetoothDialog(String pIntentAction) {
        Intent intent = new Intent(pIntentAction);
        startActivityForResult(intent, 1);
    }
}
