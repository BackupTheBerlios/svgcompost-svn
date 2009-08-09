package de.gerrit_karius.cutout.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Imports all shapes from a ready made swfml file produced from svg with swfmill simple and then swfmill swf2xml.
 * It is verrry swfmill specific, so I hope I get the SwfmlShapeExport working some day.
 * The import searches for exported sprites with names, then for placements of objects within these
 * sprites, and then for shapes. Following this chain, it can reconstruct which name belonged to which shape
 * in the original svg file.
 * @author Gerrit
 *
 */
public class SwfmlShapeImport {
	
	protected Stack<Integer> spriteContext;
	protected HashMap<String,Integer> exportedSprites;
	protected HashMap<Integer,Integer> objectsWithinSprites;
	protected HashMap<Integer,Element> swfmlShapes;

	private static Logger log = Logger.getLogger(SwfmlShapeImport.class);
	
	public SwfmlShapeImport( String infile ) {
		
		DOMParser parser = new DOMParser();
//		System.out.println( "read swfml infile..." );
		log.info("read swfml infile...");
		try {
			parser.parse(infile);
		} catch (SAXException e) {
//			e.printStackTrace();
			log.error( e.getMessage(), e );
		} catch (IOException e) {
//			e.printStackTrace();
			log.error( e.getMessage(), e );
		}
	    Document document = parser.getDocument();
	    
	    spriteContext = new Stack<Integer>();
	    exportedSprites = new HashMap<String,Integer>();
	    objectsWithinSprites = new HashMap<Integer,Integer>();
	    swfmlShapes = new HashMap<Integer,Element>();
	    
//		System.out.println( "search for shapes..." );
		log.debug("search for shapes...");
		searchForShapes(document.getDocumentElement());
	}
	
	protected void searchForShapes( Element element ) {
		String name = element.getNodeName();
		
		if( name.startsWith( "DefineShape" ) ) {
			// put shape definition
			swfmlShapes.put( getObjectId( element ), element );
			return;
		}
		else if( name.startsWith( "DefineSprite" ) ) {
			// sprite context for all following placements
			spriteContext.push( getObjectId( element ) );
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				if( element.getChildNodes().item(i) instanceof Element )
					searchForShapes( (Element) element.getChildNodes().item(i) );
			}
			spriteContext.pop();
		}
		else if( name.startsWith( "PlaceObject" ) ) {
			// object is placed within sprite
			if( spriteContext.size() > 0 && ! element.getAttribute( "objectID" ).equals("") )
				objectsWithinSprites.put( spriteContext.peek(), getObjectId( element ) );
			return;
		}
		else if( name.equals( "Export" ) ) {
			Node symbols = null;
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				if( element.getChildNodes().item(i).getNodeName().equals( "symbols" ) ) {
					symbols = element.getChildNodes().item(i);
					break;
				}
			}
			if( symbols == null )
				return;
			for (int i = 0; i < symbols.getChildNodes().getLength(); i++) {
				if( symbols.getChildNodes().item(i).getNodeName().equals( "Symbol" ) ) {
					Element symbol = (Element) symbols.getChildNodes().item(i);
					exportedSprites.put( symbol.getAttribute( "name" ), getObjectId( symbol ) );
				}
			}
			return;
		}
		
		for (int i = 0; i < element.getChildNodes().getLength(); i++) {
			if( element.getChildNodes().item(i) instanceof Element )
				searchForShapes( (Element) element.getChildNodes().item(i) );
		}
	}
	
	protected int getObjectId( Element element ) {
		String objectID = element.getAttribute( "objectID" );
//		if( objectID == null || objectID.equals("") ) {
//			log.warn("Swfml element "+element.getNodeName()+" has no objectID attribute.");
//			return -1;
//		}
		return Integer.parseInt( objectID );
	}
	
	public Integer getShapeId( String name ) {
		Integer spriteId;
		if( name.startsWith("object") )
			spriteId = Integer.parseInt( name.substring(6) );
		else
			spriteId = exportedSprites.get( name );
		if( spriteId == null ) {
			log.error( "couldn't find swfml sprite id for name: "+name );
//			System.err.println( "couldn't find swfml id for name: "+name );
			return null;
		}
		Integer shapeId = objectsWithinSprites.get( spriteId );
		if( shapeId == null ) {
//			log.warn( "couldn't find swfml shape id for name: "+name+". Using sprite id instead." );
			return spriteId;
		}
		return shapeId;
	}
	
	public Element getShapeElement( String name ) {
		Integer shapeId = getShapeId( name );
		if( shapeId == null )
			return null;
		Element shapeElement = swfmlShapes.get( shapeId );
		return shapeElement;
	}
}
