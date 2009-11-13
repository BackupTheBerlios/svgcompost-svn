/**
 * Copyright 2009 Gerrit Karius
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.berlios.svgcompost.freetransform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.w3c.dom.Element;

import de.berlios.svgcompost.util.ElementTraversalHelper;



/**
 * A command to apply a transformation to a node.
 * The command calculates the necessary local transformation for the node
 * to achieve the requested global transformation.
 * @author Gerrit Karius
 */
public class TransformSVGElementCommand extends Command {
	
	private AffineTransform newTransform;
	private AffineTransform oldTransform;

	
	/** A request to move/resize an edit part. */
	private final ChangeBoundsRequest request;

	/** SVGElement to manipulate. */
	private final Element element;
	private BridgeContext ctx;
	
	public TransformSVGElementCommand(Element element, ChangeBoundsRequest req, 
			Rectangle newBounds, BridgeContext ctx) {
		if (element == null || req == null || newBounds == null) {
			throw new IllegalArgumentException();
		}
		this.element = element;
		this.request = req;
		this.ctx = ctx;
		setLabel("move / resize");
	}

//	public TransformSVGElementCommand(Element model,
//			ChangeBoundsRequest request2, Rectangle constraint,
//			BridgeContext bridgeContext) {
//		// TODO Auto-generated constructor stub
//		request = null;
//		element = null;
//		throw new RuntimeException("Empty constructor stub");
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		Object type = request.getType();
		boolean canExecute = (RequestConstants.REQ_MOVE.equals(type)
				|| RequestConstants.REQ_MOVE_CHILDREN.equals(type) 
				|| RequestConstants.REQ_RESIZE.equals(type)
				|| RequestConstants.REQ_RESIZE_CHILDREN.equals(type));
		return canExecute;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	public boolean canUndo() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// TODO: map screen position to GVT node position
		// or save it externally for the document?
		
		calcNewTransform();
		ElementTraversalHelper.setTransform( element, newTransform, ctx );
	}
	
	protected void correctNewTransform() {
		double[] o = new double[6];
		oldTransform.getMatrix(o);
		double[] n = new double[6];
		newTransform.getMatrix(n);
		int direction = request.getResizeDirection();
		Boolean rotateSkewMode = (Boolean) request.getExtendedData().get(FreeTransformHelper.ROTATE_SKEW_MODE);
		
		switch (direction) {
		
		case PositionConstants.NORTH_EAST:
		case PositionConstants.NORTH_WEST:
		case PositionConstants.SOUTH_EAST:
		case PositionConstants.SOUTH_WEST:
			break;
			
		case PositionConstants.NORTH:
		case PositionConstants.SOUTH:
			if(rotateSkewMode)
				newTransform.setTransform(o[0], o[1], n[2], n[3], n[4], o[5]);
			else
				newTransform.setTransform(o[0], n[1], n[2], n[3], o[4], n[5]);
			break;
			
		case PositionConstants.EAST:
		case PositionConstants.WEST:
			if(rotateSkewMode)
				newTransform.setTransform(n[0], n[1], o[2], o[3], o[4], n[5]);
			else
				newTransform.setTransform(n[0], n[1], n[2], o[3], n[4], o[5]);
			break;

		case PositionConstants.NONE:
			newTransform.setTransform(o[0], o[1], o[2], o[3], n[4], n[5]);
			break;
			
		default:
			break;
		}
			
	}
	
	protected void calcNewTransform() {
		GraphicsNode gNode = ctx.getGraphicsNode(element);
		oldTransform = gNode.getTransform();
		if( oldTransform == null )
			oldTransform = new AffineTransform();
		
		// Node's global transform.
		AffineTransform globTrafo = gNode.getGlobalTransform();
		// Rotation only.
		globTrafo.setTransform(globTrafo.getScaleX(), globTrafo.getShearY(), globTrafo.getShearX(), globTrafo.getScaleY(), 0, 0);
		
		// New translation and additional rotation.
		AffineTransform newGlobTrafo = (AffineTransform) request.getExtendedData().get(FreeTransformHelper.FREE_TRANSFORM);
		
		// Combine old rotation with new rotation and translation.
		globTrafo.preConcatenate( newGlobTrafo );
				
		// Calculate the difference to parent.
		subtractFromMatrix( gNode.getParent().getGlobalTransform(), globTrafo );
		
		newTransform = globTrafo;
		
		// Node is shifted to its center, because the drag tool works with the center.
		Rectangle2D innerBounds = gNode.getBounds();

		AffineTransform offset = AffineTransform.getTranslateInstance(
				-innerBounds.getCenterX()*newTransform.getScaleX()-innerBounds.getCenterY()*newTransform.getShearX(),
				-innerBounds.getCenterY()*newTransform.getScaleY()-innerBounds.getCenterX()*newTransform.getShearY());
		newTransform.preConcatenate(offset); // Transform was created for the center.

	}

	/**
	 * Calculates from matrices source S and target T the relative matrix R so that R.concat(S) == T.
	 * In this process, an inversion of the source is subtracted from the target matrix.
	 * @param sourceMatrix
	 * @param targetMatrix
	 */
	public static void subtractFromMatrix( AffineTransform sourceMatrix, AffineTransform targetMatrix ) {
		try {
			sourceMatrix = sourceMatrix.createInverse();
		} catch(Exception e){
			e.printStackTrace();
		}
		targetMatrix.preConcatenate( sourceMatrix );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		ElementTraversalHelper.setTransform( element, newTransform, ctx );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		ElementTraversalHelper.setTransform( element, oldTransform, ctx );
	}
}
