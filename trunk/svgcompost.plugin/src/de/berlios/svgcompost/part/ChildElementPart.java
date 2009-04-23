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
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
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
import org.w3c.dom.Element;

import de.berlios.svgcompost.figure.MapModeImageFigure;
import de.berlios.svgcompost.freetransform.FreeTransformHelper;
import de.berlios.svgcompost.model.ChildElement;
import de.berlios.svgcompost.render.GVTRenderer;



/**
 * ElementPart for the edited SVG child elements.
 * @author Gerrit Karius
 *
 */
class ChildElementPart extends SVGEditPart implements PropertyChangeListener { //EventListener {
	
	public static final String EVENT_TYPE = "DOMAttrModified";

	protected GVTRenderer renderer = new GVTRenderer();

	private ChildElement childElement;
	
	public ChildElementPart(ChildElement element, BridgeContext ctx) {
		super();
		this.childElement = element;
		this.ctx = ctx;
	}
	
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			childElement.addPropertyChangeListener(this);
		}
	}

	@Override
	protected void refreshVisuals() {
		
		copyDataToFigure();
		
	}

	private static final List<Object> emptyList = new ArrayList<Object>();
	@Override
	protected List<Object> getModelChildren() {
		return emptyList;
	}

    @Override
	protected IFigure createFigure() {
    	    	
    	Element svgRoot = childElement.getElement();
 		Dimension size = childElement.getSize();
 		GraphicsNode gvtRoot = ctx.getGraphicsNode( svgRoot );
    	
    	Image image = transcodeImage(gvtRoot);
    	IFigure figure;
		if(image!=null) {
			figure = new MapModeImageFigure(image);
			((MapModeImageFigure)figure).setPreferredSize(size.width, size.height);
			if( ((MapModeImageFigure)figure).getImage() == null ) {
				figure = new RectangleFigure();
			}
		}
		else {
			figure = new RectangleFigure();
		}
		

		figure.setOpaque(false);
		figure.setBackgroundColor(ColorConstants.yellow);
		figure.setForegroundColor(ColorConstants.blue);
		
		Border border = new LineBorder(3);
		figure.setBorder(border);
		figure.setSize(size.width, size.height);
		return figure;
	}

    protected void copyDataToFigure() {
    	GraphicsNode gNode = ctx.getGraphicsNode(childElement.getElement()); 
		Image image = transcodeImage(gNode);
		if(image == null)
			return;
 		MapModeImageFigure imageFigure = (MapModeImageFigure) getFigure();
 		imageFigure.setImage(image);
 		Rectangle2D awtBounds = FreeTransformHelper.getGlobalBounds( gNode );

 		imageFigure.setAwtBounds(awtBounds);
		Rectangle draw2dBounds = new Rectangle(imageFigure.getLocation(), imageFigure.getPreferredSize());

		if( figure != null && figure.getParent() != null )
			((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), draw2dBounds );
    }
    
    protected Image transcodeImage( GraphicsNode gNode ) {
    	Image image = null;
		try {
			image = renderer.transcode(ctx, gNode);
		} catch (TranscoderException e) {
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
	    			AffineTransform transform = FreeTransformHelper.createFreeTransform( request, figure.getAwtBounds(), false );
					System.out.println( "requested transform = "+transform );
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
		if ( ChildElement.TRANSFORM.equals(prop) ) {
			refreshVisuals();
		}
	}


}