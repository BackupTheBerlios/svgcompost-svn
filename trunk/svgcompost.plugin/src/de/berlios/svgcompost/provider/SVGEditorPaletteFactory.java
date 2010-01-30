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

import org.apache.batik.util.SVGConstants;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.w3c.dom.svg.SVGEllipseElement;
import org.w3c.dom.svg.SVGRectElement;

import de.berlios.svgcompost.freetransform.FreeTransformTool;
import de.berlios.svgcompost.freetransform.SetOriginTool;
import de.berlios.svgcompost.plugin.SVGCompostPlugin;


public class SVGEditorPaletteFactory {

	private static PaletteContainer createElementsDrawer() {
		PaletteDrawer componentsDrawer = new PaletteDrawer("SVG Elements");

		CombinedTemplateCreationEntry component = new CombinedTemplateCreationEntry(
				"Ellipse", 
				"Create an ellipse", 
				SVGEllipseElement.class,
				new SVGElementCreationFactory( SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_ELLIPSE_TAG ), 
				ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/ellipse16.gif")), 
				ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/ellipse16.gif"))); 
		componentsDrawer.add(component);
		component = new CombinedTemplateCreationEntry(
				"Rectangle", 
				"Create a rectangle", 
				SVGRectElement.class,
				new SVGElementCreationFactory( SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_RECT_TAG ), 
				ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/rectangle16.gif")), 
				ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/rectangle16.gif"))); 
		componentsDrawer.add(component);
		ToolEntry setOriginEntry = new ToolEntry("Set Origin", "Set a new origin while keeping everything in place", null, null, SetOriginTool.class){};
		componentsDrawer.add(setOriginEntry);
		
		return componentsDrawer;
	}

	public static PaletteRoot createPalette() {
		PaletteRoot palette = new PaletteRoot();
		palette.add(createToolsGroup(palette));
		palette.add(createElementsDrawer());
		return palette;
	}

	private static PaletteContainer createToolsGroup(PaletteRoot palette) {
		PaletteGroup toolGroup = new PaletteGroup("Tools");

		ToolEntry tool = new PanningSelectionToolEntry() {
			@Override
			public void setToolClass(Class toolClass) {
				super.setToolClass(FreeTransformTool.class);
			}
		};
		toolGroup.add(tool);
		palette.setDefaultEntry(tool);

		toolGroup.add(new MarqueeToolEntry());


		return toolGroup;
	}

}