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
import java.awt.geom.Dimension2D;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderInfo;
import org.eclipse.gmf.runtime.draw2d.ui.render.factory.RenderedImageFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import de.berlios.svgcompost.figure.BackgroundImageFigure;
import de.berlios.svgcompost.freetransform.FreeTransformEditPolicy;
import de.berlios.svgcompost.freetransform.TransformSVGElementCommand;
import de.berlios.svgcompost.layers.InsertElementCommand;
import de.berlios.svgcompost.provider.ElementPropertySource;
import de.berlios.svgcompost.render.Transcoders;
import de.berlios.svgcompost.util.ElementTraversalHelper;



/**
 * EditPart for the SVG parent element whose child elements are edited.
 * @author Gerrit Karius
 *
 */
public class BackgroundPart extends AbstractGraphicalEditPart 
implements EventListener  {
	// TODO: make this class extend RootEditPart
	
	private static final float[] origin = new float[] {0,0}; 

	private Element editRoot;

	private BridgeContext ctx;
	
	private BackgroundImageFigure bgFigure;
	
	public BridgeContext getBridgeContext() {
		return ctx;
	}

	public Element getEditRoot() {
		return editRoot;
	}

	public void setEditRoot(SVGOMElement root) {
		editRoot = root;
    	bgFigure.setCrosshair( calcCrosshair() );
		refreshChildren();
	}

	public BackgroundPart(Element backgroundElement, BridgeContext ctx) {
		super();
		this.editRoot = backgroundElement;
		this.ctx = ctx;
	}

	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			if( editRoot instanceof SVGOMElement ) {
				SVGOMElement svgom = (SVGOMElement) editRoot;
				svgom.addEventListener(EditEvent.TRANSFORM, this, false);
				svgom.addEventListener(EditEvent.INSERT, this, false);
				svgom.addEventListener(EditEvent.REMOVE, this, false);
				svgom.addEventListener(EditEvent.CHANGE_ORDER, this, false);
				svgom.addEventListener(EditEvent.XML_ATTRIBUTE, this, false);
			}
		}
	}

	public void handleEvent(Event evt) {
		String type = evt.getType();
		if( EditEvent.INSERT.equals(type) ||
				EditEvent.REMOVE.equals(type) ||
				EditEvent.CHANGE_ORDER.equals(type) ||
				EditEvent.XML_ATTRIBUTE.equals(type)
			) {
				refreshChildren();
				refreshVisuals();
		}
		else if( EditEvent.TRANSFORM.equals(type) ) {
			refreshVisuals();
		}
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE,  new XYLayoutEditPolicy(){

			@Override
			protected Command createChangeConstraintCommand(ChangeBoundsRequest request,
					EditPart child, Object constraint) {
				if ((child instanceof EditablePart) && constraint instanceof Rectangle) {
					return new TransformSVGElementCommand(
							(Element) child.getModel(), request, (Rectangle) constraint, getBridgeContext());
				}
				return super.createChangeConstraintCommand(request, child, constraint);
			}
			
			@Override
			protected Command createChangeConstraintCommand(EditPart child,
					Object constraint) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected Command getCreateCommand(CreateRequest request) {
				Object childClass = request.getNewObjectType();
				if ( childClass.equals( Element.class ) ) {
					return new InsertElementCommand((Element)getHost().getModel(),(Element)request.getNewObject(),(Rectangle)getConstraintFor(request));
				}
				return null;
			}
			
			@Override
			protected EditPolicy createChildEditPolicy(EditPart child) {
				return new FreeTransformEditPolicy();
			}

			});
	}
	
	@Override
	protected IFigure createFigure() {
		// Background image
    	bgFigure = new BackgroundImageFigure();
    	bgFigure.setOpaque(true);
    	bgFigure.setImage( transcodeImage() );
    	bgFigure.setCrosshair( calcCrosshair() );
    	FreeformLayer layer = (FreeformLayer) getLayer(SVGScalableFreeformRootEditPart.BACKGROUND_LAYER);
		layer.add(bgFigure);

		// Layer to hold the edited parts
		Figure f = new FreeformLayer();
		f.setLayoutManager(new FreeformLayout());
		f.setOpaque(false);
		return f;
	}
	
	@Override
	public void refreshVisuals() {
		bgFigure.setImage(transcodeImage());
    	bgFigure.setCrosshair( calcCrosshair() );
		super.refreshVisuals();
	}
	
	protected float[] calcCrosshair() {
    	float[] rootOrigin = new float[2];
    	AffineTransform transform = ctx.getGraphicsNode(editRoot).getGlobalTransform();
    	transform.transform(origin, 0, rootOrigin, 0, 1);
    	return rootOrigin;
	}

	protected Image transcodeImage() {
    	Image image = null;
		try {
			Dimension2D dim = ctx.getDocumentSize();
			RenderInfo info = RenderedImageFactory.createInfo((int)dim.getWidth(), (int)dim.getHeight(), true, true, new RGB(255,255,255), new RGB(0,0,0));
//			image = Transcoders.getSVGImageConverter().renderSVGtoSWTImage(ctx.getDocument(), info);
			GraphicsNode gvtRoot = ctx.getGraphicsNode( ctx.getDocument().getDocumentElement() );
			image = Transcoders.getGVTRenderer().transcode(ctx, gvtRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
    }
		
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
		}
	}

	protected List<Element> getModelChildren() {
		return ElementTraversalHelper.getChildElements(editRoot);
	}
	
 	@Override
	public Object getAdapter(Class type) {
		if (type == IPropertySource.class) {
			return new ElementPropertySource(editRoot);
		}
		return super.getAdapter(type);
	}
}
