package br.unb.unbiquitous.ubiquitos.uos.test.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

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
		
		Map<String,String> parameters = serviceCall.getParameters();
		
		Map<String,String> responseMap = new HashMap<String,String>();
		
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
