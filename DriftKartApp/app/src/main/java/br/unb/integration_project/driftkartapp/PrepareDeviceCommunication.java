package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;


//TODO: This class should be: PrepareCommunication.
public class PrepareDeviceCommunication {

    private boolean isReceiverRegistered = false;
    private MainActivity mainActivity;
    private BluetoothConnection btConnection;
    private BluetoothDevice device;
    private Handler uiHandler;
    private IntentFilter filterAction; //Needed for unit test mock.
    //TODO: Pass real MAC Address of BT Adapter on Arduino.
    //private final String FAKE_KART_MAC_ADDRESS = "94:51:03:B6:45:8D";
    private final String KART_MAC_ADDRESS = "20:15:02:03:53:66";

    private DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int buttonValue) {
            switch (buttonValue) {
                case DialogInterface.BUTTON_POSITIVE:
                    establishBluetoothConnection();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    mainActivity.finish();
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
                mainActivity.showSearchDialog();

            } else if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice foundedDevice = intent.getParcelableExtra(BluetoothDevice
                        .EXTRA_DEVICE);
                if(foundedDevice.getAddress().equals(KART_MAC_ADDRESS)) {
                    btConnection.cancelDeviceDiscovery();
                    device = foundedDevice;
                    if(btConnection.pairWithFoundedDevice(device) == BluetoothConnection
                            .PREVIOUSLY_PAIRED) {
                        mainActivity.dismissSearchDialog();
                        if(btConnection.openSerialConnToFoundedDevice(device, uiHandler,
                                getConnectNotification()) == BluetoothConnection.SERIAL_CONN_OPENED) {
                            //TODO: Refactor this.
                        } else {
                            mainActivity.showLongToastDialog("Falha na Conexão!");
                        }
                    }
                }

            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if(device == null) {
                    btConnection.cancelDeviceDiscovery();
                    mainActivity.dismissSearchDialog();
                    mainActivity.showConnTryAgainDialog(dialogListener);
                }

            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int BOND_STATE = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                final int PREVIOUS_BOND_STATE = intent.getIntExtra(BluetoothDevice
                        .EXTRA_PREVIOUS_BOND_STATE, -1);
                if(BOND_STATE == BluetoothDevice.BOND_BONDED) {
                    mainActivity.dismissSearchDialog();
                    mainActivity.showLongToastDialog("Pareamento: Sucesso!");
                    if(btConnection.openSerialConnToFoundedDevice(device, uiHandler,
                            getConnectNotification()) == BluetoothConnection.SERIAL_CONN_OPENED) {
                        //TODO:Refactor this.
                    } else {
                        mainActivity.showLongToastDialog("Falha na Conexão!");
                    }
                } else if(PREVIOUS_BOND_STATE == BluetoothDevice.BOND_BONDING
                        && BOND_STATE == BluetoothDevice.BOND_NONE) {
                    mainActivity.dismissSearchDialog();
                }
            }
        }
    };

    public void showReadedData() {
        Runnable dataReadedNotification = new Runnable() {
            @Override
            public void run() {
                byte[] readedData = btConnection.getDataArray();
                int lowByte = (int)readedData[0];
                int highByte = (int)readedData[1] << 8;
                int dataReaded = highByte | lowByte;
                mainActivity.showLongToastDialog(String.valueOf(dataReaded));
            }
        };
        btConnection.readData(2, uiHandler, dataReadedNotification);
    }

    public Runnable getConnectNotification() {
        return new Runnable() {
            @Override
            public void run() {
                //TODO: Start and transfer execution to SensordataHanddling
                mainActivity.showLongToastDialog("Conectou1!");
                showReadedData();
            }
        };
    }

    public PrepareDeviceCommunication(MainActivity pActivity) {
        uiHandler = new Handler(Looper.getMainLooper());
        mainActivity = pActivity;
        filterAction = new IntentFilter();
        btConnection = new BluetoothConnection(mainActivity, BluetoothAdapter.getDefaultAdapter());
    }

    public void establishBluetoothConnection() {
        //TODO: Handdling the case of device not discoverable, but bluetooth ON.

        switch (btConnection.verifyBluetoothReady()) {
            case BluetoothConnection.BLUETOOTH_OFFLINE:
                mainActivity.showEnableBluetoothDialog(BluetoothAdapter.
                        ACTION_REQUEST_ENABLE);
                break;
            case BluetoothConnection.NOT_HAVE_BLUETOOTH:
                mainActivity.showLongToastDialog("Nao possui Bluetooth!");
                break;
            case BluetoothConnection.BLUETOOTH_ONLINE:
                if(!isReceiverRegistered) {
                    filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    filterAction.addAction(BluetoothDevice.ACTION_FOUND);
                    filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    filterAction.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

                    isReceiverRegistered = true;
                    mainActivity.registerReceiver(btActionReceiver, filterAction);
                }
                btConnection.startDeviceDiscovery();
                break;
        }
    }

    public void closeAllBlutoothResources() {
        btConnection.cancelDeviceDiscovery();
        if(isReceiverRegistered) {
            mainActivity.unregisterReceiver(btActionReceiver);
        }
        btConnection.closeBluetoothSocket();
    }
}
