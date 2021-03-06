package org.unbiquitous.uos.network.socket.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.cache.CachableConnection;
import org.unbiquitous.uos.core.network.cache.CacheController;
import org.unbiquitous.uos.core.network.cache.CachedConnectionData;
import org.unbiquitous.uos.core.network.cache.CachedInputStream;
import org.unbiquitous.uos.core.network.cache.CachedOutputStream;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.connectionManager.SocketConnectionManager.EthernetConnectionType;


/**
 * this class represents the ethernet client connection.
 * 
 * @author Lucas Lins
 */
public class TCPClientConnection extends ClientConnection implements CachableConnection {
	
	private static final Logger logger = UOSLogging.getLogger();

	/************************************
	 * ATTRIBUTES
	 ************************************/

	private Socket tcpSocket;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;

	protected CacheController cacheController;
	protected CachedConnectionData cachedConnectionData;

	/**********************************
	 * Constructors
	 **********************************/

	public TCPClientConnection(Socket tcpSocket, CacheController cacheController) throws IOException{
		super(new SocketDevice(tcpSocket.getInetAddress().getHostAddress(), tcpSocket.getPort(), EthernetConnectionType.TCP));
		this.tcpSocket = tcpSocket;
		this.cacheController = cacheController;
		if (cacheController != null){
			this.cachedConnectionData = cacheController.addConnection(this);
		}
		createInputStream();
		createOutputStream();
	}

	public TCPClientConnection(String host, int port, CacheController cacheController) throws IOException{
		super(new SocketDevice(host, port, EthernetConnectionType.TCP));
		this.tcpSocket = new Socket(host, port);
		this.cacheController = cacheController;
		if (cacheController != null){
			this.cachedConnectionData = cacheController.addConnection(this);
		}
		createInputStream();
		createOutputStream();
	}


	/************************************
	 * PUBLIC METHODS
	 ************************************/

	public boolean isConnected() {
		return !tcpSocket.isClosed() && tcpSocket.isBound() && tcpSocket.isConnected();
	}
	
	/**
	 * method to get the input stream of the ethernet connection.
	 */
	public DataInputStream getDataInputStream() throws IOException {

//		createInputStream();
		return dataInputStream;
	}

	private void createInputStream() throws IOException {
		if(dataInputStream == null){	
			if (cacheController != null){
				dataInputStream = new CachedInputStream(tcpSocket.getInputStream(),cachedConnectionData);
			}else{
				dataInputStream = new DataInputStream(tcpSocket.getInputStream());
			}
		}
	}

	/**
	 * method to get the output stream of the ethernet connection
	 */
	public DataOutputStream getDataOutputStream() throws IOException {
		return dataOutputStream;
	}

	private void createOutputStream() throws IOException {
		if(dataOutputStream == null){
			if (cacheController != null){
				dataOutputStream = new CachedOutputStream(tcpSocket.getOutputStream(),cachedConnectionData);
			}else{
				dataOutputStream = new DataOutputStream(tcpSocket.getOutputStream());
			}
		}
	}

	/**
	 * Method to close connection.
	 */
	public void closeConnection() throws IOException {
		try{
			if (dataInputStream != null) 
				dataInputStream.close();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"",e);
		}
		try{
			if (dataOutputStream != null)
				dataOutputStream.close();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"",e);
		}
		try{
			if (tcpSocket != null){
				if (cacheController == null){
					tcpSocket.close();
				}else{
					logger.info("Since this is a cached connection, it'll not be closed yet. (Connection from Device '"+getClientDevice().getNetworkDeviceName()+"')");
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,"",e);
		}
	}

	@Override
	public void tearDown() throws IOException {
		tcpSocket.close();	
		logger.info("Connection from device '" + getClientDevice().getNetworkDeviceName() + "' was closed.");
	}
}
