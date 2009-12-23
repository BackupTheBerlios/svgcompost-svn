package de.berlios.svgcompost.swf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import javax.xml.XMLConstants;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.flagstone.transform.FSColor;
import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSCurve;
import com.flagstone.transform.FSDefineMovieClip;
import com.flagstone.transform.FSDefineShape2;
import com.flagstone.transform.FSFillStyle;
import com.flagstone.transform.FSFrameLabel;
import com.flagstone.transform.FSLine;
import com.flagstone.transform.FSLineStyle;
import com.flagstone.transform.FSMovie;
import com.flagstone.transform.FSMovieObject;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSSetBackgroundColor;
import com.flagstone.transform.FSShape;
import com.flagstone.transform.FSShapeStyle;
import com.flagstone.transform.FSSolidFill;
import com.flagstone.transform.FSTransformObject;
import com.flagstone.transform.Transform;

public class SWF2SVG {
	
	public static final String INKSCAPE_URI = "http://www.inkscape.org/namespaces/inkscape";
	
	protected SVGDOMImplementation impl;
	protected AbstractDocument doc;
	protected Element defs;
	protected SVGOMSVGElement svg;
	protected SVGCSSEngine css;
	protected Element currentGroup;
	protected Element currentSymbol;
	
	public static void main(String args[]) throws IOException, DataFormatException {
		new SWF2SVG().exportSWF2SVG( "D:/workspaces/runtime-workspace/svgtest/lion.swf" );
	}
	
	public void exportSWF2SVG( String swfPath ) throws IOException, DataFormatException {
		FSMovie swfMovie = new FSMovie();
		swfMovie.decodeFromFile(swfPath);
		Document doc = exportSWF2SVG(swfMovie);
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(swfPath+".svg"), "UTF-8");
		DOMUtilities.writeDocument(doc, writer);
		writer.flush();
        writer.close();
	}

	public Document exportSWF2SVG( FSMovie swfMovie ) {
		impl = (SVGDOMImplementation) SVGDOMImplementation.getDOMImplementation();
		doc = (AbstractDocument) impl.createDocument(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_SVG_TAG, null);
		defs = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_DEFS_TAG);
		svg = (SVGOMSVGElement) doc.getDocumentElement();
		svg.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:inkscape", INKSCAPE_URI);
		svg.setAttributeNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_WIDTH_ATTRIBUTE, String.valueOf(swfMovie.getFrameSize().getWidth()/20)+"px");
		svg.setAttributeNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_HEIGHT_ATTRIBUTE, String.valueOf(swfMovie.getFrameSize().getHeight()/20)+"px");
		svg.appendChild(defs);
		css = (SVGCSSEngine) impl.createCSSEngine((AbstractStylableDocument) doc, new BridgeContext( new UserAgentAdapter() ));
		
		ArrayList<FSMovieObject> swfObjects = swfMovie.getObjects();
		parseSymbol(svg, swfObjects);

		return doc;
	}

	protected void parseSymbol(Element symbol, ArrayList<FSMovieObject> swfObjects) {
		int frameCount = 0;
		// Frame element collects all placements.
		Element frame = createFrame(++frameCount);
		for (Iterator<FSMovieObject> iterator = swfObjects.iterator(); iterator.hasNext();) {
			FSMovieObject swfObject = iterator.next();
			switch (swfObject.getType()) {
			case FSMovieObject.SetBackgroundColor:
				parseBackgroundColor((FSSetBackgroundColor)swfObject);
				break;
			case FSMovieObject.DefineShape2:
				Element shape = parseFSDefineShape2((FSDefineShape2)swfObject);
				defs.appendChild(shape);
				break;
			case FSMovieObject.DefineMovieClip:
				FSDefineMovieClip movieClip = (FSDefineMovieClip) swfObject;
				Element innerSymbol = createSymbol(movieClip);
				parseSymbol( innerSymbol, movieClip.getObjects() );
				defs.appendChild(innerSymbol);
				break;
			case FSMovieObject.PlaceObject2:
				Element use = parsePlaceObject2((FSPlaceObject2)swfObject);
				frame.appendChild(use);
				break;
			case FSMovieObject.FrameLabel:
				FSFrameLabel frameLabel = (FSFrameLabel) swfObject;
				frame.setAttributeNS(null, "id", frameLabel.getLabel());
				break;
			case FSMovieObject.ShowFrame:
				symbol.appendChild(frame);
				frame = createFrame(++frameCount);
				break;

			default:
				System.out.println( "Not implemented: "+swfObject.getType()+" ("+swfObject.getClass()+")" );
				break;
			}
		}
	}

	protected Element createSymbol(FSDefineMovieClip movieClip) {
		Element symbol = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		symbol.setAttributeNS(null, "id", String.valueOf(movieClip.getIdentifier()));
		return symbol;
	}

	protected Element createFrame(int frameCount) {
		Element frame = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		frame.setAttributeNS(INKSCAPE_URI, "inkscape:label", "frame"+frameCount);
		if( frameCount > 1 )
			frame.setAttributeNS(null, "visibility", "hidden");
		return frame;
	}

	protected Element parsePlaceObject2(FSPlaceObject2 placeObject) {
		int id = placeObject.getIdentifier();
		FSCoordTransform transform = placeObject.getTransform();
		Element use = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_USE_TAG); 
		if( isSet( id ) )
			use.setAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href", "#"+String.valueOf(id));
		if( transform != null )
			use.setAttributeNS(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, SVGConstants.SVG_TRANSFORM_ATTRIBUTE, parseTransform(transform));
		return use;
	}

	protected void parseBackgroundColor(FSSetBackgroundColor setBackground) {
		SVGOMSVGElement svg = (SVGOMSVGElement) doc.getDocumentElement();
		if( ! svg.hasAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE) )
			svg.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE, "");
		svg.getStyle().setProperty(CSSConstants.CSS_BACKGROUND_VALUE, parseColor(setBackground.getColor()), "");	
	}

	protected String parseColor(FSColor color) {
		String hex = "#"+Integer.toHexString(color.getRed())+Integer.toHexString(color.getGreen())+Integer.toHexString(color.getBlue());
		return hex;
	}

	protected String parseTransform(FSCoordTransform transform) {
		float[][] m = transform.getMatrix();
		String string;
		if( m[0][0] == 1 && m[0][1] == 0 && m[1][0] == 0 && m[1][1] == 1 )
			string = "translate("+m[0][2]*0.05+" "+m[1][2]*0.05+")";
		else
			string = "matrix("+m[0][0]+" "+m[1][0]+" "+m[0][1]+" "+m[1][1]+" "+m[0][2]*0.05+" "+m[1][2]*0.05+")";
		return string;
	}

	protected Element parseFSDefineShape2(FSDefineShape2 defineShape2) {
		// TODO: Implement line styles.
		// TODO: implement gradient fills.
		ArrayList<FSFillStyle> fillStyles = defineShape2.getFillStyles();
		ArrayList<FSLineStyle> lineStyles = defineShape2.getLineStyles();
		Element[] fillPaths = new Element[fillStyles.size()];
		Element[] linePaths = new Element[fillStyles.size()];
		String[] dfill = new String[fillStyles.size()];
		String[] dline = new String[lineStyles.size()];
		int fillStyle = 0;
		int altFillStyle = 0;
		int lineStyle = 0;
		FSShape shape = defineShape2.getShape();
		for (int i = 0; i < fillPaths.length; i++) {
			fillPaths[i] = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_PATH_TAG);
			dfill[i] = new String();
			String fill = null;
			if( fillStyles.get(i) instanceof FSSolidFill ) {
				FSColor color = ((FSSolidFill)fillStyles.get(i)).getColor();
				fill = parseColor( color );
				fillPaths[i].setAttributeNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_FILL_ATTRIBUTE, fill);
				if( isSet( color.getAlpha() ) )
					fillPaths[i].setAttributeNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_OPACITY_ATTRIBUTE, String.valueOf(color.getAlpha()/256.0));
			}
		}
		ArrayList<FSTransformObject> shapeObjects = shape.getObjects();
		for (Iterator<FSTransformObject> iterator = shapeObjects.iterator(); iterator.hasNext();) {
			FSTransformObject transformObject = iterator.next();
			
			if( transformObject instanceof FSShapeStyle ) {
				FSShapeStyle style = (FSShapeStyle) transformObject;
				if( isSet(style.getFillStyle()) )
					fillStyle = style.getFillStyle();
				if( isSet(style.getAltFillStyle()) )
					altFillStyle = style.getAltFillStyle();
				if( isSet(style.getMoveX()) && isSet(style.getMoveY()) ) {
					int moveX = style.getMoveX();
					int moveY = style.getMoveY();
					String pathMove = " M "+moveX*0.05f+","+moveY*0.05f;
					if( fillStyle != 0 )
						dfill[fillStyle-1] += pathMove;
					if( altFillStyle != 0 )
						dfill[altFillStyle-1] += pathMove;
					if( lineStyle != 0 )
						dfill[lineStyle-1] += pathMove;
				}
			}
			else if( transformObject instanceof FSLine ) {
				FSLine line = (FSLine) transformObject;
				int lineX = line.getX();
				int lineY = line.getY();
				String pathLine = " l "+lineX*0.05f+","+lineY*0.05f;
				if( fillStyle != 0 )
					dfill[fillStyle-1] += pathLine;
				if( altFillStyle != 0 )
					dfill[altFillStyle-1] += pathLine;
				if( lineStyle != 0 )
					dfill[lineStyle-1] += pathLine;
			}
			else if( transformObject instanceof FSCurve ) {
				FSCurve curve = (FSCurve) transformObject;
				int controlX = curve.getControlX();
				int controlY = curve.getControlY();
				int anchorX = curve.getAnchorX();
				int anchorY = curve.getAnchorY();
				String pathCurve = " q "+controlX*0.05f+","+controlY*0.05f+" "+(controlX+anchorX)*0.05f+","+(controlY+anchorY)*0.05f;
				if( fillStyle != 0 )
					dfill[fillStyle-1] += pathCurve;
				if( altFillStyle != 0 )
					dfill[altFillStyle-1] += pathCurve;
				if( lineStyle != 0 )
					dfill[lineStyle-1] += pathCurve;
			}
		}
		for (int i = 0; i < fillPaths.length; i++) {
			fillPaths[i].setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE, dfill[i]);
		}
		String id =  String.valueOf(defineShape2.getIdentifier());
		if( fillPaths.length == 1 ) {
			fillPaths[0].setAttributeNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_ID_ATTRIBUTE, id);
			return fillPaths[0];
		}
		else {
			Element g = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
			g.setAttributeNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_ID_ATTRIBUTE, id);
			for (int i = 0; i < fillPaths.length; i++)
				g.appendChild(fillPaths[i]);
			return g;
		}
	}

	public static boolean isSet( int value ) {
		return value != Transform.VALUE_NOT_SET;
	}
	
}
