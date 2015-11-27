package br.unb.integration_project.driftkartapp;

import android.os.Handler;

public class SensorDataHandling {

    private MainActivity mainActivity;
    private BluetoothConnection btConnection;
    private Handler uiHandler;

    public SensorDataHandling(MainActivity pMainActivity, BluetoothConnection pBtConnection,
                              Handler pUiHandler) {
        mainActivity = pMainActivity;
        btConnection = pBtConnection;
        uiHandler = pUiHandler;
    }

    public void startSensorMonitoring() {
        Runnable dataReadedNotification = new Runnable() {
            @Override
            public void run() {
                int[] readedData = btConnection.getDataArray();
                char flag = (char)readedData[0];
                int lowByte = readedData[1];
                int highByte = readedData[2] << 8;
                //Give permission to readData() to read next bytes of data.
                btConnection.setCanWriteToArray(true);
                int dataReaded = highByte | lowByte;
                if(flag == 'S') {
                    mainActivity.setSpeed(dataReaded);
                }else {
                    mainActivity.setBattery(dataReaded, true);
                }
            }
        };
        btConnection.readData(3, uiHandler, dataReadedNotification, null, true);
    }

}
