package org.unbiquitous.uos.network.socket.radar;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Vector;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.radar.RadarListener;

public class PingRadarTest extends TestCase {

	
	//TODO: Null breaks
	
	private RadarListener listener;
	private PingRadar radar;

	public void setUp(){
		listener = mock(RadarListener.class);
		radar = new PingRadar(listener);
	}
	
	public void test_notfyLostDevices(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		radar.deviceDiscoveryFinished(new Vector<String>());
		
		ArgumentCaptor<NetworkDevice> d = captor(NetworkDevice.class); 
		verify(listener).deviceLeft(d.capture());
		
		assertThat(d.getAllValues()).hasSize(1);
		assertThat(d.getValue().getNetworkDeviceName()).isEqualTo("1.2.3.4:14984");
	}
	
	public void test_notfyLostDevicesAfterASuccessfullDiscovery(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		
		discovered.clear();
		discovered.add("4.3.2.1");
		
		radar.deviceDiscoveryFinished(discovered);
		
		ArgumentCaptor<NetworkDevice> d = captor(NetworkDevice.class); 
		verify(listener).deviceLeft(d.capture());
		
		assertThat(d.getAllValues()).hasSize(1);
		assertThat(d.getValue().getNetworkDeviceName()).isEqualTo("1.2.3.4:14984");
	}
	
	public void test_ethutilCanSendDuplicates(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		discovered.add("4.3.2.1");
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		
		discovered.clear();
		discovered.add("4.3.2.1");
		
		radar.deviceDiscoveryFinished(discovered);
		
		ArgumentCaptor<NetworkDevice> d = captor(NetworkDevice.class); 
		verify(listener).deviceLeft(d.capture());
		
		assertThat(d.getAllValues()).hasSize(1);
		assertThat(d.getValue().getNetworkDeviceName()).isEqualTo("1.2.3.4:14984");
	}

	public void test_notfyNobodyWhenThereIsNoChange(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		radar.deviceDiscoveryFinished(discovered);
		
		verify(listener,never()).deviceLeft((NetworkDevice)any());
	}
	
	public void test_shouldNotBreakOnNull(){
		radar.deviceDiscoveryFinished(null);
		verify(listener,never()).deviceLeft((NetworkDevice)any());
	}
	
	private <T> ArgumentCaptor<T> captor(Class<T> clazz) {
		return ArgumentCaptor.forClass(clazz);
	}
	
}
