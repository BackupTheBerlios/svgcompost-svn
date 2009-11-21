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

import java.awt.geom.Dimension2D;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
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
import de.berlios.svgcompost.provider.ElementPropertySource;
import de.berlios.svgcompost.render.Transcoders;
import de.berlios.svgcompost.util.ElementTraversalHelper;



/**
 * EditPart for the SVG parent element whose child elements are edited.
 * @author Gerrit Karius
 *
 */
public class BackgroundPart extends AbstractGraphicalEditPart 
implements /*PropertyChangeListener,*/ EventListener  {
	// TODO: make this class extend RootEditPart
	
	private Element editRoot;

	private GraphicalViewer viewer;

	private BridgeContext ctx;
	
	public void setViewer(GraphicalViewer viewer) {
		this.viewer = viewer;
	}

	public GraphicalViewer getViewer() {
		return viewer;
	}

	public BackgroundPart(Element backgroundElement, BridgeContext ctx) {
		this.editRoot = backgroundElement;
		this.ctx = ctx;
	}

	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			if( editRoot instanceof SVGOMElement ) {
				SVGOMElement svgom = (SVGOMElement) editRoot;
//				svgom.addEventListener("DOMAttrModified", this, false);
//				svgom.addEventListener("DOMNodeInserted", this, false);
//				svgom.addEventListener("DOMNodeRemoved", this, false);
				svgom.addEventListener(EditEvent.TRANSFORM, this, false);
				svgom.addEventListener(EditEvent.INSERT, this, false);
				svgom.addEventListener(EditEvent.REMOVE, this, false);
				svgom.addEventListener(EditEvent.CHANGE_ORDER, this, false);
				svgom.addEventListener(EditEvent.XML_ATTRIBUTE, this, false);
			}
		}
	}
	
	

//	@Override
//	protected void addChild(EditPart child, int index) {
//		if(child != null)
//			super.addChild(child, index);
//	}

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
				// TODO Auto-generated method stub
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
		BackgroundImageFigure f = new BackgroundImageFigure();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());
		f.setImage( transcodeImage() );

		ConnectionLayer connLayer = (ConnectionLayer)getLayer(LayerConstants.CONNECTION_LAYER);
		connLayer.setConnectionRouter(new ShortestPathConnectionRouter(f));
		
		return f;
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
	
//	public void propertyChange(PropertyChangeEvent evt) {
//		String prop = evt.getPropertyName();
//		if( Element.INSERT.equals(prop) ||
//			Element.REMOVE.equals(prop) ||
//			Element.CHANGE_ORDER.equals(prop) ||
//			Element.XML_ATTRIBUTE.equals(prop)
//		) {
//			refreshChildren();
//			refreshVisuals();
//		}
//		else if( Element.TRANSFORM.equals(prop) ) {
//			refreshVisuals();
//		}
//	}
	
	

    @Override
	public void refreshVisuals() {
		// TODO Auto-generated method stub
    	((BackgroundImageFigure)getFigure()).setImage(transcodeImage());
		super.refreshVisuals();
	}

//	@Override
//	protected void registerVisuals() {
//		// TODO Auto-generated method stub
//		super.registerVisuals();
//	}

	protected Image transcodeImage() {
    	Image image = null;
		try {
			Dimension2D dim = ctx.getDocumentSize();
			RenderInfo info = RenderedImageFactory.createInfo((int)dim.getWidth(), (int)dim.getHeight(), true, true, new RGB(255,255,255), new RGB(0,0,0));
//			image = Transcoders.getSVGImageConverter().renderSVGtoSWTImage(ctx.getDocument(), info);
			GraphicsNode gvtRoot = ctx.getGraphicsNode( ctx.getDocument().getDocumentElement() );
			image = Transcoders.getGVTRenderer().transcode(ctx, gvtRoot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
    }


	public BridgeContext getBridgeContext() {
		return ctx;
	}

	public Element getEditRoot() {
		return editRoot;
	}

	public void setEditRoot(SVGOMElement root) {
		editRoot = root;
		refreshChildren();
	}
	
	@Override
	public Object getAdapter(Class type) {
		if (type == IPropertySource.class) {
			return new ElementPropertySource(editRoot);
		}
		return super.getAdapter(type);
	}
}
