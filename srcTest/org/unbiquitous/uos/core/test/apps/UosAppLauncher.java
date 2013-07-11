package org.unbiquitous.uos.core.test.apps;

import org.unbiquitous.uos.core.ContextException;
import org.unbiquitous.uos.core.UOS;

public class UosAppLauncher {

	/**
	 * @param args
	 * @throws ContextException 
	 */
	public static void main(String[] args) throws ContextException {
		UOS applicationContext = new UOS();
		try {
			applicationContext.init("app");
		} catch (ContextException e) {
			throw e;
		}finally{
			//applicationContext.tearDown();
		}
	}

}
