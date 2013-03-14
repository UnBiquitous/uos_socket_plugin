package br.unb.unbiquitous.ubiquitos.network.ethernet.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.network.cache.CachableConnection;
import br.unb.unbiquitous.ubiquitos.network.cache.CacheController;
import br.unb.unbiquitous.ubiquitos.network.cache.CachedConnectionData;
import br.unb.unbiquitous.ubiquitos.network.cache.CachedInputStream;
import br.unb.unbiquitous.ubiquitos.network.cache.CachedOutputStream;
import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager.EthernetConnectionManager.EthernetConnectionType;
import br.unb.unbiquitous.ubiquitos.network.model.connection.ClientConnection;

/**
 * this class represents the ethernet client connection.
 * 
 * @author Lucas Lins
 */
public class EthernetTCPClientConnection extends ClientConnection implements CachableConnection {
	
	private static final Logger logger = Logger.getLogger(EthernetTCPClientConnection.class);

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

	public EthernetTCPClientConnection(Socket tcpSocket, CacheController cacheController) throws IOException{
		super(new EthernetDevice(tcpSocket.getInetAddress().getHostAddress(), tcpSocket.getPort(), EthernetConnectionType.TCP));
		this.tcpSocket = tcpSocket;
		this.cacheController = cacheController;
		if (cacheController != null){
			this.cachedConnectionData = cacheController.addConnection(this);
		}
		createInputStream();
		createOutputStream();
	}

	public EthernetTCPClientConnection(String host, int port, CacheController cacheController) throws IOException{
		super(new EthernetDevice(host, port, EthernetConnectionType.TCP));
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
		return !tcpSocket.isClosed();
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
		
		logger.info(tcpSocket.getInetAddress()+" OutPut is:"+dataOutputStream);
		
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
			logger.error(e);
		}
		try{
			if (dataOutputStream != null)
				dataOutputStream.close();
		} catch (Exception e) {
			logger.error(e);
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
			logger.error(e);
		}
	}

	@Override
	public void tearDown() throws IOException {
		tcpSocket.close();	
		logger.info("Connection from device '" + getClientDevice().getNetworkDeviceName() + "' was closed.");
	}

}
