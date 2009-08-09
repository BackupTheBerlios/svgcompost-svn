package de.berlios.svgcompost.animation.export.xml;

import java.awt.Shape;
import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPath;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGPathElement;

import de.berlios.svgcompost.animation.canvas.PathConverter;

public class SvgExport extends DomExport {

	public static void main(String[] args) throws Exception {
		
		String infile = "res/elfy.svg";
		String outfile = "res/quad.svg";
		
        String xmlReaderClassName = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(xmlReaderClassName);
		System.out.println( "read infile..." );
		SVGDocument doc = (SVGDocument) factory.createSVGDocument( new File( infile ).toURI().toString() );
		
		System.out.println( "search for cubic paths..." );
		searchForPaths(doc.getRootElement());
		
		System.out.println( "write outfile..." );
		try {
            // Prepare the DOM document for writing
            Source source = new DOMSource( doc );
            // Prepare the output file
            File file = new File( outfile );
            Result result = new StreamResult( file );
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform( source, result );
    		System.out.println( "done." );
        } catch ( Exception e ) {
        	e.printStackTrace();
        }
	}
	
	public static void searchForPaths(SVGElement element) {

		if( element instanceof SVGPathElement ) {
			SVGPathElement pathElement = (SVGPathElement) element;
			String d = pathElement.getAttribute("d");
//			String id = pathElement.getAttribute("id");
			
			PathParser parser = new PathParser();
			AWTPathProducer producer = new AWTPathProducer();
			parser.setPathHandler( producer );
			parser.parse( d );
			Shape shape = producer.getShape();
			
			if( shape instanceof ExtendedGeneralPath ) {
				
				ExtendedGeneralPath convertedPath = PathConverter.convertPath( (ExtendedGeneralPath) shape );
				SVGGeneratorContext context = SVGGeneratorContext.createDefault( element.getOwnerDocument() );
				String d_new = SVGPath.toSVGPathData( convertedPath, context );
				pathElement.setAttribute( "d", d_new );
			}
		}
		if( ! element.hasChildNodes() )
			return;
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i <  nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if( node instanceof SVGElement )
			searchForPaths( (SVGElement) node );
		}
	}
	
	public void captureFrame() {
		// TODO Auto-generated method stub

	}

	public void end() {
		// TODO Auto-generated method stub

	}

}
