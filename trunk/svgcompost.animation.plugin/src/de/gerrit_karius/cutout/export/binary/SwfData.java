package de.gerrit_karius.cutout.export.binary;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Map;

import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;

	public abstract class SwfData 
	{

		protected int val;
		protected SwfWriter writer;

		public SwfData(SwfWriter writer, int val)
		{
			this.writer = writer;
			this.val = val;
		}

		public SwfData(SwfWriter writer)
		{
			this.writer = writer;
		}
		public SwfData()
		{
		}

		public abstract void calcSize();

		public abstract void write();

	}


	class SwfBitValue extends SwfData 
	{

		protected int bitSize;

		public SwfBitValue(SwfWriter writer, int val, int bitSize) //: base(writer,val)
		{
			//base( writer, val );
			super( writer, val );
			this.bitSize = bitSize;
		}

		public SwfBitValue(SwfWriter writer, int val, boolean isSigned) //: base( writer, val )
		{
			super( writer, val );
			this.bitSize = calcBitSize(val,isSigned);
		}

		public SwfBitValue(SwfWriter writer, int val) //: base( writer, val )
		{
			super( writer, val );
			this.bitSize = calcBitSize(val,true);
		}

		public SwfBitValue(SwfWriter writer, double val) //: base( writer, (int) (val * Math.Pow(2,16)) )
		{
			super( writer, (int) (val * Math.pow(2,16)) );
			this.bitSize = calcBitSize(this.val,true);
		}

		public SwfBitValue(SwfWriter writer, boolean val) //: base( writer, val ? -1 : 0 )
		{
			super( writer, val ? -1 : 0 );
			this.bitSize = 1;
		}

		@Override
		public void calcSize()
		{
			writer.bitOffset += bitSize;
		}

		@Override
		public void write()
		{

			writer.bitsToBytes();

			long longValue = val;
			long longMask = (long) Math.pow(2,bitSize) - 1;
			int gap = ( 8 - (( bitSize + writer.bitOffset ) % 8 ));
			longValue <<= gap;
			longMask <<= gap;


			int mask = (int) Math.pow(2,8) - 1;
			byte[] byteValues = new byte[5];
			byte[] byteMasks = new byte[5];

			for(int i=0; i<5; i++)
			{
				int byteValue = (int) longValue & mask;
				int byteMask = (int) longMask & mask;
				byteValues[i] = (byte) byteValue;
				byteMasks[i] = (byte) byteMask;
				longValue >>= 8;
				longMask >>= 8;
			}

			int endByte = writer.byteCount + (  writer.bitOffset + bitSize ) / 8;
			endByte++;
			int startByte = endByte - 5;

			int count = 4;
			for(int i=startByte; i<endByte; i++)
			{
				writer.buffer[i] &= (byte) (~byteMasks[count]);
				int byteValue = (int) byteValues[count] & byteMasks[count];
				writer.buffer[i] |= (byte) byteValue;
				count--;
			}

			calcSize();
		}

		public static int calcBitSize(int val, boolean isSigned)
		{

			if( isSigned && val < 0 )
				val = ~val;

			int size = 0;
			while( val != 0 )
			{
				val >>= 1;
				size++;
//				System.out.println( "while: "+val );
			}
//			System.out.println( "exit" );

			if( size == 0 )
				size++;
			if( isSigned )
				size++;

			return size;

		}

		public static int calcBitSize(int[] val, boolean isSigned)
		{

			int size = 0;
			for(int i=0; i<val.length; i++)
			{
				size = Math.max( size, calcBitSize( val[i], isSigned ) );
			}
			return size;

		}

		public static void addBitValueAttributes(SwfStruct structure, int[] vals, int size, boolean isSigned)
		{
			int bitSize = calcBitSize( vals, isSigned );
			structure.addChild( new SwfBitValue( structure.writer, bitSize, size ) );
			for(int i=0; i<vals.length;i++)
				structure.addChild( new SwfBitValue( structure.writer, vals[i], bitSize ) );
		}

	}


	class SwfByteValue extends SwfData 
	{

		protected int byteSize;

		public SwfByteValue(SwfWriter writer, int val, int byteSize) //: base( writer, val )
		{
			super( writer, val );
			this.byteSize = byteSize;
		}

		public SwfByteValue(SwfWriter writer, double val, int byteSize) //: base( writer, (int) (val * Math.Pow(2,16)) )
		{
			super( writer, (int) (val * Math.pow(2,16)) );
			this.byteSize = byteSize;
		}

		@Override
		public void calcSize()
		{
			writer.nextByte();
			writer.byteCount += byteSize;
		}

		@Override
		public void write()
		{

			writer.nextByte();

			int littleEndian = val;

			int mask = 255;
			byte[] byteValues = new byte[4];

			for(int i=0; i<4; i++)
			{
				int byteValue = (int) littleEndian & mask;
				byteValues[i] = (byte) byteValue;
				littleEndian >>= 8;
			}

			int count = 0;
			for(int i=0; i<byteSize; i++)
			{
				writer.buffer[writer.byteCount + i] = byteValues[count];
				count++;
			}

			calcSize();
		}

		/**
		 * Creates a value with zero length, which moves the cursor to the start of the next byte. 
		 * @param writer
		 * @return an empty SwfByteValue
		 */
		public static SwfByteValue getEmptyValue( SwfWriter writer )
		{
			return new SwfByteValue( writer, 0, 0 );
		}
	}

	class SwfByteArray extends SwfData 
	{

		protected byte[] source;

		public SwfByteArray(SwfWriter writer, byte[] source) //: base( writer )
		{
			super( writer );
			this.source = source;
		}

		@Override
		public void calcSize()
		{
			writer.nextByte();
			if( source == null )
				return;
			writer.byteCount += source.length;
		}

		@Override
		public void write()
		{
			writer.nextByte();
			if( source == null )
				return;
			System.arraycopy( source, 0, writer.buffer, writer.byteCount, source.length );

			//for(int i=0; i<source.length; i++)
			//writer.buffer[writer.byteCount+i] = source[i];
			writer.byteCount += source.length;
		}
	}

	class SwfStruct extends SwfData 
	{

		protected ArrayList<SwfData> swfData = new ArrayList<SwfData>();

		public SwfStruct( SwfWriter writer ) //: base( writer )
		{
			super( writer );
		}
		
		@Override
		public void calcSize()
		{
			for(int i=0; i<swfData.size(); i++)
			{
				getChild(i).calcSize();
			}
		}

		@Override
		public void write()
		{
			for(int i=0; i<swfData.size(); i++)
			{
				getChild(i).write();
			}
		}

		public SwfData getChild( int i )
		{
			if( i >= 0 && i < swfData.size() )
				return (SwfData) swfData.get(i);
			return null;
		}

		public void addChild( SwfData attribute )
		{
			swfData.add(attribute);
		}

	}

	class SwfTagHeader extends SwfStruct 
	{

		public SwfTagHeader(SwfWriter writer, int tagID, int tagLength) //: base(writer)
		{
			super( writer );
			int code = tagID;
			code <<= 6;
			if( tagLength < 63 && tagLength >= 0 )
			{
				code += tagLength;
				addChild( new SwfByteValue( writer, code, 2 ) );
			} 
			else 
			{
				code += 63;
				addChild( new SwfByteValue( writer, code, 2 ) );
				addChild( new SwfByteValue( writer, tagLength, 4 ) );
			}
		}

	}

	class SwfTag extends SwfStruct 
	{

		protected int tagID;

		public SwfTag( SwfWriter writer, int tagID ) //: base( writer )
		{
			super( writer );
			this.tagID = tagID;
			addChild( new SwfTagHeader( writer, tagID, 0 ) );
		}

		@Override
		public void calcSize()
		{
			writer.nextByte();
			int startByte = writer.byteCount;
			for(int i=1; i<swfData.size(); i++)
			{
				getChild(i).calcSize();
			}
			writer.nextByte();
			int endByte = writer.byteCount;
			int tagLength = endByte - startByte;
			swfData.set( 0, new SwfTagHeader( writer, tagID, tagLength ) );
			getChild(0).calcSize();
		}

	}

	class SwfRect extends SwfStruct 
	{

		public SwfRect( SwfWriter writer, Rectangle2D.Float rect ) //: base( writer )
		{
			super( writer );
			addChild( SwfByteValue.getEmptyValue( writer ) );
			int[] vals = { (int)rect.x*20, (int)(rect.x+rect.width)*20, (int)rect.y*20, (int)(rect.y+rect.height)*20 };
			int bitSize = SwfBitValue.calcBitSize( vals, true );
			addChild( new SwfBitValue( writer, bitSize, 5 ) );
			for(int i=0; i<4; i++)
				addChild( new SwfBitValue( writer, vals[i], bitSize ) );
		}

	}

	class SwfFileHeader extends SwfStruct 
	{

		public static int FILE_LENGTH = 4;
		public static int FRAME_COUNT = 7;

		public SwfFileHeader( SwfWriter writer, int version, Rectangle2D.Float size, int rate ) //: base( writer )
		{
			super( writer );
			addChild( new SwfByteValue( writer, (int) 'F', 1 ) );
			addChild( new SwfByteValue( writer, (int) 'W', 1 ) );
			addChild( new SwfByteValue( writer, (int) 'S', 1 ) );
			addChild( new SwfByteValue( writer, version, 1 ) );
			addChild( new SwfByteValue( writer, 0, 4 ) );
			addChild( new SwfRect( writer, size ) );
			addChild( new SwfByteValue( writer, rate * 256, 2 ) );
			addChild( new SwfByteValue( writer, 0, 2 ) );
		}

		public void setFileLength( int fileLength )
		{
			swfData.set( FILE_LENGTH, new SwfByteValue( writer, fileLength, 4 ) );
		}

	}

	class SwfFile extends SwfStruct 
	{

		protected SwfFileHeader fileHeader;

		public SwfFile( SwfWriter writer, int version, Rectangle2D.Float size, int rate ) //: base( writer )
		{
			super( writer );
			fileHeader = new SwfFileHeader( writer, version, size, rate );
			swfData.add( fileHeader );
			swfData.add( new SwfEndTag( writer ) );
		}

		@Override
		public void calcSize() 
		{
			super.calcSize();
			writer.nextByte();
			fileHeader.setFileLength( writer.byteCount );
		}

		@Override
		public void addChild( SwfData attribute )
		{
			swfData.add( swfData.size() - 1, attribute );
		}

		public void addFrame()
		{
			addChild( new SwfShowFrameTag( writer ) );
			fileHeader.getChild(SwfFileHeader.FRAME_COUNT).val++;
		}

	}

	class SwfDefineSpriteTag extends SwfTag 
	{

		protected SwfByteValue frameCount;

		public SwfDefineSpriteTag( SwfWriter writer, int spriteID ) //: base( writer, 39 )
		{
			super( writer, 39 );
			swfData.add( new SwfByteValue( writer, spriteID, 2 ) );
			frameCount = new SwfByteValue( writer, 0, 2 );
			swfData.add( frameCount );
		}

		public void addFrame()
		{
			addChild( new SwfShowFrameTag( writer ) );
			frameCount.val++;
		}

	}

	class SwfEndTag extends SwfTag 
	{

		public SwfEndTag( SwfWriter writer ) //: base( writer, 0 )
		{
			super( writer, 0 );
		}

	}

	class SwfShowFrameTag extends SwfTag
	{

		public SwfShowFrameTag( SwfWriter writer ) //: base( writer, 1 )
		{
			super( writer, 1 );
		}

	}

	class SwfSetBackgroundColorTag extends SwfTag
	{

		public SwfSetBackgroundColorTag( SwfWriter writer, int red, int green, int blue ) //: base( writer, 9 )
		{
			super( writer, 9 );
			addChild( new SwfRGBRecord( writer, red, green, blue ) );
		}
		public SwfSetBackgroundColorTag( SwfWriter writer, Color color ) //: base( writer, 9 )
		{
			super( writer, 9 );
			addChild( new SwfRGBRecord( writer, color.getRed(), color.getGreen(), color.getBlue() ) );
		}

	}

	class SwfRGBRecord extends SwfStruct
	{

		public SwfRGBRecord( SwfWriter writer, int red, int green, int blue ) //: base( writer )
		{
			super( writer );
			addChild( new SwfByteValue( writer, red, 1 ) );
			addChild( new SwfByteValue( writer, green, 1 ) );
			addChild( new SwfByteValue( writer, blue, 1 ) );
		}

	}

	class SwfResourceTag extends SwfTag
	{

		public SwfResourceTag( SwfWriter writer, int tagID, int characterID, byte[] source ) //: base( writer, tagID )
		{  
			super( writer, tagID );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			addChild( new SwfByteArray( writer, source ) );
		}

	}

	class SwfPlaceObject2Tag extends SwfTag 
	{

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, int characterID, AffineTransform matrix, boolean replace ) //: base( writer, 26 )
		{
			super( writer, 26 );

			for(int i=0; i<5; i++)
				addChild( new SwfBitValue( writer, false ) );

			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, replace ) );

			addChild( new SwfByteValue( writer, depth, 2 ) );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			addChild( new SwfMatrix( writer, matrix ) );

		}

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, int characterID, AffineTransform matrix, boolean replace, String spriteName ) //: base( writer, 26 )
		{
			super( writer, 26 );

			for(int i=0; i<2; i++)
				addChild( new SwfBitValue( writer, false ) );
			addChild( new SwfBitValue( writer, true ) ); // has name
			for(int i=0; i<2; i++)
				addChild( new SwfBitValue( writer, false ) );

			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, replace ) );

			addChild( new SwfByteValue( writer, depth, 2 ) );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			addChild( new SwfMatrix( writer, matrix ) );

			addChild( new SwfString( writer, spriteName ) );
			//char[] chars = spriteName.ToCharArray();
			//for(int i=0; i<chars.length; i++)
				//AddAttribute( new SwfByteValue( writer, (byte) chars[i], 1 ) );
			//AddAttribute( new SwfByteValue( writer, 0, 1 ) );

		}

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, int characterID, AffineTransform matrix, int ratio, boolean replace ) //: base( writer, 26 )
		{
			super( writer, 26 );
			
			for(int i=0; i<3; i++)
				addChild( new SwfBitValue( writer, false ) );

			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, false ) );

			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, replace ) );

			addChild( new SwfByteValue( writer, depth, 2 ) );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			addChild( new SwfMatrix( writer, matrix ) );
			addChild( new SwfByteValue( writer, ratio, 2 ) ); // ratio for morph shape tweens

		}

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, AffineTransform matrix ) //: base( writer, 26 )
		{
			super( writer, 26 );

			for(int i=0; i<5; i++)
				addChild( new SwfBitValue( writer, false ) );

			addChild( new SwfBitValue( writer, true ) );
			addChild( new SwfBitValue( writer, false ) );
			addChild( new SwfBitValue( writer, true ) );

			addChild( new SwfByteValue( writer, depth, 2 ) );
			addChild( new SwfMatrix( writer, matrix ) );
		}

	}

	class SwfString extends SwfStruct
	{
		public SwfString( SwfWriter writer, String s ) //: base( writer )
		{
			super( writer );
			byte[] b = new byte[s.length()+1];
			for(int i=0; i<s.length(); i++)
			{
				b[i] = (byte) s.charAt(i);
			}
			b[s.length()] = (byte) 0;
			addChild( new SwfByteArray( writer, b ) );
		}
	}

	class SwfImportAssetsTag extends SwfTag
	{
		public SwfImportAssetsTag( SwfWriter writer, String url, Map<Integer,String> identifiers ) //: base( writer, 57 )
		{
			super( writer, 57 );
			addChild( new SwfString(writer, url) );
			addChild( new SwfByteValue( writer, identifiers.size(), 2 ) );
			for( Integer key : identifiers.keySet() )
			{
				addChild( new SwfByteValue( writer, (Integer) key, 2 ) );
				addChild( new SwfString( writer, (String) identifiers.get(key) ) );
			}
		}
	}

	class SwfExportAssetsTag extends SwfTag
	{
		public SwfExportAssetsTag( SwfWriter writer, Map<Integer,String> identifiers ) //: base( writer, 56 )
		{
			super( writer, 56 );
			addChild( new SwfByteValue( writer, identifiers.size(), 2 ) );
			for( Integer key : identifiers.keySet() )
			{
				addChild( new SwfByteValue( writer, (Integer) key, 2 ) );
				addChild( new SwfString( writer, (String) identifiers.get(key) ) );
			}
		}
		public SwfExportAssetsTag( SwfWriter writer, int key, String identifier ) //: base( writer, 56 )
		{
			super( writer, 56 );
			// for only one asset to export
			addChild( new SwfByteValue( writer, 1, 2 ) );
			addChild( new SwfByteValue( writer, key, 2 ) );
			addChild( new SwfString( writer, identifier ) );
		}
	}

	class SwfFrameLabelTag extends SwfTag
	{
		public SwfFrameLabelTag( SwfWriter writer, String label ) //: base( writer, 43 )
		{
			super( writer, 43 );
			addChild( new SwfString( writer, label ) );
		}
	}

	class SwfActionGotoFrameTag extends SwfTag 
	{
		public SwfActionGotoFrameTag( SwfWriter writer, int frameCount ) //: base( writer, 12 )
		{
			super( writer, 12 );
			// execute goto frame action for a frame in the current timeline

			addChild( new SwfByteValue( writer, 0x81, 1 ) ); // goto frame action
			addChild( new SwfByteValue( writer, 2, 2 ) ); // record length, always 2
			// System.out.println( frameCount );
			addChild( new SwfByteValue( writer, frameCount, 2 ) ); // target frame count, zero-based
			addChild( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionGotoLabelTag extends SwfTag 
	{
		public SwfActionGotoLabelTag( SwfWriter writer, String label ) //: base( writer, 12 )
		{
			super( writer, 12 );
			// execute goto label action for a label in the current timeline

			addChild( new SwfByteValue( writer, 0x8c, 1 ) ); // goto label action
			addChild( new SwfByteValue( writer, label.length() + 1, 2 ) ); // record length
			addChild( new SwfString( writer, label ) );
			addChild( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionGotoAndStopTag extends SwfTag 
	{
		public SwfActionGotoAndStopTag( SwfWriter writer, String label ) //: base( writer, 12 )
		{
			super( writer, 12 );
			// execute goto label action for a label in the current timeline

			addChild( new SwfByteValue( writer, 0x07, 1 ) ); // stop action
			addChild( new SwfByteValue( writer, 0x8c, 1 ) ); // goto label action
			addChild( new SwfByteValue( writer, label.length() + 1, 2 ) ); // record length
			addChild( new SwfString( writer, label ) );
			addChild( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionGotoAndPlayTag extends SwfTag 
	{
		public SwfActionGotoAndPlayTag( SwfWriter writer, int frameCount ) //: base( writer, 12 )
		{
			super( writer, 12 );
			// execute goto frame action for a frame in the current timeline

			addChild( new SwfByteValue( writer, 0x81, 1 ) ); // goto frame action
			addChild( new SwfByteValue( writer, 2, 2 ) ); // record length, always 2
			addChild( new SwfByteValue( writer, frameCount, 2 ) ); // target frame count, zero-based
			addChild( new SwfByteValue( writer, 0x06, 1 ) ); // play action
			addChild( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionStopTag extends SwfTag 
	{
		public SwfActionStopTag( SwfWriter writer ) //: base( writer, 12 )
		{
			super( writer, 12 );
			// execute stop action for the current timeline

			addChild( new SwfByteValue( writer, 0x07, 1 ) ); // stop action
			addChild( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfDoActionTag extends SwfTag 
	{

		public SwfDoActionTag( SwfWriter writer, String spriteName, int frameNumber ) //: base( writer, 12 )
		{
			super( writer, 12 );
			// execute goto frame action for a sprite

			addChild( new SwfByteValue( writer, 0x8b, 1 ) ); // set target action
			addChild( new SwfByteValue( writer, spriteName.length() + 1, 2 ) ); // record length

			char[] chars = spriteName.toCharArray();
			for(int i=0; i<chars.length; i++)
				addChild( new SwfByteValue( writer, (byte) chars[i], 1 ) );
			addChild( new SwfByteValue( writer, 0, 1 ) ); // end of string

			addChild( new SwfByteValue( writer, 0x81, 1 ) ); // goto frame action
			addChild( new SwfByteValue( writer, 2, 2 ) ); // record length
			addChild( new SwfByteValue( writer, frameNumber, 2 ) ); // set target action

			addChild( new SwfByteValue( writer, 0, 1 ) ); // end of actions
		}

	}

	class SwfMatrix extends SwfStruct
	{
		public SwfMatrix( SwfWriter writer, AffineTransform transform ) //: base( writer )
		{
			super( writer );
			double[] matrix = new double[6];
			transform.getMatrix( matrix );
			
			// TODO: check if these are the right fields
			int pow = (int) Math.pow(2,16);
			int[] scale = { (int) (pow*matrix[0]), (int) (pow*matrix[3]) };
			int[] rotate = { (int) (pow*matrix[1]), (int) (pow*matrix[2]) };
			int[] translate = { (int) (20*matrix[4]), (int) (20*matrix[5]) };

			boolean hasScale = ( scale[0] != pow || scale[1] != pow );
			addChild( new SwfBitValue( writer, hasScale ) );
			if( hasScale )
				SwfBitValue.addBitValueAttributes(this,scale,5,true);

			boolean hasRotate = ( rotate[0] != 0 || rotate[1] != 0 );
			addChild( new SwfBitValue( writer, hasRotate ) );
			if( hasRotate )
				SwfBitValue.addBitValueAttributes(this,rotate,5,true);

			SwfBitValue.addBitValueAttributes(this,translate,5,true);

		}
	}

	class SwfRemoveObject2Tag extends SwfTag 
	{

		public SwfRemoveObject2Tag( SwfWriter writer, int depth ) //: base( writer, 28 )
		{
			super( writer, 28 );
			addChild( new SwfByteValue( writer, depth, 2 ) );
		}

	}

	class SwfDefineShapeTag extends SwfTag 
	{
		public SwfDefineShapeTag( SwfWriter writer, int characterID, ExtendedGeneralPath path, byte[] ratios, Color[] colors, AffineTransform matrix ) //: base( writer, 2 )
		{
			super( writer, 2 );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			Rectangle2D b2d = path.getBounds2D();
			Rectangle2D.Float bounds = new Rectangle2D.Float( (float)b2d.getX(), (float)b2d.getY(), (float)b2d.getWidth(), (float)b2d.getHeight() );
			bounds.x -= 10;
			bounds.y -= 10;
			bounds.width += 20;
			bounds.height += 20;
			addChild( new SwfRect( writer, bounds ) );
			addChild( new SwfByteValue( writer, 1, 1 ) ); // 1 fill style
			addChild( new SwfByteValue( writer, 0x12, 1 ) ); // radial gradient
			addChild( new SwfMatrix( writer, matrix ) ); // matrix for gradient fill
			addChild( new SwfGradient( writer, ratios, colors, false ) ); // gradient fill definition
			addChild( new SwfByteValue( writer, 1, 1 ) ); // 1 line style
			//AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			addChild( new SwfByteValue( writer, 40, 2 ) ); // line width 40
			addChild( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
			//AddAttribute( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)

			addChild( new SwfShape( writer, path, false, true ) );
		}

	}
	class SwfDefineShape3Tag extends SwfTag 
	{

		public SwfDefineShape3Tag( SwfWriter writer, int characterID, ExtendedGeneralPath path ) //: base( writer, 32 )
		{
			super( writer, 32 );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			Rectangle2D b2d = path.getBounds2D();
			Rectangle2D.Float bounds = new Rectangle2D.Float( (float)b2d.getX(), (float)b2d.getY(), (float)b2d.getWidth(), (float)b2d.getHeight() );
			bounds.x -= 10;
			bounds.y -= 10;
			bounds.width += 20;
			bounds.height += 20;
			addChild( new SwfRect( writer, bounds ) );
			addChild( new SwfByteValue( writer, 0, 1 ) ); // 0 fill styles
			addChild( new SwfByteValue( writer, 1, 1 ) ); // 1 line style
			//AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			addChild( new SwfByteValue( writer, 40, 2 ) ); // line width 40
			addChild( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
//			AddAttribute( new SwfByteValue( writer, 255, 1 ) );
//			AddAttribute( new SwfByteValue( writer, 255, 1 ) );
//			AddAttribute( new SwfByteValue( writer, 0, 1 ) );
			addChild( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)

			addChild( new SwfShape( writer, path, true, false ) );
		}
		public SwfDefineShape3Tag( SwfWriter writer, int characterID, ExtendedGeneralPath path, Paint fill, Stroke stroke ) //: base( writer, 32 )
		{
			super( writer, 32 );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			Rectangle2D b2d = path.getBounds2D();
			Rectangle2D.Float bounds = new Rectangle2D.Float( (float)b2d.getX(), (float)b2d.getY(), (float)b2d.getWidth(), (float)b2d.getHeight() );
			bounds.x -= 10;
			bounds.y -= 10;
			bounds.width += 20;
			bounds.height += 20;
			addChild( new SwfRect( writer, bounds ) );

			addChild( new SwfByteValue( writer, fill == null ? 0 : 1, 1 ) ); // # of fill styles
			//AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			
			if( fill == null ) {
				addChild( SwfByteValue.getEmptyValue( writer ) );
			}
			else if( fill instanceof Color ) {
				Color fillColor = (Color) fill;
				addChild( new SwfByteValue( writer, 0x00, 1 ) ); // solid fill
				addChild( new SwfByteValue( writer, fillColor.getRed(), 1 ) );
				addChild( new SwfByteValue( writer, fillColor.getGreen(), 1 ) );
				addChild( new SwfByteValue( writer, fillColor.getBlue(), 1 ) );
				addChild( new SwfByteValue( writer, fillColor.getAlpha(), 1 ) );
			}
			else {
				// TODO: implement gradients and bitmap fills
				System.err.println( "Swf fill type not implemented." );
			}

			addChild( new SwfByteValue( writer, stroke == null ? 0 : 1, 1 ) ); // # of line styles

			if( stroke == null ) {
				addChild( SwfByteValue.getEmptyValue( writer ) );
			}
			else if( stroke instanceof BasicStroke ) {
				BasicStroke basicStroke = (BasicStroke) stroke;
				addChild( new SwfByteValue( writer, basicStroke.getLineWidth()*20, 2 ) ); // line width 40
//				AddAttribute( new SwfByteValue( writer, fillColor.getRed(), 1 ) );
//				AddAttribute( new SwfByteValue( writer, fillColor.getGreen(), 1 ) );
//				AddAttribute( new SwfByteValue( writer, fillColor.getBlue(), 1 ) );
				addChild( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
				addChild( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)
			}
//			AddAttribute( new SwfByteValue( writer, 40, 2 ) ); // line width 40
//			AddAttribute( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
//			AddAttribute( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)

			addChild( new SwfShape( writer, path, true, fill != null ) );
		}
		public SwfDefineShape3Tag( SwfWriter writer, int characterID, ExtendedGeneralPath path, byte[] ratios, Color[] colors, AffineTransform matrix ) //: base( writer, 32 )
		{
			super( writer, 32 );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			Rectangle2D b2d = path.getBounds2D();
			Rectangle2D.Float bounds = new Rectangle2D.Float( (float)b2d.getX(), (float)b2d.getY(), (float)b2d.getWidth(), (float)b2d.getHeight() );
			bounds.x -= 10;
			bounds.y -= 10;
			bounds.width += 20;
			bounds.height += 20;
			addChild( new SwfRect( writer, bounds ) );
			addChild( new SwfByteValue( writer, 1, 1 ) ); // 1 fill style
			addChild( new SwfByteValue( writer, 0x12, 1 ) ); // radial gradient
			addChild( new SwfMatrix( writer, matrix ) ); // matrix for gradient fill
			addChild( new SwfGradient( writer, ratios, colors, true ) ); // gradient fill definition
			addChild( new SwfByteValue( writer, 1, 1 ) ); // 1 line style
			//AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			addChild( new SwfByteValue( writer, 20, 2 ) ); // line width 20
			addChild( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
			addChild( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)

			addChild( new SwfShape( writer, path, true, true ) );
		}

	}


	class SwfMorphShapeTag extends SwfTag 
	{

		SwfByteValue offset;
		SwfShape shape1, shape2;

		public SwfMorphShapeTag( SwfWriter writer, int characterID, GeneralPath[] paths ) //: base( writer, 46 )
		{
			super( writer, 46 );
			addChild( new SwfByteValue( writer, characterID, 2 ) );
			for(int i=0; i<2; i++)
			{
				Rectangle2D b2d = paths[i].getBounds2D();
				Rectangle2D.Float bounds = new Rectangle2D.Float( (float)b2d.getX(), (float)b2d.getY(), (float)b2d.getWidth(), (float)b2d.getHeight() );
				bounds.x -= 10;
				bounds.y -= 10;
				bounds.width += 20;
				bounds.height += 20;
				addChild( new SwfRect( writer, bounds ) );
			}

			offset = new SwfByteValue( writer, 0, 4 );
			addChild( offset );

			addChild( new SwfByteValue( writer, 0, 1 ) );
			addChild( new SwfByteValue( writer, 1, 1 ) );
			addChild( new SwfByteValue( writer, 20, 2 ) );
			addChild( new SwfByteValue( writer, 20, 2 ) );
			addChild( new SwfByteValue( writer, 0, 3 ) );
			addChild( new SwfByteValue( writer, 255, 1 ) );
			addChild( new SwfByteValue( writer, 0, 3 ) );
			addChild( new SwfByteValue( writer, 255, 1 ) );

			//AddAttribute( new SwfBitValue( writer, 0, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );

			// change ? AddAttribute( new SwfShape( writer, paths[0], true, true, 1 ) );
			//AddAttribute( new SwfByteValue( writer, 0, 1 ) );
			// change ? AddAttribute( new SwfShape( writer, paths[1], true, false, 0 ) );

			shape1 = (SwfShape) swfData.get( swfData.size()-2 );
			shape2 = (SwfShape) swfData.get( swfData.size()-1 );
		}

		@Override
		public void calcSize()
		{
			writer.nextByte();
			int startByte = writer.byteCount;
			for(int i=1; i<swfData.size(); i++)
			{
				if( getChild(i) == offset )
				{
					writer.nextByte();
					offset.val = writer.byteCount + 4;
				} 
				else if( getChild(i) == shape2 )
				{
					writer.nextByte();
					offset.val = writer.byteCount - offset.val +2;
				}
				getChild(i).calcSize();
			}
			writer.nextByte();
			int endByte = writer.byteCount;
			int tagLength = endByte - startByte;
			swfData.set( 0, new SwfTagHeader( writer, tagID, tagLength ) );
			getChild(0).calcSize();
		}

	}

	class SwfGradient extends SwfStruct 
	{

		public SwfGradient( SwfWriter writer, byte[] ratios, Color[] colors, boolean hasAlpha ) //: base( writer )
		{
			super( writer );
			addChild( new SwfByteValue( writer, ratios.length, 1 ) ); // number of gradient records
			for(int i=0; i<ratios.length; i++)
			{
				addChild( new SwfByteValue( writer, ratios[i], 1 ) ); // ratio of gradient control point
				addChild( new SwfByteValue( writer, colors[i].getRed(), 1 ) ); // RGBA vals of gradient control point
				addChild( new SwfByteValue( writer, colors[i].getGreen(), 1 ) );
				addChild( new SwfByteValue( writer, colors[i].getBlue(), 1 ) );
				if( hasAlpha )
					addChild( new SwfByteValue( writer, colors[i].getAlpha(), 1 ) );

			}
		}

	}

	class SwfShape extends SwfStruct 
	{

		public SwfShape( SwfWriter writer, ExtendedGeneralPath path, boolean hasNewStyles, boolean shapeHasFill ) //: base( writer )
		{
			super( writer );
			addChild( SwfByteValue.getEmptyValue(writer) );
			addChild( new SwfBitValue( writer, 1, 4 ) ); // one fill style
			addChild( new SwfBitValue( writer, 1, 4 ) ); // one line style

			PathIterator iterator = path.getPathIterator( new AffineTransform() );
			int type = 0;
			Point2D.Float position = new Point2D.Float();
			Point2D.Float startPos = new Point2D.Float();
			
			float[] points = new float[6];
			
			boolean isFirstRecord = true;
			while( ! iterator.isDone() ) {
				type = iterator.currentSegment( points );
				
				addChild( new SwfShapeRecord( writer, points, type, position, hasNewStyles, isFirstRecord, isFirstRecord, shapeHasFill ? 1 : 0 ) );
				if( isFirstRecord ) {
					startPos.x = position.x;
					startPos.y = position.y;
				}
				isFirstRecord = false;
				iterator.next();
			}
			if( position.x != startPos.x || position.y != startPos.y ) { // close shape if necessary
				points[0] = startPos.x;
				points[1] = startPos.y;
				addChild( new SwfShapeRecord( writer, points, PathIterator.SEG_MOVETO, position, hasNewStyles, false, false, shapeHasFill ? 1 : 0 ) );
			}
			addChild( new SwfBitValue( writer, 0, 6 ) ); // end of shape record
		}

	}

	class SwfShapeRecord extends SwfStruct 
	{

		public SwfShapeRecord( SwfWriter writer, float[] points, int type, Point2D.Float position, boolean hasNewStyles, boolean setFillStyle, boolean setLineStyle, int initFillStyle ) //: base( writer )
		{
			super( writer );
			
//			if( start==0 )
			if( type == PathIterator.SEG_MOVETO )
			{
				// initial or connecting move record
				// it seems that moveTo coordinates have to be ABSOLUTE and NOT RELATIVE !!!

//				System.out.println( "move" );

				// move record

				int[] vals = {
								 (int) (20*(points[0]-position.x)),
								 (int) (20*(points[1]-position.y))
							 };

				addChild( new SwfBitValue( writer, false ) ); // non-edge record

				//if( hasNewStyles ) // this was already commented out in the C# version
					addChild( new SwfBitValue( writer, false ) ); // new styles not implemented

				addChild( new SwfBitValue( writer, setLineStyle ) );

				addChild( new SwfBitValue( writer, false ) );
				addChild( new SwfBitValue( writer, setFillStyle ) ); // here only fill style 0 is set

				addChild( new SwfBitValue( writer, true ) );
				SwfBitValue.addBitValueAttributes(this,vals,5,true);

				if( setFillStyle )
				{
					addChild( new SwfBitValue( writer, initFillStyle, 1 ) ); // fill style 0 uses the 1st fill style with 1 fill style bit
					// AddAttribute( new SwfBitValue( writer, 0, 1 ) ); // fill style 1 uses no fill style with 1 fill style bit
					// shape has to be drawn counter-clockwise !
				}
				if( setLineStyle )
					addChild( new SwfBitValue( writer, 1, 1 ) );
				position.x = points[0];
				position.y = points[1];
				return;
			}

			addChild( new SwfBitValue( writer, true ) ); // edge record

			switch( type )
			{
				case PathIterator.SEG_QUADTO: // (byte) PathPointType.Bezier3:
//					System.out.println( "quad" );
					addChild( new SwfBitValue( writer, false ) ); // curve edge record
					int[] vals =  new int[]{
							 (int) (20*(points[0]-position.x)),
							 (int) (20*(points[1]-position.y)),
							 (int) (20*(points[2]-points[0])),
							 (int) (20*(points[3]-points[1])),
					};
					SwfBitValue.addBitValueAttributes(this,vals,4,true);
					((SwfBitValue)swfData.get( swfData.size()-5 )).val -= 2;
					position.x = points[2];
					position.y = points[3];
					break;

				case PathIterator.SEG_LINETO: //(byte) PathPointType.Line:
//					System.out.println( "line" );
					addChild( new SwfBitValue( writer, true ) ); // straight edge record
					vals = new int[]{
									 (int) (20*(points[0]-position.x)),
									 (int) (20*(points[1]-position.y))
								 };
					int nBits = SwfBitValue.calcBitSize( vals, true );

					addChild( new SwfBitValue( writer, nBits-2, 4 ) );
					addChild( new SwfBitValue( writer, true ) );
					for(int i=0; i<2; i++)
						addChild( new SwfBitValue( writer, vals[i], nBits ) );
					position.x = points[0];
					position.y = points[1];
					break;
				default:
//					throw new Exception( "unexpected path type encountered: "+type );
			}
		}
		
		public static Point2D.Float CalcIntersectionAbs( Point2D.Float a, Point2D.Float da, Point2D.Float b, Point2D.Float db )
		{
			Point2D.Float da2 = new Point2D.Float(da.x,da.y), db2 = new Point2D.Float(db.x,db.y);
			da2.x -= a.x;
			da2.y -= a.y;
			db2.x -= b.x;
			db2.y -= b.y;
			return CalcIntersection(a,da2,b,db2);
		}
		public static Point2D.Float CalcIntersection( Point2D.Float a, Point2D.Float da, Point2D.Float b, Point2D.Float db )
		{
			float m = (float) ( ( b.y - a.y ) * da.x - ( b.x - a.x ) * da.y ) / (float) ( db.x * da.y - db.y * da.x );
			return new Point2D.Float( (float) (b.x + m * db.x), (float) (b.y + m * db.y) );
		}
	}

