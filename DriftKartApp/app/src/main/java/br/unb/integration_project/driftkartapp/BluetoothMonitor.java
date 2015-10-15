package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

//The app needs to be Client and the Arduino the Server. Client will connect on Server MAC Address.
//TODO: 1- Get BluetoohAdapter; 2- Device discovery; 3- Pair with device;
public class BluetoothMonitor {

    private BluetoothAdapter btAdapter;
    private Context appContext;

    //getDefaultAdapter() returns null if called from emulator.
    public BluetoothMonitor(Context pContext, BluetoothAdapter pBtAdapter) {
        btAdapter = pBtAdapter;
        appContext = pContext;
    }

    public void startDeviceDiscovery() {
        //TODO: Before device discovery, query devices previous paired to see if its available.
        //Asynchronous call.
        btAdapter.startDiscovery();
    }

    public void cancelDeviceDiscovery() {
        btAdapter.cancelDiscovery();
    }

    public void openSerialConnToFoundedDevice(BluetoothDevice btDevice) {
        final String SERIAL_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        try {
                //TODO: Implement this method.
                BluetoothSocket btScocket = btDevice.createInsecureRfcommSocketToServiceRecord(UUID
                        .fromString(SERIAL_UUID));
                btScocket.connect();
                Toast.makeText(appContext, "Conectou", Toast.LENGTH_LONG).show();
                btScocket.close();
        } catch(IOException ioe) {}
    }
}
