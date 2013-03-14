package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.io.IOException;

import br.unb.unbiquitous.ubiquitos.network.ethernet.connection.EthernetUDPClientConnection;

public class TestSendMessageUDP extends TestSendMessage {

	@Override
	protected void connect() throws IOException{
		con = new EthernetUDPClientConnection("localhost",15001/*EthernetUDPConnectionManager.UBIQUITOS_ETH_UDP_PORT*/);
	}

}
