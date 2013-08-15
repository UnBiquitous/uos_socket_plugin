package org.unbiquitous.uos.network.socket.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.connectionManager.SocketConnectionManager.EthernetConnectionType;
import org.unbiquitous.uos.network.socket.udp.UdpAccept;
import org.unbiquitous.uos.network.socket.udp.UdpChannel;


/**
 * this class represents the ethernet client connection.
 * 
 * @author Lucas Lins
 */
public class UDPClientConnection extends ClientConnection {
	
	/************************************
	 * ATTRIBUTES
	 ************************************/
	
	private UdpChannel udpChannel;
	private SocketAddress peerAddress;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	
	/**********************************
	 * Constructors
	 **********************************/
	
	public UDPClientConnection(UdpChannel udpChannel, InetSocketAddress socketAddress, UdpAccept udpAccept) throws IOException{
		super(new SocketDevice(socketAddress.getAddress().getHostName(), socketAddress.getPort(), EthernetConnectionType.UDP));
		this.peerAddress = socketAddress;
		this.udpChannel = udpChannel;
		this.udpChannel.connect(this.peerAddress, udpAccept.getInputStream(), udpAccept.getOutputStream());
	}
	
	public UDPClientConnection(String host, int port) throws IOException{
		super(new SocketDevice(host, port, EthernetConnectionType.UDP));
		this.peerAddress = new InetSocketAddress(host,port);
		this.udpChannel = UdpChannel.openChannel(this.peerAddress);
	}
	
	
	/************************************
	 * PUBLIC METHODS
	 ************************************/
	
	public boolean isConnected() {
		return udpChannel.isAlive();
	}
	
	/**
	 * method to get the input stream of the ethernet connection.
	 */
	public DataInputStream getDataInputStream() throws IOException {
		if(dataInputStream == null){
			dataInputStream = new DataInputStream(udpChannel.getInputStream(peerAddress));
		}
		return dataInputStream;
	}

	/**
	 * method to get the output stream of the ethernet connection
	 */
	public DataOutputStream getDataOutputStream() throws IOException {
		if(dataOutputStream == null){
			dataOutputStream = new DataOutputStream(udpChannel.getOutputStream(peerAddress));
		}
		return dataOutputStream;
	}
	
	/**
	 * Method to close connection.
	 */
	public void closeConnection() throws IOException {
		try{
			dataInputStream.close();
		} catch (Exception e) {}
		try{
			dataOutputStream.close();
		} catch (Exception e) {}
		try{
			udpChannel.close(peerAddress);
		} catch (Exception e) {}
	}
	
}
