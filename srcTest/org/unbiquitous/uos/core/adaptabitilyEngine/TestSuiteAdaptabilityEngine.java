package org.unbiquitous.uos.core.adaptabitilyEngine;

import junit.framework.Test;
import junit.framework.TestSuite;


public class TestSuiteAdaptabilityEngine {

	public static Test suite() { 
        TestSuite suite = new TestSuite(TestSuiteAdaptabilityEngine.class.getName());

        suite.addTestSuite(TestAdaptabilityEngineNullProvider.class);
        suite.addTestSuite(TestAdaptabilityEngineCurrentProvider.class);
        suite.addTestSuite(TestAdaptabilityEngineLoopbackProvider.class);
        suite.addTestSuite(TestAdaptabilityEngineTCPStreamChannel.class); 
        suite.addTestSuite(TestAdaptabilityEngineUDPStreamChannel.class);
//        suite.addTestSuite(TestAdaptabilityEngineLoopbackStreamChannel.class);
        //FIXME : BasicAuthentication : Tests Not working properly  
        //suite.addTestSuite(TestAdaptabilityEngineBasicAuthentication.class);
        
        return suite; 
	}
	
}
