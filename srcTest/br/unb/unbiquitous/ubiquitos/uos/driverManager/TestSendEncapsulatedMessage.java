package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.io.IOException;
import java.net.UnknownHostException;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connection.EthernetTCPClientConnection;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.Base64;

public class TestSendEncapsulatedMessage extends TestSendMessage {
	
	private static Logger logger = Logger.getLogger(TestSendEncapsulatedMessage.class); 

	protected void connect() throws IOException{
		con = new EthernetTCPClientConnection("localhost",14984, null/*EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT*/);
	}
	
	@Override
	protected String sendReceive(String message) throws UnknownHostException,
			IOException, InterruptedException {
		
		logger.debug("Encapsulating message : "+message);
		String base64message = new String(Base64.encodeBase64(message.getBytes()));
		logger.debug("into message (base64) : "+base64message);
		
		
		String encapsulatedRequest = "{type:'ENCAPSULATED_MESSAGE',securityType:'BASIC',innerMessage:'"+base64message+"'}";
		
		String encapsulatedResponse = super.sendReceive(encapsulatedRequest);
		
		String base64Response = encapsulatedResponse.replace("{\"securityType\":\"BASIC\",\"type\":\"ENCAPSULATED_MESSAGE\",\"innerMessage\":\"", "");
		base64Response = base64Response.substring(0,base64Response.length()-2);
		
		logger.debug("Uncapsulating response (base64) : "+base64Response);
		String response = new String (Base64.decodeBase64(base64Response.getBytes()));
		logger.debug("into response  : "+response);
		return response ;
	}
	
}
