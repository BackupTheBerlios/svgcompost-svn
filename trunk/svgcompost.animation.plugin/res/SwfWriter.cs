using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Collections;

namespace MatrixMaker
{

	class SwfWriter 
	{

		MMData document;
		MMData frames;

		public byte[] buffer;
		public int byteCount;
		public int bitOffset;

		public SwfWriter( MMData document )
		{
			this.document = document;
			this.frames = document["_frames"];
		}
		public SwfWriter()
		{
		}

		/*
		 * not needed any more
		 * 
		public void makeResourceFile()
		{
			Matrix matrix = new Matrix();
			matrix.Translate( 200, 150 );

			Rectangle bounds = new Rectangle( 0, 0, 400, 300 );
			SwfFile swfFile = new SwfFile( this, 5, bounds, 3 );

			//System.Console.WriteLine("number of resources: "+MMImage.Resources.Keys.Count);
			foreach(int resourceID in MMImage.Resources.Keys)
			{
				//System.Console.WriteLine("loop for resource: "+resourceID);
				MMImage image = MMImage.GetImage( resourceID );
				//System.Console.WriteLine("image for resource tag: "+image.tagID);
				swfFile.AddAttribute( new SwfResourceTag (
					this, image.tagID, resourceID, image.swfCode //MMImage.GetSwfResource( resourceID )
					) );
				swfFile.AddAttribute( new SwfPlaceObject2Tag (
					this, 1, resourceID, matrix, false
					) );
				swfFile.addFrame();
				swfFile.AddAttribute( new SwfRemoveObject2Tag( this, 1 ) );
			}
			swfFile.calcSize();
			buffer = new byte[byteCount];
			reset();
			swfFile.write();
		}
		//*/
		public void convertData( SwfData swfData )
		{
			swfData.calcSize();
			buffer = new byte[byteCount];
			reset();
			swfData.write();
		}
		public void convertData()
		{

			ArrayList queues = new ArrayList();
			ArrayList frameLabels = new ArrayList();
			ArrayList frameActions = new ArrayList();
			ArrayList levels = new ArrayList();
			ArrayList resourceList = new ArrayList(); // now contains MMImage objects
			ArrayList spriteList = new ArrayList();

			foreach( MMData frame in frames.Children )
			{
				if( frame["display"] != null && (bool) frame["display"] == false )
					continue;
				ArrayList queue = new ArrayList();
				//System.Console.WriteLine("displaying for export:");

				MMStudio.DisplayFrame( frame, queue, null );

				string label = null;
				string action = null;
				if( frame.Type == "keyframe" )
				{
					if( frame["label"] != null )
						label = (string) frame["label"];
					if( frame["action"] != null )
						action = (string) frame["action"];
				}
				frameLabels.Add( label );
				frameActions.Add( action );

				//System.Console.WriteLine("queue.Count = "+queue.Count);
				foreach( MMImageInstance instance in queue )
				{
					//System.Console.WriteLine("instance of "+instance.imageID);
					if( instance.source.Type == "sprite" )
					{
						if( spriteList.IndexOf( instance.source.Name ) == -1 )
						{
							spriteList.Add( instance.source.Name );
						}
					}
					else 
					{
						MMImage image = MMImage.GetImage( instance );
						int resourceIndex = resourceList.IndexOf( image );
						if( resourceIndex == -1 )
						{
							resourceIndex = resourceList.Count;
							resourceList.Add( image );
						}
					}
				}
				queues.Add(queue);
			}

			// set imageID values

			for( int i=0; i<resourceList.Count; i++ )
			{
				if( resourceList[i] != null )
					( (MMImage) resourceList[i] ).imageID = i;
			}



			// start writing tags

			Rectangle bounds = new Rectangle( 0, 0,
				(int) document["_preferences.movie.width"],
				(int) document["_preferences.movie.height"]
				);
			SwfFile swfFile = new SwfFile( this, 5, bounds, 12 );
			swfFile.AddAttribute( new SwfSetBackgroundColorTag( this,
				document["_preferences.movie.color"]
				) );


			// write character tags

			// write import assets tags

			int maxID = 0;
			SwfStruct resources = new SwfStruct( this );
			swfFile.AddAttribute( resources );

			Hashtable importLists = new Hashtable();

			foreach(MMImage image in resourceList)
			{
				if( importLists.ContainsKey( image.importUrl ) )
					( (ArrayList) importLists[image.importUrl] ).Add( image );
				else
				{
					ArrayList importList = new ArrayList();
					importList.Add( image );
					importLists[ image.importUrl ] = importList;
				}
			}

			// write import assets tag
			foreach( string importUrl in importLists.Keys )
			{
				ArrayList importList = (ArrayList) importLists[ importUrl ];
				Hashtable identifiers = new Hashtable();
				foreach( MMImage image in importList )
				{
					identifiers[image.imageID] = image.importIdentifier;
				}
				resources.AddAttribute( new SwfImportAssetsTag ( this, importUrl, identifiers ) );
			}

			// write shape definition tags
			// (might be needed if a choice between porting and copying is added in the future)

			/*
			foreach(MMImage image in resourceList)
			{
				resources.AddAttribute( new SwfResourceTag (
					this, image.tagID, image.imageID, image.swfCode //MMImage.GetSwfResource( resourceID )
					) );
				maxID = (int) Math.Max( maxID, image.imageID );
			}
			*/

			// write sprite definition tags

			//System.Console.WriteLine("sprites.Count = "+spriteList.Count);

			/*
			int spriteStart = maxID + 1;
			SwfStruct sprites = new SwfStruct( this );
			swfFile.AddAttribute( sprites );
			for(int i=0; i<spriteList.Count; i++)
				sprites.AddAttribute( new SwfDefineSpriteTag( this, spriteStart+i ) );
			*/
			

			// define the movie as sprite for import
			// id is currently set as topmost - this has to be organised better if there are other sprites

			int mainSpriteKey = resourceList.Count;
			SwfDefineSpriteTag mainSprite = new SwfDefineSpriteTag( this, mainSpriteKey );
			swfFile.AddAttribute( mainSprite );

			ArrayList lastObjectQueue = new ArrayList();

			// write place object and frame tags

			int lastQueueCount = 0;

			for(int i=0; i<queues.Count; i++)
			{
				// write frame label tags
				if( frameLabels[i] != null )
					mainSprite.AddAttribute( new SwfFrameLabelTag( this, (string) frameLabels[i] ) );

				// write goto and stop action tags
				if( frameActions[i] != null )
				{
					string action = (string) frameActions[i];
					if( action.StartsWith("goto ") )
					{
						int frameCount = 0;
						string frameLabel = action.Substring(5);
						foreach( MMData frame in document["frames"].Children )
						{
							if( frame.Name == frameLabel || ( frame["label"] != null && (string) frame["label"] == frameLabel ) )
							{
								frameCount = frame.ChildIndex;
								break;
							}
						}
						mainSprite.AddAttribute( new SwfActionGotoAndPlayTag( this, frameCount ) );
					}
					else if( action == "stop" )
						mainSprite.AddAttribute( new SwfActionStopTag( this ) );					
				}


				ArrayList queue = (ArrayList) queues[i];

				// write display list tags
				for(int j=0; j<queue.Count; j++)
				{
					MMImageInstance instance = (MMImageInstance) queue[j];

					MMImageInstance lastInstance = null;
					if( i > 0 && j < lastQueueCount )
						lastInstance = (MMImageInstance) ( (ArrayList) queues[i-1] )[j];
					

					// primitive algorithm, just stacking the shapes on top of each other

					string spriteName = "";
					if( instance.source["spritename"] != null )
						spriteName = instance.source["spritename"];

					if( i == 0 || j >= lastQueueCount )
					{
						// new character is placed at new level
						mainSprite.AddAttribute( new SwfPlaceObject2Tag (
							this, j, MMImage.GetImage( instance ).imageID, instance.matrix, false, spriteName
							) );
					}
					else if( lastInstance.source != instance.source ) 
					{
						P.r("swap "+lastInstance.source.Name+" for "+instance.source.Name+" at "+i+"."+j);
						// new character on existing level
						// (originally intended code)
						/*
						mainSprite.AddAttribute( new SwfPlaceObject2Tag (
							this, j, MMImage.GetImage( instance ).imageID, instance.matrix, true, spriteName
							) );
						//*/
						// for some reason, the replace feature (with SWF code move=1 and character=1) doesn't work
						// using remove and place expressed in 2 tags instead:
						///*
						mainSprite.AddAttribute( new SwfRemoveObject2Tag( this, j ) );
						mainSprite.AddAttribute( new SwfPlaceObject2Tag (
							this, j, MMImage.GetImage( instance ).imageID, instance.matrix, false, spriteName
							) );
						//*/
					}
					else if( ! lastInstance.matrix.Equals( instance.matrix ) )
					{
						// modify existig character (new matrix)
						mainSprite.AddAttribute( new SwfPlaceObject2Tag (
							this, j, instance.matrix
							) );
					}
					else
					{
						// no modifications
					}

				}

				// remove upper levels that are dropped in the new frame
				if( i > 0 && lastQueueCount > queue.Count )
					for( int k = queue.Count; k < lastQueueCount; k++ )
						mainSprite.AddAttribute( new SwfRemoveObject2Tag( this, k ) );


				mainSprite.addFrame();

				lastQueueCount = queue.Count;

			}

			// place the main sprite in the main movie's timeline

			swfFile.AddAttribute( new SwfExportAssetsTag( this, mainSpriteKey, document.Name ) );
			swfFile.AddAttribute( new SwfPlaceObject2Tag( this, 1, mainSpriteKey, new Matrix(), false, document.Name ) );
			swfFile.AddAttribute( new SwfActionStopTag( this ) );
			swfFile.addFrame();

			swfFile.calcSize();
			buffer = new byte[byteCount];
			reset();
			swfFile.write();


		}

		public void writeFile( FileInfo file )
		{
			Stream stream = file.OpenWrite();
			stream.Write( buffer, 0, buffer.Length );
			stream.Close();
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

}