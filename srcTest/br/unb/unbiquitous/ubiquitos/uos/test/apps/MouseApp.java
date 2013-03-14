package br.unb.unbiquitous.ubiquitos.uos.test.apps;

import java.util.HashMap;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyDeploy;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyStart;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyUndeploy;

public class MouseApp implements UosApplication {

	@Override
	public void start(Gateway gateway, OntologyStart ontology) {
		
		try {
			Thread.sleep(1000);
			Map<String, String> parameters =  new HashMap<String, String>();
			
			for (int x = 10 ,y =10; y < 500 && x < 500 ; y += 10 , x += 10 ){
			
				parameters.put("xValue", x+"");
				parameters.put("yValue", y+"");
				gateway.callService(null, "movePointer", "MouseDriver", null, null, parameters);
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	@Override
	public void stop() throws Exception {}

	@Override
	public void init(OntologyDeploy ontology) {}

	@Override
	public void tearDown(OntologyUndeploy ontology) throws Exception {}

}
