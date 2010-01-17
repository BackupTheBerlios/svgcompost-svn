package de.berlios.svgcompost.swf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import com.flagstone.transform.FSBounds;
import com.flagstone.transform.FSCoderException;
import com.flagstone.transform.FSColor;
import com.flagstone.transform.FSColorTable;
import com.flagstone.transform.FSDefineMovieClip;
import com.flagstone.transform.FSDefineShape2;
import com.flagstone.transform.FSFillStyle;
import com.flagstone.transform.FSLineStyle;
import com.flagstone.transform.FSMovie;
import com.flagstone.transform.FSMovieObject;
import com.flagstone.transform.FSSetBackgroundColor;
import com.flagstone.transform.FSSolidFill;
import com.flagstone.transform.FSSolidLine;
import com.flagstone.transform.FSTransformObject;
import com.flagstone.transform.util.FSShapeConstructor;

public class SVG2SWF {
	
	protected int framesPerSecond = 12;
	protected Rectangle2D.Float screenSize;
	protected Map<Element, Integer> swfIds = new HashMap<Element, Integer>();
	protected int idCount = 0;
	
	protected ArrayList<FSTransformObject> definitions = new ArrayList<FSTransformObject>();
	
	protected FSMovie movie;
	protected BridgeContext ctx;

	public static void main(String args[]) throws IOException, DataFormatException {
		if( args.length < 1 )
			System.out.println( "Usage: SWF2SVG <path to SVG file>" );
		else {
			String outPath = new SVG2SWF().exportSVG2SWF( args[0] );
			System.out.println( "SVG file written to "+outPath );
		}
	}
	
	public String exportSVG2SWF( String svgPath ) throws IOException, DataFormatException {
		
        String xmlReaderClassName = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(xmlReaderClassName);
		SVGDocument doc = null;
		try {
			File file = new File( svgPath );
			doc = (SVGDocument) factory.createSVGDocument( "file:///"+file.getAbsolutePath() );
			ctx = new BridgeContext( new UserAgentAdapter() );
			// true for Batik 1.6, false for 1.7
			ctx.setDynamic( true );
			GVTBuilder builder = new GVTBuilder();
			builder.build( ctx, doc );
		}
		catch( IOException exc ) {
			exc.printStackTrace();
		}
		FSMovie movie = exportSWF2SVG(ctx);
		
		String outPath = svgPath+".swf";

		try
		{
		    movie.encodeToFile(outPath);
		}
		catch (FSCoderException e) {
		    e.printStackTrace();
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
        return outPath;
	}
	
	public FSMovie exportSWF2SVG( BridgeContext ctx ) {
		movie = new FSMovie();

	    movie.setSignature("FWS");
	    movie.setFrameRate(framesPerSecond);
	    screenSize = new Rectangle2D.Float( 0, 0, (int)ctx.getDocumentSize().getWidth(), (int)ctx.getDocumentSize().getHeight() );
	    movie.setFrameSize(new FSBounds(0, 0, (int)(screenSize.getWidth()*20), (int)(screenSize.getHeight()*20)));
	    movie.add(new FSSetBackgroundColor(FSColorTable.white()));
	    
	    Element svg = ctx.getDocument().getDocumentElement();
	    if( hasClass(svg,SWF2SVG.TIMELINE) ) {
	    	ArrayList<FSMovieObject> placementList = parseTimeline(svg);
		    movie.add( definitions );
		    movie.add( placementList );
	    }
	    else {
	    	List<DisplayItem> displayList = parseGroup(svg);
		    movie.add( definitions );
		    movie.add( DisplayItem.createNewTags( displayList ) );
	    }

	    return movie;
	}
	
	public List<DisplayItem> parseGroup( Element g ) {
		
		// Collects the items displayed in this group or frame.
		List<DisplayItem> displayList = new ArrayList<DisplayItem>();
		
		// Children of the parsed element.
		NodeList children = g.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if( children.item(i).getNodeType() != Node.ELEMENT_NODE )
				continue;
			Element element = (Element) children.item(i);
			String name = element.getLocalName();
			if( name.equals( SVGConstants.SVG_G_TAG ) ) {
				
				if( hasClass( element, SWF2SVG.TIMELINE ) ) {
//					ArrayList<FSMovieObject> placementList = parseTimeline(element);
					FSDefineMovieClip movieClip = new FSDefineMovieClip(getSwfId(element), parseTimeline(element));
//					for( FSMovieObject object : placementList )
//						movieClip.add( object );
					definitions.add(movieClip);
					displayList.add( parsePlacement(element));
				}
				else {
					List<DisplayItem> groupList = parseGroup( element );
					FSDefineMovieClip movieClip = new FSDefineMovieClip(getSwfId(element),DisplayItem.createNewTags(groupList));
					definitions.add(movieClip);
					displayList.add( parsePlacement(element));
				}
			}
			else if( name.equals( SVGConstants.SVG_PATH_TAG ) ) {
				parseShape( element );
				displayList.add( parsePlacement(element));
			}
			else if( name.equals( SVGConstants.SVG_RECT_TAG ) ) {
				parseShape( element );
				displayList.add( parsePlacement(element));
			}
			else if( name.equals( SVGConstants.SVG_CIRCLE_TAG ) ) {
				parseShape( element );
				displayList.add( parsePlacement(element));
			}
			else if( name.equals( SVGConstants.SVG_ELLIPSE_TAG ) ) {
				parseShape( element );
				displayList.add( parsePlacement(element));
			}
			else if( name.equals( SVGConstants.SVG_USE_TAG ) ) {
				// No definition, just placement.
				displayList.add( parsePlacement(element));
			}
			else if( name.equals( SVGConstants.SVG_DEFS_TAG ) ) {
				// No placement, just definitions.
				parseGroup( element );
			}
			else {
				System.out.println( "Not implemented: "+name );
			}
		}
		
		return displayList;
	}
	
	public ArrayList<FSMovieObject> parseTimeline( Element g ) {
		// FIXME: Timelines don't appear!
		ArrayList<FSMovieObject> placementList = new ArrayList<FSMovieObject>();
		NodeList frames = g.getChildNodes();
		List<DisplayItem> previousFrame = new ArrayList<DisplayItem>();
		for (int f = 0; f < frames.getLength(); f++) {
			if( frames.item(f).getNodeType() != Node.ELEMENT_NODE )
				continue;
			Element frame = (Element) frames.item(f);
			if( ! hasClass(frame, SWF2SVG.FRAME) ) {
				if( frame.getLocalName().equals( SVGConstants.SVG_DEFS_TAG ) ) {
					// Child element is a defs tag, not a frame.
					parseGroup( frame );
				}
				continue;
			}
//			System.out.println( "frame "+frame.getAttributeNS(SWF2SVG.INKSCAPE_NAMESPACE_URI,"label")+" of "+g.getAttributeNS(null,"id") );
//			System.out.println( "frame "+frame.getAttributeNS(null,"id")+" of "+g.getAttributeNS(null,"id") );
			List<DisplayItem> newFrame = parseGroup( frame );
			placementList.addAll( DisplayItem.createSWFTags(previousFrame, newFrame) );
			previousFrame = newFrame;
		}
		return placementList;
	}
	
	private DisplayItem parsePlacement(Element element) {
		String name = element.getLocalName();
		
		int swfId;
		if( name.equals(SVGConstants.SVG_USE_TAG) ) {
			// XLink namespace is sometimes screwed up.
			String reference = null;
			if(element.hasAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href"))
				reference = element.getAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href");
			else if(element.hasAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "href"))
				reference = element.getAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "href");
			
			Element referenced = resolve( reference, element );
			swfId = getSwfId( referenced );
		}
		else {
			swfId = getSwfId( element );
		}
				
		DisplayItem item = new DisplayItem();
		item.setIdentifier( swfId );
		if( element.hasAttributeNS( SWF2SVG.INKSCAPE_NAMESPACE_URI, "label" ) )
			item.setName( element.getAttributeNS( SWF2SVG.INKSCAPE_NAMESPACE_URI, "label" ) );
		GraphicsNode gNode = ctx.getGraphicsNode(element);
		AffineTransform trafo;
		if( gNode == null ) {
			trafo = AffineTransform.getTranslateInstance(0,0);
//			System.out.println( "Warning: no GraphicsNode with transformation found for "+element.getAttributeNS(null, "id") );
		}
		else {
			if(element.getLocalName().equals( SVGConstants.SVG_USE_TAG )) {
				CompositeGraphicsNode useNode = (CompositeGraphicsNode) gNode;
				GraphicsNode child = (GraphicsNode)useNode.get(0);
				trafo = child.getTransform();
				if (trafo != null) {
					if( useNode.getTransform() != null )
						trafo.concatenate( useNode.getTransform() );
				}
				else {
					trafo = useNode.getTransform();
				}
			}
			else {
				trafo = gNode.getTransform();				
			}
//			System.out.println( "Transformation found for "+element.getAttribute("id")+": "+trafo );
		}
		item.setTransform(trafo);
		
		return item;
	}

	public int getSwfId( Element symbol ) {
		Integer swfId = swfIds.get( symbol );
		if( swfId == null ) {
			idCount++;
			swfId = idCount;
			swfIds.put( symbol, swfId );
		}
		return swfId;
	}

	public Element resolve( String reference, Element fromElement ) {
		int index = reference.indexOf("#");
		if( index == -1 )
			return fromElement.getOwnerDocument().getElementById(reference);
		if( index == 0 )
	    	return fromElement.getOwnerDocument().getElementById(reference.substring(1));
		return ctx.getReferencedElement(fromElement, reference);
	}
	
	protected void parseShape( Element shapeElement ) {
		
		Integer swfId = getSwfId( shapeElement );
		ShapeNode shapeNode = (ShapeNode) ctx.getGVTBuilder().build( ctx, shapeElement );
		Shape shape = shapeNode.getShape();
		if( shape == null ) {
			System.out.println( "Shape element "+shapeElement.getAttribute("id")+" has no shape." );
			shape = new ExtendedGeneralPath();
		}
		ExtendedGeneralPath path = new NonstaticPathConverter().convertPath( new ExtendedGeneralPath( shape ) );

		FSShapeConstructor constructor = new FSShapeConstructor();
		constructor.COORDINATES_ARE_PIXELS = false;
		
		captureStyles(shapeNode, constructor);

		try {
		capturePath(path, constructor);
		}catch (Throwable e) {
			e.printStackTrace();
		}
		FSDefineShape2 defineShape = constructor.defineShape(swfId);
		definitions.add( defineShape );
	}

	protected void captureStyles( ShapeNode shapeNode, FSShapeConstructor constructor ) {
		ShapePainter painter = shapeNode.getShapePainter();
		FillShapePainter fillPainter = null;
		StrokeShapePainter strokePainter = null;
		if( painter instanceof CompositeShapePainter ) {
			CompositeShapePainter compositePainter = (CompositeShapePainter) painter;
			for (int i = 0; i < compositePainter.getShapePainterCount(); i++) {
				ShapePainter childPainter = compositePainter.getShapePainter(i);
				if( childPainter instanceof FillShapePainter )
					fillPainter = (FillShapePainter) childPainter;
				else if( childPainter instanceof StrokeShapePainter )
					strokePainter = (StrokeShapePainter) childPainter;
				else
					System.out.println( "Not implemented: "+childPainter );
			}
		}
		else if(painter instanceof FillShapePainter) {
			fillPainter = (FillShapePainter) painter;
		}
		else if(painter instanceof StrokeShapePainter) {
			strokePainter = (StrokeShapePainter) painter;
		}
		
		PainterAccess access = new PainterAccess();
		if( strokePainter != null ) {
			Paint strokePaint = access.getPaint( strokePainter );
			Stroke stroke = access.getStroke( strokePainter );
			if( strokePaint instanceof Color && stroke instanceof BasicStroke ) {
				Color color = (Color) strokePaint;
				FSColor fsColor = new FSColor( color.getRed(), color.getGreen(), color.getBlue() );
				int width = (int) (((BasicStroke)stroke).getLineWidth()*20);
				constructor.add(new FSSolidLine( width, fsColor ));
			}
			else if( strokePaint != null || stroke != null ) {
				System.out.println( "Not implemented: "+stroke+" with "+strokePaint );
				constructor.add(new FSSolidLine(20, FSColorTable.black()));
			}
		}
		if( fillPainter != null ) {
			Paint fillPaint = access.getPaint( fillPainter );
			if( fillPaint == null ) {
				System.out.println( "No fill!" );
			}
			if( fillPaint instanceof Color ) {
				Color color = (Color) fillPaint;
				FSColor fsColor = new FSColor( color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() );
				constructor.add(new FSSolidFill( fsColor ));
			}
			else if( fillPaint != null ) {
				System.out.println( "Not implemented: "+fillPaint.getClass() );
				constructor.add(new FSSolidFill(FSColorTable.lightgrey()));
			}
		}
		
	}

	protected void capturePath(ExtendedGeneralPath path, FSShapeConstructor constructor) { 
        ExtendedPathIterator iterator = path.getExtendedPathIterator(); 
        double[] points = new double[6]; 
        int[] twips = new int[6]; 
        int[] current = new int[] { 0, 0 };
        int[] subpath = new int[] { 0, 0 };

        constructor.newPath(); 
        
        if( constructor.getLineStyles().size() > 0 )
        	constructor.selectLineStyle(0);
        else
        	constructor.setLineStyles(new ArrayList<FSLineStyle>());
        if( constructor.getFillStyles().size() > 0 )
        	constructor.selectFillStyle(0);
        else
        	constructor.setFillStyles(new ArrayList<FSFillStyle>());

        while(!iterator.isDone()) {
        		int type = iterator.currentSegment(points);
        		for (int i = 0; i < 4; i++) {
					twips[i] = (int) (points[i]*20);
				}
                switch(type) {
                case ExtendedPathIterator.SEG_MOVETO:
                		if( current[0] != subpath[0] || current[1] != subpath[1] ) {
                			if( constructor.getFillStyles().size() > 0 ) {
                    			System.out.println( "Subpath not closed." );
                    			// If an path with a fill is not closed, the fill will leak.
                    			// This might also be true for subpaths.
                				constructor.rline(subpath[0]-current[0], subpath[1]-current[1]);
                			}
                		}
                		constructor.move(twips[0],twips[1]);
                        current[0] = twips[0];
                        current[1] = twips[1];
                        subpath[0] = twips[0];
                        subpath[1] = twips[1];
                        break;
                case ExtendedPathIterator.SEG_LINETO:
                        constructor.rline(twips[0]-current[0], twips[1]-current[1]);
                        current[0] = twips[0];
                        current[1] = twips[1];
                        break;
                case ExtendedPathIterator.SEG_QUADTO:
                        constructor.rcurve(
                        		twips[0]-current[0], twips[1]-current[1],
                        		twips[2]-twips[0], twips[3]-twips[1]);
                        current[0] = twips[2];
                        current[1] = twips[3];
                        break;
                case ExtendedPathIterator.SEG_ARCTO:
                        System.err.println("Arc in path!");
                        break;
                case ExtendedPathIterator.SEG_CUBICTO:
                        System.err.println("Cubic curve in path!");
                        break;
                case ExtendedPathIterator.SEG_CLOSE:
                    	System.err.println("Closure in path!");
                    	break;
                default:
                        break;
                }
                iterator.next();
        }

		if( current[0] != subpath[0] || current[1] != subpath[1] ) {
			// If an path with a fill is not closed, the fill will leak.
			if( constructor.getFillStyles().size() > 0 )
		        constructor.closePath();
		}
	}

	public static boolean hasClass( Element element, String className ) {
		if( ! element.hasAttributeNS(null, SVGConstants.SVG_CLASS_ATTRIBUTE) )
			return false;
		String classAtt = element.getAttributeNS(null, SVGConstants.SVG_CLASS_ATTRIBUTE);
		if( classAtt.equals( className ) )
			return true;
		String[] classes = classAtt.split(" ");
		for (int i = 0; i < classes.length; i++) {
			if( classes[i].equals( className ) )
				return true;
		}
		return false;
	}
}
