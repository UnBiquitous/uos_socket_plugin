package br.unb.unbiquitous.ubiquitos.uos.test.model;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.List;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService.ParameterType;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

public class MouseDriver implements UosDriver {

	private static final String VALUE_KEY = "value";

	private static final String Y_VALUE_KEY = "yValue";

	private static final String X_VALUE_KEY = "xValue";

	
	private Robot robot; 
	
	
	public void movePointer(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext){
		
		Map<String,String> parameters = serviceCall.getParameters();
		
		String xValue = parameters.get(X_VALUE_KEY);
		String yValue = parameters.get(Y_VALUE_KEY);
		
		
		try {
			int x = Integer.parseInt(xValue);
			int y = Integer.parseInt(yValue);
			
			robot.mouseMove(x, y);
		} catch (NumberFormatException e) {
			serviceResponse.setError("Invalid parameters informed : ("+xValue+","+yValue+")");
		}
	}
	
	public void moveScroll(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext){
		
		Map<String,String> parameters = serviceCall.getParameters();
		
		String value = parameters.get(VALUE_KEY);
		
		
		try {
			int v = Integer.parseInt(value);
			
			robot.mouseWheel(v);
		} catch (NumberFormatException e) {
			serviceResponse.setError("Invalid parameters informed : ("+value+")");
		}
	}
	
	@Override
	public UpDriver getDriver() {
		UpDriver mouseDriver = new UpDriver("MouseDriver");
		
		mouseDriver.addService("movePointer")
					.addParameter(X_VALUE_KEY, ParameterType.MANDATORY)
					.addParameter(Y_VALUE_KEY, ParameterType.MANDATORY);
		mouseDriver.addService("moveScroll")
					.addParameter(VALUE_KEY, ParameterType.MANDATORY);
		
		return mouseDriver;
	}

	@Override
	public void init(Gateway gateway, String instanceId) {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void destroy() {}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
