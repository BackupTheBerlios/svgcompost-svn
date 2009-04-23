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

package de.berlios.svgcompost.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * A wrapper for an SVG element who´se children are currently being edited.
 * @author Gerrit Karius
 *
 */
public class ParentElement {

	private Element element;
	private List<ChildElement> childElements = new ArrayList<ChildElement>();

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public ParentElement(Element element, BridgeContext ctx) {
		super();
		this.element = element;
		NodeList list = element.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if( list.item(i) instanceof Element && ctx.getGraphicsNode((Element)list.item(i)) != null )
				childElements.add( new ChildElement( (Element) list.item(i), ctx ) );
		}
	}

	public List<ChildElement> getChildElements() {
		return childElements;
	}
}
