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
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;


/**
 * Parent class for SVG related EditParts.
 * @author Gerrit Karius
 *
 */
abstract public class SVGEditPart extends AbstractGraphicalEditPart {
	
	protected BridgeContext ctx;

	public BridgeContext getBridgeContext() {
		if( ctx == null && getParent() != null && getParent() instanceof SVGEditPart )
			ctx = ((SVGEditPart)getParent()).getBridgeContext();
		return ctx;
	}
	
}
