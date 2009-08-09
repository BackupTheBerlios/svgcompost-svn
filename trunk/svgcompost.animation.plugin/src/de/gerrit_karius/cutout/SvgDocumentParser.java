package de.gerrit_karius.cutout;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SvgDocumentParser {
	
	public static final String inkscapeURI = "http://www.inkscape.org/namespaces/inkscape";
//	public static final NameSpace inkscapeNS = new NameSpace( "inkscape", inkscapeURI );
	public static final String groupmode = "groupmode";
	public static final String layer = "layer";

	public static ArrayList<String> parseSvgDocument( Document doc ) {
//		Element firstLayer = findLayer( doc.getDocumentElement() );
		ArrayList<String> layerIds = new ArrayList<String>();
		NodeList list = doc.getDocumentElement().getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			Node node = list.item( i );
			if( node instanceof Element ) {
				Element child = (Element) node;
				if( child.getAttributeNS( inkscapeURI, groupmode ).equals( layer ) ) {
					layerIds.add( child.getAttribute( "id" ) );
					System.out.println( "found layer" );
				}
			}
		}		
		return layerIds;
	}
	
	private static Element findLayer( Element el ) {
		
		NodeList list = el.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			Node node = list.item( i );
			if( node instanceof Element ) {
				Element child = (Element) node;
//				if( child.getNodeName().equals( "g" ) ) {
//					System.out.println( child.getNodeName() );
//					System.out.println( child.hasAttributeNS( inkscape, groupmode ) );
//					System.out.println( child.getAttributeNS( inkscapeURI, groupmode ) );
//					System.out.println( child.getAttributeNS( inkscapeURI, "label" ) );
//					System.out.println( child.getAttribute( "id" ) );
//				}
//				if( child.getNodeName().equals( "label" ) )
//					return child;
				if( child.getAttributeNS( inkscapeURI, groupmode ).equals( layer ) ) {
					System.out.println( "found layer" );
					return child;
				}
				else {
					Element layerEl = findLayer( child );
					if( layerEl != null )
						return layerEl;
				}
			}
		}		
		return null;
	}
}
