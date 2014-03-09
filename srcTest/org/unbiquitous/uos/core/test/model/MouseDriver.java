package org.unbiquitous.uos.core.test.model;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;


public class MouseDriver implements UosDriver {

	private static final String VALUE_KEY = "value";

	private static final String Y_VALUE_KEY = "yValue";

	private static final String X_VALUE_KEY = "xValue";

	
	private Robot robot; 
	
	
	public void movePointer(Call serviceCall, Response serviceResponse, CallContext messageContext){
		
		Map<String,Object> parameters = serviceCall.getParameters();
		
		String xValue = (String) parameters.get(X_VALUE_KEY);
		String yValue = (String) parameters.get(Y_VALUE_KEY);
		
		
		try {
			int x = Integer.parseInt(xValue);
			int y = Integer.parseInt(yValue);
			
			robot.mouseMove(x, y);
		} catch (NumberFormatException e) {
			serviceResponse.setError("Invalid parameters informed : ("+xValue+","+yValue+")");
		}
	}
	
	public void moveScroll(Call serviceCall, Response serviceResponse, CallContext messageContext){
		
		Map<String,Object> parameters = serviceCall.getParameters();
		
		String value = (String) parameters.get(VALUE_KEY);
		
		
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
	public void init(Gateway gateway, InitialProperties properties, String instanceId) {
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
