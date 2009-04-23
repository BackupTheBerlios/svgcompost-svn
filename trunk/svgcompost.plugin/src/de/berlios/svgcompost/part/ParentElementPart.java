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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.AbstractNode;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import de.berlios.svgcompost.freetransform.FreeTransformEditPolicy;
import de.berlios.svgcompost.freetransform.TransformSVGElementCommand;
import de.berlios.svgcompost.model.ChildElement;
import de.berlios.svgcompost.model.ParentElement;



/**
 * EditPart for the SVG parent element whode child elements are edited.
 * @author Gerrit Karius
 *
 */
public class ParentElementPart extends SVGEditPart 
implements PropertyChangeListener, EventListener  {
	
	private ParentElement parentElement;

	public ParentElementPart(ParentElement parentElement, BridgeContext ctx) {
		this.parentElement = parentElement;
		this.ctx = ctx;
	}

	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			AbstractNode node = null;
		}
	}

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
				if ((child instanceof ChildElementPart) && constraint instanceof Rectangle) {
					return new TransformSVGElementCommand(
							(ChildElement) child.getModel(), request, (Rectangle) constraint, getBridgeContext());
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
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());

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

	protected List<ChildElement> getModelChildren() {
		return parentElement.getChildElements();
	}
	

	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
