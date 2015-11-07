package br.unb.integration_project.driftkartapp;

import android.os.Handler;

public class SensorDataHanddling {

    private MainActivity mainActivity;
    private BluetoothConnection btConnection;
    private Handler uiHandler;

    public SensorDataHanddling(MainActivity pMainActivity, BluetoothConnection pBtConnection,
                               Handler pUiHandler) {
        mainActivity = pMainActivity;
        btConnection = pBtConnection;
        uiHandler = pUiHandler;
    }

    public void startSensorMonitoring() {
        showReadedData();
    }

    public void showReadedData() {
        Runnable dataReadedNotification = new Runnable() {
            @Override
            public void run() {
                byte[] readedData = btConnection.getDataArray();
                int lowByte = (int)readedData[0];
                int highByte = (int)readedData[1] << 8;
                int dataReaded = highByte | lowByte;
                mainActivity.setSpeed(String.valueOf(dataReaded));
            }
        };
        btConnection.readData(2, uiHandler, dataReadedNotification, true);
    }

    public void stopLoopedReadData() {
        btConnection.setIsAllowedToContinue(false);
    }
}
