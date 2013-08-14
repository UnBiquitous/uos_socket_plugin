package org.unbiquitous.uos.network.socket.radar;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.radar.Radar;
import org.unbiquitous.uos.core.network.radar.RadarListener;
import org.unbiquitous.uos.network.socket.EthernetDevice;
import org.unbiquitous.uos.network.socket.connectionManager.EthernetConnectionManager.EthernetConnectionType;

public class MulticastRadarTest {

	private Integer port;
	private MulticastRadar radar;
	private DatagramSocket serverSocket;
	private DatagramSocketFactory factory;
	private RadarListener listener;

	@Before public void setUp() throws Exception{
		port = 14984;
		//TODO: port is not used
		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
						{ "ubiquitos.eth.tcp.port", port.toString() }
				}; 
			}
		};
		listener = mock(RadarListener.class);
		radar = new MulticastRadar(listener);
		mockSockets();
	}

	private void mockSockets() throws SocketException {
		serverSocket = mock(DatagramSocket.class);
		factory = mock(DatagramSocketFactory.class);
		when(factory.newSocket(port)).thenReturn(serverSocket);
		radar.socketFactory = factory;
	}
	
	@After public void tearDown(){
		radar.stopRadar();
	}
	
	@Test public void mustBeARadarWithAProperFactory(){
		radar = new MulticastRadar(null);
		assertThat(radar).isInstanceOf(Radar.class);
		assertThat(radar.socketFactory)
			.isNotNull()
			.isInstanceOf(DatagramSocketFactory.class);
	}
	
	@Test public void listenToThePortSpecifyed() throws Exception{
		run();
		
		verify(factory).newSocket(port);
	}
	
	@Test public void waitForDeviceToNotifyItsExistence() throws Exception{
		run();
		
		ArgumentCaptor<DatagramPacket> arg = forClass(DatagramPacket.class); 
		verify(serverSocket,atLeastOnce()).receive(arg.capture());
		assertThat(arg.getValue()).isNotNull();
	}
	
	@Test public void startAndStopMustControlTheNumberOfThreads(){
		final int before = Thread.activeCount();
		
		run();
		assertThat(Thread.activeCount()).isEqualTo(before+1);
		
		radar.stopRadar();
		assertEventually(1000, new Runnable() {
			public void run() {
				assertThat(Thread.activeCount()).isEqualTo(before);
			}
		});
	}
	
	@Test public void sendsABroadcastBeaconAtStartup() throws Exception{
		run();
		final ArgumentCaptor<DatagramPacket> arg = forClass(DatagramPacket.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				try {
					verify(serverSocket,times(1)).send(arg.capture());
					DatagramPacket beacon = arg.getValue();
					assertThat(beacon.getAddress().getHostAddress())
						.isEqualTo("255.255.255.255");
					assertThat(beacon.getPort())
						.isEqualTo(port);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	@Test public void whenSomebodySendsABeaconNotifiesItsDiscoveryOnce() throws Exception{
		mockADeviceEntry("1.1.1.1");
		run();
		final ArgumentCaptor<NetworkDevice> arg = forClass(NetworkDevice.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				verify(listener,times(1)).deviceEntered(arg.capture());
				NetworkDevice device = arg.getValue();
				assertThat(device).isInstanceOf(EthernetDevice.class);
				assertThat(device.getNetworkDeviceName()).isEqualTo("1.1.1.1"+":"+port);
			}
		});
	}

	@Test public void sendsADirectResponseBeaconWhenSomebodyIsFound() throws Exception{
		mockADeviceEntry("1.1.1.1");
		run();
		final ArgumentCaptor<DatagramPacket> arg = forClass(DatagramPacket.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				try {
					verify(serverSocket,times(2)).send(arg.capture());
					DatagramPacket beacon = arg.getAllValues().get(1);
					assertThat(beacon.getAddress().getHostAddress())
						.isEqualTo("1.1.1.1");
					assertThat(beacon.getPort())
						.isEqualTo(port);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	private void mockADeviceEntry(final String enteredAddress) throws IOException {
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation)
					throws Throwable {
				Object[] args = invocation.getArguments();
				DatagramPacket packet = (DatagramPacket) args[0];
				packet.setAddress(InetAddress.getByName(enteredAddress));
				return null;
			}
		}).when(serverSocket).receive((DatagramPacket)any());
	}
	
	private void run(){
		radar.startRadar(); //TODO: why ?
		Thread t = new Thread(radar);
		t.start();
	}
	
	private void assertEventually(int timeoutInMilliseconds, Runnable assertion){
		long begin = System.currentTimeMillis();
		long now = begin;
		Throwable lastException = null;
		do{
			try{
				assertion.run();
				return;
			}catch(RuntimeException e){
				lastException = e;
			}catch(AssertionError e){
				lastException = e;
			}
			now = System.currentTimeMillis(); 
		}while((now - begin) < timeoutInMilliseconds);
		throw new RuntimeException(lastException);
	}
}
