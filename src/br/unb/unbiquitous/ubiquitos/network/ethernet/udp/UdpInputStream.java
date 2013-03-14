package br.unb.unbiquitous.ubiquitos.network.ethernet.udp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager.EthernetUDPConnectionManager;

/**
 * This class abstract the input stream for a udp connection.
 * 
 * @author Lucas Lins
 */
public class UdpInputStream extends InputStream{
	
	/*************************************
	 * ATTRIBUTES
	 *************************************/
	
	private final int MAX_BUFFER_SIZE = EthernetUDPConnectionManager.UDP_BUFFER_SIZE * 8;
	private Vector<Byte> buffer = new Vector<Byte>();
	private int bufferSize;
	private boolean isClosed = false;
	
	/*************************************
	 * CONSTRUCTORS
	 *************************************/
	
	public UdpInputStream(){}
	
	public UdpInputStream(byte[] initialBuffer, int initialBufferSize){
		if(initialBufferSize > 0){
			this.putIntoBuffer(initialBuffer, initialBufferSize);
		}
	}
	
	/*************************************
	 * PRIVATE METHODS
	 *************************************/
	
	private void putIntoBuffer(byte[] data, int dataSize){
		for (int i = 0; i < dataSize; i++) this.buffer.add(data[i]);
		
		if(this.buffer.size() > MAX_BUFFER_SIZE){
			for(int i = 0; i < this.buffer.size() - this.buffer.size(); i++) this.buffer.remove(0);
		}
		
		this.bufferSize = this.buffer.size();
	}
	
	/*************************************
	 * PUBLIC METHODS
	 *************************************/
	
	public boolean isClosed(){
		return isClosed;
	}
	
	public void putInBuffer(byte[] data, int size){
		synchronized (buffer) {
			this.putIntoBuffer(data, size);
		}
	}
	
	/**
	 * Returns the number of bytes that can be read (or skipped over) from this input stream 
	 * without blocking by the next caller of a method for this input stream.
	 * @throws IOException
	 */
	public int available() throws IOException {
		synchronized (buffer) {
			return bufferSize == -1 ? 0 : bufferSize;
		}
	}

	/**
	 * Reads the next byte of data from the input stream.
	 * @throws IOException
	 */
	public int read() throws IOException {
		synchronized (buffer) {
			if(bufferSize > 0){
				bufferSize--;
				return ((byte)buffer.remove(0)) & 0xff;
			}else{
				return -1;
			}
		}
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		synchronized (buffer) {
			if(bufferSize > 0){
				int size = bufferSize > len ? len : bufferSize;
				for(int i = off; i < (off + size); i++) b[i] = buffer.remove(0);
				bufferSize -= size;
				return size;
			}else{
				return -1;
			}
		}
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Skips over and discards n bytes of data from this input stream.
	 * @throws IOException
	 */
	public long skip(long n) throws IOException {
		synchronized (buffer) {
			if(bufferSize == 0){
				return 0;
			}else{
				Long length = (bufferSize < n) ? bufferSize : n;
				for(int i = 0; i < length; i++) read();
				return length;
			}
		}
	}
	
	/**
	 * close the input stream.
	 * @throws IOException
	 */
	public void close() throws IOException {
		if(!isClosed){
			synchronized (buffer) {
				bufferSize = 0;
				buffer.clear();
				isClosed = true;
				super.close();
			}
		}
	}
	
	/******************************************
	 * NOT SUPPORTED
	 ******************************************/
	public boolean markSupported() { return false;}
	public synchronized void mark(int readlimit) {}
	public synchronized void reset() throws IOException {}
	
}
