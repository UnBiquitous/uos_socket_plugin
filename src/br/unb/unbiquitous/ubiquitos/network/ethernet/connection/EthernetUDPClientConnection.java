package br.unb.unbiquitous.ubiquitos.network.ethernet.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager.EthernetConnectionManager.EthernetConnectionType;
import br.unb.unbiquitous.ubiquitos.network.ethernet.udp.UdpAccept;
import br.unb.unbiquitous.ubiquitos.network.ethernet.udp.UdpChannel;
import br.unb.unbiquitous.ubiquitos.network.model.connection.ClientConnection;

/**
 * this class represents the ethernet client connection.
 * 
 * @author Lucas Lins
 */
public class EthernetUDPClientConnection extends ClientConnection {
	
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
	
	public EthernetUDPClientConnection(UdpChannel udpChannel, InetSocketAddress socketAddress, UdpAccept udpAccept) throws IOException{
		super(new EthernetDevice(socketAddress.getAddress().getHostName(), socketAddress.getPort(), EthernetConnectionType.UDP));
		this.peerAddress = socketAddress;
		this.udpChannel = udpChannel;
		this.udpChannel.connect(this.peerAddress, udpAccept.getInputStream(), udpAccept.getOutputStream());
	}
	
	public EthernetUDPClientConnection(String host, int port) throws IOException{
		super(new EthernetDevice(host, port, EthernetConnectionType.UDP));
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
