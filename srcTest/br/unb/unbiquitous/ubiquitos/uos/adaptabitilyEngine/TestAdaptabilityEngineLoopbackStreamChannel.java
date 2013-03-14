package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.List;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;

public class TestAdaptabilityEngineLoopbackStreamChannel extends TestAdaptabilityEngineStreamChannel{

	@Override
	protected void setProviderDevice() {
		providerDevice = new UpDevice();
		providerDevice.setName("WrongDummyDevice");
		List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
		UpNetworkInterface nInf = new UpNetworkInterface();
		
		nInf.setNetType("Loopback");
		nInf.setNetworkAddress("This Device:0");
		
		networks.add(nInf);
		providerDevice.setNetworks(networks);
		
	}

}
