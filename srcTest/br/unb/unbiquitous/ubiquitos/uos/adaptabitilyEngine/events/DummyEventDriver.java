package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.events;

import java.util.List;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosEventDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService.ParameterType;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

public class DummyEventDriver implements UosEventDriver {
	
	private static DummyEventDriver currentDummyEventDriver ;
	
	private ServiceCall lastServiceCall ;
	
	private int lastServiceCallCount = 0;

	public DummyEventDriver() {
		currentDummyEventDriver = this;
	}
	
	@Override
	public void registerListener(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		// store service call for test check
		lastServiceCall = serviceCall;
		lastServiceCallCount++;
	}

	@Override
	public void unregisterListener(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		// store service call for test check
		lastServiceCall = serviceCall;
		lastServiceCallCount++;

	}

	@Override
	public UpDriver getDriver() {
		
		UpDriver driver = new UpDriver("br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.events.DummyEventDriver");
		
		driver.addService("registerListener")
				.addParameter("eventKey", ParameterType.MANDATORY);
		
		driver.addService("unregisterListener")
				.addParameter("eventKey", ParameterType.OPTIONAL);
		
		return driver;
	}

	@Override
	public void init(Gateway gateway, String instanceId) {}

	@Override
	public void destroy() {}

	/**
	 * @return the currentDummyEventDriver
	 */
	public static DummyEventDriver getCurrentDummyEventDriver() {
		return currentDummyEventDriver;
	}

	/**
	 * @return the lastServiceCall
	 */
	public ServiceCall getLastServiceCall() {
		return lastServiceCall;
	}

	/**
	 * @return the lastServiceCallCount
	 */
	public int getLastServiceCallCount() {
		return lastServiceCallCount;
	}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
