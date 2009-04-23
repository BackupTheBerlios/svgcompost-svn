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

package de.berlios.svgcompost.provider;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Element;


/**
 * LabelProvider for the SVG outline view.
 * @author Gerrit Karius
 *
 */
public class SVGLabelProvider implements ILabelProvider {
	
	public Image getImage(Object arg0) {
		if( arg0 instanceof Element )
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}

	public String getText(Object arg0) {
		if( arg0 instanceof Element ) {
			Element element = (Element) arg0;
			if( element.hasAttribute("id") )
				return element.getAttribute("id");
			return element.getNodeName();
		}
		return null;
	}

	public void addListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		String arg0Name = arg0 == null ? "null" : arg0.getClass().getSimpleName();
		System.out.println( "isLabelProperty: "+arg0Name+", "+arg1 );
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub

	}

}
