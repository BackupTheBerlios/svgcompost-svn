package de.gerrit_karius.cutout.export;

import java.util.HashMap;

import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Element;

import de.gerrit_karius.cutout.canvas.Canvas;
import de.gerrit_karius.cutout.canvas.CanvasNode;

public class SwfmlSimpleExport extends DomExport {
	
	protected Element movie;
	protected Element firstFrame;

	public SwfmlSimpleExport( Canvas canvas ) {
		this.canvas = canvas;
		swfIds = new HashMap<String, Integer>();
		movie = createDocumentWithRoot("movie");		
		movie.setAttribute( "width", "" + (int) canvas.width );
		movie.setAttribute( "height", "" + (int) canvas.height );
		movie.setAttribute( "framerate", ""+(int)Math.floor(1000/timePerFrame) );
		addElement( movie, "background" ).setAttribute( "color", "#ffffff" );
	}
	
	public void captureFrame() {
		if( firstFrame != null )
			return;
		Element frame = addElement( movie, "frame" );
//		int highestDepth = captureNode( canvas.getRoot(), 1, frame );
		firstFrame = frame;
//		for( int i=highestDepth; i<lastHighestDepth; i++ ) {
//			Element removeObject2 = new GenericElement( "RemoveObject2", document );
//			tags.appendChild( removeObject2 );
//			removeObject2.setAttribute( "depth", ""+i );
//		}
//		tags.appendChild( new GenericComment("frame "+frameCount+" with "+(highestDepth-1)+" nodes",document) );
//		addFrame();
//		lastHighestDepth = highestDepth;
//		frameCount++;
	}

	protected int captureNode( CanvasNode gNode, int currentDepth, Element frame ) {
		if( ! gNode.isVisible() )
			return currentDepth;
		if( gNode.getSize() > 0 ) {
//			CompositeGraphicsNode group = (CompositeGraphicsNode) gNode;
			for (int i = 0; i < gNode.getSize(); i++) {
				currentDepth = captureNode( gNode.get(i), currentDepth, frame );
			}
		}
		else if( gNode.getGraphicsNode() instanceof ShapeNode ) {
			ShapeNode shapeNode = (ShapeNode) gNode.getGraphicsNode();
			String shapeId = CanvasNode.getCanvasNode( shapeNode, gNode.getCanvas() ).getSymbolId();
			Element place = addElement( frame, "place" );
			place.setAttribute( "id", shapeId );
			place.setAttribute( "depth", ""+currentDepth );
			place.setAttribute( "x", ""+gNode.getGlobalXY().x );
			place.setAttribute( "y", ""+gNode.getGlobalXY().y );
//			int swfId = getSwfId( shapeId );

//			Element placeObject2 = new GenericElement( "PlaceObject2", document );
//			tags.appendChild( placeObject2 );
//			placeObject2.setAttribute( "replace", currentDepth < lastHighestDepth ? "1" : "0" );
//			placeObject2.setAttribute( "depth", ""+currentDepth );
//			placeObject2.setAttribute( "objectID", ""+swfId );
//			Element transform = new GenericElement( "transform", document );
//			placeObject2.appendChild( transform );
//			
//			AffineTransform globTrafo = shapeNode.getGlobalTransform();
//			Element matrix = matrix(globTrafo);
//			transform.appendChild( matrix );
			
			currentDepth++;
		}
		return currentDepth;
	}

	public void end() {
		Element clip = addElement( insertElementAtStart( movie, "library" ), "clip" );
		clip.setAttribute( "id", "some_id" );
		String svgUrl = canvas.getSourceDoc().getURL();
		String svgFilename = svgUrl.substring( svgUrl.lastIndexOf( "/" )+1 );
		clip.setAttribute( "import", svgFilename );
	}

}
