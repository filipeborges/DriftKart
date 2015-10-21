package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
@PrepareForTest({BluetoothAdapter.class, BluetoothDevice.class})
public class TestBluetoothConnection {

    BluetoothConnection btMonitor;
    Context mockContext;
    BluetoothAdapter btAdapterMock;

    @Before
    public void setUp() {
        btAdapterMock = PowerMockito.mock(BluetoothAdapter.class);
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(true);
        mockContext = Mockito.mock(Context.class);
        btMonitor = new BluetoothConnection(mockContext, btAdapterMock);
    }

    @Test
    public void testPairWithFoundedDevice() {
        BluetoothDevice btDeviceMock = PowerMockito.mock(BluetoothDevice.class);
        Set<BluetoothDevice> emptySet = new HashSet<BluetoothDevice>();
        Mockito.when(btAdapterMock.getBondedDevices()).thenReturn(emptySet);
        btMonitor.pairWithFoundedDevice(btDeviceMock);


        Mockito.when(btDeviceMock.getAddress()).thenReturn("AAA");
        BluetoothDevice btDeviceMock2 = PowerMockito.mock(BluetoothDevice.class);
        emptySet.add(btDeviceMock2);
        Mockito.when(btDeviceMock2.getAddress()).thenReturn("AAA");
        btMonitor.pairWithFoundedDevice(btDeviceMock);
        Mockito.verify(btDeviceMock2, Mockito.times(1)).getAddress();
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
    }
}
