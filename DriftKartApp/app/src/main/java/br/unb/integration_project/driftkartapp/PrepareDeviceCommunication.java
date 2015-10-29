package br.unb.integration_project.driftkartapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

//TODO: This class should be: PrepareCommunication.
//TODO: Create a Toasty method in MainActivity, and removes all Toasty references from this class.
public class PrepareDeviceCommunication {

    public boolean isReceiverRegistered = false;
    private Context appContext;
    private BluetoothConnection btConnection;
    private AlertDialog searchDialog;
    private BluetoothDevice device;
    private IntentFilter filterAction; //Needed for unit test mock.
    //TODO: Pass real MAC Address of BT Adapter on Arduino.
    private final String FAKE_KART_MAC_ADDRESS = "94:51:03:B6:45:8D";

    private DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int buttonValue) {
            switch (buttonValue) {
                case DialogInterface.BUTTON_POSITIVE:
                    establishBluetoothConnection();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    ((MainActivity)appContext).finish();
                    break;
            }
        }
    };

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
                    btConnection.cancelDeviceDiscovery();
                    if(btConnection.pairWithFoundedDevice(device)
                            == BluetoothConnection.PREVIOUSLY_PAIRED) {
                        searchDialog.dismiss();
                        if(btConnection.openSerialConnToFoundedDevice(device) ==
                                BluetoothConnection.SERIAL_CONN_OPENED) {
                            Toast.makeText(appContext, "Conectou!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(appContext, "Falha na Conexão!", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    device = null;
                }

            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if(device == null) {
                    btConnection.cancelDeviceDiscovery();
                    searchDialog.dismiss();

                    AlertDialog.Builder btAlertBuilder = new AlertDialog.Builder(appContext);
                    btAlertBuilder.setTitle("Sem conexão com o Kart.");
                    btAlertBuilder.setPositiveButton("Tentar denovo", dialogListener);
                    btAlertBuilder.setNegativeButton("Sair", dialogListener);

                    AlertDialog alertDialog = btAlertBuilder.create();
                    alertDialog.show();
                }

            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int BOND_STATE = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                final int PREVIOUS_BOND_STATE = intent.getIntExtra(BluetoothDevice
                        .EXTRA_PREVIOUS_BOND_STATE, -1);
                if(BOND_STATE == BluetoothDevice.BOND_BONDED) {
                    searchDialog.dismiss();
                    Toast.makeText(appContext, "Pareamento: Sucesso!", Toast.LENGTH_LONG).show();
                    if(btConnection.openSerialConnToFoundedDevice(device) ==
                            BluetoothConnection.SERIAL_CONN_OPENED) {
                        Toast.makeText(appContext, "Conectou!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(appContext, "Falha na Conexão!", Toast.LENGTH_LONG).show();
                    }
                } else if(PREVIOUS_BOND_STATE == BluetoothDevice.BOND_BONDING
                        && BOND_STATE == BluetoothDevice.BOND_NONE) {
                    searchDialog.dismiss();
                }
            }
        }
    };

    public PrepareDeviceCommunication(MainActivity pActivity, BluetoothAdapter pBtAdapter,
                                      IntentFilter pFilterAction) {
        appContext = pActivity;
        filterAction = pFilterAction;
        btConnection = new BluetoothConnection(appContext, pBtAdapter);
    }

    public void establishBluetoothConnection() {
        //TODO: Handdling the case of device not discoverable, but bluetooth ON.

        switch (btConnection.verifyBluetoothReady()) {
            case BluetoothConnection.BLUETOOTH_OFFLINE:
                ((MainActivity)appContext).showEnableBluetoothDialog(BluetoothAdapter.
                        ACTION_REQUEST_ENABLE);
                break;
            case BluetoothConnection.NOT_HAVE_BLUETOOTH:
                Toast.makeText(appContext, "Nao possui Bluetooth!", Toast.LENGTH_LONG).show();
                break;
            case BluetoothConnection.BLUETOOTH_ONLINE:
                filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filterAction.addAction(BluetoothDevice.ACTION_FOUND);
                filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filterAction.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

                isReceiverRegistered = true;
                appContext.registerReceiver(btActionReceiver, filterAction);

                btConnection.startDeviceDiscovery();
                break;
        }
    }

    public BroadcastReceiver getBtActionReceiver() {
        return btActionReceiver;
    }
}
