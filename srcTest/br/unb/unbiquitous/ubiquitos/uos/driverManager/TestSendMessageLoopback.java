package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import br.unb.unbiquitous.ubiquitos.network.loopback.LoopbackDevice;
import br.unb.unbiquitous.ubiquitos.network.loopback.connection.LoopbackClientConnection;
import br.unb.unbiquitous.ubiquitos.network.loopback.connectionManager.LoopbackConnectionManager;

public class TestSendMessageLoopback extends TestSendMessage{

	
	@Override
	protected void connect() throws Exception {

		Thread.sleep(1000);
		con = new LoopbackClientConnection(new LoopbackDevice(), LoopbackConnectionManager.DEFAULT_ID);		
	}
	

}
