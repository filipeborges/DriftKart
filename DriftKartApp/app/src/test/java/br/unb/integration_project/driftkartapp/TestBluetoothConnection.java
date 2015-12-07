package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BluetoothAdapter.class, BluetoothDevice.class, BluetoothSocket.class,
        BluetoothSocket.class, Handler.class})
public class TestBluetoothConnection {

    BluetoothConnection btConnection;
    BluetoothAdapter btAdapterMock;
    BluetoothDevice btDeviceMock;
    BluetoothSocket btSocketMock;
    Handler handlerMock;
    Runnable runnable;

    @Before
    public void setUp() {
        runnable = new Runnable() {
            @Override
            public void run() {}
        };
        btAdapterMock = PowerMockito.mock(BluetoothAdapter.class);
        btConnection = new BluetoothConnection(btAdapterMock);
        btDeviceMock = PowerMockito.mock(BluetoothDevice.class);
        btSocketMock = PowerMockito.mock(BluetoothSocket.class);
        handlerMock = PowerMockito.mock(Handler.class);
    }

    @Test
    public void testSendData() {
        OutputStream outStreamMock = Mockito.mock(OutputStream.class);
        Runnable notification = Mockito.mock(Runnable.class);
        Mockito.when(btDeviceMock.getBondState()).thenReturn(BluetoothDevice.BOND_BONDED);
        UUID uuidRecord = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            Mockito.when(btDeviceMock.createRfcommSocketToServiceRecord(uuidRecord))
                    .thenReturn(btSocketMock);
            Mockito.when(btSocketMock.getOutputStream()).thenReturn(outStreamMock);
        }catch(IOException ioe){}
        btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, notification, notification);

        //Case of best scenario.
        byte[] fakeArray = new byte[5];
        int returnSendData = btConnection.sendData(fakeArray);
        Assert.assertEquals(fakeArray.length, returnSendData);

        //Case of NullPointerException.
        try {
            btConnection = new BluetoothConnection(btAdapterMock);
            Mockito.when(btSocketMock.getOutputStream()).thenReturn(null);
            btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, notification, notification);
        }catch (IOException ioe){}
        returnSendData = btConnection.sendData(fakeArray);
        Assert.assertEquals(-2, returnSendData);

        try {
            Thread.sleep(100);
        }catch (InterruptedException ie){}

        //Case of IOException.
        try {
            btConnection = new BluetoothConnection(btAdapterMock);
            Mockito.when(btSocketMock.getOutputStream()).thenThrow(new IOException());
            btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, notification, notification);
        }catch (IOException ioe){}
        returnSendData = btConnection.sendData(fakeArray);
        Assert.assertEquals(-1, returnSendData);
    }

    @Test
    public void testCloseBluetoothSocket() {
        Mockito.when(btDeviceMock.getBondState()).thenReturn(BluetoothDevice.BOND_BONDED);
        OutputStream outStreamMock = Mockito.mock(OutputStream.class);
        UUID serial_uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        InputStream inputMock = Mockito.mock(InputStream.class);
        try {
            Mockito.when(btDeviceMock.createRfcommSocketToServiceRecord(serial_uuid))
                    .thenReturn(btSocketMock);
            Mockito.when(btSocketMock.getInputStream()).thenReturn(inputMock);
            Mockito.when(btSocketMock.getOutputStream()).thenReturn(outStreamMock);
            btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, runnable, runnable);
            btConnection.sendData(new byte[1]);
            btConnection.closeBluetoothResources();
            Mockito.verify(inputMock, Mockito.times(1)).close();
            Mockito.verify(btSocketMock, Mockito.times(1)).close();
            Mockito.verify(outStreamMock, Mockito.times(1)).close();
        }catch(IOException ioe) {}
    }

    @Test
    public void testGetDataArray() {
        Assert.assertNull(btConnection.getDataArray());
    }

    @Test
    public void testOpenSerialConnToDevice() {
        //Case of device not paired.
        int random_code = -1000;
        Mockito.when(btDeviceMock.getBondState()).thenReturn(random_code);
        Assert.assertEquals(BluetoothConnection.NOT_PREVIOUSLY_PAIRED,
                btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, runnable, runnable));

        //Case of successful connection.
        Mockito.when(btDeviceMock.getBondState()).thenReturn(BluetoothDevice.BOND_BONDED);
        try {
            Mockito.when(btDeviceMock.createRfcommSocketToServiceRecord(Mockito.any(UUID.class)))
                    .thenReturn(btSocketMock);
        }catch (IOException ioe){}
        Assert.assertEquals(BluetoothConnection.SERIAL_CONN_OPENED,
                btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, runnable, runnable));

        //Case of connection exception.
        try {
            Mockito.when(btDeviceMock.createRfcommSocketToServiceRecord(Mockito.any(UUID.class)))
                    .thenThrow(IOException.class);
        }catch (IOException ioe){}
        Assert.assertEquals(BluetoothConnection.SERIAL_CONN_EXCEPTION,
                btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, runnable, runnable));
    }

    @Test
    public void testReadData() {
        Mockito.when(btDeviceMock.getBondState()).thenReturn(BluetoothDevice.BOND_BONDED);
        try {
            Mockito.when(btDeviceMock.createRfcommSocketToServiceRecord(Mockito.any(UUID.class)))
                    .thenReturn(btSocketMock);
            InputStream inputMock = Mockito.mock(InputStream.class);
            Mockito.when(btSocketMock.getInputStream()).thenReturn(inputMock);

            btConnection.openSerialConnToDevice(btDeviceMock, handlerMock, runnable, runnable);
            btConnection.readData(3, handlerMock, runnable, runnable, false);
            Mockito.verify(inputMock, Mockito.timeout(200).times(3)).read();
            Mockito.verify(inputMock, Mockito.timeout(200).times(1)).close();
            Mockito.verify(handlerMock, Mockito.timeout(200).times(2)).post(Mockito.any(Runnable.class));
        }catch (Exception ioe){}
    }

    @Test
    public void testPairWithFoundedDevice() {
        //Not previously paired.
        Set<BluetoothDevice> emptySet = new HashSet<BluetoothDevice>();
        Mockito.when(btAdapterMock.getBondedDevices()).thenReturn(emptySet);
        Assert.assertEquals(BluetoothConnection.PAIRING_IN_PROGRESS,
                btConnection.pairWithFoundedDevice(btDeviceMock));

        //Previously paired.
        Mockito.when(btDeviceMock.getAddress()).thenReturn("AAA");
        BluetoothDevice btDeviceMock2 = PowerMockito.mock(BluetoothDevice.class);
        emptySet.add(btDeviceMock2);
        Mockito.when(btDeviceMock2.getAddress()).thenReturn("AAA");
        btConnection.pairWithFoundedDevice(btDeviceMock);
        Assert.assertEquals(BluetoothConnection.PREVIOUSLY_PAIRED,
                btConnection.pairWithFoundedDevice(btDeviceMock));
    }

    @Test
    public void testVerifyBluetoothReady() {
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(true);
        Assert.assertEquals(BluetoothConnection.BLUETOOTH_ONLINE, btConnection.verifyBluetoothReady());
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(false);
        Assert.assertEquals(BluetoothConnection.BLUETOOTH_OFFLINE, btConnection.verifyBluetoothReady());
        btConnection = new BluetoothConnection(null);
        Assert.assertEquals(BluetoothConnection.NOT_HAVE_BLUETOOTH, btConnection.verifyBluetoothReady());
    }

    @Test
    public void testStartDiscovery() {
        btConnection.startDeviceDiscovery();
        Mockito.verify(btAdapterMock, Mockito.times(1)).startDiscovery();
    }

    @Test
    public void testCancelDiscovery() {
        btConnection.cancelDeviceDiscovery();
        Mockito.verify(btAdapterMock, Mockito.times(1)).cancelDiscovery();
    }

    @After
    public void tearDown() {
        btAdapterMock = null;
        btConnection = null;
        btDeviceMock = null;
        handlerMock = null;
        runnable = null;
        btSocketMock = null;
    }
}
