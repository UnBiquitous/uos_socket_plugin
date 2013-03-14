package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManagerException;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

public class TestAdaptabilityEngineBasicAuthentication extends TestCase {
	private static final String TEST_DATA_ECHO_SERVICE_PARAMETER_KEY = "message";

	private static final String TEST_DATA_DUMMY_DRIVER_ID = "dummyDriverId";
	
	private static final String TEST_DATA_DUMMY_DRIVER_INVALID_ID = "dummyDriverIdInvalid";

	private static final String TEST_DATA_ECHO_SERVICE = "echoService";

	private static final String TEST_DATA_DUMMY_DRIVER_NAME = "DummyDriver";
	
	private static final String TEST_DATA_DUMMY_DRIVER_INVALID_NAME = "DummyDriver";
	
	private static final String TEST_DATA_SECURITY_TYPE_VALID = "BASIC";

	private static final Logger logger = Logger.getLogger(TestAdaptabilityEngineNullProvider.class);
	
	private static UOSApplicationContext context;
	
	private static int testNumber = 0;
	
	private static final int timeToWaitBetweenTests = 100;
	
	protected UpDevice providerDevice;
	
	private Gateway gateway;
	
	protected void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests/2);
		logger.debug("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOSApplicationContext();
		context.init();
		Thread.sleep(timeToWaitBetweenTests/2);
		gateway = context.getGateway();
		
		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice");
		List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
		UpNetworkInterface nInf = new UpNetworkInterface();
		nInf.setNetType("Ethernet:TCP");
		nInf.setNetworkAddress("localhost:14984");
		networks.add(nInf);
		providerDevice.setNetworks(networks);
	};
	
	protected void tearDown() throws Exception {
		context.tearDown();
		System.gc();
	}
	
	public void testSendDechoRequestWithValidIntanceIdByServiceCall() throws ServiceCallException{
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver(TEST_DATA_DUMMY_DRIVER_NAME);
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_DUMMY_DRIVER_ID);
		serviceCall.setSecurityType(TEST_DATA_SECURITY_TYPE_VALID);
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		expectedResponse.setResponseData(responseMap);
		
		assertEquals(expectedResponse, response);
	}
	
	public void testSendDechoRequestWithValidIntanceIdByParameters() throws ServiceCallException{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		
		ServiceResponse response = gateway.callService(
				providerDevice, 
				TEST_DATA_ECHO_SERVICE, 
				TEST_DATA_DUMMY_DRIVER_NAME, 
				TEST_DATA_DUMMY_DRIVER_ID,
				TEST_DATA_SECURITY_TYPE_VALID,
				parameters);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		expectedResponse.setResponseData(responseMap);
		
		assertEquals(expectedResponse, response);
	}
	
	public void testSendDechoRequestWithInvalidIntanceIdByServiceCall() throws ServiceCallException{
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver(TEST_DATA_DUMMY_DRIVER_NAME);
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_DUMMY_DRIVER_INVALID_ID);
		serviceCall.setSecurityType(TEST_DATA_SECURITY_TYPE_VALID);
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		try {
			gateway.callService(providerDevice, serviceCall);
		} catch (ServiceCallException e) {
			assertTrue(e.getCause() instanceof DriverManagerException );
		}
		
	}
	
	public void testSendDechoRequestWithInvalidIntanceIdByParameters() throws ServiceCallException{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		
		try {
			gateway.callService(
					providerDevice, 
					TEST_DATA_ECHO_SERVICE, 
					TEST_DATA_DUMMY_DRIVER_NAME, 
					TEST_DATA_DUMMY_DRIVER_INVALID_ID, 
					TEST_DATA_SECURITY_TYPE_VALID,
					parameters);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof DriverManagerException );
		}
		
	}
	
	
	public void testSendDechoRequestWithoutInstanceIdAndValidDriverByServiceCall() throws ServiceCallException{
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver(TEST_DATA_DUMMY_DRIVER_NAME);
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(null);
		serviceCall.setSecurityType(TEST_DATA_SECURITY_TYPE_VALID);
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		expectedResponse.setResponseData(responseMap);
		
		assertEquals(expectedResponse, response);
	}
	
	public void testSendDechoRequestWithoutInstanceIdAndValidDriverByParameters() throws ServiceCallException{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		
		ServiceResponse response = gateway.callService(
				providerDevice, 
				TEST_DATA_ECHO_SERVICE, 
				TEST_DATA_DUMMY_DRIVER_NAME, 
				null, 
				TEST_DATA_SECURITY_TYPE_VALID,
				parameters);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		expectedResponse.setResponseData(responseMap);
		
		assertEquals(expectedResponse, response);
	}
	
	
	
	public void testSendDechoRequestWithoutInstanceIdAndInvalidDriverServiceCall() throws ServiceCallException{
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver(TEST_DATA_DUMMY_DRIVER_INVALID_NAME);
		serviceCall.setService(TEST_DATA_ECHO_SERVICE);
		serviceCall.setInstanceId(null);
		serviceCall.setSecurityType(TEST_DATA_SECURITY_TYPE_VALID);
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		try {
			gateway.callService(providerDevice, serviceCall);
		} catch (ServiceCallException e) {
			assertTrue(e.getCause() instanceof DriverManagerException );
		}
		
	}
	
	public void testSendDechoRequestWithoutInstanceIdAndInvalidDriverByParameters() throws ServiceCallException{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		
		try {
			gateway.callService(
					providerDevice, 
					TEST_DATA_ECHO_SERVICE, 
					TEST_DATA_DUMMY_DRIVER_INVALID_NAME, 
					null, 
					TEST_DATA_SECURITY_TYPE_VALID,
					parameters);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof DriverManagerException );
		}
		
	}
	
	public void testSendDechoRequestWithInvalidSecurityType() throws ServiceCallException{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		
		try {
			gateway.callService(
					providerDevice, 
					TEST_DATA_ECHO_SERVICE, 
					TEST_DATA_DUMMY_DRIVER_INVALID_NAME, 
					null, 
					TEST_DATA_SECURITY_TYPE_VALID,
					parameters);
		} catch (Exception e) {
			assertTrue(e instanceof ServiceCallException );
			assertNull(e.getCause());
		}
		
	}
}
