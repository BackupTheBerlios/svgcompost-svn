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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
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
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderInfo;
import org.eclipse.gmf.runtime.draw2d.ui.render.factory.RenderedImageFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import de.berlios.svgcompost.figure.BackgroundImageFigure;
import de.berlios.svgcompost.freetransform.FreeTransformEditPolicy;
import de.berlios.svgcompost.freetransform.TransformSVGElementCommand;
import de.berlios.svgcompost.model.BackgroundElement;
import de.berlios.svgcompost.model.EditableElement;
import de.berlios.svgcompost.render.Transcoders;



/**
 * EditPart for the SVG parent element whose child elements are edited.
 * @author Gerrit Karius
 *
 */
public class BackgroundElementPart extends SVGEditPart 
implements PropertyChangeListener, EventListener  {
	
	private BackgroundElement backgroundElement;

	private GraphicalViewer viewer;
	
	public void setViewer(GraphicalViewer viewer) {
		this.viewer = viewer;
	}

	public GraphicalViewer getViewer() {
		return viewer;
	}

	public BackgroundElementPart(BackgroundElement backgroundElement, BridgeContext ctx) {
		this.backgroundElement = backgroundElement;
		this.ctx = ctx;
	}

	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			backgroundElement.addPropertyChangeListener(this);
		}
	}
	
	

//	@Override
//	protected void addChild(EditPart child, int index) {
//		if(child != null)
//			super.addChild(child, index);
//	}

	public void handleEvent(Event evt) {
		String prop = evt.getType();
		
		refreshChildren();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE,  new XYLayoutEditPolicy(){

			@Override
			protected Command createChangeConstraintCommand(ChangeBoundsRequest request,
					EditPart child, Object constraint) {
				if ((child instanceof EditableElementPart) && constraint instanceof Rectangle) {
					return new TransformSVGElementCommand(
							(EditableElement) child.getModel(), request, (Rectangle) constraint, getBridgeContext());
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

	protected List<EditableElement> getModelChildren() {
		return backgroundElement.getChildElements();
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if( BackgroundElement.INSERT.equals(prop) || BackgroundElement.REMOVE.equals(prop) ) {
			refreshVisuals();
			refreshChildren();
		}
	}
	
	

    @Override
	public void refresh() {
		// TODO Auto-generated method stub
    	((BackgroundImageFigure)getFigure()).setImage(transcodeImage());
		super.refresh();
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
			RenderInfo info = RenderedImageFactory.createInfo((int)dim.getWidth(), (int)dim.getHeight(), true, true, null, new RGB(0,0,0));
			image = Transcoders.getSVGImageConverter().renderSVGtoSWTImage(ctx.getDocument(), info);
//			GraphicsNode gvtRoot = ctx.getGraphicsNode( ctx.getDocument().getDocumentElement() );
//			image = Transcoders.getGVTRenderer().transcode(ctx, gvtRoot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
    }


	public BridgeContext getCtx() {
		return ctx;
	}

}