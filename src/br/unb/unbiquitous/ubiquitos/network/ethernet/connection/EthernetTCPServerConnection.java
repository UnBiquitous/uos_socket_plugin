package br.unb.unbiquitous.ubiquitos.network.ethernet.connection;

import java.io.IOException;
import java.net.ServerSocket;

import br.unb.unbiquitous.ubiquitos.network.cache.CacheController;
import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.model.connection.ServerConnection;

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
	public EthernetTCPServerConnection(EthernetDevice serverDevice, CacheController cacheController) throws IOException{
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
