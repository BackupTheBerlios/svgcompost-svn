package de.berlios.svgcompost.animation;

import java.io.File;
import java.io.IOException;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

public class GraphicsBuilder {
	
	public static BridgeContext readLibrary(String fileName) {
		
        String xmlReaderClassName = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(xmlReaderClassName);
//		System.out.println( "read infile..." );
		SVGDocument doc = null;
		BridgeContext ctx = null;
//		RootGraphicsNode sourceRoot = null;
//		CanvasGraphicsNode sourceCanvas = null;
		try {
			doc = (SVGDocument) factory.createSVGDocument( new File( fileName ).toURI().toString() );
			ctx = new BridgeContext( new UserAgentAdapter() );
			// true for Batik 1.6, false for 1.7
			ctx.setDynamic( true );
			GVTBuilder builder = new GVTBuilder();
			builder.build( ctx, doc );
//			sourceRoot = (RootGraphicsNode) builder.build( ctx, doc );
		}
		catch( IOException exc ) {
//			exc.printStackTrace();
		}

		return ctx;
//		Canvas canvas = new Canvas( ctx );
//		sourceCanvas = (CanvasGraphicsNode) sourceRoot.get(0);
//		canvas.readSourceCanvas( sourceCanvas );
//		return canvas;
	}

}
