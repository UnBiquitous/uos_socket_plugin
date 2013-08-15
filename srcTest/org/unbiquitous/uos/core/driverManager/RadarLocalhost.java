package org.unbiquitous.uos.core.driverManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.radar.Radar;
import org.unbiquitous.uos.core.network.radar.RadarListener;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.connectionManager.SocketConnectionManager.EthernetConnectionType;


public class RadarLocalhost implements Radar {
	
	private static Logger logger = UOSLogging.getLogger();

	private RadarListener listenner;
	
	private boolean keepSearching = true;
	
	private List<NetworkDevice> deviceEnteredPool = new ArrayList<NetworkDevice>();
	private List<NetworkDevice> deviceLeftPool = new ArrayList<NetworkDevice>();
	
	private static RadarLocalhost singletonReference = null;
	
	/**
     * The connection manager responsible for handling the information of connections.
     */
    private ConnectionManager connectionManager;
	
	public RadarLocalhost(RadarListener listenner) {
		this.listenner = listenner;
		if (singletonReference != null){
			singletonReference.stopRadar();
		}
		singletonReference = this;
	}
	
	
	@Override
	public void startRadar() {}

	@Override
	public void stopRadar() {
		keepSearching = false;
	}

	@Override
	public void run() {
		try {
			logger.info("Starting Radar");
			
			while (keepSearching) {
				synchronized (deviceEnteredPool) {
					if (deviceEnteredPool != null
							&& !deviceEnteredPool.isEmpty()) {
						for (NetworkDevice nd : deviceEnteredPool) {
							logger.info("Registering Device");
							listenner.deviceEntered(nd);
						}
						deviceEnteredPool.clear();
						deviceEnteredPool.notifyAll();
					}
				}
				synchronized (deviceLeftPool) {
					if (deviceLeftPool != null && !deviceLeftPool.isEmpty()) {
						for (NetworkDevice nd : deviceLeftPool) {
							logger.info("UnRegistering Device");
							listenner.deviceLeft(nd);
						}
						deviceLeftPool.clear();
						deviceLeftPool.notifyAll();
					}
				}
			}
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,"",e);
			e.printStackTrace();
		}
	}
	
	public static void forceDeviceJoin(){
		synchronized (singletonReference.deviceEnteredPool) {
			SocketDevice device = new SocketDevice("0.0.0.0",15002,EthernetConnectionType.TCP); 
			logger.info("Creating a Device");
			singletonReference.deviceEnteredPool.add(device);
			
			try {
				singletonReference.deviceEnteredPool.wait();
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,"",e);
			}
		}
	}
	
	public static void forceDeviceLeft(){
		synchronized (singletonReference.deviceLeftPool) {
			SocketDevice device = new SocketDevice("0.0.0.0",15002,EthernetConnectionType.TCP); 
			logger.info("Creating a Device");
			singletonReference.deviceLeftPool.add(device);
			
			try {
				singletonReference.deviceLeftPool.wait();
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,"",e);
			}
		}
	}
	
	@Override
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
}
