package br.unb.unbiquitous.ubiquitos.uos.driver.deviceDriver;

import junit.framework.TestCase;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDaoHSQLDB;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

public class TestCompleteBasicAuthentication extends TestCase {
	
	private static final Logger logger = Logger.getLogger(TestCompleteBasicAuthentication.class);
	private static UOSApplicationContext context;
	private static int testNumber = 0;
	protected UpDevice providerDevice;
	private Gateway gateway;
	
	/** 
	 * Method executed before each test. It creates a new context of ubiquitOS and initializes the device
	 * for which the test will be executed.
	 * */
	protected void setUp() throws Exception {
		logger.debug("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOSApplicationContext();
		context.init("br/unb/unbiquitous/ubiquitos/uos/deviceManager/ubiquitos");
		gateway = context.getGateway();
		
		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice");
		
	};
	
	/** 
	 * Method that tear down the context after each test execution.
	 * */
	protected void tearDown() throws Exception {
		context.tearDown();
		System.gc();
	}
	
	/** 
	 * Method to test successful authentication. It inserts the device to be authenticated in database and creates 
	 * an instance of session key database. Then creates the serviceCall and call authentication service. The expected
	 * result is successful authentication.
	 * 
	 **/
	public void testSuccessfulAuthentication() throws Exception{
		
		// creates a new instance of devices database and inserts the device.
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("d7faf27c8a03382d73c1db4fe308210578cdaed4c8f6109a9fa9d17426b0feeb") == null){
			authenticationDao.insert("WrongDummyDevice", "d7faf27c8a03382d73c1db4fe308210578cdaed4c8f6109a9fa9d17426b0feeb", "5f8d93682477592c1479ee7803ac44e1");
		}
		if (authenticationDao.findByHashId("6cd4ce92874cba99a0b5ddfa9b31f3334a3a179d7b6c996f07fd66cc5a54642b") == null){
			authenticationDao.insert("LocalDummyDevice", "6cd4ce92874cba99a0b5ddfa9b31f3334a3a179d7b6c996f07fd66cc5a54642b", "5f8d93682477592c1479ee7803ac44e1");
		}
			
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		//sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		String SECURITY_TYPE = "BASIC";

		// creates a new instance of serviceCall and initializes their parameters
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setSecurityType(SECURITY_TYPE);
		serviceCall.setServiceType(ServiceCall.ServiceType.DISCRETE);
		serviceCall.setService("authenticate");
		serviceCall.setDriver("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver");

		logger.debug("Starts authentication proccess calling the service \"authenticate\"");
		
		// Call service
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		if (response.getResponseData().containsValue("true")){
			logger.debug("Authentication performed successfully. Service returned value \"true\"");
		} else{
			logger.debug("Authentication failure. Service returned value \"false\"");
		}
	}

	/** 
	 * Method to test unsuccessful authentication. The procedure is the same of the first test, 
	 * except that here the device name is incorrect. Is informed a name that is not in devices
	 * databaase. The expected result is unsuccessful authentication. It is verified in the end 
	 * of test, checking if the device name is in session keys database. If the authentication 
	 * did not complete the device name is not there.
	 * */
	public void testUnsuccessfulAuthentication_IncorrectDeviceName() throws Exception{

		// sets the device name with a name that is not in devices database.
		
		providerDevice.setName("WrongDummyDevice1");
		
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("d7faf27c8a03382d73c1db4fe308210578cdaed4c8f6109a9fa9d17426b0feeb") == null){
			authenticationDao.insert("WrongDummyDevice", "d7faf27c8a03382d73c1db4fe308210578cdaed4c8f6109a9fa9d17426b0feeb", "5f8d93682477592c1479ee7803ac44e1");
		}
		if (authenticationDao.findByHashId("6cd4ce92874cba99a0b5ddfa9b31f3334a3a179d7b6c996f07fd66cc5a54642b") == null){
			authenticationDao.insert("LocalDummyDevice", "6cd4ce92874cba99a0b5ddfa9b31f3334a3a179d7b6c996f07fd66cc5a54642b", "5f8d93682477592c1479ee7803ac44e1");
		}
			
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		// deletes all records from database. Then is possible to know that if the authentication
		// did not completed the device name will not be in session keys database.
		sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		String SECURITY_TYPE = "BASIC";

		// creates service call and initializes their parameters
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setSecurityType(SECURITY_TYPE);
		serviceCall.setServiceType(ServiceCall.ServiceType.DISCRETE);
		serviceCall.setService("authenticate");
		serviceCall.setDriver("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver");

		logger.debug("Starts authentication proccess calling the service \"authenticate\"");
		
		// Call service
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		if (response.getResponseData().containsValue("true")){
			logger.debug("Authentication performed successfully. Service returned value \"true\"");
		} else{
			logger.debug("Authentication failure. Service returned value \"false\"");
		}
	}

	
	/** 
	 * Method to test unsuccessful authentication. The procedure is the same of the first test, 
	 * except that here the device name is empty. The expected result is unsuccessful 
	 * authentication. It is verified in the end of test, checking if the device name is in 
	 * session keys database. If the authentication did not complete the device name is not there.
	 * */
	public void testUnsuccessfulAuthentication_EmptyDeviceName() throws Exception{

		// sets the device name with empty string.
		providerDevice.setName("");
		
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("d7faf27c8a03382d73c1db4fe308210578cdaed4c8f6109a9fa9d17426b0feeb") == null){
			authenticationDao.insert("WrongDummyDevice", "d7faf27c8a03382d73c1db4fe308210578cdaed4c8f6109a9fa9d17426b0feeb", "5f8d93682477592c1479ee7803ac44e1");
		}
		if (authenticationDao.findByHashId("6cd4ce92874cba99a0b5ddfa9b31f3334a3a179d7b6c996f07fd66cc5a54642b") == null){
			authenticationDao.insert("LocalDummyDevice", "6cd4ce92874cba99a0b5ddfa9b31f3334a3a179d7b6c996f07fd66cc5a54642b", "5f8d93682477592c1479ee7803ac44e1");
		}
			
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		// deletes all records from database. Then is possible to know that if the authentication
		// did not completed the device name will not be in session keys database.
		sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		String SECURITY_TYPE = "BASIC";

		// creates service call and initializes their parameters
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setSecurityType(SECURITY_TYPE);
		serviceCall.setServiceType(ServiceCall.ServiceType.DISCRETE);
		serviceCall.setService("authenticate");
		serviceCall.setDriver("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver");

		logger.debug("Starts authentication proccess calling the service \"authenticate\"");
		
		// Call service
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		if (response.getResponseData().containsValue("true")){
			logger.debug("Authentication performed successfully. Service returned value \"true\"");
		} else{
			logger.debug("Authentication failure. Service returned value \"false\"");
		}
	}	
}
