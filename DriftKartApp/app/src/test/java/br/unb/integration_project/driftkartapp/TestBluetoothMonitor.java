package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BluetoothAdapter.class)
public class TestBluetoothMonitor {

    BluetoothMonitor btMonitor;
    Context mockContext;
    BluetoothAdapter btAdapterMock;
    IntentFilter filter;

    @Before
    public void setUp() {
        btAdapterMock = PowerMockito.mock(BluetoothAdapter.class);
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(true);
        mockContext = Mockito.mock(Context.class);
        filter = Mockito.mock(IntentFilter.class);
    }

    @Test
    public void constructorTestBestCase() {
        btMonitor = new BluetoothMonitor(mockContext, btAdapterMock, filter);
        Assert.assertNotNull(btMonitor);
        Mockito.verify(btAdapterMock, Mockito.times(1)).startDiscovery();
        Mockito.verify(filter, Mockito.times(1)).addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        Mockito.verify(filter, Mockito.times(1)).addAction(BluetoothDevice.ACTION_FOUND);
        Mockito.verify(filter, Mockito.times(1)).addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

}
