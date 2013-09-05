package org.unbiquitous.uos.security;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.messageEngine.messages.Call;

import junit.framework.TestCase;
import br.unb.unbiquitous.ubiquitos.authentication.AuthenticationDao;
import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDao;
import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDaoHSQLDB;
import br.unb.unbiquitous.ubiquitos.authentication.messages.FirstMessage;
import br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB;

public class TestBasicAuthenticationHandler extends TestCase {

	public void testeSucessoAutenticacao() throws Exception {

		UOS context = new UOS();
		context.init();
		Gateway gateway = context.getGateway();
	
		String SECURITY_TYPE = "BASIC";
	
		AuthenticationDaoHSQLDB authenticationDaoHSQLDB = new AuthenticationDaoHSQLDB(); 
		AuthenticationDao authenticationDao = authenticationDaoHSQLDB;
		SessionKeyDaoHSQLDB sessionKeyDaoHSQLDB = new SessionKeyDaoHSQLDB(); 
		SessionKeyDao sessionKeyDao = sessionKeyDaoHSQLDB;
		
		br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler authentication;
		authentication = new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);
		FirstMessage firstMessage = authentication.runFirstStep("LocalDummyDevice", "5f8d93682477592c1479ee7803ac44e1");
	
		Call serviceCall = new Call();
		
		Map<String,Object> authenticationData = new HashMap<String,Object>();
		//String hashId, String idEncriptadoM1, String ra1EncriptadoM1, String ra2EncriptadoM1, String hmacM1
		authenticationData.put("hashId", firstMessage.getHashId());
		authenticationData.put("idEnc", firstMessage.getIdEnc());
		authenticationData.put("ra1Enc", firstMessage.getRa1Enc());
		authenticationData.put("ra2Enc", firstMessage.getRa2Enc());
		authenticationData.put("hmacM1", firstMessage.getHmacM1());
		
		serviceCall.setParameters(authenticationData);
		serviceCall.setSecurityType(SECURITY_TYPE);
		serviceCall.setServiceType(Call.ServiceType.DISCRETE);
		serviceCall.setService("authenticate");
		serviceCall.setDriver("uos.DeviceDriver");
		
		
		gateway.callService(gateway.getCurrentDevice(), serviceCall);

	}
	
}
