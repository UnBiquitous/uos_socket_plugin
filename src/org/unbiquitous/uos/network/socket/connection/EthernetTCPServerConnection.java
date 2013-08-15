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
public class EthernetTCPServerConnection extends ServerConnection {
	
	/************************************
	 * ATTRIBUTES
	 ************************************/

	private ServerSocket tcpSocket;
	
	/**
     * Controller responsible for the active connections cache. 
     */
    private CacheController cacheController;
	
	/************************************
	 * CONSTRUCTOR
	 * @param cacheController 
	 ************************************/
	public EthernetTCPServerConnection(SocketDevice serverDevice, CacheController cacheController) throws IOException{
		super(serverDevice);
		this.cacheController = cacheController;
		tcpSocket = new ServerSocket(serverDevice.getPort());
	}
	
	/************************************
	 * PUBLIC METHODS
	 ************************************/
	
	/**
	 * accept a client connection and return the {@link EthernetClientConnection} from this action. 
	 */
	public EthernetTCPClientConnection accept() throws IOException{
		return new EthernetTCPClientConnection(tcpSocket.accept(), null);
	}
	
	/**
	 * close the socket connection.
	 */
	public void closeConnection() throws IOException {
		tcpSocket.close();
	}
	
}
