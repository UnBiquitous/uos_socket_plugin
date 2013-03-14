package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.List;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;

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
