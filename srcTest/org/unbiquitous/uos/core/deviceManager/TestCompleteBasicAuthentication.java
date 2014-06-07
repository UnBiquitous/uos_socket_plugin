package org.unbiquitous.uos.core.deviceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.driverManager.DriverDao;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDaoHSQLDB;

@SuppressWarnings({"unchecked","rawtypes"})
public class TestCompleteBasicAuthentication extends TestCase {

	private static Logger logger = UOSLogging.getLogger();
	protected UOS context;
	protected DeviceManager deviceManager;
	protected DeviceDao deviceDao;
	protected DriverDao remoteDriverDao;
	protected static long currentTest = 0;
	
	//
	protected UpDevice providerDevice;
	private SmartSpaceGateway gateway;
	
	/** 
	 * Method executed before each test. It creates a new context of ubiquitOS and initializes the device
	 * for which the test will be executed.
	 * */
	protected void setUp() throws Exception {
		logger.info("\n");
		logger.info("============== Teste : "+currentTest+++" ==========================");
		logger.info("\n");
		
		context = new UOS();
		context.start();
		
		deviceManager = context.getFactory().gateway().getDeviceManager();
		
		gateway = (SmartSpaceGateway) context.getGateway();
		deviceDao = gateway.getDeviceManager().getDeviceDao();
		remoteDriverDao = gateway.getDriverManager().getDriverDao();
		
		Thread.sleep(100);
	}

	
	/** 
	 * Method that tear down the context after each test execution.
	 * */
	protected void tearDown() throws Exception {
		context.stop();
		System.gc();
	}
	
	/** 
	 * Method to test successful authentication. It inserts the device to be authenticated in database and creates 
	 * an instance of session key database. Then creates the serviceCall and call authentication service. The expected
	 * result is successful authentication.
	 **/
	public void testSuccessfulAuthentication() throws Exception {

		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice");
		providerDevice.setNetworks(gateway.getCurrentDevice().getNetworks());
		providerDevice.getNetworks().get(0).setNetworkAddress("localhost");

		String DEVICE_NAME = "WrongDummyDevice";
		String SECURITY_TYPE = "BASIC";
		String TEST_DATA_ECHO_SERVICE = "echoService";
		String TEST_DATA_DUMMY_DRIVER_ID = "dummyDriverId";
		String TEST_DATA_ECHO_SERVICE_PARAMETER_KEY = "message";
		
		// inserts the device in devices database
		deviceDao.save(providerDevice);
		
		// check if the device is correctly stored
		UpDevice returned = deviceDao.find(DEVICE_NAME);
		assertNotNull("error while saving device", returned);
				
		// creates a new instance of devices database and inserts the device.
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("ccabd4cf9dff07fa7bede47764aa114fd551652d") == null){
			authenticationDao.insert("WrongDummyDevice", "ccabd4cf9dff07fa7bede47764aa114fd551652d", "5f8d93682477592c1479ee7803ac44e1");
		} 
		
// para bluetooth basta ir até aqui. Esperar mensagem neste ponto		
		
		
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		//sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		// creates a new instance of serviceCall and initializes their parameters
		Call serviceCall = new Call();
		serviceCall.setDriver("uos.DeviceDriver");
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_DUMMY_DRIVER_ID);//verificar isso
		serviceCall.setSecurityType(SECURITY_TYPE);
		//serviceCall.setServiceType(ServiceCall.ServiceType.DISCRETE); //verificar se é necessário

		// defines parameters for the service call
		Map parameters = new HashMap();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);

		logger.fine("Starts authentication proccess calling the service \"echoService\"");
		
		// Call service
		Response response = gateway.callService(providerDevice, serviceCall);
		
		// creates a service response to compare the obtained response to expected response
		Response expectedResponse = new Response();
		Map responseMap = new HashMap();
		responseMap.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		expectedResponse.setResponseData(responseMap);
		
		// checks the service response
		assertEquals(expectedResponse, response);
		
		if (response.getResponseData().containsValue("true")){
			logger.fine("Authentication performed successfully. Service returned value \"true\"");
		} else if (response.getResponseData().containsValue("false")){
			logger.fine("Authentication failure. Service returned value \"false\"");
		} else{
			logger.fine("resposta retornada: "+response.getResponseData().values());
		}
	}
	
	/** 
	 * Method to test unsuccessful authentication. The procedure is the same of the first test, 
	 * except that here the device name is incorrect. Is informed a name that is not in devices
	 * databaase. The expected result is unsuccessful authentication. It is verified in the end 
	 * of test, checking if the device name is in session keys database. If the authentication 
	 * did not complete the device name is not there.
	 * */
	public void testAuthenticationWithInvalidId() throws Exception {

		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice2");
		providerDevice.setNetworks(gateway.getCurrentDevice().getNetworks());
		providerDevice.getNetworks().get(0).setNetworkAddress("localhost");

		
		String DEVICE_NAME = "WrongDummyDevice2";
		String SECURITY_TYPE = "BASIC";
		String TEST_DATA_ECHO_SERVICE = "echoService";
		String TEST_DATA_DUMMY_DRIVER_ID = "dummyDriverId";
		String TEST_DATA_ECHO_SERVICE_PARAMETER_KEY = "message";
		
		// inserts the device in devices database
		deviceDao.save(providerDevice);
		
		// check if the device is correctly stored
		UpDevice returned = deviceDao.find(DEVICE_NAME);
		assertNotNull("error while saving device", returned);
		
		// creates a new instance of devices database and inserts the device.
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("ccabd4cf9dff07fa7bede47764aa114fd551652d") == null){
			authenticationDao.insert("WrongDummyDevice", "ccabd4cf9dff07fa7bede47764aa114fd551652d", "12345abcde43215bdcca54321fdeab12");
		} else{
			authenticationDao.delete("WrongDummyDevice");
			authenticationDao.insert("WrongDummyDevice", "ccabd4cf9dff07fa7bede47764aa114fd551652d", "12345abcde43215bdcca54321fdeab12");
		}
		
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		//sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		// creates a new instance of serviceCall and initializes their parameters
		Call serviceCall = new Call();
		serviceCall.setDriver("uos.DeviceDriver");
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_DUMMY_DRIVER_ID);//verificar isso
		serviceCall.setSecurityType(SECURITY_TYPE);

		// defines parameters for the service call
		Map parameters = new HashMap();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);

		logger.fine("Starts authentication proccess calling the service \"echoService\"");
		
		// Call service
		Response response = gateway.callService(providerDevice, serviceCall);
		
		// checks the service response
		assertNull(response);

	}

	/** 
	 * Method to test unsuccessful authentication. The procedure is the same of the first test, 
	 * except that here the device name is empty. The expected result is unsuccessful 
	 * authentication. It is verified in the end of test, checking if the device name is in 
	 * session keys database. If the authentication did not complete the device name is not there.
	 * */
	public void testAuthenticationWithEmptyId() throws Exception {

		providerDevice = new UpDevice();
		providerDevice.setName("");
		providerDevice.setNetworks(gateway.getCurrentDevice().getNetworks());
		providerDevice.getNetworks().get(0).setNetworkAddress("localhost");

		String DEVICE_NAME = "";
		String SECURITY_TYPE = "BASIC";
		String TEST_DATA_ECHO_SERVICE = "echoService";
		String TEST_DATA_DUMMY_DRIVER_ID = "dummyDriverId";
		String TEST_DATA_ECHO_SERVICE_PARAMETER_KEY = "message";
		
		// inserts the device in devices database
		deviceDao.save(providerDevice);
		
		// check if the device is correctly stored
		UpDevice returned = deviceDao.find(DEVICE_NAME);
		assertNotNull("error while saving device", returned);
		
		// creates a new instance of devices database and inserts the device.
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("ccabd4cf9dff07fa7bede47764aa114fd551652d") == null){
			authenticationDao.insert("WrongDummyDevice", "ccabd4cf9dff07fa7bede47764aa114fd551652d", "12345abcde43215bdcca54321fdeab12");
		} else{
			authenticationDao.delete("WrongDummyDevice");
			authenticationDao.insert("WrongDummyDevice", "ccabd4cf9dff07fa7bede47764aa114fd551652d", "12345abcde43215bdcca54321fdeab12");
		}
		
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		//sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		// creates a new instance of serviceCall and initializes their parameters
		Call serviceCall = new Call();
		serviceCall.setDriver("uos.DeviceDriver");
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_DUMMY_DRIVER_ID);//verificar isso
		serviceCall.setSecurityType(SECURITY_TYPE);

		// defines parameters for the service call
		Map parameters = new HashMap();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);

		logger.fine("Starts authentication proccess calling the service \"echoService\"");
		
		// Call service
		Response response = gateway.callService(providerDevice, serviceCall);
		
		// checks the service response
		assertNull(response);

	}
	
	public void testAuthenticationWithInvalidPassword() throws Exception {

		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice1");
		providerDevice.setNetworks(gateway.getCurrentDevice().getNetworks());
		providerDevice.getNetworks().get(0).setNetworkAddress("localhost");

		String DEVICE_NAME = "WrongDummyDevice1";
		String SECURITY_TYPE = "BASIC";
		String TEST_DATA_ECHO_SERVICE = "echoService";
		String TEST_DATA_DUMMY_DRIVER_ID = "dummyDriverId";
		String TEST_DATA_ECHO_SERVICE_PARAMETER_KEY = "message";
		
		// inserts the device in devices database
		deviceDao.save(providerDevice);
		
		// check if the device is correctly stored
		UpDevice returned = deviceDao.find(DEVICE_NAME);
		assertNotNull("error while saving device", returned);
		
		// creates a new instance of devices database and inserts the device.
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("cfd3d42fff9866dafa6b5d0f4de0fd04978256da") == null){
			authenticationDao.insert("WrongDummyDevice1", "cfd3d42fff9866dafa6b5d0f4de0fd04978256da", "123d93682577592c1479ee7803ac44e1");
		}
		
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		//sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		// creates a new instance of serviceCall and initializes their parameters
		Call serviceCall = new Call();
		serviceCall.setDriver("uos.DeviceDriver");
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_DUMMY_DRIVER_ID);//verificar isso
		serviceCall.setSecurityType(SECURITY_TYPE);

		// defines parameters for the service call
		Map parameters = new HashMap();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);

		logger.fine("Starts authentication proccess calling the service \"echoService\"");
		
		// Call service
		Response response = gateway.callService(providerDevice, serviceCall);
		
		// checks the service response
		assertNull(response);

	}

	
	public void testAuthenticationWithEmptyPassword() throws Exception {
		
		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice3");
		providerDevice.setNetworks(gateway.getCurrentDevice().getNetworks());
		providerDevice.getNetworks().get(0).setNetworkAddress("localhost");

		String DEVICE_NAME = "WrongDummyDevice3";
		String SECURITY_TYPE = "BASIC";
		String TEST_DATA_ECHO_SERVICE = "echoService";
		String TEST_DATA_DUMMY_DRIVER_ID = "dummyDriverId";
		String TEST_DATA_ECHO_SERVICE_PARAMETER_KEY = "message";
		
		// inserts the device in devices database
		deviceDao.save(providerDevice);
		
		// check if the device is correctly stored
		UpDevice returned = deviceDao.find(DEVICE_NAME);
		assertNotNull("error while saving device", returned);
		
		// creates a new instance of devices database and inserts the device.
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("9e8e2b7ecaaf1f11d0835d627a201b92fce176a0") == null){
			authenticationDao.insert("WrongDummyDevice3", "9e8e2b7ecaaf1f11d0835d627a201b92fce176a0", "");
		}
		
		// creates a new instance of session keys database
		SessionKeyDaoHSQLDB sessionKeyDao = new SessionKeyDaoHSQLDB(); 
		//sessionKeyDao.deleteAll();
		
		// creates a instance of authenticationHandler
		new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);

		// creates a new instance of serviceCall and initializes their parameters
		Call serviceCall = new Call();
		serviceCall.setDriver("uos.DeviceDriver");
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_DUMMY_DRIVER_ID);//verificar isso
		serviceCall.setSecurityType(SECURITY_TYPE);

		// defines parameters for the service call
		Map parameters = new HashMap();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);

		logger.fine("Starts authentication proccess calling the service \"echoService\"");
		
		// Call service
		Response response = gateway.callService(providerDevice, serviceCall);
		
		// checks the service response
		assertNull(response);
	}

	
	
}
