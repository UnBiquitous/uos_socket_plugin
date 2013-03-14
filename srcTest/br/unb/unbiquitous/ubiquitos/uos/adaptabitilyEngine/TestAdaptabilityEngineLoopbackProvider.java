package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.List;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;

public class TestAdaptabilityEngineLoopbackProvider extends TestAdaptabilityEngineNullProvider {
	public TestAdaptabilityEngineLoopbackProvider() {
		 super();
			providerDevice = new UpDevice();
			providerDevice.setName("WrongDummyDevice");
			List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
			UpNetworkInterface nInf = new UpNetworkInterface();
			nInf.setNetType("Ethernet:TCP");
			nInf.setNetworkAddress("localhost:14984");
			networks.add(nInf);
			providerDevice.setNetworks(networks);
	}
}
