package org.unbiquitous.uos.network.socket;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.network.socket.connectionManager.TCPConnectionManager;
import org.unbiquitous.uos.network.socket.radar.MulticastRadar;

public class MulticastRadarSpike {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		UOS u = new UOS();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
					{"ubiquitos.message.response.timeout", "100"}, //Optional
					{"ubiquitos.message.response.retry", "30"},//Optional
					{"ubiquitos.connectionManager",
						TCPConnectionManager.class.getName() 
//						+ ',' +
//						UDPConnectionManager.class.getName()
						},
					{"ubiquitos.radar",
						MulticastRadar.class.getName()},
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
