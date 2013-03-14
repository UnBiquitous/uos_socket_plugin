package br.unb.unbiquitous.ubiquitos.uos.test.apps;

import br.unb.unbiquitous.ubiquitos.uos.context.ContextException;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;

public class UosAppLauncher {

	/**
	 * @param args
	 * @throws ContextException 
	 */
	public static void main(String[] args) throws ContextException {
		UOSApplicationContext applicationContext = new UOSApplicationContext();
		try {
			applicationContext.init("app");
		} catch (ContextException e) {
			throw e;
		}finally{
			//applicationContext.tearDown();
		}
	}

}
