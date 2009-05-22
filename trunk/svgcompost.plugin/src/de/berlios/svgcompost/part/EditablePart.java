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

package de.berlios.svgcompost.part;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.swt.graphics.Image;

import de.berlios.svgcompost.figure.MapModeImageFigure;
import de.berlios.svgcompost.freetransform.FreeTransformHelper;
import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.render.Transcoders;



/**
 * ElementPart for the edited SVG child elements.
 * @author Gerrit Karius
 *
 */
public class EditablePart extends AbstractGraphicalEditPart implements PropertyChangeListener { //EventListener {
	
	public static final String EVENT_TYPE = "DOMAttrModified";

	private SVGNode editableElement;

	private BridgeContext ctx;
	
	public EditablePart(SVGNode element, BridgeContext ctx) {
		super();
		this.editableElement = element;
		this.ctx = ctx;
	}
	
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			editableElement.addPropertyChangeListener(this);
		}
	}

	@Override
	protected void refreshVisuals() {
		copyDataToFigure();
	}
	
//	protected BackgroundPart getBackground() {
//		EditPart root = this;
//		while( ! (root instanceof BackgroundPart) && root.getParent() != null )
//			root = root.getParent();
//		return (BackgroundPart) root;
//	}

//	private static final List<Object> emptyList = new ArrayList<Object>();
//	@Override
//	protected List<Object> getModelChildren() {
//		return emptyList;
//	}

    @Override
	protected IFigure createFigure() {
    	    	
 		Dimension size = editableElement.getSize();
    	
    	Image image = null;//transcodeImage(gvtRoot);
    	IFigure figure;
		figure = new MapModeImageFigure(image);
		((MapModeImageFigure)figure).setPreferredSize(size.width, size.height);

		figure.setOpaque(false);
		figure.setBackgroundColor(ColorConstants.white);
		figure.setForegroundColor(ColorConstants.blue);
		
		Border border = new LineBorder(3);
		figure.setBorder(border);
		figure.setSize(size.width, size.height);
		return figure;
	}

    protected void copyDataToFigure() {
    	GraphicsNode gNode = ctx.getGraphicsNode(editableElement.getElement());
		Image image = null;//transcodeImage(gNode);
 		MapModeImageFigure imageFigure = (MapModeImageFigure) getFigure();
 		imageFigure.setImage(image);
 		Rectangle2D awtBounds = FreeTransformHelper.getGlobalBounds( gNode );

 		imageFigure.setAwtBounds(awtBounds);
		Rectangle draw2dBounds = new Rectangle(imageFigure.getLocation(), imageFigure.getPreferredSize());

		if( figure != null && figure.getParent() != null )
			((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), draw2dBounds );
 		figure.setBounds(new Rectangle((int)awtBounds.getX(),(int)awtBounds.getY(),(int)awtBounds.getWidth(),(int)awtBounds.getHeight()));
 		
 		AffineTransform globTrafo = gNode.getGlobalTransform();
 		imageFigure.setAwtOrigin( new Point2D.Double( globTrafo.getTranslateX(), globTrafo.getTranslateY() ) );
    }
    
    protected Image transcodeImage( GraphicsNode gNode ) {
    	Image image = null;
		try {
			image = Transcoders.getGVTRenderer().transcode(ctx, gNode);
		} catch (TranscoderException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		}
		return image;
    }
    
    public DragTracker getDragTracker(Request request) {
    	return new DragEditPartsTracker(this) {
    		@Override
    		protected void updateTargetRequest() {
    			super.updateTargetRequest();
				ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
    			EditPart editPart = this.getSourceEditPart();
    			if (editPart instanceof AbstractGraphicalEditPart) {
					MapModeImageFigure figure = (MapModeImageFigure) ((AbstractGraphicalEditPart)editPart).getFigure();
					request.getExtendedData().put(FreeTransformHelper.ORIGIN, figure.getAwtOrigin());
	    			AffineTransform transform = FreeTransformHelper.createFreeTransform( request, figure.getAwtBounds(), false );
					request.getExtendedData().put(FreeTransformHelper.FREE_TRANSFORM, transform);
				}
    		}
    	};
    }

    @Override
    protected void createEditPolicies() {
    }

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if ( SVGNode.TRANSFORM.equals(prop) ) {
			refreshVisuals();
		}
	}


}