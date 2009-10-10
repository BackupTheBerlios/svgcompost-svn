package de.berlios.svgcompost.animation.export.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.GenericDocument;
import org.apache.batik.dom.GenericElement;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.export.Export;

public abstract class DomExport implements Export {

	public Canvas canvas;
	protected GenericDocument document;
	protected HashMap<String, Integer> swfIds;
	protected ArrayList<String> elementIds;
	protected int shapeIdCount = 0;
	protected boolean shapesAreCaptured = false;
	public float timePerFrame = 80;

	protected int coord(double value) {
		return (int) Math.ceil( value * 20 );
	}

	protected double round(double value) {
		return Math.floor( value * 10000 ) / 10000;
	}

	protected Element insertElementAtStart(Element node, String name) {
		Node firstChild = node.getFirstChild();
		if( firstChild == null )
			return (Element) node.appendChild( new GenericElement( name, (AbstractDocument) node.getOwnerDocument() ) );
		else
			return (Element) node.insertBefore( new GenericElement( name, (AbstractDocument) node.getOwnerDocument() ), firstChild );
	}

	protected Element addElement(Element node, String name) {
		Element child = (Element) node.appendChild( new GenericElement( name, (AbstractDocument) node.getOwnerDocument() ) );
		return child;
	}
	
	protected Element newElement( String name ) {
		return new GenericElement( name, document );
	}
	
	protected Element createDocumentWithRoot(String rootName) {
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		document =  new GenericDocument( null, domImpl );
		Element root = (Element) document.appendChild( new GenericElement( rootName, document ) );
		return root;
	}
	
	protected Element cloneWithAttributes( Element source ) {
		Element clone = new GenericElement( source.getNodeName(), document );
		for (int i = 0; i < source.getAttributes().getLength(); i++) {
			Attr attr = (Attr) source.getAttributes().item(i);
			clone.setAttribute( attr.getNodeName(), attr.getNodeValue() );
		}
		for (int i = 0; i < source.getChildNodes().getLength(); i++) {
			if( source.getChildNodes().item(i) instanceof Element )
				clone.appendChild( cloneWithAttributes( (Element) source.getChildNodes().item(i) ) );
		}
		return clone;
	}

	public int getSwfId( String symbolId ) {
		Integer swfId = swfIds.get( symbolId );
		if( swfId == null ) {
			shapeIdCount++;
			swfId = shapeIdCount;
			swfIds.put( symbolId, swfId );
			elementIds.add( symbolId );
		}
		return swfId;
	}
	
	public void writeOutput( String fileName ) {
//		System.out.println( "write outfile..." );
		try {
            // Prepare the DOM document for writing
            Source source = new DOMSource( document );
            // Prepare the output file
            File file = new File( fileName );
            Result result = new StreamResult( file );
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform( source, result );
//    		System.out.println( "done." );
        } catch ( Exception e ) {
        	e.printStackTrace();
        }
	}
}
