package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

//TODO: 1- Get BluetoohAdapter; 2- Device discovery; 3- Pair with device;
public class BluetoothMonitor {

    //TODO: This attribute needs to be passed to this Class. Actions depends from the desired behavior.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        //CAUTION: This method will run on main thread of process.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    //If discovery started: Popup with animation.
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    //If device founded.
                    //TODO: cancelDiscovey() needs to be called on ACTION_FOUND Intent.
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    //If discovery finished.
                    break;
            }
        }
    };

    //getDefaultAdapter() returns null if called from emulator.
    public BluetoothMonitor(Context pContext, BluetoothAdapter pBtAdapter,
                            IntentFilter pFilter) {
        if(pBtAdapter != null) {
            if (pBtAdapter.isEnabled()) {
                pFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                pFilter.addAction(BluetoothDevice.ACTION_FOUND);
                pFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                pContext.registerReceiver(mReceiver, pFilter);

                //TODO: Handle the boolean return of method.
                pBtAdapter.startDiscovery();
            } else {
                //TODO: Handle bluetooth offline case.
            }
        } else {
            //TODO: Handle the case of device not having bluetooth.
        }
    }
}
