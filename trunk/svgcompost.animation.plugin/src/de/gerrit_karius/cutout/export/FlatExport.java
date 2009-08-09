package de.gerrit_karius.cutout.export;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Element;

import de.gerrit_karius.cutout.canvas.Canvas;

public abstract class FlatExport implements Export {

	protected Canvas canvas;
	protected List<String> shapeIds = new ArrayList<String>();
	protected boolean shapesAreCaptured = false;

	public void captureFrame() {
		startFrame();
		captureNode( canvas.getRoot().getGraphicsNode() );
		endFrame();
	}
	
	protected abstract void startFrame();
	protected abstract void endFrame();

	protected void captureNode( GraphicsNode gNode ) {
		if( ! gNode.isVisible() )
			return;
		if( gNode instanceof CompositeGraphicsNode ) {
			CompositeGraphicsNode group = (CompositeGraphicsNode) gNode;
			for (int i = 0; i < group.size(); i++) {
				captureNode( (GraphicsNode) group.get(i) );
			}
		}
		else if( gNode instanceof ShapeNode ) {
			captureShapeInstance( (ShapeNode) gNode );
		}
	}
	
	protected abstract void captureShapeInstance( ShapeNode shapeNode );

	protected void captureShapes() {
		for( String shapeId : shapeIds ) {
			Element shapeElement = canvas.getSourceDoc().getElementById( shapeId );
			if( shapeElement == null )
				continue;
			captureShapeDefinition( shapeElement );
		}
		shapesAreCaptured = true;
	}
	
	protected abstract void captureShapeDefinition( Element shapeElement );
	
	public void end() {
		if( ! shapesAreCaptured )
			captureShapes();
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

}
