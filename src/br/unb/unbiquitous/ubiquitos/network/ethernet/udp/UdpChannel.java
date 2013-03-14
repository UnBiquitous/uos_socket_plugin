package br.unb.unbiquitous.ubiquitos.network.ethernet.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager.EthernetUDPConnectionManager;

public class UdpChannel extends Thread{
	
	/**************************************
	 * POOL ATTRIBUTES
	 **************************************/
	
	private static Map<Integer, UdpChannel> pool = new HashMap<Integer, UdpChannel>();
	
	/**************************************
	 * CHANNEL ATTRIBUTES
	 **************************************/
	
	private static int UDP_ACCEPT_SLEEP = 50;
	private static int UDP_MAX_BUFFER_SIZE = 100;
	
	private static String UDP_CHANNEL_SIMPLE_CONNECT_CONTROLER_KEY = "UDP_CHANNEL_SIMPLE_CONNECT_CONTROLER_KEY";
	
	private DatagramSocket socket;
	
	private LinkedHashMap<SocketAddress, UdpAccept> udpAcceptMap;
	private Boolean udpAcceptListenerClosed;
	private Map<SocketAddress, UdpInputStream> udpInputControlMap;
	private Map<UdpOutputStream, SocketAddress> udpOutputControlMap;
	
	/**************************************
	 * CONSTRUCTOR
	 **************************************/
	
	private UdpChannel(int port) throws IOException{
		socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
		
		udpAcceptMap = new LinkedHashMap<SocketAddress, UdpAccept>();
		udpAcceptListenerClosed = new Boolean(false);
		udpInputControlMap = new HashMap<SocketAddress, UdpInputStream>();
		udpOutputControlMap = new HashMap<UdpOutputStream, SocketAddress>();
	}
	
	
	/**************************************
	 * PUBLIC STATIC METHODS
	 **************************************/
	
	/**
	 * Open a UDP Channel in a free port.
	 */
	public static UdpChannel openChannel() throws IOException{
		return openChannel(0);
	}
	
	/**
	 * Open a UDP Channel in a free port, but already connected with the given socket address.
	 */
	public static UdpChannel openChannel(SocketAddress socketAddress) throws IOException{
		UdpChannel channel = openChannel(0);
		channel.connect(socketAddress);
		return channel;
	}
	
	/**
	 * Open a UDP Channel in a specific port, but already connected with the given socket address.
	 */
	public static UdpChannel openChannel(int localPort, SocketAddress socketAddress) throws IOException{
		UdpChannel channel = openChannel(localPort);
		channel.connect(socketAddress);
		return channel;
	}
	
	/**
	 * Open a UDP Channel in a specific port.
	 */
	public static UdpChannel openChannel(int localPort) throws IOException{
		synchronized (pool) {
			UdpChannel channel = pool.get(localPort);
			if(channel == null){
				channel = new UdpChannel(localPort);
				pool.put(channel.socket.getLocalPort(), channel);
				channel.start();
			}
			return channel;
		}
	}
	
	public static void tearDown() throws IOException{
		synchronized (pool) {
			for(UdpChannel channel : pool.values()){
				channel.close();
			}
			pool.clear();
		}
	}
	
	/**************************************
	 * PUBLIC METHODS
	 **************************************/
	
	public void connect(SocketAddress sa, UdpInputStream uis, UdpOutputStream uos) throws IOException{
		synchronized (udpInputControlMap) {
			if(udpInputControlMap.containsKey(sa) || udpOutputControlMap.containsKey(uos)){
				throw new IOException("Socket already connected.");
			}
			udpInputControlMap.put(sa, uis);
		}
		synchronized (udpOutputControlMap) {
			udpOutputControlMap.put(uos, sa);
		}
	}
	
	public UdpInputStream getInputStream(SocketAddress sa){
		synchronized (udpInputControlMap) {
			return udpInputControlMap.get(sa);
		}
	}
	
	public UdpOutputStream getOutputStream(SocketAddress sa){
		synchronized (udpOutputControlMap) {
			UdpOutputStream uos = null;
			Set<UdpOutputStream> uosSet = udpOutputControlMap.keySet();
			for(UdpOutputStream uosKey : uosSet){
				if(udpOutputControlMap.get(uosKey).equals(sa)){
					uos = uosKey;
					break;
				}
			}
			return uos;
		}
	}
	
	public void close(SocketAddress socketAddress) throws IOException{
		synchronized (udpInputControlMap) {
			UdpInputStream uis = udpInputControlMap.get(socketAddress);
			if(uis != null){
				uis.close();
				udpInputControlMap.remove(socketAddress);
			}
		}
		
		synchronized (udpOutputControlMap) {
			UdpOutputStream uos = null;
			Set<UdpOutputStream> uosSet = udpOutputControlMap.keySet();
			for(UdpOutputStream uosKey : uosSet){
				if(udpOutputControlMap.get(uosKey).equals(socketAddress)){
					uos = uosKey;
					break;
				}
			}
			if(uos != null){
				uos.close();
				udpOutputControlMap.remove(uos);
			}
		}
	}
	
	public synchronized void send(byte data[], int offset, int len, UdpOutputStream uos) throws IOException{
		synchronized (udpOutputControlMap) {
			if(udpOutputControlMap.containsKey(uos)){
				SocketAddress sa = udpOutputControlMap.get(uos);
				socket.send(new DatagramPacket(data, offset, len, sa));
			}else{
				throw new IOException("Channel not connected.");
			}
		}
	}
	
	public UdpAccept accept() throws IOException{
		try{
			while(true){
				if(udpAcceptListenerClosed){
					throw new IOException("Socket is closed.");
				}
				
				synchronized (udpAcceptMap) {
					Iterator<UdpAccept> it = udpAcceptMap.values().iterator();
					if(it.hasNext()){
						UdpAccept udpAccept = it.next();
						it.remove();
						return udpAccept;
					}
				}
				
				Thread.sleep(UDP_ACCEPT_SLEEP);
			}
		}catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public void closeServer(){
		if (udpAcceptMap != null){
			synchronized (udpAcceptMap) {
				if(udpAcceptListenerClosed == true){
					udpAcceptListenerClosed = false;
					udpAcceptMap = null;
				}
			}
		}
	}
	
	public void run() {
		try{
			SocketAddress socketAddress = null;
			
			int bufferSize = EthernetUDPConnectionManager.UDP_BUFFER_SIZE * 2;
			byte[] buffer = new byte[bufferSize];
			DatagramPacket packet = null;
			
			while(true){
				for (int i = 0; i < bufferSize; i++) buffer[i] = 0;
				packet = new DatagramPacket(buffer, bufferSize);
				socket.receive(packet);
				
				socketAddress = packet.getSocketAddress();
				
				synchronized (udpInputControlMap) {
					UdpInputStream uis = udpInputControlMap.get(socketAddress);
					if(uis != null){
						if(uis.isClosed()){
							close(socketAddress);
						}else{
							uis.putInBuffer(packet.getData(), packet.getLength());
						}
					}else if(!udpAcceptListenerClosed){
						synchronized (udpAcceptMap) {
							if(udpAcceptMap.size() >= UDP_MAX_BUFFER_SIZE){
								Iterator<UdpAccept> it = udpAcceptMap.values().iterator();
								it.next();
								it.remove();
							}
							
							byte[] receivedBuffer = packet.getData();
							int receivedBufferSize = packet.getLength();
							
							UdpAccept udpAccept = udpAcceptMap.get(socketAddress);
							if(udpAccept != null){
								udpAccept.getInputStream().putInBuffer(receivedBuffer, receivedBufferSize);
							}else{
								if(isConnectionMsg(receivedBuffer, receivedBufferSize)){
									udpAcceptMap.put(socketAddress, new UdpAccept(socketAddress, new UdpInputStream(), new UdpOutputStream(this)));
								}else{
									udpAcceptMap.put(socketAddress, new UdpAccept(socketAddress, new UdpInputStream(receivedBuffer, receivedBufferSize), new UdpOutputStream(this)));
								}
							}
						}
					}
				}
				
				cleanUp();
			}
		}catch (Exception e) {}
	}
	
	@Override
	public String toString() {
		return "UdpChannel[port: "+socket.getLocalPort()+"]";
	}

	/**************************************
	 * PRIVATE METHODS
	 **************************************/
	
	private void connect(SocketAddress sa) throws IOException{
		UdpInputStream in = null;
		UdpOutputStream out = null;
		
		synchronized (udpInputControlMap) {
			if(udpInputControlMap.containsKey(sa)){
				throw new IOException("Socket already connected.");
			}
			in = new UdpInputStream();
			udpInputControlMap.put(sa, in);
		}
		synchronized (udpOutputControlMap) {
			out = new UdpOutputStream(this);
			udpOutputControlMap.put(out, sa);
			this.sendConnectionMsg(out);
		}
	}
	
	private void sendConnectionMsg(UdpOutputStream out) throws IOException{
		for(int i = 0; i < UDP_CHANNEL_SIMPLE_CONNECT_CONTROLER_KEY.length(); i++){
			out.write(UDP_CHANNEL_SIMPLE_CONNECT_CONTROLER_KEY.charAt(i));
		}
		out.flush();
	}
	
	private boolean isConnectionMsg(byte[] bufferArray, int bufferSize){
		if(bufferSize == UDP_CHANNEL_SIMPLE_CONNECT_CONTROLER_KEY.length()){
			for(int i = 0; i < bufferSize; i++){
				if((char)bufferArray[i] != UDP_CHANNEL_SIMPLE_CONNECT_CONTROLER_KEY.charAt(i)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private void close() throws IOException{
		udpAcceptMap = null;
		udpInputControlMap = null;
		udpOutputControlMap = null;
		socket.close();
	}
	
	private void cleanUp(){
		try{
			synchronized (udpInputControlMap) {
				Iterator<UdpInputStream> it = udpInputControlMap.values().iterator();
				while(it.hasNext()){
					UdpInputStream uis = it.next();
					if(uis.isClosed()) it.remove();
				}
			}
			
			synchronized (udpOutputControlMap) {
				Set<UdpOutputStream> uosSet = udpOutputControlMap.keySet();
				for(UdpOutputStream uos : uosSet){
					if(uos.isClosed()) udpOutputControlMap.remove(uos);
				}
			}
		}catch (Exception e) {}
	}
}
