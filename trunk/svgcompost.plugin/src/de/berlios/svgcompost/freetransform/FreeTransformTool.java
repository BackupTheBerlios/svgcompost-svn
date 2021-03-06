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

import org.apache.batik.dom.svg.SVGOMElement;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Handle;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.tools.PanningSelectionTool;

import de.berlios.svgcompost.part.BackgroundPart;



/**
 * A tool which switches from scale / translate mode to rotate / skew mode and back,
 * if an already selected part is clicked on again. 
 * @author Gerrit Karius
 *
 */
public class FreeTransformTool extends PanningSelectionTool {

	protected EditPart initialEditPart;
	
	protected boolean rotateSkewMode = false;
	protected boolean newRotateSkewMode = false;
	
	protected boolean wasDragged = false;
	
	
	@Override
	protected boolean handleDrag() {
		// Set the drag flag, which cancels mode changes.
		wasDragged = true;
		return super.handleDrag();
	}
	
	@Override
	protected boolean handleButtonDown(int button) {

		// Determine which EditPart was selected and calc the new mode depending on if it was a reselection.
		if (getCurrentViewer() instanceof GraphicalViewer) {
			Handle handle = ((GraphicalViewer) getCurrentViewer()).findHandleAt(getLocation());
			// If there's no handle in the way, an EditPart might have been selected.
			if (handle == null) {
				EditPart editPart = getCurrentViewer().findObjectAtExcluding(
						getLocation(),
						getExclusionSet(),
						getTargetingConditional());
				calcRotateSkewMode(editPart);
			}
		}

		return super.handleButtonDown(button);
	}
	
	@Override
	protected boolean handleButtonUp(int button) {
		// Drag cancels mode change.
		if( wasDragged ) {
			wasDragged = false;
		}
		// If no drag has occurred, change rotate/skew mode if applicable.
		else if(initialEditPart != null) {
			EditPart editPart = initialEditPart.getTargetEditPart(getTargetRequest());
			EditPolicy policy = editPart.getEditPolicy( EditPolicy.PRIMARY_DRAG_ROLE );
			if (policy instanceof FreeTransformEditPolicy) {
				FreeTransformEditPolicy freeTransformPolicy = (FreeTransformEditPolicy) policy;
				freeTransformPolicy.setRotateSkewMode(newRotateSkewMode);
				rotateSkewMode = newRotateSkewMode;
			}
		}
		
		return super.handleButtonUp(button);
	}

	@Override
	protected boolean handleDoubleClick(int button) {
		EditPart editPart = getCurrentViewer().findObjectAtExcluding(
				getLocation(),
				getExclusionSet(),
				getTargetingConditional());
		if(editPart instanceof ScalableFreeformRootEditPart) {
			// up one level
			ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) editPart;
			BackgroundPart parentPart = (BackgroundPart) root.getChildren().get(0);
			if( parentPart.getEditRoot().getParentNode() != null
					&& parentPart.getEditRoot().getParentNode() instanceof SVGOMElement)
				parentPart.setEditRoot( (SVGOMElement) parentPart.getEditRoot().getParentNode() );

		}
		else {
			// open part for editing of inner components
			BackgroundPart parentPart = (BackgroundPart) editPart.getParent();
			if( editPart.getModel() instanceof SVGOMElement )
				parentPart.setEditRoot( (SVGOMElement) editPart.getModel() );

		}
		return super.handleDoubleClick(button);
	}

	private void calcRotateSkewMode(EditPart editPart) {
		// If the same EditPart was reselected (clicked on again), change rotate/skew mode.
		if( initialEditPart == editPart ) {
			newRotateSkewMode = ! rotateSkewMode;
		}
		// If a new EditPart was selected, set mode to false (which means normal resize).
		else {
			rotateSkewMode = false;
			newRotateSkewMode = false;
		}
		// Save the EditPart, so reselection can be detected next time.
		initialEditPart = editPart;
	}
	
}
