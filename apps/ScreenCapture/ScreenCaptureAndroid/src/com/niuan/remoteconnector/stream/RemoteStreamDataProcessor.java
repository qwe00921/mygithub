package com.niuan.remoteconnector.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnection;
import com.niuan.remoteconnector.data.DataConverter;
import com.niuan.remoteconnector.data.DataConverterManager;
import com.niuan.remoteconnector.data.RemoteDataHeader;
import com.niuan.remoteconnector.data.RemoteDataProcessor;
import com.niuan.remoteconnector.util.Log;


public class RemoteStreamDataProcessor extends RemoteDataProcessor {

	private static final char HEADER_RECOGNIZER = '\r';
	private static final String DEFAULT_ENCODING = "utf-8";
	private static final String TAG = "[RemoteDataProcessor]";
	
	public final void write(OutputStream out, Object object) throws IOException {
		if(out == null) {
			return;
		}
		
		if(object == null) {
			return;
		}
		
		byte[] data = readByteFromObject(object);
    	writeHeader(out, data, object);
		writeData(out, data);
		out.flush();
	}
	
	protected byte[] readByteFromObject(Object object) {
		if(object == null) {
			return null;
		}
		if(object instanceof byte[]) {
			return (byte[])object;
		}
		
    	DataConverter<?> converter = getDataConverter(object.getClass().getName());
    	byte[] data = null;
    	if(converter != null) {
    		data = converter.readByteFromObject(object);
    	}
		
		return data;
	}
	
    private void writeData(OutputStream out, byte[] data) throws IOException {
    	
    	if(out == null) {
    		return;
    	}
    	
    	if(data == null) {
    		return;
    	}
        
        out.write(data);
    }
    
    private void writeHeader(OutputStream out, byte[] data, Object object) throws IOException {
    	if(out == null) {
    		return;
    	}
    	
    	if(data == null) {
    		return;
    	}
    	RemoteDataHeader header = getHeader(data, object);
        String headerString = header == null ? "" : header.toString();
        Log.d(TAG, "[writeHeader] headerString = " + headerString);
        
        if(headerString != null) {
            out.write(headerString.getBytes(DEFAULT_ENCODING));
        }
        out.write(HEADER_RECOGNIZER);
    }
    
    protected RemoteDataHeader getHeader(byte[]data, Object object) {

    	if(data == null) {
    		return null;
    	}
    	
    	if(object == null) {
    		return null;
    	}
        
        RemoteDataHeader header = new RemoteDataHeader();
        header.setClassTypeName(object.getClass().getName());
        header.setLength(data.length);
        
        return header;
    }
    
    public final Object read(InputStream in) throws IOException {
    	
    	if(in == null) {
    		return null;
    	}
    	RemoteDataHeader header = readHeader(in);
    	byte[] byteData = readByteData(in, header);
    	Object object = readObjectFromByte(byteData, header);
    	
    	return object;
    }
    
    private Object readObjectFromByte(byte[] byteData, RemoteDataHeader header) {
    	if(header == null || header.getClassTypeName() ==  null) {
    		return null;
    	}
    	
    	DataConverter<?> converter = getDataConverter(header.getClassTypeName());
    	Object object = null;
    	if(converter != null) {
    		
    		object = converter.readObjectFromByte(byteData);
    	}
		return object;
    }
    
    private DataConverter<?> getDataConverter(String classTypeName) {
    	return DataConverterManager.getConverter(classTypeName);
    }
    
    private RemoteDataHeader readHeader(InputStream in) throws IOException{
		int d = -1;
		StringBuilder headerString = new StringBuilder();

		StringBuilder builder = new StringBuilder();

		while ((d = in.read()) != HEADER_RECOGNIZER) {
			if (d == -1) {
				break;
			}
			builder.append(d);
			headerString.append((char) d);
		}
		
		RemoteDataHeader header = new RemoteDataHeader(headerString.toString());
		return header;
		
    }
    
    private byte[] readByteData(InputStream in, RemoteDataHeader header) throws IOException {
		byte[] data = null;
		int d = -1;

		Log.d(TAG, "[readByteData] header = " + header);
		int size = header.getLength();
		data = new byte[size];
		int index = 0;

		while (size > 0 && (d = in.read()) != -1) {
			data[index] = (byte) d;
			size--;
			index++;
		}

		return data;
    }

	@Override
	public Object read(RemoteConnection connection) throws RemoteConnectionException {
		Object object = null;
		if(connection instanceof RemoteStreamConnection) {
			RemoteStreamConnection streamConn = (RemoteStreamConnection) connection;
			InputStream in = streamConn.getInputStream();
			try {
				object = read(in);
			} catch (IOException e) {
				e.printStackTrace();
				
				throw new RemoteConnectionException();
			}
		}
		
		return object;
	}

	@Override
	public void write(RemoteConnection connection, Object object)
			throws RemoteConnectionException {

		if(connection instanceof RemoteStreamConnection) {
			RemoteStreamConnection streamConn = (RemoteStreamConnection) connection;
			OutputStream out = streamConn.getOutputStream();
			try {
				write(out, object);
			} catch (IOException e) {
				e.printStackTrace();
				
				throw new RemoteConnectionException();
			}
		}
	}

}
