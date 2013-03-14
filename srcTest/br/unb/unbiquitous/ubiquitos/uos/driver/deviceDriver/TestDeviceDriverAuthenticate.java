package br.unb.unbiquitous.ubiquitos.uos.driver.deviceDriver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;

public class TestDeviceDriverAuthenticate extends TestCase {
	
	private static final Logger logger = Logger.getLogger(TestDeviceDriverAuthenticate.class);
	
	private static UOSApplicationContext context;
	
	private static int testNumber = 0;
	
	private static final int timeToWaitBetweenTests = 2000;
	
	protected void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests/2);
		logger.debug("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOSApplicationContext();
		context.init("br/unb/unbiquitous/ubiquitos/uos/deviceManager/ubiquitos");
		Thread.sleep(timeToWaitBetweenTests/2);
	};
	
	protected void tearDown() throws Exception {
		context.tearDown();
		System.gc();
	}
	
	public void testSendAuthenticate() 
		throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',service:'authenticate',parameters:{securityType:'BASIC'}}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		new JSONObject(response);
		
		
	}
	
	private static String sendReceive(String message) throws UnknownHostException, IOException, InterruptedException{
		Socket socket = new Socket("localhost",14984/*EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT*/);
		
		OutputStream outputStream = socket.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		
		writer.write(message);
		writer.write('\n');
		writer.flush();
		Thread.sleep(1000);
		
		InputStream inputStream = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		StringBuilder builder = new StringBuilder();
		if (reader.ready()){
        	for(Character c = (char)reader.read();c != '\n';c = (char)reader.read()){
        		builder.append(c);
        	}
		}
		socket.close();
		if (builder.length() == 0){
			return null;
		}
		return builder.toString(); 
	}
}
