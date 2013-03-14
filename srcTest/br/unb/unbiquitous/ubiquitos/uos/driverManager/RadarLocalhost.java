package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.util.ArrayList;
import java.util.List;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManager;
import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager.EthernetConnectionManager.EthernetConnectionType;
import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;
import br.unb.unbiquitous.ubiquitos.network.radar.Radar;
import br.unb.unbiquitous.ubiquitos.network.radar.RadarListener;

public class RadarLocalhost implements Radar {
	
	private static Logger logger = Logger.getLogger(RadarLocalhost.class);

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
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	public static void forceDeviceJoin(){
		synchronized (singletonReference.deviceEnteredPool) {
			EthernetDevice device = new EthernetDevice("0.0.0.0",15002,EthernetConnectionType.TCP); 
			logger.info("Creating a Device");
			singletonReference.deviceEnteredPool.add(device);
			
			try {
				singletonReference.deviceEnteredPool.wait();
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}
	
	public static void forceDeviceLeft(){
		synchronized (singletonReference.deviceLeftPool) {
			EthernetDevice device = new EthernetDevice("0.0.0.0",15002,EthernetConnectionType.TCP); 
			logger.info("Creating a Device");
			singletonReference.deviceLeftPool.add(device);
			
			try {
				singletonReference.deviceLeftPool.wait();
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}
	
	@Override
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
}
