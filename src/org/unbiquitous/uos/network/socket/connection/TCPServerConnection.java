package org.unbiquitous.uos.network.socket.connection;

import java.io.IOException;
import java.net.ServerSocket;

import org.unbiquitous.uos.core.network.cache.CacheController;
import org.unbiquitous.uos.core.network.model.connection.ServerConnection;
import org.unbiquitous.uos.network.socket.SocketDevice;


/**
 * this class represents the ethernet server connection.
 * 
 * @author Lucas Lins
 */
public class TCPServerConnection extends ServerConnection {
	
	/************************************
	 * ATTRIBUTES
	 ************************************/

	private ServerSocket tcpSocket;
	
	/************************************
	 * CONSTRUCTOR
	 * @param cacheController 
	 ************************************/
	public TCPServerConnection(SocketDevice serverDevice, CacheController cacheController) throws IOException{
		super(serverDevice);
		tcpSocket = new ServerSocket(serverDevice.getPort());
		int FIVE_MINUTES_IN_MILLIS = 5*60*1000;
		tcpSocket.setSoTimeout(FIVE_MINUTES_IN_MILLIS);
	}
	
	/************************************
	 * PUBLIC METHODS
	 ************************************/
	
	/**
	 * accept a client connection and return the {@link EthernetClientConnection} from this action. 
	 */
	public TCPClientConnection accept() throws IOException{
		return new TCPClientConnection(tcpSocket.accept(), null);
	}
	
	/**
	 * close the socket connection.
	 */
	public void closeConnection() throws IOException {
		tcpSocket.close();
	}
	
}
