package org.unbiquitous.uos.core.driver.deviceDriver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.UOSApplicationContext;

import junit.framework.TestCase;
import br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler;
import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDaoHSQLDB;
import br.unb.unbiquitous.ubiquitos.authentication.messages.FirstMessage;
import br.unb.unbiquitous.ubiquitos.authentication.messages.SecondMessage;
import br.unb.unbiquitous.ubiquitos.authentication.messages.ThirdMessage;
import br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB;

public class TestBasicMiddlewareAuthenticationHandler extends TestCase {
	
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

	public void testSendSuccessfulAuthenticate_Second_Step() 
	throws UnknownHostException, IOException, InterruptedException, JSONException, Exception {

		AuthenticationDaoHSQLDB authenticationDao = new AuthenticationDaoHSQLDB(); 
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		sessionKeyDao.deleteAll();
		
		AuthenticationHandler authentication = new AuthenticationHandler(authenticationDao, sessionKeyDao);
		
		FirstMessage m1 = authentication.runFirstStep("WrongDummyDevice", "5f8d93682477592c1479ee7803ac44e1");
	
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',service:'authenticate',parameters:{securityType:'BASIC','hashId':'" +m1.getHashId()+ "','idEnc':'" +m1.getIdEnc()+ "','ra1Enc':'" +m1.getRa1Enc()+ "','ra2Enc':'" +m1.getRa2Enc() + "','hmacM1':'" +m1.getHmacM1()+ "'}}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		new JSONObject(response);
	
		SecondMessage m2 = authentication.runSecondStep(m1.getHashId(), m1.getIdEnc(), m1.getRa1Enc(), m1.getRa2Enc(), m1.getHmacM1());
		
		//checks returned message. If the return is equals the return of m2, the test is successful
		//checks ra1IncEnc
		int beginIndex = response.indexOf("ra1IncEnc") + "ra1IncEnc".length()+3;
		int endIndex = beginIndex+44;
		assertEquals(m2.getRa1IncEnc(), response.substring(beginIndex, endIndex));
		//checks ra2IncEnc
		beginIndex = response.indexOf("ra2IncEnc") + "ra2IncEnc".length()+3;
		endIndex = beginIndex+44;
		assertEquals(m2.getRa2IncEnc(), response.substring(beginIndex, endIndex));
		//checks idEnc
		beginIndex = response.indexOf("idEnc") + "idEnc".length()+3;
		endIndex = beginIndex+24;
		assertEquals(m2.getIdEnc(), response.substring(beginIndex, endIndex));

	}
	
	public void testSendSuccessfulAuthenticate_Fourth_Step() 
		throws UnknownHostException, IOException, InterruptedException, JSONException, Exception {
		
		AuthenticationDaoHSQLDB authenticationDao = new AuthenticationDaoHSQLDB(); 
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		sessionKeyDao.deleteAll();
		
		AuthenticationHandler authentication = new AuthenticationHandler(authenticationDao, sessionKeyDao);
		
		FirstMessage m1 = authentication.runFirstStep("WrongDummyDevice", "5f8d93682477592c1479ee7803ac44e1");
		SecondMessage m2 = authentication.runSecondStep(m1.getHashId(), m1.getIdEnc(), m1.getRa1Enc(), m1.getRa2Enc(), m1.getHmacM1());
		ThirdMessage m3 = authentication.runThirdStep("5f8d93682477592c1479ee7803ac44e1", m1.getRa1(), m1.getRa2(), "WrongDummyDevice", m2.getHmac(), m2.getIdEnc(), m2.getRa1IncEnc(), m2.getRa2IncEnc(), m2.getRb1Enc(), m2.getRb2Enc()); 
		
		String sessionKeyEnc = m3.getSessionKeyEnc();
		String hmacM3 = m3.getHmac();
		String id = "WrongDummyDevice";

		String rb1 = (m2.getRb1());
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',service:'authenticate',parameters:{securityType:'BASIC','sessionKeyEnc':'" +sessionKeyEnc+ "','hmacM3':'" +hmacM3+ "','id':'" +id+ "','rb1':'" +rb1+ "'}}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		new JSONObject(response);

		assertTrue(response.contains("true"));
	}
	
	
	public void testSendUnsuccessfulAuthenticate_Second_Step() 
	throws UnknownHostException, IOException, InterruptedException, JSONException, Exception {

	AuthenticationDaoHSQLDB authenticationDao = new AuthenticationDaoHSQLDB(); 
	SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
	sessionKeyDao.deleteAll();
	
	AuthenticationHandler authentication = new AuthenticationHandler(authenticationDao, sessionKeyDao);
	
	FirstMessage m1 = authentication.runFirstStep("WrongDummyDevice", "5f8d93682477592c1479ee7803ac44e1");
	//SecondMessage m2 = authentication.runSecondStep(m1.getHashId(), m1.getIdEnc(), m1.getRa1Enc(), m1.getRa2Enc(), m1.getHmacM1());

	// send notifyMessage with incorrect ra2Enc
	String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',service:'authenticate',parameters:{securityType:'BASIC','hashId':'" +m1.getHashId()+ "','idEnc':'" +m1.getIdEnc()+ "','ra1Enc':'" +m1.getRa1Enc()+ "','ra2Enc':'" +"yVJTzuEJI07h9Gj45bWaakbt/LyXLHOuVeE1NIZUnIw=" + "','hmacM1':'" +m1.getHmacM1()+ "'}}";
	
	logger.debug("Sending Message:");
	logger.debug(notifyMessage);
	
	String response = sendReceive(notifyMessage);
	
	logger.debug("Returned Message:");
	logger.debug("["+response+"]");
	
	new JSONObject(response);
}
	

	public void testSendUnsuccessfulAuthenticate_Fourth_Step() 
	throws UnknownHostException, IOException, InterruptedException, JSONException, Exception {
	
	AuthenticationDaoHSQLDB authenticationDao = new AuthenticationDaoHSQLDB(); 
	SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
	sessionKeyDao.deleteAll();
	
	AuthenticationHandler authentication = new AuthenticationHandler(authenticationDao, sessionKeyDao);

	
	FirstMessage m1 = authentication.runFirstStep("WrongDummyDevice", "5f8d93682477592c1479ee7803ac44e1");
	SecondMessage m2 = authentication.runSecondStep(m1.getHashId(), m1.getIdEnc(), m1.getRa1Enc(), m1.getRa2Enc(), m1.getHmacM1());
	ThirdMessage m3 = authentication.runThirdStep("5f8d93682477592c1479ee7803ac44e1", m1.getRa1(), m1.getRa2(), "WrongDummyDevice", m2.getHmac(), m2.getIdEnc(), m2.getRa1IncEnc(), m2.getRa2IncEnc(), m2.getRb1Enc(), m2.getRb2Enc()); 

	
	String sessionKeyEnc = m3.getSessionKeyEnc();
	m3.getHmac();
	String id = "WrongDummyDevice";

	String rb1 = (m2.getRb1());
	
	//send notifyMessage with incorrect HMAC
	String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',service:'authenticate',parameters:{securityType:'BASIC','sessionKeyEnc':'" +sessionKeyEnc+ "','hmacM3':'" +"758F3AB2EB34658ED3A011A51B695E4A"+ "','id':'" +id+ "','rb1':'" +rb1+ "'}}";
	
	logger.debug("Sending Message:");
	logger.debug(notifyMessage);
	
	String response = sendReceive(notifyMessage);
	
	logger.debug("Returned Message:");
	logger.debug("["+response+"]");
	
	new JSONObject(response);
	
	assertTrue(response.contains("false"));
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
