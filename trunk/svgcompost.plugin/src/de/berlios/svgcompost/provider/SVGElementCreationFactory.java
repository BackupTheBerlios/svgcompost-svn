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

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.eclipse.gef.requests.CreationFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGElementCreationFactory implements CreationFactory {
	
	SVGDOMImplementation imp = new SVGDOMImplementation();
	
	protected static Document doc = SVGDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
	protected String namespaceUri;
	protected String qualifiedName;
	
	public SVGElementCreationFactory( String namespaceUri, String qualifiedName ) {
		this.namespaceUri = namespaceUri;
		this.qualifiedName = qualifiedName;
	}

	@Override
	public Object getNewObject() {
//		Element newElement = ((SVGDOMImplementation)SVGDOMImplementation.getDOMImplementation()).createElementNS(null, namespaceUri, qualifiedName);
		// Needs a document, even if it's not the final one.
		Element newElement = doc.createElementNS(namespaceUri, qualifiedName);
		return newElement;
	}

	@Override
	public Object getObjectType() {
		return Element.class;
	}

}
