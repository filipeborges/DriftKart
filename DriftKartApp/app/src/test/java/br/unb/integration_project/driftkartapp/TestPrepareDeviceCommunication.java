package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BluetoothAdapter.class, Looper.class})
public class TestPrepareDeviceCommunication {
    BluetoothAdapter btAdapterMock;
    PrepareDeviceCommunication deviceComm;
    IntentFilter filterActionMock;
    MainActivity mockActivity;
    BluetoothConnection btConnectionMock;

    @Before
    public void setUp() {
        mockActivity = Mockito.mock(MainActivity.class);
        PowerMockito.mockStatic(BluetoothAdapter.class);
        btAdapterMock = PowerMockito.mock(BluetoothAdapter.class);
        filterActionMock = Mockito.mock(IntentFilter.class);
        PowerMockito.mockStatic(Looper.class);
        deviceComm = new PrepareDeviceCommunication(mockActivity);
        btConnectionMock = Mockito.mock(BluetoothConnection.class);
    }

    @Test
    public void testBroadcastReceiver() {
        Context contextMock = Mockito.mock(Context.class);
        Intent intentMock = Mockito.mock(Intent.class);
        deviceComm.setAttributesForUnitTest(btConnectionMock, null, null);

        //Discovery started.
        Mockito.when(intentMock.getAction()).thenReturn(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        deviceComm.btActionReceiver.onReceive(contextMock, intentMock);
        Mockito.verify(mockActivity, Mockito.times(1)).showSearchDialog();

        //Discovery finished and btDevice is null.
        Mockito.when(intentMock.getAction()).thenReturn(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        deviceComm.btActionReceiver.onReceive(contextMock, intentMock);
        Mockito.verify(btConnectionMock, Mockito.times(1)).cancelDeviceDiscovery();
        Mockito.verify(mockActivity, Mockito.times(1)).dismissSearchDialog();
        Mockito.verify(mockActivity, Mockito.times(1)).showConnTryAgainDialog();

        //Device founded and succeeded pairing.
        btConnectionMock = Mockito.mock(BluetoothConnection.class);
        mockActivity = Mockito.mock(MainActivity.class);
        deviceComm = new PrepareDeviceCommunication(mockActivity);
        deviceComm.setAttributesForUnitTest(btConnectionMock, null, null);
        Mockito.when(intentMock.getAction()).thenReturn(BluetoothDevice.ACTION_FOUND);
        BluetoothDevice btDeviceMock = PowerMockito.mock(BluetoothDevice.class);
        String macAdress = "20:15:02:03:53:66";
        Mockito.when(intentMock.getAction()).thenReturn(BluetoothDevice.ACTION_FOUND);
        Mockito.when(intentMock.getParcelableExtra(Mockito.anyString())).thenReturn(btDeviceMock);
        Mockito.when(btDeviceMock.getAddress()).thenReturn(macAdress);
        Mockito.when(btConnectionMock.pairWithFoundedDevice(Mockito.any(BluetoothDevice.class)))
                .thenReturn(BluetoothConnection.PREVIOUSLY_PAIRED);
        Mockito.when(btConnectionMock.openSerialConnToDevice(Mockito.any(BluetoothDevice.class),
                Mockito.any(Handler.class), Mockito.any(Runnable.class), Mockito.any(Runnable.class)))
                .thenReturn(BluetoothConnection.SERIAL_CONN_OPENED);
        deviceComm.btActionReceiver.onReceive(contextMock, intentMock);
        Mockito.verify(btConnectionMock, Mockito.times(1)).cancelDeviceDiscovery();
        Mockito.verify(btConnectionMock, Mockito.times(1))
                .pairWithFoundedDevice(Mockito.any(BluetoothDevice.class));
        Mockito.verify(btConnectionMock, Mockito.times(1))
                .openSerialConnToDevice(Mockito.any(BluetoothDevice.class),
                        Mockito.any(Handler.class), Mockito.any(Runnable.class), Mockito.any(Runnable.class));
        Mockito.verify(mockActivity, Mockito.times(0)).showLongToastDialog(Mockito.anyString());

        //Device founded and failed pairing. Depends on before test.
        Mockito.when(btConnectionMock.openSerialConnToDevice(Mockito.any(BluetoothDevice.class),
                Mockito.any(Handler.class), Mockito.any(Runnable.class), Mockito.any(Runnable.class)))
                .thenReturn(-90);
        deviceComm.btActionReceiver.onReceive(contextMock, intentMock);
        Mockito.verify(mockActivity, Mockito.times(1)).showLongToastDialog(Mockito.anyString());

        //Discovery finished and btDevice not null.
        btConnectionMock = Mockito.mock(BluetoothConnection.class);
        mockActivity = Mockito.mock(MainActivity.class);
        Mockito.when(intentMock.getAction()).thenReturn(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        deviceComm.btActionReceiver.onReceive(contextMock, intentMock);
        Mockito.verify(btConnectionMock, Mockito.times(0)).cancelDeviceDiscovery();
        Mockito.verify(mockActivity, Mockito.times(0)).dismissSearchDialog();
        Mockito.verify(mockActivity, Mockito.times(0)).showConnTryAgainDialog();


    }

    @Test
    public void testCloseBluetoothResources() {
        deviceComm.setAttributesForUnitTest(btConnectionMock, null, filterActionMock);

        //Case of receiver not registered.
        deviceComm.closeBluetoothResources();
        Mockito.verify(btConnectionMock, Mockito.times(1)).cancelDeviceDiscovery();
        Mockito.verify(mockActivity, Mockito.times(0))
                .unregisterReceiver(Mockito.any(BroadcastReceiver.class));
        try {
            Mockito.verify(btConnectionMock, Mockito.times(1)).closeBluetoothResources();
        }catch (IOException ioe){}

        //Case of receiver registered.
        Mockito.when(btConnectionMock.verifyBluetoothReady())
                .thenReturn(BluetoothConnection.BLUETOOTH_ONLINE);
        deviceComm.establishBluetoothConnection();
        deviceComm.closeBluetoothResources();
        Mockito.verify(btConnectionMock, Mockito.times(2)).cancelDeviceDiscovery();
        Mockito.verify(mockActivity, Mockito.times(1))
                .unregisterReceiver(Mockito.any(BroadcastReceiver.class));
        try {
            Mockito.verify(btConnectionMock, Mockito.times(2)).closeBluetoothResources();
        }catch (IOException ioe){}
    }

    @Test
    public void testGetDataFlowHandling() {
        Assert.assertNotNull(deviceComm.getDataFlowHandling());
    }

    @Test
    public void testGetConnectNotification() {
        Runnable connectNotification = deviceComm.getConnectNotification();
        Assert.assertNotNull(connectNotification);
    }

    @Test
    public void testGetExceptionNotification() {
        Runnable exceptionNotification = deviceComm.getExceptionNotification();
        Assert.assertNotNull(exceptionNotification);
    }

    @Test
    public void testEstablishBluetoothConnection() {
        Handler uiHandlerMock = Mockito.mock(Handler.class);
        DataFlowHandling dtFlowHandlingMock = new DataFlowHandling(mockActivity,
                btConnectionMock, uiHandlerMock);
        IntentFilter filterActionMock = Mockito.mock(IntentFilter.class);
        deviceComm.setAttributesForUnitTest(btConnectionMock, dtFlowHandlingMock, filterActionMock);

        //Case of not have bluetooth.
        Mockito.when(btConnectionMock.verifyBluetoothReady())
                .thenReturn(BluetoothConnection.NOT_HAVE_BLUETOOTH);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(mockActivity, Mockito.times(1)).showLongToastDialog(Matchers.anyString());

        //Case of bluetooth offline.
        Mockito.when(btConnectionMock.verifyBluetoothReady())
                .thenReturn(BluetoothConnection.BLUETOOTH_OFFLINE);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(mockActivity, Mockito.times(1))
                .showEnableBluetoothDialog(Matchers.anyString());

        //Case of bluetooth online and isReceiverRegistered equals to false.
        Mockito.when(btConnectionMock.verifyBluetoothReady())
                .thenReturn(BluetoothConnection.BLUETOOTH_ONLINE);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(filterActionMock, Mockito.times(4)).addAction(Mockito.anyString());
        Mockito.verify(mockActivity, Mockito.times(1))
                .registerReceiver(Mockito.any(BroadcastReceiver.class),
                        Mockito.any(IntentFilter.class));
        Mockito.verify(btConnectionMock, Mockito.times(1)).startDeviceDiscovery();

        //Case of bluetooth online and isReceiverRegistered equals to true.
        filterActionMock = Mockito.mock(IntentFilter.class);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(filterActionMock, Mockito.times(0)).addAction(Mockito.anyString());
        Mockito.verify(btConnectionMock, Mockito.times(2)).startDeviceDiscovery();
    }

    @Test
    public void testGetBtActionReceiver() {
    }

    @After
    public void tearDown() {
        this.mockActivity = null;
        this.btAdapterMock = null;
        this.filterActionMock = null;
        this.deviceComm = null;
    }
}