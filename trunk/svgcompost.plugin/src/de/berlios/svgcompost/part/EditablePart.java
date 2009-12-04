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

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.dom.svg.SVGOMElement;
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
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGRect;

import de.berlios.svgcompost.figure.MapModeImageFigure;
import de.berlios.svgcompost.freetransform.FreeTransformHelper;
import de.berlios.svgcompost.provider.ElementPropertySource;
import de.berlios.svgcompost.render.Transcoders;



/**
 * ElementPart for the edited SVG child elements.
 * @author Gerrit Karius
 *
 */
public class EditablePart extends AbstractGraphicalEditPart implements EventListener {
	
	public static final String EVENT_TYPE = "DOMAttrModified";

	private Element editableElement;

	private BridgeContext ctx;
	
	public EditablePart(Element element, BridgeContext ctx) {
		super();
		setModel( element );
		this.editableElement = element;
		this.ctx = ctx;
	}
	
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			if( editableElement instanceof SVGOMElement ) {
				SVGOMElement svgom = (SVGOMElement) editableElement;
				svgom.addEventListener(EditEvent.TRANSFORM, this, false);
				svgom.addEventListener(EditEvent.INSERT, this, false);
				svgom.addEventListener(EditEvent.REMOVE, this, false);
				svgom.addEventListener(EditEvent.CHANGE_ORDER, this, false);
				svgom.addEventListener(EditEvent.XML_ATTRIBUTE, this, false);
			}
		}
	}

	@Override
	protected void refreshVisuals() {
		copyDataToFigure();
	}
	
    @Override
	protected IFigure createFigure() {
    	    	
 		Dimension size = null;
 		
		if( editableElement instanceof SVGGraphicsElement ) {
			SVGRect bounds = ((SVGGraphicsElement)editableElement).getBBox();
			if( bounds != null )
				size = new Dimension( (int)bounds.getWidth(), (int)bounds.getHeight() );
		}
		if( size == null )
			size = new Dimension();

    	
    	Image image = null;//transcodeImage(gvtRoot);
    	IFigure figure;
		figure = new MapModeImageFigure(image);
		((MapModeImageFigure)figure).setPreferredSize(size.width, size.height);

		figure.setOpaque(false);
		figure.setBackgroundColor(ColorConstants.white);
		figure.setForegroundColor(ColorConstants.blue);
		
		Border border = new LineBorder(1);
		figure.setBorder(border);
		figure.setSize(size.width, size.height);
		return figure;
	}

    protected void copyDataToFigure() {
    	GraphicsNode gNode = ctx.getGraphicsNode(editableElement);
		Image image = null;//transcodeImage(gNode);
 		MapModeImageFigure imageFigure = (MapModeImageFigure) getFigure();
 		imageFigure.setImage(image);
 		Rectangle2D awtBounds = FreeTransformHelper.getGlobalBounds( gNode );

 		imageFigure.setAwtBounds(awtBounds);
		Rectangle draw2dBounds = new Rectangle(imageFigure.getLocation(), imageFigure.getPreferredSize());

		if( figure != null && figure.getParent() != null )
			((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), draw2dBounds );
 		figure.setBounds(new Rectangle((int)awtBounds.getX(),(int)awtBounds.getY(),(int)awtBounds.getWidth(),(int)awtBounds.getHeight()));
 		
 		AffineTransform globTrafo = gNode != null ? gNode.getGlobalTransform() : new AffineTransform();
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

	public BridgeContext getBridgeContext() {
		return ctx;
	}

	@Override
	public void handleEvent(Event evt) {
		String type = evt.getType();
		if( EditEvent.XML_ATTRIBUTE.equals(type) ) {
			refreshVisuals();
		}
		else if( EditEvent.TRANSFORM.equals(type) ) {
			refreshVisuals();
		}
		else if( EditEvent.INSERT.equals(type) ) {
			refreshVisuals();
		}
		else if( EditEvent.REMOVE.equals(type) ) {
			refreshVisuals();
		}
		else if( "DOMAttrModified".equals(type) ) {
			refreshVisuals();
		}
	}

	@Override
	public Object getAdapter(Class type) {
		if (type == IPropertySource.class) {
			return new ElementPropertySource(editableElement);
		}
		return super.getAdapter(type);
	}

}