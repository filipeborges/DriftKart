package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
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