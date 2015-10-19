package br.unb.integration_project.driftkartapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

//TODO: This class should be: PrepareCommunication.
public class PrepareDeviceCommunication {

    private Context appContext;
    private BluetoothMonitor btMonitor;
    private AlertDialog searchDialog;
    private BluetoothDevice device;
    private IntentFilter filterAction; //Needed for unit test mock.
    //TODO: Pass real MAC Address of BT Adapter on Arduino.
    private final String FAKE_KART_MAC_ADDRESS = "94:51:03:B6:45:8D";
    public boolean isReceiverRegistered = false;
    private BroadcastReceiver btActionReceiver = new BroadcastReceiver() {
        //CAUTION: This method will run on main thread of process.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(appContext);
                dialogBuilder.setMessage("Fazendo busca...");
                searchDialog = dialogBuilder.create();
                searchDialog.show();

            } else if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getAddress().equals(FAKE_KART_MAC_ADDRESS)) {
                    btMonitor.cancelDeviceDiscovery();
                    btMonitor.pairWithFoundedDevice(device);
                } else {
                    device = null;
                }

            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                searchDialog.dismiss();
                if(device == null) {
                    btMonitor.cancelDeviceDiscovery();

                    AlertDialog.Builder btAlertBuilder = new AlertDialog.Builder(appContext);
                    btAlertBuilder.setTitle("Sem conexão com o Kart.");
                    //TODO: Pass DialogInterface.OnClickListener to positive and negative buttons.
                    btAlertBuilder.setPositiveButton("Tentar denovo", null);
                    btAlertBuilder.setNegativeButton("Sair", null);

                    AlertDialog alertDialog = btAlertBuilder.create();
                    alertDialog.show();
                }

            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int BOND_STATE = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                final int PREVIOUS_BOND_STATE = intent.getIntExtra(BluetoothDevice
                        .EXTRA_PREVIOUS_BOND_STATE, -1);
                if(BOND_STATE == BluetoothDevice.BOND_BONDED) {
                    searchDialog.dismiss();
                    //TODO: Initiating connection with Device.
                    Toast.makeText(appContext, "Pareamento: Sucesso!", Toast.LENGTH_LONG).show();
                } else if(PREVIOUS_BOND_STATE == BluetoothDevice.BOND_BONDING
                        && BOND_STATE == BluetoothDevice.BOND_NONE) {
                    searchDialog.dismiss();
                }
            }
        }
    };

    public PrepareDeviceCommunication(Context pContext, BluetoothAdapter pBtAdapter,
                                      IntentFilter pFilterAction) {
        appContext = pContext;
        filterAction = pFilterAction;
        btMonitor = new BluetoothMonitor(appContext, pBtAdapter);
    }

    public void establishBluetoothConnection() {
        //TODO: Handdling the case of device not discoverable, but bluetooth ON.

        switch (btMonitor.verifyBluetoothReady()) {
            case BluetoothMonitor.BLUETOOTH_OFFLINE:
                Toast.makeText(appContext, "Bluetooth Desligado!", Toast.LENGTH_LONG).show();
                break;
            case BluetoothMonitor.NOT_HAVE_BLUETOOTH:
                Toast.makeText(appContext, "Nao possui Bluetooth!", Toast.LENGTH_LONG).show();
                break;
            case BluetoothMonitor.BLUETOOTH_ONLINE:
                filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filterAction.addAction(BluetoothDevice.ACTION_FOUND);
                filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filterAction.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

                isReceiverRegistered = true;
                appContext.registerReceiver(btActionReceiver, filterAction);

                btMonitor.startDeviceDiscovery();
                break;
        }
    }

    public BroadcastReceiver getBtActionReceiver() {
        return btActionReceiver;
    }
}
