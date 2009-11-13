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


import org.apache.batik.bridge.BridgeContext;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.w3c.dom.Element;



/**
 * EditPartFactory that created EditParts for a single level of an SVG document,
 * i.e. only the children of a single node are edited.
 * @author Gerrit Karius
 *
 */
public class SingleLevelFactory implements EditPartFactory {

	private BridgeContext ctx;
	
	private GraphicalViewer viewer;
	
	private BackgroundPart background;
	
	public void setViewer(GraphicalViewer viewer) {
		this.viewer = viewer;
	}

	public void setBridgeContext( BridgeContext ctx ) {
		this.ctx = ctx;
	}
	
	public EditPart createEditPart(EditPart context, Object modelElement) {
		EditPart part = null;
		if( ! (modelElement instanceof Element) ) {
			System.out.println( "modelElement = "+modelElement );
			return null;
		}
		Element node = (Element) modelElement;
		if( context == null ) {
			background = new BackgroundPart(node,ctx);
			background.setViewer(viewer);
			part = background;
		}
		else {
			part = new EditablePart(node,ctx);
			part.setParent(context);
//			node.addPropertyChangeListener(background);
		}
			
		part.setModel(modelElement);
		return part;
	}


}