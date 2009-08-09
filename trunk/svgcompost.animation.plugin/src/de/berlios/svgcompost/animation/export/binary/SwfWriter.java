package de.berlios.svgcompost.animation.export.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SwfWriter {

	public int bitOffset;
	public int byteCount;
	public byte[] buffer;

	/**
	 * Converts swf data objects into an array of bytes,
	 * ready to be written to a file.
	 * @param swfData the swf data root.
	 */
	public void convertData( SwfData swfData )
	{
		swfData.calcSize();
		buffer = new byte[byteCount];
		reset();
		swfData.write();
	}

	public void writeFile( File file )
	{
		try {
			OutputStream stream = new FileOutputStream( file );
			stream.write( buffer, 0, buffer.length );
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void nextByte()
	{
		bitsToBytes();
		if(bitOffset != 0)
		{
			byteCount += bitOffset /8;
			bitOffset = 0;
			byteCount ++;
		}
	}

	public void bitsToBytes()
	{
		byteCount += bitOffset /8;
		bitOffset %= 8;
	}

	public void reset()
	{
		byteCount = 0;
		bitOffset = 0;
	}

}
