/**
 * Copyright 2010 Gerrit Karius
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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.apache.batik.bridge.BridgeContext;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.tools.TargetingTool;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditablePart;

/**
 * Sets a new origin for the current coordinate system,
 * while shifting all children to keep them in place.
 * @author Gerrit Karius
 *
 */
public class SetOriginTool extends TargetingTool {

	@Override
	protected String getCommandName() {
		return "Set Origin";
	}
	
	protected Command getCommand() {
		
		if (getTargetEditPart() instanceof ScalableFreeformRootEditPart)
			setTargetEditPart( (BackgroundPart) getTargetEditPart().getChildren().get(0) );
		
		if (getTargetEditPart() instanceof EditablePart)
			setTargetEditPart( getTargetEditPart().getParent() );
		
		BackgroundPart bg = (BackgroundPart) getTargetEditPart();
		Element element = (Element) getTargetEditPart().getModel();
		BridgeContext ctx = bg.getBridgeContext();
		
		Point draw2dPoint = getLocation();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getCurrentViewer();

		FigureCanvas canvas = (FigureCanvas) viewer.getControl();
		Point viewLoc = canvas.getViewport().getViewLocation();
		
		Point2D.Float awtPoint = new Point2D.Float(viewLoc.x + draw2dPoint.x, viewLoc.y + draw2dPoint.y);
		AffineTransform globalTransform = ctx.getGraphicsNode(element).getGlobalTransform();
		try {
			globalTransform.inverseTransform(awtPoint, awtPoint);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		
		return new SetOriginCommand(element, awtPoint, ctx);
	}
	
	protected boolean handleButtonDown(int button) {
		EditPart editPart = getCurrentViewer().findObjectAtExcluding(
				getLocation(),
				getExclusionSet(),
				getTargetingConditional());
		setTargetEditPart(editPart);
		
		Command command = getCommand();
		if (command != null) {
			setCurrentCommand(command);
		}
		executeCurrentCommand();
		return true;
	}

	protected void executeCurrentCommand() {
		Command curCommand = getCurrentCommand();
		if (curCommand != null && curCommand.canExecute())
			executeCommand(curCommand);
		setCurrentCommand(null);
	}


}
