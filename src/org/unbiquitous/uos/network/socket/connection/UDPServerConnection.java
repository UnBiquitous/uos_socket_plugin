package org.unbiquitous.uos.network.socket.connection;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.unbiquitous.uos.core.network.model.connection.ServerConnection;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.udp.UdpAccept;
import org.unbiquitous.uos.network.socket.udp.UdpChannel;


/**
 * this class represents the ethernet server connection.
 * 
 * @author Lucas Lins
 */
public class UDPServerConnection extends ServerConnection {
	
	/************************************
	 * ATTRIBUTES
	 ************************************/

	private UdpChannel udpChannel;
	
	/************************************
	 * CONSTRUCTOR
	 ************************************/
	
	public UDPServerConnection(SocketDevice serverDevice) throws IOException{
		super(serverDevice);
		udpChannel = UdpChannel.openChannel(serverDevice.getPort());
	}
	
	/************************************
	 * PUBLIC METHODS
	 ************************************/
	
	/**
	 * accept a client connection and return the {@link UDPClientConnection} from this action. 
	 */
	public UDPClientConnection accept() throws IOException{
		UdpAccept udpAccept = udpChannel.accept();
		return new UDPClientConnection(udpChannel, (InetSocketAddress)udpAccept.getSocketAddress(), udpAccept);
	}
	
	/**
	 * close the socket connection.
	 */
	public void closeConnection() throws IOException {
		udpChannel.closeServer();
	}
	
}
