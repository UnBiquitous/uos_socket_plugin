package org.unbiquitous.uos.network.socket.radar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.radar.Radar;
import org.unbiquitous.uos.core.network.radar.RadarListener;
import org.unbiquitous.uos.network.socket.TCPProperties;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.connectionManager.TCPConnectionManager;
import org.unbiquitous.uos.network.socket.connectionManager.SocketConnectionManager.EthernetConnectionType;

public class MulticastRadar implements Radar {

	private static final Logger logger = UOSLogging.getLogger();
	
	protected DatagramSocketFactory socketFactory = new DatagramSocketFactory();
	private Boolean running = false;

	private final RadarListener listener;
	private Set<String> knownAddresses;

	private DateTime lastCheck;
	private Set<String> lastAddresses;

	private MulticastSocket socket;

	private Integer port = 14984;

	private int secondsBetweenBeacons = 30;
	
	public MulticastRadar(RadarListener listener) {
		this.listener = listener;
		this.knownAddresses = new HashSet<String>();
	}
	
	@Override
	public void run() {
		try {
			this.lastCheck = new DateTime();
			this.lastAddresses = new HashSet<String>();
			socket = socketFactory.newSocket(port);
			socket.setBroadcast(true);
			socket.setReuseAddress(true);
			int tenSeconds = 10*1000;
			socket.setSoTimeout(tenSeconds);
			sendBeacon(socket, InetAddress.getByName("255.255.255.255"), port);
			while(running){
				receiveAnswers(port, socket);
				checkLeftDevices(port, socket);
			}
			socket.close();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Problems running the radar.", e);
		}
		
	}

	private void checkLeftDevices(Integer port, DatagramSocket socket) throws IOException {
		DateTime now = new DateTime();
		if (Seconds.secondsBetween(lastCheck, now).getSeconds() > secondsBetweenBeacons){
			sendBeacon(socket, InetAddress.getByName("255.255.255.255"), port);
			lastAddresses.removeAll(knownAddresses);
			for(String address: lastAddresses){
				SocketDevice left = new SocketDevice(address,port, 
						EthernetConnectionType.TCP);
				listener.deviceLeft(left);
			}
			lastAddresses = knownAddresses;
			knownAddresses =  new HashSet<String>();
			lastCheck = now;
		}
	}

	private void sendBeacon(DatagramSocket socket, InetAddress address, Integer port)
			throws UnknownHostException, IOException {
		socket.send(new DatagramPacket(new byte[]{1}, 1, address, port));
	}

	private void receiveAnswers(Integer port, DatagramSocket socket) throws IOException {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[1], 1);
			socket.receive(packet);
			handleBeacon(port, socket, packet);
		} catch (SocketTimeoutException e) {
			// Timeout is expected to happen.
		}
	}

	private void handleBeacon(Integer port, DatagramSocket socket,
			DatagramPacket packet) throws UnknownHostException, IOException {
		if (packet.getAddress() != null){
			String address = packet.getAddress().getHostAddress();
			if (!knownAddresses.contains(address)){
				//TODO: rename this class to SocketDevice
				logger.info(String.format("Entered device %s.", address));
				SocketDevice found = new SocketDevice(address,port, 
						EthernetConnectionType.TCP);
				listener.deviceEntered(found);
				sendBeacon(socket, packet.getAddress(), port);
				knownAddresses.add(address);
			}
		}
	}

	@Override
	public void setConnectionManager(ConnectionManager manager) {
		if(manager == null) return;
		InitialProperties properties = manager.getProperties();
		if (properties.containsKey("ubiquitos.eth.tcp.port")){
			port = properties.getInt("ubiquitos.eth.tcp.port");
		}
		if (properties.containsKey("ubiquitos.multicast.beaconFrequencyInSeconds")){
			secondsBetweenBeacons = properties.getInt("ubiquitos.multicast.beaconFrequencyInSeconds");
		}
	}

	@Override
	public void startRadar() {
		running = true;
	}

	@Override
	public void stopRadar() {
		running = false;
	}

	@SuppressWarnings("serial")
	public static class Properties  extends TCPProperties{
		public Properties() {
			addRadar(MulticastRadar.class, TCPConnectionManager.class);
		}
	}
	
}

class DatagramSocketFactory{
	MulticastSocket newSocket() throws IOException{
		return new MulticastSocket();
	}
	MulticastSocket newSocket(int port) throws IOException{
		return new MulticastSocket(port);
	}
}