package de.berlios.svgcompost.swf;

import java.awt.geom.AffineTransform;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.zip.DataFormatException;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;

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
import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.flagstone.transform.FSAction;
import com.flagstone.transform.FSActionObject;
import com.flagstone.transform.FSClipEvent;
import com.flagstone.transform.FSColor;
import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSCurve;
import com.flagstone.transform.FSDefineMovieClip;
import com.flagstone.transform.FSDefineShape;
import com.flagstone.transform.FSDefineShape2;
import com.flagstone.transform.FSDefineShape3;
import com.flagstone.transform.FSDoAction;
import com.flagstone.transform.FSExport;
import com.flagstone.transform.FSFillStyle;
import com.flagstone.transform.FSFrameLabel;
import com.flagstone.transform.FSGradient;
import com.flagstone.transform.FSGradientFill;
import com.flagstone.transform.FSLine;
import com.flagstone.transform.FSLineStyle;
import com.flagstone.transform.FSMovie;
import com.flagstone.transform.FSMovieObject;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSPush;
import com.flagstone.transform.FSRemoveObject2;
import com.flagstone.transform.FSSetBackgroundColor;
import com.flagstone.transform.FSShape;
import com.flagstone.transform.FSShapeStyle;
import com.flagstone.transform.FSSolidFill;
import com.flagstone.transform.FSSolidLine;
import com.flagstone.transform.FSTable;
import com.flagstone.transform.FSTableIndex;
import com.flagstone.transform.FSTransformObject;
import com.flagstone.transform.Transform;

import de.berlios.svgcompost.xmlconstants.Classes;
import de.berlios.svgcompost.xmlconstants.Elements;

public class SWF2SVG {
	
	public static final String INKSCAPE_NAMESPACE_URI = "http://www.inkscape.org/namespaces/inkscape";
	public static final String TIMELINE = "timeline";
//	public static final String FRAME = "frame";
	
	protected SVGDOMImplementation impl;
	protected AbstractDocument doc;
	protected Element defs;
	protected SVGOMSVGElement svg;
	protected SVGCSSEngine css;
	protected Element currentGroup;
	protected Element currentSymbol;
	
	protected Map<Integer,String> export = new Hashtable<Integer,String>();
	
	protected int gradientCount = 0;
	
	public static void main(String args[]) throws IOException, DataFormatException {
		if( args.length < 1 )
			System.out.println( "Usage: SWF2SVG <path to SWF file>" );
		else {
			String outPath = new SWF2SVG().exportSWF2SVG( args[0] );
			System.out.println( "SVG file written to "+outPath );
		}
	}
	
	public String exportSWF2SVG( String swfPath ) throws IOException, DataFormatException {
		FSMovie swfMovie = new FSMovie();
		swfMovie.decodeFromFile(swfPath);

		Document doc = exportSWF2SVG(swfMovie);
		String outPath = swfPath+".svg";
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8");
		
    	Properties props = OutputPropertiesFactory.getDefaultMethodProperties("xml");
    	props.setProperty(OutputKeys.INDENT, "yes");
    	props.setProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "1");
    	
		Serializer serializer = SerializerFactory.getSerializer(props);
		serializer.asDOMSerializer();
		serializer.setWriter(writer);
		((DOMSerializer) serializer).serialize(doc);
			
        return outPath;
	}
	public String _exportSWF2SVG( String swfPath ) throws IOException, DataFormatException {
		FSMovie swfMovie = new FSMovie();
		swfMovie.decodeFromFile(swfPath);

		Document doc = exportSWF2SVG(swfMovie);
		String outPath = swfPath+".svg";
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8");
		DOMUtilities.writeDocument(doc, writer);
		writer.flush();
        writer.close();
        return outPath;
	}

	public Document exportSWF2SVG( FSMovie swfMovie ) {
		impl = (SVGDOMImplementation) SVGDOMImplementation.getDOMImplementation();
		doc = (AbstractDocument) impl.createDocument(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_SVG_TAG, null);
		svg = (SVGOMSVGElement) doc.getDocumentElement();
		svg.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:inkscape", INKSCAPE_NAMESPACE_URI);
		svg.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xlink", XLinkSupport.XLINK_NAMESPACE_URI);
		svg.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:svgcompost", Elements.SVGCOMPOST_NAMESPACE_URI);
		svg.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, String.valueOf(swfMovie.getFrameSize().getWidth()/20)+"px");
		svg.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, String.valueOf(swfMovie.getFrameSize().getHeight()/20)+"px");
		svg.setAttributeNS(null, SVGConstants.SVG_CLASS_ATTRIBUTE, TIMELINE);
		defs = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_DEFS_TAG);
		svg.appendChild(defs);
		css = (SVGCSSEngine) impl.createCSSEngine((AbstractStylableDocument) doc, new BridgeContext( new UserAgentAdapter() ));
		
		ArrayList<FSMovieObject> swfObjects = swfMovie.getObjects();
		parseSymbol(svg, swfObjects);

		return doc;
	}

	protected void parseSymbol(Element symbol, ArrayList<FSMovieObject> swfObjects) {
		// Parse exports first.
		for (FSMovieObject object : swfObjects) {
			if( object.getType() == FSMovieObject.Export )
				parseExport((FSExport)object);
		}
		ArrayList<DisplayItem> displayList = new ArrayList<DisplayItem>();
		int frameCount = 0;
		// Frame element collects all placements.
		Element frame = createFrame(++frameCount);
		for (Iterator<FSMovieObject> iterator = swfObjects.iterator(); iterator.hasNext();) {
			FSMovieObject swfObject = iterator.next();
			switch (swfObject.getType()) {
			case FSMovieObject.SetBackgroundColor:
				parseBackgroundColor((FSSetBackgroundColor)swfObject);
				break;
			case FSMovieObject.DefineShape:
				Element shape = parseFSDefineShape((FSDefineShape)swfObject);
				defs.appendChild(shape);
				break;
			case FSMovieObject.DefineShape2:
				Element shape2 = parseFSDefineShape2((FSDefineShape2)swfObject);
				defs.appendChild(shape2);
				break;
			case FSMovieObject.DefineShape3:
				Element shape3 = parseFSDefineShape3((FSDefineShape3)swfObject);
				defs.appendChild(shape3);
				break;
			case FSMovieObject.DefineMovieClip:
				FSDefineMovieClip movieClip = (FSDefineMovieClip) swfObject;
				Element innerSymbol = createSymbol(movieClip);
				parseSymbol( innerSymbol, movieClip.getObjects() );
				defs.appendChild(innerSymbol);
				break;
			case FSMovieObject.DoAction:
				FSDoAction doAction = (FSDoAction) swfObject;
				parseActions( frame, doAction.getActions() );
				break;
			case FSMovieObject.PlaceObject2:
				// Set a new item on the display list.
				parsePlaceObject2((FSPlaceObject2)swfObject,displayList);
				break;
			case FSMovieObject.RemoveObject2:
				// Remove an item from the display list.
				displayList.set(((FSRemoveObject2)swfObject).getLayer(), null);
				break;
			case FSMovieObject.Export:
				// Has already been parsed in advance.
				break;
			case FSMovieObject.FrameLabel:
				FSFrameLabel frameLabel = (FSFrameLabel) swfObject;
				frame.setAttributeNS(null, "id", frameLabel.getLabel());
				break;
			case FSMovieObject.ShowFrame:
				showFrame(frame, displayList);
				symbol.appendChild(frame);
				frame = createFrame(++frameCount);
				break;

			default:
				System.out.println( "Not implemented: "+swfObject.name() );
				break;
			}
		}
	}

	protected void parseActions(Element frame, List<FSActionObject> actions) {
		FSTable table = null;
		Stack<Object> stack = new Stack<Object>();
		String variable = null;
		for (Iterator<FSActionObject> iterator = actions.iterator(); iterator.hasNext();) {
			FSActionObject action = (FSActionObject) iterator.next();
//			System.out.println( "Action type is "+action.name() );
			switch (action.getType()) {
			
			case FSActionObject.Table:
				table = (FSTable) action;
				break;
			case FSActionObject.Push:
				FSPush push = (FSPush) action;
				List<Object> pushList = push.getValues();
				for (Object object : pushList) {
					if( object instanceof FSTableIndex ) {
						FSTableIndex index = (FSTableIndex) object;
						Object value = table.getValues().get(index.getIndex());
						stack.push(value);
					}
					else {
						stack.push(object);
					}
				}
				break;
			case FSAction.ExecuteMethod: {
				String methodName = stack.pop().toString();
				String variableName = stack.pop().toString();
				int numArgs = (Integer) stack.pop();
				Object[] args = new Object[numArgs];
				for (int i = numArgs-1; i >= 0; i--) {
					args[i] = stack.pop();
				}
				System.out.print( "ExecuteMethod "+variableName+"."+methodName+"(" );
				for (int i = args.length-1; i >= 0; i--) {
					System.out.print(args[i]);
					if( i > 0 )
						System.out.print(",");
				}
				System.out.println(")");
				// Push result on stack.
				stack.push(null);
				
				// Create XML elements for ExecuteMethod action.
//				Element executeMethod = doc.createElementNS(Elements.SVGCOMPOST_NAMESPACE_URI, Elements.ACTIONSCRIPT_EXECUTEMETHOD);
//				frame.appendChild(executeMethod);
//				executeMethod.setAttribute(Elements.OBJECT, variableName);
//				executeMethod.setAttribute(Elements.NAME, methodName);
//				for (int i = args.length-1; i >= 0; i--) {
//					Element param = doc.createElementNS(Elements.SVGCOMPOST_NAMESPACE_URI, Elements.ACTIONSCRIPT_PARAM);
//					executeMethod.appendChild(param);
//					param.setAttribute(Elements.VALUE, args[i].toString());
//				}
				break;
			}
			case FSAction.NewObject: {
				Integer numArgs = (Integer) stack.pop();
				Map<String,Object> args = new HashMap<String,Object>();
				for (int i = numArgs-1; i >= 0; i--) {
					Object argValue = stack.pop();
					String argName = stack.pop().toString();
					args.put(argName, argValue);
				}
				// Script would invoke generic object constructor here.
				stack.push( args );
				System.out.println("NewObject "+args);
				break;
			}
			case FSAction.SetAttribute: {
				String attributeValue = stack.pop().toString();
				String attributeName = stack.pop().toString();
				String variableName = stack.pop().toString();
				System.out.println("SetAttribute "+variableName+"."+attributeName+" = "+attributeValue);
				
				// Create XML elements for SetAttribute action.
//				Element setAttribute = doc.createElementNS(Elements.SVGCOMPOST_NAMESPACE_URI, Elements.ACTIONSCRIPT_SETATTRIBUTE);
//				frame.appendChild(setAttribute);
//				setAttribute.setAttribute(Elements.OBJECT, variableName);
//				setAttribute.setAttribute(Elements.NAME, attributeName);
//				setAttribute.setAttribute(Elements.VALUE, attributeValue);
				break;
			}
			case FSAction.GetVariable: {
				// Pop name from stack and push variable onto stack.
				// Here, both are the same, since we are not actually executing a script.
				String variableName = stack.peek().toString();
				System.out.println("GetVariable "+variableName);
			}
				break;
			case FSAction.SetVariable: {
				String variableValue = stack.pop().toString();
				String variableName = stack.pop().toString();
				System.out.println("SetVariable "+variableName+" = "+variableValue);
				
				// Include variables definitions starting with SVGCompost prefix as XML attributes.
				if(variableName.startsWith(Elements.SVGCOMPOST_AS_PREFIX)) {
					String name = variableName.substring(Elements.SVGCOMPOST_AS_PREFIX.length());
					frame.setAttributeNS(Elements.SVGCOMPOST_NAMESPACE_URI, Elements.SVGCOMPOST_XMLNS_PREFIX+name, variableValue);
				}
				break;
			}
			case FSAction.Pop:
				stack.pop();
				break;
			case FSAction.End:
				// End of actions.
				break;

			case FSAction.GetAttribute:
				String attributName = stack.pop().toString();
				String objectName = stack.pop().toString();
				System.out.println("GetAttribute "+objectName+"."+attributName);
				stack.push(objectName+"."+attributName);
				break;
			case FSAction.NamedObject: {
				String type = (String) stack.pop();
				Integer numArgs = Integer.parseInt(stack.pop().toString());
				Object[] args = new Object[numArgs];
				for (int i = numArgs-1; i >= 0; i--) {
					args[i] = stack.pop();
				}
//				String name = (String) stack.pop();
//				stack.push(name);
				// Script would invoke object class constructor here.
				stack.push(args); // Push value on stack.
				System.out.print( "NamedObject:" );
				System.out.print(" type="+type);
				System.out.print(" numArgs="+numArgs);
//				System.out.print(" name="+name);
				System.out.println();
				
				// Include object definitions starting with SVGCompost prefix as XML elements.
				if(type.startsWith(Elements.SVGCOMPOST_AS_PREFIX)) {
					String name = type.substring(Elements.SVGCOMPOST_AS_PREFIX.length());
					addSVGCompostProperty(frame, args[0], name);
				}

//				Element svgcompostElement = doc.createElementNS(Elements.SVGCOMPOST_NAMESPACE_URI, Elements.SVGCOMPOST_XMLNS_PREFIX+name);
//				frame.appendChild(svgcompostElement);
//				for (int i = 0; i < args.length; i++) {
//					addSVGCompostProperty(svgcompostElement, args[i], type);
//				}

				break;
			}
			case FSAction.NewArray: {
				Integer numArgs = Integer.parseInt(stack.pop().toString());
				Object[] args = new Object[numArgs];
				for (int i = numArgs-1; i >= 0; i--) {
					args[i] = stack.pop();
				}
				stack.push(args); // Push value on stack.
				System.out.print( "NewArray:" );
				System.out.print(" numArgs="+numArgs);
				for (int i = 0; i < numArgs; i++) {
					System.out.print(" "+args[i].getClass()+" args["+i+"] = "+args[i]);
				}
				System.out.println();
				break;
			}
			case FSActionObject.Call:
			case FSActionObject.ExceptionHandler:
			case FSActionObject.SetTarget:
			case FSActionObject.With:
			default:
				System.out.println("Not implemented: action type "+action.name()+" ("+action.getType()+")");
				break;
			}
		}
	}
	
	protected void addSVGCompostProperty(Element element, Object object, String name) {
		if( object instanceof Object[] ) {
			Object[] array = (Object[]) object;
			if(name.endsWith(Elements.SET))
				name = name.substring(0,name.length()-Elements.SET.length());
			for (int i = 0; i < array.length; i++) {
				addSVGCompostProperty( element, array[i], name );
			}
		}
		else if( object instanceof HashMap ) {
			Element childElement = doc.createElementNS(Elements.SVGCOMPOST_NAMESPACE_URI, Elements.SVGCOMPOST_XMLNS_PREFIX+name);
			element.appendChild(childElement);
			HashMap<String,Object> map = (HashMap<String,Object>) object;
			for (String key : map.keySet()) {
				addSVGCompostProperty( childElement, map.get(key), key );
			}
		}
		else if( object instanceof String ) {
			element.setAttribute(name, object.toString());
		}
	}

	protected void parseExport(FSExport export) {
		// In the Flash 5 editor, it seems that all exported (i.e. named) symbols
		// are exported and defined three times.
		// This is a Flash bug, not a Flagstone bug.
		Hashtable<Integer,String> table = export.getObjects();
		for( Integer id : table.keySet() ) {
			this.export.put(id, table.get(id).replace(" ","_"));
		}
	}

	protected Element createSymbol(FSDefineMovieClip movieClip) {
		Element symbol = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		symbol.setAttributeNS(null, "id", getExportedId(movieClip.getIdentifier()));
		symbol.setAttributeNS(null, SVGConstants.SVG_CLASS_ATTRIBUTE, TIMELINE);
		return symbol;
	}
	
	protected String getExportedId(int identifier) {
		return export.containsKey(identifier) ? export.get(identifier) : String.valueOf(identifier);
	}

	protected Element createFrame(int frameCount) {
		Element frame = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		frame.setAttributeNS(INKSCAPE_NAMESPACE_URI, "inkscape:label", "frame"+frameCount);
		frame.setAttributeNS(null, SVGConstants.SVG_CLASS_ATTRIBUTE, Classes.FRAME);
		if( frameCount > 1 )
			frame.setAttributeNS(null, "visibility", "hidden");
		return frame;
	}

//	protected Element parsePlaceObject2(FSPlaceObject2 placeObject) {
//	int id = placeObject.getIdentifier();
//	FSCoordTransform transform = placeObject.getTransform();
//	Element use = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_USE_TAG); 
//	if( isSet( id ) ) {
//		use.setAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href", "#"+getExportedId(id));
//	}
//	if( transform != null )
//		use.setAttributeNS(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, SVGConstants.SVG_TRANSFORM_ATTRIBUTE, parseTransform(transform));
//	return use;
//}

	protected Element createUseElement(DisplayItem item) {
		int id = item.getIdentifier();
		FSCoordTransform transform = item.getFSCoordTransform();
		String name = item.getName();
		List<FSClipEvent> events = item.getEvents();
		// Element use = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_USE_TAG);
		// Hack: Don't use the proper namespace to prevent the use element to be cluttered with useless attributes.
		Element use = doc.createElement(SVGConstants.SVG_USE_TAG);
		if( isSet( id ) ) {
			use.setAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href", "#"+getExportedId(id));
		}
		if( transform != null )
			use.setAttributeNS(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, SVGConstants.SVG_TRANSFORM_ATTRIBUTE, parseTransform(transform));
		if( name != null )
			use.setAttributeNS(SWF2SVG.INKSCAPE_NAMESPACE_URI,"inkscape:label",name);
		if( events != null ) {
			for (FSClipEvent clipEvent : events)
				// Parse onLoad actions only.
				if( clipEvent.getEvent() == FSClipEvent.Load )
					parseActions(use, clipEvent.getActions());
		}
		return use;
	}

	protected void parsePlaceObject2(FSPlaceObject2 placeObject, ArrayList<DisplayItem> displayList) {
		int type = placeObject.getPlaceType();
		int layer = placeObject.getLayer();
		int id = placeObject.getIdentifier();
		String name = placeObject.getName();
		FSCoordTransform transform = placeObject.getTransform();
		
		List<FSClipEvent> events = placeObject.getClipEvents();
//		if( events != null ) {
//			for (FSClipEvent clipEvent : events)
//				parseActions(null, clipEvent.getActions());
//		}

		DisplayItem item = new DisplayItem( displayList.size() <= layer ? null : displayList.get(layer) );

		if( isSet( id ) && id != 0 )
			item.setIdentifier(id);
		if( transform != null )
			item.setTransform(transform);
		if( name != null )
			item.setName(name);
		if( events != null )
			item.setEvents( events );

		if( displayList.size() < layer+1 ) {
			for (int i = displayList.size(); i < layer+1; i++)
				displayList.add(null);
		}
		displayList.set(layer,item);
	}
	
	protected void showFrame(Element frame, ArrayList<DisplayItem> displayList) {
		for(int i=0; i<displayList.size(); i++) {
			DisplayItem item = displayList.get(i);
			if(item != null)
				frame.appendChild(createUseElement(item));
		}
	}

	protected void parseBackgroundColor(FSSetBackgroundColor setBackground) {
		SVGOMSVGElement svg = (SVGOMSVGElement) doc.getDocumentElement();
		if( ! svg.hasAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE) )
			svg.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE, "");
		svg.getStyle().setProperty(CSSConstants.CSS_BACKGROUND_VALUE, parseColor(setBackground.getColor()), "");	
	}

	protected String parseColor(FSColor color) {
		String hex = "#"+hex2digits(color.getRed())+hex2digits(color.getGreen())+hex2digits(color.getBlue());
		return hex;
	}
	
	protected String hex2digits( int i ) {
		if( i < 16 )
			return "0"+Integer.toHexString(i);
		else
			return Integer.toHexString(i).substring(0,2);
	}

	protected static String parseTransform(FSCoordTransform transform) {
		float[][] m = transform.getMatrix();
		String string;
		if( m[0][0] == 1 && m[0][1] == 0 && m[1][0] == 0 && m[1][1] == 1 )
			string = "translate("+m[0][2]*0.05+" "+m[1][2]*0.05+")";
		else
			string = "matrix("+m[0][0]+" "+m[1][0]+" "+m[0][1]+" "+m[1][1]+" "+m[0][2]*0.05+" "+m[1][2]*0.05+")";
		return string;
	}

	protected AffineTransform parseAffineTransform(FSCoordTransform transform) {
		float[][] m = transform.getMatrix();
		AffineTransform affineTransform = new AffineTransform(m[0][0],m[1][0],m[0][1],m[1][1],m[0][2],m[1][2]);
		return affineTransform;
	}

	protected Element parseFSDefineShape(FSDefineShape defineShape) {
		ArrayList<FSFillStyle> fillStyles = defineShape.getFillStyles();
		ArrayList<FSLineStyle> lineStyles = defineShape.getLineStyles();
		FSShape shape = defineShape.getShape();
		Element shapeElement = parseShapeDefinition(fillStyles, lineStyles, shape);
		String id = getExportedId(defineShape.getIdentifier());
		shapeElement.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id);
		return shapeElement;
	}
	
	protected Element parseFSDefineShape2(FSDefineShape2 defineShape2) {
		ArrayList<FSFillStyle> fillStyles = defineShape2.getFillStyles();
		ArrayList<FSLineStyle> lineStyles = defineShape2.getLineStyles();
		FSShape shape = defineShape2.getShape();
		Element shapeElement = parseShapeDefinition(fillStyles, lineStyles, shape);
		String id = getExportedId(defineShape2.getIdentifier());
		shapeElement.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id);
		return shapeElement;
	}
	
	protected Element parseFSDefineShape3(FSDefineShape3 defineShape3) {
		ArrayList<FSFillStyle> fillStyles = defineShape3.getFillStyles();
		ArrayList<FSLineStyle> lineStyles = defineShape3.getLineStyles();
		FSShape shape = defineShape3.getShape();
		Element shapeElement = parseShapeDefinition(fillStyles, lineStyles, shape);
		String id = getExportedId(defineShape3.getIdentifier());
		shapeElement.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id);
		return shapeElement;
	}
	
	protected Element parseShapeDefinition(List<FSFillStyle> fillStyles, List<FSLineStyle> lineStyles, FSShape shape) {
		Element[] fillPaths = new Element[fillStyles.size()];
		String[] dfill = new String[fillStyles.size()];
		Element[] linePaths = new Element[lineStyles.size()];
		String[] dline = new String[lineStyles.size()];
		boolean[] hasActualStroke = new boolean[lineStyles.size()];
		int fillStyle = 0;
		int altFillStyle = 0;
		int lineStyle = 0;
		int x = 0;
		int y = 0;
		for (int i = 0; i < fillPaths.length; i++) {
			fillPaths[i] = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_PATH_TAG);
			dfill[i] = new String();
			switch (fillStyles.get(i).getType()) {
			case FSFillStyle.Solid:
				FSColor color = ((FSSolidFill)fillStyles.get(i)).getColor();
				String fill = parseColor( color );
				fillPaths[i].setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fill);
				if( isSet( color.getAlpha() ) )
					fillPaths[i].setAttributeNS(null, SVGConstants.SVG_OPACITY_ATTRIBUTE, String.valueOf(color.getAlpha()/255.0));
				break;

			case FSFillStyle.Linear:
			case FSFillStyle.Radial:
				Element gradient = parseGradient((FSGradientFill)fillStyles.get(i));
				String gradientId = "gradient"+(++gradientCount);
				gradient.setAttributeNS(null,"id",gradientId);
				defs.appendChild(gradient);
				fillPaths[i].setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, "url(#"+gradientId+")");
				break;

			default:
				System.out.println( "Not implemented: "+fillStyles.get(i).getType()+" ("+fillStyles.get(i).getClass()+")" );
				break;
			}
		}
		for (int i = 0; i < linePaths.length; i++) {
			linePaths[i] = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_PATH_TAG);
			dline[i] = new String();
			FSSolidLine solidLine = (FSSolidLine) lineStyles.get(i);
			linePaths[i].setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, parseColor( solidLine.getColor() ));
			linePaths[i].setAttributeNS(null, SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, String.valueOf( solidLine.getWidth()*0.05f ));
			linePaths[i].setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
			linePaths[i].setAttributeNS(null, SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE, SVGConstants.SVG_ROUND_VALUE);
			linePaths[i].setAttributeNS(null, SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE, SVGConstants.SVG_ROUND_VALUE);
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
				if( isSet(style.getLineStyle()) )
					lineStyle = style.getLineStyle();
				if( isSet(style.getMoveX()) && isSet(style.getMoveY()) ) {
					int moveX = style.getMoveX();
					int moveY = style.getMoveY();
					String pathMove = " M "+moveX*0.05f+","+moveY*0.05f;
					x = moveX;
					y = moveY;
					for (int i = 0; i < dfill.length; i++)
						dfill[i] += pathMove;
					for (int i = 0; i < dline.length; i++)
						dline[i] += pathMove;
//					if( fillStyle != 0 )
//						dfill[fillStyle-1] += pathMove;
//					if( altFillStyle != 0 )
//						dfill[altFillStyle-1] += pathMove;
//					if( lineStyle != 0 )
//						dline[lineStyle-1] += pathMove;
				}
			}
			else if( transformObject instanceof FSLine ) {
				FSLine line = (FSLine) transformObject;
				int lineX = line.getX();
				int lineY = line.getY();
				String pathLine = " l "+lineX*0.05f+","+lineY*0.05f;
				x += lineX;
				y += lineY;
				String pathMove = " M "+x*0.05f+","+y*0.05f;
				for (int i = 0; i < dfill.length; i++)
					if( fillStyle-1 == i || altFillStyle-1 == i )
						dfill[i] += pathLine;
					else
						dfill[i] += pathMove;
				for (int i = 0; i < dline.length; i++)
					if( lineStyle-1 == i ) {
						dline[i] += pathLine;
						hasActualStroke[i] = true;
					}
					else
						dline[i] += pathMove;

//				if( fillStyle != 0 )
//					dfill[fillStyle-1] += pathLine;
//				if( altFillStyle != 0 )
//					dfill[altFillStyle-1] += pathLine;
//				if( lineStyle != 0 )
//					dline[lineStyle-1] += pathLine;
			}
			else if( transformObject instanceof FSCurve ) {
				FSCurve curve = (FSCurve) transformObject;
				int controlX = curve.getControlX();
				int controlY = curve.getControlY();
				int anchorX = curve.getAnchorX();
				int anchorY = curve.getAnchorY();
				String pathCurve = " q "+controlX*0.05f+","+controlY*0.05f+" "+(controlX+anchorX)*0.05f+","+(controlY+anchorY)*0.05f;
				x += controlX+anchorX;
				y += controlY+anchorY;
				String pathMove = " M "+x*0.05f+","+y*0.05f;
				for (int i = 0; i < dfill.length; i++)
					if( fillStyle-1 == i || altFillStyle-1 == i )
						dfill[i] += pathCurve;
					else
						dfill[i] += pathMove;
				for (int i = 0; i < dline.length; i++)
					if( lineStyle-1 == i ) {
						dline[i] += pathCurve;
						hasActualStroke[i] = true;
					}
					else
						dline[i] += pathMove;
//				if( fillStyle != 0 )
//					dfill[fillStyle-1] += pathCurve;
//				if( altFillStyle != 0 )
//					dfill[altFillStyle-1] += pathCurve;
//				if( lineStyle != 0 )
//					dline[lineStyle-1] += pathCurve;
			}
		}
		for (int i = 0; i < fillPaths.length; i++) {
			dfill[i] += " Z";
			fillPaths[i].setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE, dfill[i]);
		}
		for (int i = 0; i < linePaths.length; i++) {
			linePaths[i].setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE, dline[i]);
		}
		if( linePaths.length == 1 && fillPaths.length <= 1 ) {
			// Only 1 stroke and at most 1 fill.
			if( fillPaths.length == 1 ) {
				linePaths[0].setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fillPaths[0].getAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE) );
			}
			return linePaths[0];
		}
		if( linePaths.length == 0 && fillPaths.length == 1 ) {
			// Only 1 fill and no stroke.
			return fillPaths[0];
		}
		else {
			// More than 1 fill and / or more than 1 stroke.
			Element g = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
			for (int i = 0; i < fillPaths.length; i++)
				g.appendChild(fillPaths[i]);
			for (int i = 0; i < linePaths.length; i++)
				if( hasActualStroke[i] )
					g.appendChild(linePaths[i]);
			return g;
		}
	}
	
	public Element parseGradient(FSGradientFill gradientFill) {
		int type = gradientFill.getType();
		Element gradient = null;
		if( type == FSGradientFill.Linear ) {
			gradient = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_LINEAR_GRADIENT_TAG);
		}
		else if( type == FSGradientFill.Radial ) {
			gradient = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_RADIAL_GRADIENT_TAG);
		}
		// TODO: Gradient transform doesn't work.
//		int[] left = gradientFill.getTransform().transformPoint(-16384, 0);
//		int[] right = gradientFill.getTransform().transformPoint(16384, 0);
//		gradient.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, String.valueOf(left[0]*0.05));
//		gradient.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, String.valueOf(left[1]*0.05));
//		gradient.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, String.valueOf(right[0]*0.05));
//		gradient.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, String.valueOf(right[1]*0.05));
//		System.out.println( "gradient transform = "+gradientFill.getTransform() );
//		gradient.setAttributeNS(null, SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE, parseTransform(gradientFill.getTransform()));
		List<FSGradient> gradients = gradientFill.getGradients();
		for (FSGradient grad : gradients) {
			FSColor color = grad.getColor();
			int ratio = grad.getRatio();
			Element stop = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_STOP_TAG);
			stop.setAttributeNS(null, SVGConstants.SVG_STOP_COLOR_ATTRIBUTE, parseColor(color));
			if( isSet(color.getAlpha()) )
				stop.setAttributeNS(null, SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE, String.valueOf(color.getAlpha()/255.0));
			stop.setAttributeNS(null, SVGConstants.SVG_OFFSET_ATTRIBUTE, String.valueOf(ratio/255.0));
			gradient.appendChild(stop);
		}
		return gradient;
	}
		
	public static boolean isSet( int value ) {
		return value != Transform.VALUE_NOT_SET;
	}
	
}
