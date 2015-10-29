package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BluetoothAdapter.class, BluetoothDevice.class, BluetoothSocket.class})
public class TestBluetoothConnection {

    BluetoothConnection btMonitor;
    Context mockContext;
    BluetoothAdapter btAdapterMock;
    BluetoothDevice btDeviceMock;

    @Before
    public void setUp() {
        btAdapterMock = PowerMockito.mock(BluetoothAdapter.class);
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(true);
        mockContext = Mockito.mock(Context.class);
        btMonitor = new BluetoothConnection(mockContext, btAdapterMock);
        btDeviceMock = PowerMockito.mock(BluetoothDevice.class);
    }

    @Test
    public void testOpenSerialConnToFoundedDevice() {
        int not_paired_code = -1000;
        Mockito.when(btDeviceMock.getBondState()).thenReturn(not_paired_code);
        Assert.assertEquals(BluetoothConnection.NOT_PREVIOUSLY_PAIRED,
                btMonitor.openSerialConnToFoundedDevice(btDeviceMock));

        /*Mockito.when(btDeviceMock.getBondState()).thenReturn(BluetoothDevice.BOND_BONDED);
        BluetoothSocket btSocketMock = PowerMockito.mock(BluetoothSocket.class);
        UUID uuidMock = PowerMockito.mock(UUID.class);
        String SERIAL_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        PowerMockito.mockStatic(UUID.class);
        Mockito.when(UUID.fromString(SERIAL_UUID)).thenReturn(uuidMock);
        try {
            Mockito.when(btDeviceMock.createRfcommSocketToServiceRecord(uuidMock))
                    .thenReturn(btSocketMock);
        }catch (IOException ioe){}

        Assert.assertEquals(BluetoothConnection.SERIAL_CONN_OPENED,
                btMonitor.openSerialConnToFoundedDevice(btDeviceMock));*/
    }

    @Test
    public void testPairWithFoundedDevice() {
        //Not previously paired.
        Set<BluetoothDevice> emptySet = new HashSet<BluetoothDevice>();
        Mockito.when(btAdapterMock.getBondedDevices()).thenReturn(emptySet);
        Assert.assertEquals(BluetoothConnection.PAIRING_IN_PROGRESS,
                btMonitor.pairWithFoundedDevice(btDeviceMock));

        //Previously paired.
        Mockito.when(btDeviceMock.getAddress()).thenReturn("AAA");
        BluetoothDevice btDeviceMock2 = PowerMockito.mock(BluetoothDevice.class);
        emptySet.add(btDeviceMock2);
        Mockito.when(btDeviceMock2.getAddress()).thenReturn("AAA");
        btMonitor.pairWithFoundedDevice(btDeviceMock);
        Assert.assertEquals(BluetoothConnection.PREVIOUSLY_PAIRED,
                btMonitor.pairWithFoundedDevice(btDeviceMock));

    }

    @Test
    public void testVerifyBluetoothReady() {
        Assert.assertEquals(BluetoothConnection.BLUETOOTH_ONLINE, btMonitor.verifyBluetoothReady());
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(false);
        Assert.assertEquals(BluetoothConnection.BLUETOOTH_OFFLINE, btMonitor.verifyBluetoothReady());
        btMonitor = new BluetoothConnection(mockContext, null);
        Assert.assertEquals(BluetoothConnection.NOT_HAVE_BLUETOOTH, btMonitor.verifyBluetoothReady());

    }

    @Test
    public void testStartDiscovery() {
        btMonitor.startDeviceDiscovery();
        Mockito.verify(btAdapterMock, Mockito.times(1)).startDiscovery();
    }

    @Test
    public void testCancelDiscovery() {
        btMonitor.cancelDeviceDiscovery();
        Mockito.verify(btAdapterMock, Mockito.times(1)).cancelDiscovery();
    }

    @After
    public void tearDown() {
        btAdapterMock = null;
        mockContext = null;
        btMonitor = null;
        btDeviceMock = null;
    }
}
