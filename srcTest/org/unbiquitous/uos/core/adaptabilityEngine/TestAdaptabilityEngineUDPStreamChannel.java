package org.unbiquitous.uos.core.adaptabilityEngine;

import java.util.ArrayList;
import java.util.List;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;


public class TestAdaptabilityEngineUDPStreamChannel extends TestAdaptabilityEngineStreamChannel{

	@Override
	protected void setProviderDevice() {
		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice");
		List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
		UpNetworkInterface nInf = new UpNetworkInterface();
		
		nInf.setNetType("Ethernet:UDP");
		nInf.setNetworkAddress("localhost:15001");
		
		networks.add(nInf);
		providerDevice.setNetworks(networks);
		
	}

}