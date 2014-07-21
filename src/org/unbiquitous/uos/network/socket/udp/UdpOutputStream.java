package org.unbiquitous.uos.network.socket.udp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.unbiquitous.uos.network.socket.connectionManager.UDPConnectionManager;


/**
 * This class abstract the output stream for a udp connection.
 * 
 * @author Lucas Lins
 */
public class UdpOutputStream extends OutputStream{
	
	/*************************************
	 * ATTRIBUTES
	 *************************************/
	private UdpChannel channel;
	
	private Vector<Byte> buffer = new Vector<Byte>();
	private boolean isClosed = false;
	
	
	/*************************************
	 * CONSTRUCTOR
	 *************************************/
	
	public UdpOutputStream(UdpChannel channel){
		this.channel = channel;
	}
	
	/*************************************
	 * PRIVATE METHODS
	 *************************************/
	
	/**
	 * send the buffer, or part of it to the address:port
	 * @throws IOException
	 */
	private void send() throws IOException{
		int length = buffer.size();
		if(length > 0){
			int size = (length < UDPConnectionManager.UDP_BUFFER_SIZE)? length : UDPConnectionManager.UDP_BUFFER_SIZE;
			byte[] sendBuffer = new byte[size];
			for (int i = 0; i < size; i++) sendBuffer[i] = buffer.remove(0);
			channel.send(sendBuffer, 0, size, this);
		}
	}
	
	/**
	 * verify if we have to send part of the buffer.
	 * @throws IOException
	 */
	private void checkToSend() throws IOException{
		while(buffer.size() >= UDPConnectionManager.UDP_BUFFER_SIZE){
			send();
		}
	}
	
	/*************************************
	 * PUBLIC METHODS
	 *************************************/
	
	public boolean isClosed(){
		return isClosed;
	}
	
	/**
	 * append a int to the buffer and call checkToSend
	 */
	public void write(int b) throws IOException {
		buffer.add((byte)b);
		checkToSend();
	}

	/**
	 * append a array of byte to the buffer and call checkToSend.
	 */
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	/**
	 * append a array of byte to the buffer starting from off and ends in len. Call checkToSend.
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		for(int i = off; i < (off + len); i++) buffer.add((byte)b[i]);
		checkToSend();
	}
	
	/**
	 * close the output stream
	 */
	public void close() throws IOException {
		if(!isClosed){
			isClosed = true;
			super.close();
		}
	}
	
	/**
	 * call send.
	 */
	public void flush() throws IOException {
		send();
	}

}
