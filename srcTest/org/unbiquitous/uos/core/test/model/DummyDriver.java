package org.unbiquitous.uos.core.test.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;


/**
 * Driver for testing the basic functions of the uOS.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DummyDriver implements UosDriver {
	
	private static final String MESSAGE_KEY = "message";
	
	/**
	 * Basic service wich receives a message through the ServiceCall parameter "message" and echoes this message
	 * in the ServiceResponse "message" parameter in its response Data 
	 */
	public void echoService(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext){
		
		Map<String,Object> parameters = serviceCall.getParameters();
		
		Map<String,Object> responseMap = new HashMap<String,Object>();
		
		responseMap.put(MESSAGE_KEY, parameters.get(MESSAGE_KEY));
		
		serviceResponse.setResponseData(responseMap);
		
	}

	@Override
	public UpDriver getDriver() {
		UpDriver driver = new UpDriver();
    	driver.setName("DummyDriver");
    	List<UpService> services = new ArrayList<UpService>();
    	
    	//populate services
    	
    	UpService listDrivers = new UpService();
    	listDrivers.setName("echoService");
    	Map<String, UpService.ParameterType> listDriversParameters = new HashMap<String, UpService.ParameterType>(); 
    	listDriversParameters.put(MESSAGE_KEY, UpService.ParameterType.MANDATORY);
    	listDrivers.setParameters(listDriversParameters);
    	
    	services.add(listDrivers);
    	
    	driver.setServices(services);
    	return driver;
	}

	@Override
	public void init(Gateway gateway, String instanceId) {
		// Do Nothing
	}

	@Override
	public void destroy() {
		// Do Nothing
	}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
