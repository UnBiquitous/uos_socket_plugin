package org.unbiquitous.uos.network.socket;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.network.socket.connectionManager.EthernetTCPConnectionManager;
import org.unbiquitous.uos.network.socket.connectionManager.EthernetUDPConnectionManager;
import org.unbiquitous.uos.network.socket.radar.EthernetPingRadar;

public class RadarSpike {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		UOSApplicationContext u = new UOSApplicationContext();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
					{"ubiquitos.message.response.timeout", "100"}, //Optional
					{"ubiquitos.message.response.retry", "30"},//Optional
					{"ubiquitos.connectionManager",
						EthernetTCPConnectionManager.class.getName() + ',' +
						EthernetUDPConnectionManager.class.getName()},
					{"ubiquitos.radar",
						EthernetPingRadar.class.getName()},
					{"ubiquitos.eth.tcp.port", "14984"}, 
					{"ubiquitos.eth.tcp.passivePortRange", "14985-15000"}, 
					{"ubiquitos.eth.udp.port", "15001"}, 
					{"ubiquitos.eth.udp.passivePortRange", "15002-15017"}, 
		        };
			}
		};
		u.init(prop);
	}

}
