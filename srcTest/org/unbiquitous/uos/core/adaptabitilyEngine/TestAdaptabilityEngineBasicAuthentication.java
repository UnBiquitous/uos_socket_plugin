package org.unbiquitous.uos.core.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

import junit.framework.TestCase;

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
		
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,Object> responseMap = new HashMap<String,Object>();
		responseMap.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		expectedResponse.setResponseData(responseMap);
		
		assertEquals(expectedResponse, response);
	}
	
	public void testSendDechoRequestWithValidIntanceIdByParameters() throws ServiceCallException{
		
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		
		ServiceResponse response = gateway.callService(
				providerDevice, 
				TEST_DATA_ECHO_SERVICE, 
				TEST_DATA_DUMMY_DRIVER_NAME, 
				TEST_DATA_DUMMY_DRIVER_ID,
				TEST_DATA_SECURITY_TYPE_VALID,
				parameters);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,Object> responseMap = new HashMap<String,Object>();
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
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		try {
			gateway.callService(providerDevice, serviceCall);
		} catch (ServiceCallException e) {
			assertTrue(e.getCause() instanceof DriverManagerException );
		}
		
	}
	
	public void testSendDechoRequestWithInvalidIntanceIdByParameters() throws ServiceCallException{
		
		Map<String,Object> parameters = new HashMap<String,Object>();
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
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,Object> responseMap = new HashMap<String,Object>();
		responseMap.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		expectedResponse.setResponseData(responseMap);
		
		assertEquals(expectedResponse, response);
	}
	
	public void testSendDechoRequestWithoutInstanceIdAndValidDriverByParameters() throws ServiceCallException{
		
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		
		ServiceResponse response = gateway.callService(
				providerDevice, 
				TEST_DATA_ECHO_SERVICE, 
				TEST_DATA_DUMMY_DRIVER_NAME, 
				null, 
				TEST_DATA_SECURITY_TYPE_VALID,
				parameters);
		
		ServiceResponse expectedResponse = new ServiceResponse();
		Map<String,Object> responseMap = new HashMap<String,Object>();
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
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put(TEST_DATA_ECHO_SERVICE_PARAMETER_KEY, "testMessage");
		serviceCall.setParameters(parameters);
		
		try {
			gateway.callService(providerDevice, serviceCall);
		} catch (ServiceCallException e) {
			assertTrue(e.getCause() instanceof DriverManagerException );
		}
		
	}
	
	public void testSendDechoRequestWithoutInstanceIdAndInvalidDriverByParameters() throws ServiceCallException{
		
		Map<String,Object> parameters = new HashMap<String,Object>();
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
		
		Map<String,Object> parameters = new HashMap<String,Object>();
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
