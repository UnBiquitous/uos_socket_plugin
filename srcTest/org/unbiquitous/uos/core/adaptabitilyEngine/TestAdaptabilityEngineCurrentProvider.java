package org.unbiquitous.uos.core.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.List;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;


public class TestAdaptabilityEngineCurrentProvider extends
		TestAdaptabilityEngineNullProvider {

	public TestAdaptabilityEngineCurrentProvider() {
		super();
		providerDevice = new UpDevice();
		providerDevice.setName("LocalDummyDevice");
		List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
		UpNetworkInterface nInf = new UpNetworkInterface();
		nInf.setNetType("Ethernet:TCP");
		nInf.setNetworkAddress("WrongDummyAddress:14984");
		networks.add(nInf);
		providerDevice.setNetworks(networks);
	}
	
}
