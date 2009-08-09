using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Collections;

namespace MatrixMaker
{

	///*
	class SwfData 
	{

		public int val;
		public SwfWriter writer;

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

		public virtual void calcSize()
		{
		}

		public virtual void write()
		{
		}

	}


	class SwfBitValue : SwfData 
	{

		int bitSize;

		public SwfBitValue(SwfWriter writer, int val, int bitSize) : base(writer,val)
		{
			//base( writer, val );
			this.bitSize = bitSize;
		}

		public SwfBitValue(SwfWriter writer, int val, bool isSigned) : base( writer, val )
		{
			this.bitSize = calcBitSize(val,isSigned);
		}

		public SwfBitValue(SwfWriter writer, int val) : base( writer, val )
		{
			this.bitSize = calcBitSize(val,true);
		}

		public SwfBitValue(SwfWriter writer, double val) : base( writer, (int) (val * Math.Pow(2,16)) )
		{
			this.bitSize = calcBitSize(this.val,true);
		}

		public SwfBitValue(SwfWriter writer, bool val) : base( writer, val ? -1 : 0 )
		{
			this.bitSize = 1;
		}

		public override void calcSize()
		{
			writer.bitOffset += bitSize;
		}

		public override void write()
		{

			writer.bitsToBytes();

			long longValue = val;
			long longMask = (long) Math.Pow(2,bitSize) - 1;
			int gap = ( 8 - (( bitSize + writer.bitOffset ) % 8 ));
			longValue <<= gap;
			longMask <<= gap;


			int mask = (int) Math.Pow(2,8) - 1;
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

		public static int calcBitSize(int val, bool isSigned)
		{

			if( isSigned && val < 0 )
				val = ~val;

			int size = 0;
			while( val != 0 )
			{
				val >>= 1;
				size++;
			}

			if( size == 0 )
				size++;
			if( isSigned )
				size++;

			return size;

		}

		public static int calcBitSize(int[] val, bool isSigned)
		{

			int size = 0;
			for(int i=0; i<val.Length; i++)
			{
				size = Math.Max( size, calcBitSize( val[i], isSigned ) );
			}
			return size;

		}

		///*
		public static void addBitValueAttributes(SwfStruct structure, int[] vals, int size, bool isSigned)
		{
			int bitSize = calcBitSize( vals, isSigned );
			structure.AddAttribute( new SwfBitValue( structure.writer, bitSize, size ) );
			for(int i=0; i<vals.Length;i++)
				structure.AddAttribute( new SwfBitValue( structure.writer, vals[i], bitSize ) );
		}
		//*/

	}


	class SwfByteValue : SwfData 
	{

		int byteSize;

		public SwfByteValue(SwfWriter writer, int val, int byteSize) : base( writer, val )
		{
			this.byteSize = byteSize;
		}

		public SwfByteValue(SwfWriter writer, double val, int byteSize) : base( writer, (int) (val * Math.Pow(2,16)) )
		{
			this.byteSize = byteSize;
		}

		public override void calcSize()
		{
			writer.nextByte();
			writer.byteCount += byteSize;
		}

		public override void write()
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

		public static SwfByteValue getEmptyValue( SwfWriter writer )
		{
			return new SwfByteValue( writer, 0, 0 );
		}
	}

	class SwfByteArray : SwfData 
	{

		byte[] source;

		public SwfByteArray(SwfWriter writer, byte[] source) : base( writer )
		{
			this.source = source;
		}

		public override void calcSize()
		{
			writer.nextByte();
			if( source == null )
				return;
			writer.byteCount += source.Length;
		}

		public override void write()
		{
			writer.nextByte();
			if( source == null )
				return;
			Array.Copy( source, 0, writer.buffer, writer.byteCount, source.Length );

			//for(int i=0; i<source.Length; i++)
			//writer.buffer[writer.byteCount+i] = source[i];
			writer.byteCount += source.Length;
		}
	}

	class SwfStruct : SwfData 
	{

		public ArrayList attributes = new ArrayList();

		public SwfStruct( SwfWriter writer ) : base( writer )
		{
		}
		public override void calcSize()
		{
			for(int i=0; i<attributes.Count; i++)
			{
				getAttribute(i).calcSize();
			}
		}

		public override void write()
		{
			for(int i=0; i<attributes.Count; i++)
			{
				getAttribute(i).write();
			}
		}

		public SwfData getAttribute( int i )
		{
			if( i >= 0 && i < attributes.Count )
				return (SwfData) attributes[i];
			return null;
		}

		public virtual void AddAttribute( SwfData attribute )
		{
			attributes.Add(attribute);
		}

	}

	class SwfTagHeader : SwfStruct 
	{

		public SwfTagHeader(SwfWriter writer, int tagID, int tagLength) : base(writer)
		{
			int code = tagID;
			code <<= 6;
			if( tagLength < 63 && tagLength >= 0 )
			{
				code += tagLength;
				AddAttribute( new SwfByteValue( writer, code, 2 ) );
			} 
			else 
			{
				code += 63;
				AddAttribute( new SwfByteValue( writer, code, 2 ) );
				AddAttribute( new SwfByteValue( writer, tagLength, 4 ) );
			}
		}

	}

	class SwfTag : SwfStruct 
	{

		public int tagID;

		public SwfTag( SwfWriter writer, int tagID ) : base( writer )
		{
			this.tagID = tagID;
			AddAttribute( new SwfTagHeader( writer, tagID, 0 ) );
		}

		public override void calcSize()
		{
			writer.nextByte();
			int startByte = writer.byteCount;
			for(int i=1; i<attributes.Count; i++)
			{
				getAttribute(i).calcSize();
			}
			writer.nextByte();
			int endByte = writer.byteCount;
			int tagLength = endByte - startByte;
			attributes[0] = new SwfTagHeader( writer, tagID, tagLength );
			getAttribute(0).calcSize();
		}

	}

	class SwfRect : SwfStruct 
	{

		public SwfRect( SwfWriter writer, RectangleF rect ) : base( writer )
		{
			AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			int[] vals = { (int)rect.Left*20, (int)rect.Right*20, (int)rect.Top*20, (int)rect.Bottom*20 };
			int bitSize = SwfBitValue.calcBitSize( vals, true );
			AddAttribute( new SwfBitValue( writer, bitSize, 5 ) );
			for(int i=0; i<4; i++)
				AddAttribute( new SwfBitValue( writer, vals[i], bitSize ) );
		}

	}

	class SwfFileHeader : SwfStruct 
	{

		public static int FILE_LENGTH = 4;
		public static int FRAME_COUNT = 7;

		public SwfFileHeader( SwfWriter writer, int version, Rectangle size, int rate ) : base( writer )
		{
			AddAttribute( new SwfByteValue( writer, (int) 'F', 1 ) );
			AddAttribute( new SwfByteValue( writer, (int) 'W', 1 ) );
			AddAttribute( new SwfByteValue( writer, (int) 'S', 1 ) );
			AddAttribute( new SwfByteValue( writer, version, 1 ) );
			AddAttribute( new SwfByteValue( writer, 0, 4 ) );
			AddAttribute( new SwfRect( writer, size ) );
			AddAttribute( new SwfByteValue( writer, rate * 256, 2 ) );
			AddAttribute( new SwfByteValue( writer, 0, 2 ) );
		}

		public void setFileLength( int fileLength )
		{
			attributes[FILE_LENGTH] = new SwfByteValue( writer, fileLength, 4 );
		}

	}

	class SwfFile : SwfStruct 
	{

		SwfFileHeader fileHeader;

		public SwfFile( SwfWriter writer, int version, Rectangle size, int rate ) : base( writer )
		{
			fileHeader = new SwfFileHeader( writer, version, size, rate );
			attributes.Add( fileHeader );
			attributes.Add( new SwfEndTag( writer ) );
		}

		public override void calcSize() 
		{
			base.calcSize();
			writer.nextByte();
			fileHeader.setFileLength( writer.byteCount );
		}

		public override void AddAttribute( SwfData attribute )
		{
			attributes.Insert( attributes.Count - 1, attribute );
		}

		public void addFrame()
		{
			AddAttribute( new SwfShowFrameTag( writer ) );
			fileHeader.getAttribute(SwfFileHeader.FRAME_COUNT).val++;
		}

	}

	class SwfDefineSpriteTag : SwfTag 
	{

		SwfByteValue frameCount;

		public SwfDefineSpriteTag( SwfWriter writer, int spriteID ) : base( writer, 39 )
		{
			attributes.Add( new SwfByteValue( writer, spriteID, 2 ) );
			frameCount = new SwfByteValue( writer, 0, 2 );
			attributes.Add( frameCount );
		}

		public void addFrame()
		{
			AddAttribute( new SwfShowFrameTag( writer ) );
			frameCount.val++;
		}

	}

	class SwfEndTag : SwfTag 
	{

		public SwfEndTag( SwfWriter writer ) : base( writer, 0 )
		{
		}

	}

	class SwfShowFrameTag : SwfTag
	{

		public SwfShowFrameTag( SwfWriter writer ) : base( writer, 1 )
		{
		}

	}

	class SwfSetBackgroundColorTag : SwfTag
	{

		public SwfSetBackgroundColorTag( SwfWriter writer, int red, int green, int blue ) : base( writer, 9 )
		{
			AddAttribute( new SwfRGBRecord( writer, red, green, blue ) );
		}
		public SwfSetBackgroundColorTag( SwfWriter writer, Color color ) : base( writer, 9 )
		{
			AddAttribute( new SwfRGBRecord( writer, color.R, color.G, color.B ) );
		}

	}

	class SwfRGBRecord : SwfStruct
	{

		public SwfRGBRecord( SwfWriter writer, int red, int green, int blue ) : base( writer )
		{
			AddAttribute( new SwfByteValue( writer, red, 1 ) );
			AddAttribute( new SwfByteValue( writer, green, 1 ) );
			AddAttribute( new SwfByteValue( writer, blue, 1 ) );
		}

	}

	class SwfResourceTag : SwfTag
	{

		public SwfResourceTag( SwfWriter writer, int tagID, int characterID, byte[] source ) : base( writer, tagID )
		{  
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			AddAttribute( new SwfByteArray( writer, source ) );
		}

		/*
		public SwfResourceTag( SwfWriter writer, int tagID, int characterID, byte[] source ) : base( writer, tagID )
		{  
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			AddAttribute( new SwfByteArray( writer, source ) );
		}
		*/

	}

	class SwfPlaceObject2Tag : SwfTag 
	{

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, int characterID, Matrix matrix, bool replace ) : base( writer, 26 )
		{

			for(int i=0; i<5; i++)
				AddAttribute( new SwfBitValue( writer, false ) );

			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, replace ) );

			AddAttribute( new SwfByteValue( writer, depth, 2 ) );
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			AddAttribute( new SwfMatrix( writer, matrix ) );

		}

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, int characterID, Matrix matrix, bool replace, string spriteName ) : base( writer, 26 )
		{

			for(int i=0; i<2; i++)
				AddAttribute( new SwfBitValue( writer, false ) );
			AddAttribute( new SwfBitValue( writer, true ) ); // has name
			for(int i=0; i<2; i++)
				AddAttribute( new SwfBitValue( writer, false ) );

			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, replace ) );

			AddAttribute( new SwfByteValue( writer, depth, 2 ) );
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			AddAttribute( new SwfMatrix( writer, matrix ) );

			AddAttribute( new SwfString( writer, spriteName ) );
			//char[] chars = spriteName.ToCharArray();
			//for(int i=0; i<chars.Length; i++)
				//AddAttribute( new SwfByteValue( writer, (byte) chars[i], 1 ) );
			//AddAttribute( new SwfByteValue( writer, 0, 1 ) );

		}

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, int characterID, Matrix matrix, int ratio, bool replace ) : base( writer, 26 )
		{
			for(int i=0; i<3; i++)
				AddAttribute( new SwfBitValue( writer, false ) );

			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, false ) );

			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, replace ) );

			AddAttribute( new SwfByteValue( writer, depth, 2 ) );
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			AddAttribute( new SwfMatrix( writer, matrix ) );
			AddAttribute( new SwfByteValue( writer, ratio, 2 ) ); // ratio for morph shape tweens

		}

		public SwfPlaceObject2Tag( SwfWriter writer, int depth, Matrix matrix ) : base( writer, 26 )
		{

			for(int i=0; i<5; i++)
				AddAttribute( new SwfBitValue( writer, false ) );

			AddAttribute( new SwfBitValue( writer, true ) );
			AddAttribute( new SwfBitValue( writer, false ) );
			AddAttribute( new SwfBitValue( writer, true ) );

			AddAttribute( new SwfByteValue( writer, depth, 2 ) );
			AddAttribute( new SwfMatrix( writer, matrix ) );
		}

	}

	class SwfString : SwfStruct
	{
		public SwfString( SwfWriter writer, string s ) : base( writer )
		{
			byte[] b = new byte[s.Length+1];
			for(int i=0; i<s.Length; i++)
			{
				b[i] = (byte) s[i];
			}
			b[s.Length] = (byte) 0;
			AddAttribute( new SwfByteArray( writer, b ) );
		}
	}

	class SwfImportAssetsTag : SwfTag
	{
		public SwfImportAssetsTag( SwfWriter writer, string url, Hashtable identifiers ) : base( writer, 57 )
		{
			AddAttribute( new SwfString(writer, url) );
			AddAttribute( new SwfByteValue( writer, identifiers.Count, 2 ) );
			foreach( int key in identifiers.Keys )
			{
				AddAttribute( new SwfByteValue( writer, key, 2 ) );
				AddAttribute( new SwfString( writer, (string) identifiers[key] ) );
			}
		}
	}

	class SwfExportAssetsTag : SwfTag
	{
		public SwfExportAssetsTag( SwfWriter writer, Hashtable identifiers ) : base( writer, 56 )
		{
			AddAttribute( new SwfByteValue( writer, identifiers.Count, 2 ) );
			foreach( int key in identifiers.Keys )
			{
				AddAttribute( new SwfByteValue( writer, key, 2 ) );
				AddAttribute( new SwfString( writer, (string) identifiers[key] ) );
			}
		}
		public SwfExportAssetsTag( SwfWriter writer, int key, string identifier ) : base( writer, 56 )
		{
			// for only one asset to export
			AddAttribute( new SwfByteValue( writer, 1, 2 ) );
			AddAttribute( new SwfByteValue( writer, key, 2 ) );
			AddAttribute( new SwfString( writer, identifier ) );
		}
	}

	class SwfFrameLabelTag : SwfTag
	{
		public SwfFrameLabelTag( SwfWriter writer, string label ) : base( writer, 43 )
		{
			AddAttribute( new SwfString( writer, label ) );
		}
	}

	class SwfActionGotoFrameTag : SwfTag 
	{
		public SwfActionGotoFrameTag( SwfWriter writer, int frameCount ) : base( writer, 12 )
		{
			// execute goto frame action for a frame in the current timeline

			AddAttribute( new SwfByteValue( writer, 0x81, 1 ) ); // goto frame action
			AddAttribute( new SwfByteValue( writer, 2, 2 ) ); // record length, always 2
			P.r( frameCount );
			AddAttribute( new SwfByteValue( writer, frameCount, 2 ) ); // target frame count, zero-based
			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionGotoLabelTag : SwfTag 
	{
		public SwfActionGotoLabelTag( SwfWriter writer, string label ) : base( writer, 12 )
		{
			// execute goto label action for a label in the current timeline

			AddAttribute( new SwfByteValue( writer, 0x8c, 1 ) ); // goto label action
			AddAttribute( new SwfByteValue( writer, label.Length + 1, 2 ) ); // record length
			AddAttribute( new SwfString( writer, label ) );
			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionGotoAndStopTag : SwfTag 
	{
		public SwfActionGotoAndStopTag( SwfWriter writer, string label ) : base( writer, 12 )
		{
			// execute goto label action for a label in the current timeline

			AddAttribute( new SwfByteValue( writer, 0x07, 1 ) ); // stop action
			AddAttribute( new SwfByteValue( writer, 0x8c, 1 ) ); // goto label action
			AddAttribute( new SwfByteValue( writer, label.Length + 1, 2 ) ); // record length
			AddAttribute( new SwfString( writer, label ) );
			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionGotoAndPlayTag : SwfTag 
	{
		public SwfActionGotoAndPlayTag( SwfWriter writer, int frameCount ) : base( writer, 12 )
		{
			// execute goto frame action for a frame in the current timeline

			AddAttribute( new SwfByteValue( writer, 0x81, 1 ) ); // goto frame action
			AddAttribute( new SwfByteValue( writer, 2, 2 ) ); // record length, always 2
			AddAttribute( new SwfByteValue( writer, frameCount, 2 ) ); // target frame count, zero-based
			AddAttribute( new SwfByteValue( writer, 0x06, 1 ) ); // play action
			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfActionStopTag : SwfTag 
	{
		public SwfActionStopTag( SwfWriter writer ) : base( writer, 12 )
		{
			// execute stop action for the current timeline

			AddAttribute( new SwfByteValue( writer, 0x07, 1 ) ); // stop action
			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // end action flag
		}
	}

	class SwfDoActionTag : SwfTag 
	{

		public SwfDoActionTag( SwfWriter writer, string spriteName, int frameNumber ) : base( writer, 12 )
		{
			// execute goto frame action for a sprite

			AddAttribute( new SwfByteValue( writer, 0x8b, 1 ) ); // set target action
			AddAttribute( new SwfByteValue( writer, spriteName.Length + 1, 2 ) ); // record length

			char[] chars = spriteName.ToCharArray();
			for(int i=0; i<chars.Length; i++)
				AddAttribute( new SwfByteValue( writer, (byte) chars[i], 1 ) );
			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // end of string

			AddAttribute( new SwfByteValue( writer, 0x81, 1 ) ); // goto frame action
			AddAttribute( new SwfByteValue( writer, 2, 2 ) ); // record length
			AddAttribute( new SwfByteValue( writer, frameNumber, 2 ) ); // set target action

			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // end of actions
		}

	}

	class SwfMatrix : SwfStruct
	{
		public SwfMatrix( SwfWriter writer, Matrix matrix ) : base( writer )
		{
			float pow = (float) Math.Pow(2,16);
			int[] scale = { (int) (pow*(float)matrix.Elements[0]), (int) (pow*(float)matrix.Elements[3]) };
			int[] rotate = { (int) (pow*(float)matrix.Elements[1]), (int) (pow*(float)matrix.Elements[2]) };
			int[] translate = { (int) (20f*matrix.Elements[4]), (int) (20f*matrix.Elements[5]) };

			bool hasScale = ( matrix.Elements[0] != 1f || matrix.Elements[3] != 1f );
			AddAttribute( new SwfBitValue( writer, hasScale ) );
			if( hasScale )
				SwfBitValue.addBitValueAttributes(this,scale,5,true);

			bool hasRotate = ( matrix.Elements[1] != 0f || matrix.Elements[2] != 0f );
			AddAttribute( new SwfBitValue( writer, hasRotate ) );
			if( hasRotate )
				SwfBitValue.addBitValueAttributes(this,rotate,5,true);

			SwfBitValue.addBitValueAttributes(this,translate,5,true);

		}
	}

	class SwfRemoveObject2Tag : SwfTag 
	{

		public SwfRemoveObject2Tag( SwfWriter writer, int depth ) : base( writer, 28 )
		{
			AddAttribute( new SwfByteValue( writer, depth, 2 ) );
		}

	}

	/*
	class SwfDefineShapeTag : SwfTag {

	  public SwfDefineShapeTag( SwfWriter writer, int characterID, GraphicsPath path ){
		base( writer, 2 );
		AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
		Rectangle bounds = path.getBounds();
		bounds.x -= 10;
		bounds.y -= 10;
		bounds.width += 20;
		bounds.height += 20;
		AddAttribute( new SwfRect( writer, bounds ) );
		AddAttribute( new SwfByteValue( writer, 0, 1 ) );
		AddAttribute( new SwfByteValue( writer, 1, 1 ) );
		AddAttribute( new SwfByteValue( writer, 20, 2 ) );
		AddAttribute( new SwfByteValue( writer, 0, 3 ) );
		AddAttribute( new SwfShape( writer, path, true, false, 1 ) );
	  }

	}
	//*/
	class SwfDefineShapeTag : SwfTag 
	{
		public SwfDefineShapeTag( SwfWriter writer, int characterID, GraphicsPath path, byte[] ratios, Color[] colors, Matrix matrix ) : base( writer, 2 )
		{
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			RectangleF bounds = path.GetBounds();
			bounds.X -= 10;
			bounds.Y -= 10;
			bounds.Width += 20;
			bounds.Height += 20;
			AddAttribute( new SwfRect( writer, bounds ) );
			AddAttribute( new SwfByteValue( writer, 1, 1 ) ); // 1 fill style
			AddAttribute( new SwfByteValue( writer, 0x12, 1 ) ); // radial gradient
			AddAttribute( new SwfMatrix( writer, matrix ) ); // matrix for gradient fill
			AddAttribute( new SwfGradient( writer, ratios, colors, false ) ); // gradient fill definition
			AddAttribute( new SwfByteValue( writer, 1, 1 ) ); // 1 line style
			//AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			AddAttribute( new SwfByteValue( writer, 40, 2 ) ); // line width 40
			AddAttribute( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
			//AddAttribute( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)

			AddAttribute( new SwfShape( writer, path, false ) );
		}

	}
	class SwfDefineShape3Tag : SwfTag 
	{

		public SwfDefineShape3Tag( SwfWriter writer, int characterID, GraphicsPath path ) : base( writer, 32 )
		{
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			RectangleF bounds = path.GetBounds();
			bounds.X -= 10;
			bounds.Y -= 10;
			bounds.Width += 20;
			bounds.Height += 20;
			AddAttribute( new SwfRect( writer, bounds ) );
			AddAttribute( new SwfByteValue( writer, 0, 1 ) ); // 0 fill styles
			AddAttribute( new SwfByteValue( writer, 1, 1 ) ); // 1 line style
			//AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			AddAttribute( new SwfByteValue( writer, 40, 2 ) ); // line width 40
			AddAttribute( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
			AddAttribute( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)

			AddAttribute( new SwfShape( writer, path, true ) );
		}
		public SwfDefineShape3Tag( SwfWriter writer, int characterID, GraphicsPath path, byte[] ratios, Color[] colors, Matrix matrix ) : base( writer, 32 )
		{
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			RectangleF bounds = path.GetBounds();
			bounds.X -= 10;
			bounds.Y -= 10;
			bounds.Width += 20;
			bounds.Height += 20;
			AddAttribute( new SwfRect( writer, bounds ) );
			AddAttribute( new SwfByteValue( writer, 1, 1 ) ); // 1 fill style
			AddAttribute( new SwfByteValue( writer, 0x12, 1 ) ); // radial gradient
			AddAttribute( new SwfMatrix( writer, matrix ) ); // matrix for gradient fill
			AddAttribute( new SwfGradient( writer, ratios, colors, true ) ); // gradient fill definition
			AddAttribute( new SwfByteValue( writer, 1, 1 ) ); // 1 line style
			//AddAttribute( SwfByteValue.getEmptyValue( writer ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );
			AddAttribute( new SwfByteValue( writer, 20, 2 ) ); // line width 20
			AddAttribute( new SwfByteValue( writer, 0, 3 ) ); // RGB to 0 (black)
			AddAttribute( new SwfByteValue( writer, 255, 1 ) ); // A to 255 (full opacity)

			AddAttribute( new SwfShape( writer, path, true ) );
		}

	}


	class SwfMorphShapeTag : SwfTag 
	{

		SwfByteValue offset;
		SwfShape shape1, shape2;

		public SwfMorphShapeTag( SwfWriter writer, int characterID, GraphicsPath[] paths ) : base( writer, 46 )
		{
			AddAttribute( new SwfByteValue( writer, characterID, 2 ) );
			for(int i=0; i<2; i++)
			{
				RectangleF bounds = paths[i].GetBounds();
				bounds.X -= 10;
				bounds.Y -= 10;
				bounds.Width += 20;
				bounds.Height += 20;
				AddAttribute( new SwfRect( writer, bounds ) );
			}

			offset = new SwfByteValue( writer, 0, 4 );
			AddAttribute( offset );

			AddAttribute( new SwfByteValue( writer, 0, 1 ) );
			AddAttribute( new SwfByteValue( writer, 1, 1 ) );
			AddAttribute( new SwfByteValue( writer, 20, 2 ) );
			AddAttribute( new SwfByteValue( writer, 20, 2 ) );
			AddAttribute( new SwfByteValue( writer, 0, 3 ) );
			AddAttribute( new SwfByteValue( writer, 255, 1 ) );
			AddAttribute( new SwfByteValue( writer, 0, 3 ) );
			AddAttribute( new SwfByteValue( writer, 255, 1 ) );

			//AddAttribute( new SwfBitValue( writer, 0, 4 ) );
			//AddAttribute( new SwfBitValue( writer, 1, 4 ) );

			// change ? AddAttribute( new SwfShape( writer, paths[0], true, true, 1 ) );
			//AddAttribute( new SwfByteValue( writer, 0, 1 ) );
			// change ? AddAttribute( new SwfShape( writer, paths[1], true, false, 0 ) );

			shape1 = (SwfShape) attributes[ attributes.Count-2 ];
			shape2 = (SwfShape) attributes[attributes.Count-1];
		}

		public override void calcSize()
		{
			writer.nextByte();
			int startByte = writer.byteCount;
			for(int i=1; i<attributes.Count; i++)
			{
				if( getAttribute(i) == offset )
				{
					writer.nextByte();
					offset.val = writer.byteCount + 4;
				} 
				else if( getAttribute(i) == shape2 )
				{
					writer.nextByte();
					offset.val = writer.byteCount - offset.val +2;
				}
				getAttribute(i).calcSize();
			}
			writer.nextByte();
			int endByte = writer.byteCount;
			int tagLength = endByte - startByte;
			attributes[0] = new SwfTagHeader( writer, tagID, tagLength );
			getAttribute(0).calcSize();
		}

	}

	class SwfGradient : SwfStruct 
	{

		public SwfGradient( SwfWriter writer, byte[] ratios, Color[] colors, bool hasAlpha ) : base( writer )
		{
			AddAttribute( new SwfByteValue( writer, ratios.Length, 1 ) ); // number of gradient records
			for(int i=0; i<ratios.Length; i++)
			{
				AddAttribute( new SwfByteValue( writer, ratios[i], 1 ) ); // ratio of gradient control point
				AddAttribute( new SwfByteValue( writer, colors[i].R, 1 ) ); // RGBA vals of gradient control point
				AddAttribute( new SwfByteValue( writer, colors[i].G, 1 ) );
				AddAttribute( new SwfByteValue( writer, colors[i].B, 1 ) );
				if( hasAlpha )
					AddAttribute( new SwfByteValue( writer, colors[i].A, 1 ) );

			}
		}

	}

	class SwfShape : SwfStruct 
	{

		public SwfShape( SwfWriter writer, GraphicsPath path, bool hasNewStyles ) : base( writer )
		{
			//System.Console.WriteLine( "SwfShape()" );
			AddAttribute( SwfByteValue.getEmptyValue(writer) );
			AddAttribute( new SwfBitValue( writer, 1, 4 ) ); // 1 fill style
			AddAttribute( new SwfBitValue( writer, 1, 4 ) ); // 1 line style

			GraphicsPathIterator iterator = new GraphicsPathIterator( path );
			iterator.Rewind();
			bool b;
			iterator.NextSubpath(new GraphicsPath(),out b);
			int index, start, end;
			byte type = 0;
			PointF position = new PointF();
			int step = 0;
			while( ( index = iterator.NextPathType( out type, out start, out end ) ) != 0 )
			{
				switch( type )
				{
					case (byte) PathPointType.Bezier3:
						step = 3;
						break;
					case (byte) PathPointType.Line:
						step = 1;
						break;
					default:
						throw new Exception( "unexpected path type encountered: "+type );
				}
				for(int i=start; i+step<=end; i+=step)
				{
					AddAttribute( new SwfShapeRecord( writer, path, type, i, i+step, position, hasNewStyles, false, false, 1 ) );
				}
			}
			AddAttribute( new SwfBitValue( writer, 0, 6 ) ); // end of shape record
		}

		/*
		public SwfShape_old( SwfWriter writer, GraphicsPath path, bool hasNewStyles ) : base( writer )
		{
    
			// for a single fill style and a single line style

			AddAttribute( SwfByteValue.getEmptyValue(writer) );
			AddAttribute( new SwfBitValue( writer, 1, 4 ) ); // 1 fill style bit
			AddAttribute( new SwfBitValue( writer, 1, 4 ) ); // 1 line style bit

			//float[] segmentPoints = new float[6];
			bool setFillStyle = true;
			bool setLineStyle = true;
			Point position = new Point();
			//int segmentType;
			//bool setLineStyle = true;
			GraphicsPathIterator iterator = new GraphicsPathIterator( path );
			iterator.Rewind();
			bool b;
			iterator.NextSubpath(new GraphicsPath(),out b);
			int index, start, end, count;
			byte type = 12;
			int step;
			while( ( index = iterator.NextPathType( out type, out start, out end ) ) != 0 )
			{
				switch( type )
				{
					default:
						throw new Exception( "this method needs to be implemented anew!" );
				}
				for(int i=start; i+step<=end; i++)
				{
					AddAttribute( new SwfShapeRecord( writer, path, type, start, end, position, hasNewStyles, false, false, 1 ) );
				}
				//setFillStyle = false;
				//setLineStyle = false;
			}
			AddAttribute( new SwfBitValue( writer, 0, 6 ) );
		}
		*/

	}

	class SwfShapeRecord : SwfStruct 
	{

		public SwfShapeRecord( SwfWriter writer, GraphicsPath path, byte type, int start, int end, PointF position, bool hasNewStyles, bool setFillStyle, bool setLineStyle, int initFillStyle ) : base( writer )
		{
			//int segmentType = path.PathTypes[index];
			//int[] indices = path.PathData
			PointF[] points = path.PathPoints;
			//PointF position = start == 0 ? new PointF() : points[start-1];
			//System.Console.WriteLine( (byte)PathPointType.Bezier+", "+(byte)PathPointType.Bezier3+", "+(byte)PathPointType.Line );

			//System.Console.WriteLine( "New Record" );
			//System.Console.WriteLine( position );
			//System.Console.WriteLine( points[start] );
			//System.Console.WriteLine( "position != points[start] ? " + (position != points[start]) );

			if( start==0 )
			{
				// initial or connecting move record
				// it seems that moveTo coordinates have to be ABSOLUTE and NOT RELATIVE !!!

				//System.Console.WriteLine( "Move" );

				// move record

				setFillStyle = setFillStyle || (start == 0);
				setLineStyle = setLineStyle || (start == 0);

				int[] vals = {
								 (int) (20*points[start].X),// - position.x,
								 (int) (20*points[start].Y)// - position.y
							 };

				AddAttribute( new SwfBitValue( writer, false ) ); // non-edge record

				//if( hasNewStyles )
					AddAttribute( new SwfBitValue( writer, false ) ); // new styles not implemented

				AddAttribute( new SwfBitValue( writer, setLineStyle ) );

				AddAttribute( new SwfBitValue( writer, false ) );
				AddAttribute( new SwfBitValue( writer, setFillStyle ) ); // here only fill style 0 is set

				AddAttribute( new SwfBitValue( writer, true ) );
				SwfBitValue.addBitValueAttributes(this,vals,5,true);

				if( setFillStyle )
				{
					AddAttribute( new SwfBitValue( writer, initFillStyle, 1 ) ); // fill style 0 uses the 1st fill style with 1 fill style bit
					// AddAttribute( new SwfBitValue( writer, 0, 1 ) ); // fill style 1 uses no fill style with 1 fill style bit
					// shape has to be drawn counter-clockwise !
				}
				if( setLineStyle )
					AddAttribute( new SwfBitValue( writer, 1, 1 ) );
			}

			AddAttribute( new SwfBitValue( writer, true ) ); // edge record

			switch( type )
			{
				case (byte) PathPointType.Bezier3:
					//System.Console.WriteLine( "Bezier" );
					//for(int i=start; i<=end; i++)
						//System.Console.WriteLine( ""+i+"\n"+path.PathPoints[i] );
					AddAttribute( new SwfBitValue( writer, false ) ); // curve edge record
					PointF c = MMStudio.CalcIntersectionAbs( points[start], points[start+1], points[end], points[end-1] );
					int[] vals =  new int[]{
									 (int) (20*(c.X - points[start].X)),
									 (int) (20*(c.Y - points[start].Y)),
									 (int) (20*(points[end].X - c.X)),
									 (int) (20*(points[end].Y - c.Y)),
					};
					SwfBitValue.addBitValueAttributes(this,vals,4,true);
					((SwfBitValue)attributes[ attributes.Count-5 ]).val -= 2;
					break;

				case (byte) PathPointType.Line:
					//System.Console.WriteLine( "Line" );
					AddAttribute( new SwfBitValue( writer, true ) ); // straight edge record
					vals = new int[]{
									 (int) (20*(points[end].X - points[start].X)),
									 (int) (20*(points[end].Y - points[start].Y))
								 };
					int nBits = SwfBitValue.calcBitSize( vals, true );

					AddAttribute( new SwfBitValue( writer, nBits-2, 4 ) );
					AddAttribute( new SwfBitValue( writer, true ) );
					for(int i=0; i<2; i++)
						AddAttribute( new SwfBitValue( writer, vals[i], nBits ) );
					break;
				default:
					throw new Exception( "unexpected path type encountered: "+type );
			}
		}
	}

			/*
			// hier noch unbedingt veraendern
			//if( segmentType == PathIterator.SEG_CLOSE || segmentType == PathIterator.SEG_CUBICTO ){
		  if( segmentType == PathPointType.CloseSubpath ){
			return;
		  }

		  bool isEdge = segmentType != PathIterator.SEG_MOVETO;
		  AddAttribute( new SwfBitValue( writer, isEdge ) );

		  if( isEdge ){
			bool isCurve = segmentType == PathIterator.SEG_QUADTO;
			AddAttribute( new SwfBitValue( writer, !isCurve ) );

			if( isCurve ){

			  //System.out.println("writing curve");

			  int[] vals = {
				((int)segmentPoints[0]) - position.x,
				((int)segmentPoints[1]) - position.y,
				((int)segmentPoints[2]) - position.x,
				((int)segmentPoints[3]) - position.y
			  };
			  vals[2] -= vals[0];
			  vals[3] -= vals[1];
			  SwfBitValue.addBitValueAttributes(this,vals,4,true);
			  ((SwfBitValue)attributes.get( attributes.Count-5 )).val -= 2;
			  position.x += vals[0];
			  position.y += vals[1];
			  position.x += vals[2];
			  position.y += vals[3];

			} else {

			  //System.out.println(segmentType == PathIterator.SEG_LINETO);
			  //System.out.println("writing straight line");

			  int[] vals = {
				((int)segmentPoints[0]) - position.x,
				((int)segmentPoints[1]) - position.y
			  };
			  int nBits = SwfBitValue.calcBitSize( vals, true );

			  AddAttribute( new SwfBitValue( writer, nBits-2, 4 ) );
			  AddAttribute( new SwfBitValue( writer, true ) );
			  for(int i=0; i<2; i++)
				AddAttribute( new SwfBitValue( writer, vals[i], nBits ) );
			  //SwfBitValue.addBitValueAttributes(this,vals,4,true);
			  //((SwfBitValue)attributes.get( attributes.Count-3 )).val -= 2;
			  //attributes.insertElementAt( new SwfBitValue( writer, true ), attributes.Count-2 );
			  position.x += vals[0];
			  position.y += vals[1];

			}

		  } else {
			// non-edge record
			// it seems that moveTo coordinates are ABSOLUTE and NOT RELATIVE !!!
			int[] vals = {
			  ((int)segmentPoints[0]),// - position.x,
			  ((int)segmentPoints[1])// - position.y
			};

			//if( vals[0] == 0 && vals[1] == 0 && !setLineStyle )
			  //return;

			if( hasNewStyles )
			  AddAttribute( new SwfBitValue( writer, false ) ); // new styles not implemented

			AddAttribute( new SwfBitValue( writer, setLineStyle ) );

			AddAttribute( new SwfBitValue( writer, setFillStyle ) );
			AddAttribute( new SwfBitValue( writer, setFillStyle ) ); // here both fill styles are set together

			//for(int i=0; i<2; i++)
			  //AddAttribute( new SwfBitValue( writer, false ) );

			//if( vals[0] != 0 || vals[1] != 0 ){
			  AddAttribute( new SwfBitValue( writer, true ) );
			  SwfBitValue.addBitValueAttributes(this,vals,5,true);
			//}
			//else AddAttribute( new SwfBitValue( writer, false ) );

			if( setFillStyle ){
			  AddAttribute( new SwfBitValue( writer, 1, 1 ) ); // fill style #1 with 1 fill style bit
			  AddAttribute( new SwfBitValue( writer, 0, 1 ) ); // no fill style with 1 fill style bit
			}
			if( setLineStyle )
			  AddAttribute( new SwfBitValue( writer, 1, 1 ) );

			position.x = vals[0];
			position.y = vals[1];
		  }
		  //*/

	//*/


}
