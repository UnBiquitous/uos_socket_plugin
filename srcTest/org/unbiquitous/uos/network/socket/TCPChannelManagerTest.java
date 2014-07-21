package org.unbiquitous.uos.network.socket;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.TestCase;

import org.unbiquitous.uos.core.network.cache.CacheController;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.network.socket.channelManager.TCPChannelManager;

public class TCPChannelManagerTest extends TestCase {

	private final int defaultPort = 14984;
	private CacheController cacheController = new CacheController();
	private ServerSocket server;
	private TCPChannelManager mng;
	
	public void setUp() throws Exception{
		mng = new TCPChannelManager(defaultPort, 14896, 14899, cacheController);
	}
	
	public void tearDown() throws Exception{
		if(server != null){
			server.close();
		}
	}
	
	public void test_opensAConnectionOnTheAddress() throws Exception{
		final boolean[] opened = openServerSocket(defaultPort);
		
		ClientConnection conn = mng.openActiveConnection("localhost");
		assertThat(conn).isNotNull();
		assertThat(opened[0]).isTrue();
	}
	
	public void test_rejectsStrangeAdresses() throws Exception{
		try {
			final boolean[] opened = openServerSocket(defaultPort);
			
			ClientConnection conn = mng.openActiveConnection("localhost:1:2:3");
			assertThat(conn).isNotNull();
			assertThat(opened[0]).isTrue();
		} catch (NetworkException e) {
			// expected
		}
	}
	
	public void test_opensAConnectionOnTheAddressAndPort() throws Exception{
		final boolean[] opened = openServerSocket(14896);
		
		ClientConnection conn = mng.openActiveConnection("localhost:14896");
		assertThat(conn).isNotNull();
		assertThat(opened[0]).isTrue();
	}
	
	public void test_opensACachedConnectionOnTheAddressAndPort() throws Exception{
		final boolean[] opened = openServerSocket(14896);
		
		ClientConnection conn1 = mng.openActiveConnection("localhost:14896");
		ClientConnection conn2 = mng.openActiveConnection("localhost:14896");
		assertThat(conn1).isNotNull().isSameAs(conn2);
		
		assertThat(opened[0]).isTrue();
	}
	
	public void test_doNotOpenAConnectionOnTheDefaultPortWhenInRange() throws Exception{
		final boolean[] opened = openServerSocket(defaultPort);
		
		ClientConnection conn = mng.openActiveConnection("localhost:14896");
		assertThat(conn).isNull();
		assertThat(opened[0]).isFalse();
	}
	
	public void xtest_opensAConnectionOnTheDefaultPortIfItsNotOnTheRightRange() throws Exception{
		final boolean[] opened = openServerSocket(defaultPort);
		
		ClientConnection conn = mng.openActiveConnection("localhost:12345");
		assertThat(conn).isNotNull();
		assertThat(opened[0]).isTrue();
	}

	private boolean[] openServerSocket(final int defaultPort) {
		final boolean opened[] = {false};
		new Thread(new Runnable() {
			public void run() {
				Socket conn = null;
				try {
					server = new ServerSocket(defaultPort);
					conn = server.accept();
					opened[0] = true;
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally{
					close(conn);
				}
			}

			private void close(Socket conn) {
				if (conn != null){
					try {
						conn.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}).start();
		return opened;
	}
	
}
