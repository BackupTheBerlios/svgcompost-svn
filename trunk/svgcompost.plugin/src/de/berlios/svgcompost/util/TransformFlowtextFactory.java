package de.berlios.svgcompost.util;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.apache.batik.dom.svg12.SVG12DOMImplementation;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Produces a document with Batik and Inkscape compatible flowtext
 * from a file with Inkscape-only compatible flowtext.
 * @author Gerrit Karius
 *
 */
public class TransformFlowtextFactory extends SAXSVGDocumentFactory {
	
	public static final String SVG_FLOWROOT_TAG = "flowRoot";
	public static final String SVG_FLOWDIV_TAG = "flowDiv";
	public static final String SVG_FLOWPARA_TAG = "flowPara";
	public static final String SVG_FLOWSPAN_TAG = "flowSpan";
	
	public TransformFlowtextFactory(String parser) {
		super(parser);
	}
	
	
	// This could probably be done in a cleaner way with XSTL,
	// using the xerces bundle:
	// org.apache.xerces;bundle-version="2.9.0"
	
	@Override
	public void startElement(String uri, String localName, String rawName,
			Attributes attributes) throws SAXException {
//		System.out.println("TransformFlowtextFactory.startElement("+rawName+")");
//		
//		System.out.println("currentNode = "+currentNode);
		
//		if( SVGConstants.SVG_NAMESPACE_URI.equals(uri) && SVGConstants.SVG_SVG_TAG.equals(localName) ) {
//			XMLAttributes xmlAttributes = new XMLAttributesImpl();
//			AttributesProxy proxy = new AttributesProxy(xmlAttributes);
//			int len = attributes.getLength();
//			for (int i = 0; i < len; i++) {
//				String rawname = attributes.getQName(i);
//				String value = attributes.getValue(i);
//				String auri = attributes.getURI(i);
//				String type = attributes.getType(i);
//				System.out.println("rawname = "+rawname);
//				String localPart;
//				String prefix;
//				int delimIndex = rawname.indexOf(':');
//				if(delimIndex == -1) {
//					localPart = rawname;
//					prefix = "";
//				}
//				else {
//					localPart = rawname.substring(delimIndex+1);
//					prefix = rawname.substring(0,delimIndex);
//				}
//				QName qName = new QName(prefix, localPart, rawname, auri);
//				xmlAttributes.addAttribute(qName, value, type);
//			}
//			proxy.setAttributes(xmlAttributes);
//		}
		
		if( currentNode != null
		&& currentNode.getNodeType() == Node.ELEMENT_NODE
		&& SVGConstants.SVG_NAMESPACE_URI.equals(((Element)currentNode).getNamespaceURI())
		&& SVGConstants.SVG_NAMESPACE_URI.equals(uri) ) {
			if( SVG_FLOWPARA_TAG.equals(localName)
			&& ((Element)currentNode).getLocalName().equals(SVG_FLOWROOT_TAG) ) {
				super.startElement(uri, SVG_FLOWDIV_TAG, rawName.replace(SVG_FLOWPARA_TAG, SVG_FLOWDIV_TAG), new AttributesImpl());
				super.startElement(uri, localName, rawName, attributes);
				super.startElement(uri, SVG_FLOWSPAN_TAG, rawName.replace(SVG_FLOWPARA_TAG, SVG_FLOWSPAN_TAG), new AttributesImpl());
			}
			else {
				if(SVG_FLOWROOT_TAG.equals(localName)) {
					System.out.println("change version to 1.2");
//					implementation = getDOMImplementation("1.2");
					SVGOMDocument doc = (SVGOMDocument) currentNode.getOwnerDocument();
					SVGOMSVGElement svgElement = (SVGOMSVGElement) doc.getDocumentElement();
					svgElement.setAttributeNS(null, SVGConstants.SVG_VERSION_ATTRIBUTE, "1.2");
				}
				super.startElement(uri, localName, rawName, attributes);
			}
		}
		else
			super.startElement(uri, localName, rawName, attributes);

	}

	@Override
	public void endElement(String uri, String localName, String rawName)
			throws SAXException {
//		System.out.println("TransformFlowtextFactory.endElement("+rawName+")");
		
		if( currentNode != null
//		&& currentNode.getNodeType() == Node.TEXT_NODE
		&&
		! (currentNode.getNodeType() == Node.ELEMENT_NODE
		&& SVGConstants.SVG_NAMESPACE_URI.equals(((Element)currentNode).getNamespaceURI())
		&& SVG_FLOWPARA_TAG.equals(((Element)currentNode).getLocalName())
		)
		&& SVGConstants.SVG_NAMESPACE_URI.equals(uri)
		&& SVG_FLOWPARA_TAG.equals(localName) ) {
			super.endElement(uri, SVG_FLOWSPAN_TAG, rawName.replace(SVG_FLOWPARA_TAG, SVG_FLOWSPAN_TAG));
			super.endElement(uri, localName, rawName);
			super.endElement(uri, SVG_FLOWDIV_TAG, rawName.replace(SVG_FLOWPARA_TAG, SVG_FLOWDIV_TAG));
		}
		else
			super.endElement(uri, localName, rawName);
		
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
//		System.out.println("TransformFlowtextFactory.characters()");
//		for (int i = start; i < start+length; i++) {
//			System.out.print(ch[i]);
//		}
//		System.out.println();
		super.characters(ch, start, length);
	}
	
	@Override
    public DOMImplementation getDOMImplementation(String ver) {
        return SVG12DOMImplementation.getDOMImplementation();
    }

	
	
	

}
