package org.unbiquitous.uos.security;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;

import junit.framework.TestCase;

import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDaoHSQLDB;
import br.unb.unbiquitous.ubiquitos.authentication.messages.FirstMessage;

public class TestBasicAuthentication2 extends TestCase {
	
	private static final Logger logger = Logger.getLogger(TestBasicAuthentication2.class);
	
	private static UOSApplicationContext context;
	
	private static int testNumber = 0;
	
	protected UpDevice providerDevice;
	
	private Gateway gateway;
	
	protected void setUp() throws Exception {
		logger.debug("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOSApplicationContext();
		context.init();
		gateway = context.getGateway();
		
		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice");
		
	};
	
	protected void tearDown() throws Exception {
		context.tearDown();
		System.gc();
	}
	
	public void test1() throws Exception{
	
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler authentication = new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		FirstMessage firstMessage = authentication.runFirstStep("LocalDummyDevice", "5f8d93682477592c1479ee7803ac44e1");

		String SECURITY_TYPE = "BASIC";
		
		Map<String,Object> authenticationData = new HashMap<String,Object>();
		//String hashId, String idEncriptadoM1, String ra1EncriptadoM1, String ra2EncriptadoM1, String hmacM1
		authenticationData.put("hashId", firstMessage.getHashId());
		authenticationData.put("idEnc", firstMessage.getIdEnc());
		authenticationData.put("ra1Enc", firstMessage.getRa1Enc());
		authenticationData.put("ra2Enc", firstMessage.getRa2Enc());
		authenticationData.put("hmacM1", firstMessage.getHmacM1());

		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setParameters(authenticationData);
		serviceCall.setSecurityType(SECURITY_TYPE);
		serviceCall.setServiceType(ServiceCall.ServiceType.DISCRETE);
		serviceCall.setService("authenticate");
		serviceCall.setDriver("uos.DeviceDriver");

		gateway.callService(providerDevice, serviceCall);
	}
	
}
