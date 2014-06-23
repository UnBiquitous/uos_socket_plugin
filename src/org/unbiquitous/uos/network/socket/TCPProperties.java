package org.unbiquitous.uos.network.socket;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.network.socket.connectionManager.TCPConnectionManager;

@SuppressWarnings("serial")
public class TCPProperties extends InitialProperties {

	public TCPProperties() {
		this(new InitialProperties());
	}
	
	public TCPProperties(InitialProperties props) {
		super(props);
		try {
			if(getConnectionManagers().isEmpty()){
				addConnectionManager(TCPConnectionManager.class);
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setPort(Integer port){
		put("ubiquitos.eth.tcp.port", port);
	}
	
	public Integer getPort(){
		return getInt("ubiquitos.eth.tcp.port");
	}
	
	public void setPassivePortRange(Integer start, Integer end){
		put("ubiquitos.eth.tcp.passivePortRange", new Tuple<Integer, Integer>(start, end));
	}
	
	@SuppressWarnings("unchecked")
	public Tuple<Integer, Integer> getPassivePortRange(){
		if(containsKey("ubiquitos.eth.tcp.passivePortRange")){
			Object value = get("ubiquitos.eth.tcp.passivePortRange");
			if(value instanceof Tuple ) return (Tuple<Integer, Integer>) value;
			if(value instanceof String ){
				return translatePortRange(value);
			}
		}
		return null;
	}

	private Tuple<Integer, Integer> translatePortRange(Object value) {
		String portRange = (String) value;
		String[] limitPorts = portRange.split("-");
		int start = Integer.parseInt(limitPorts[0]);
		int end = Integer.parseInt(limitPorts[1]);
		return new Tuple<Integer, Integer>(start, end);
	}
}
