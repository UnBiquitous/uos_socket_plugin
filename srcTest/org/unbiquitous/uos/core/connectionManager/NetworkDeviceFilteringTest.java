package org.unbiquitous.uos.core.connectionManager;

import static org.fest.assertions.api.Assertions.*;
import junit.framework.TestCase;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.network.socket.connectionManager.TCPConnectionManager;

public class NetworkDeviceFilteringTest extends TestCase {

	private TCPConnectionManager mng;

	private void setupManager(String ignoreFilter) {
		InitialProperties props = new InitialProperties();
		props.put("ubiquitos.eth.tcp.port", "5678");
		if (ignoreFilter != null) {
			props.put("ubiquitos.eth.tcp.ignoreFilter", ignoreFilter);
		}
		mng = new TCPConnectionManager();
		mng.init(props);
	}

	
	public void testMustReturnTheFirstOfTheProvidedList() {
		setupManager(null);
		mng.setNetworkInterfaceProvider(new TCPConnectionManager.NetworkInterfaceProvider() {
			public String[] interfaces() {
				return new String[] { "1.2.3.4", "1.1.2.2", "3.3.4.4" };
			}
		});
		assertThat(mng.getNetworkDevice().getNetworkDeviceName()).isEqualTo(
				"1.2.3.4:5678");
	}

	public void testMustAlertToNullInterfaceAvailable() {
		setupManager(null);
		mng.setNetworkInterfaceProvider(new TCPConnectionManager.NetworkInterfaceProvider() {
			public String[] interfaces() {
				return null;
			}
		});
		try {
			mng.getNetworkDevice();
			fail("Expected to receive an alert exception.");
		} catch (NetworkException e) {
			// Expected
		}
	}

	public void testMustAlertToNoInterfaceAvailable() {
		setupManager(null);
		mng.setNetworkInterfaceProvider(new TCPConnectionManager.NetworkInterfaceProvider() {
			public String[] interfaces() {
				return new String[]{};
			}
		});
		try {
			mng.getNetworkDevice();
			fail("Expected to receive an alert exception.");
		} catch (NetworkException e) {
			// Expected
		}
	}
	
	public void testMustFilterTheProvidedWhenAskedList() {
		setupManager("1.2.*");
		mng.setNetworkInterfaceProvider(new TCPConnectionManager.NetworkInterfaceProvider() {
			public String[] interfaces() {
				return new String[] { "1.2.3.4", "1.1.2.2", "3.3.4.4" };
			}
		});
		assertThat(mng.getNetworkDevice().getNetworkDeviceName()).isEqualTo(
				"1.1.2.2:5678");
	}


	public void testMustFilterTheProvidedWhenAskedList_() {
		setupManager("1.2.3.*");
		mng.setNetworkInterfaceProvider(new TCPConnectionManager.NetworkInterfaceProvider() {
			public String[] interfaces() {
				return new String[] { "1.2.3.4", "1.2.3.2", "1.2.4.4" };
			}
		});
		assertThat(mng.getNetworkDevice().getNetworkDeviceName()).isEqualTo(
				"1.2.4.4:5678");
	}
}
