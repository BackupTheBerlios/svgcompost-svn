package de.berlios.svgcompost.swf;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.TransformListParser;

import com.flagstone.transform.FSClipEvent;
import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSMovieObject;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSRemoveObject2;
import com.flagstone.transform.FSShowFrame;
import com.flagstone.transform.Transform;

public class DisplayItem {

	protected Integer id = Transform.VALUE_NOT_SET;
	protected String characterID;
	protected String name;
	protected AffineTransform transform = new AffineTransform();
	
	protected List<FSClipEvent> events;

	protected TransformListParser parser;

//public DisplayItem( String id ) {
//this.id = id;
//}

	public DisplayItem() {
	}

	public DisplayItem( DisplayItem item ) {
		if( item != null )
			copyFrom( item );
	}

	public void copyFrom( DisplayItem item ) {
		this.id = item.id;
		this.characterID = item.characterID;
		this.name = item.name;
		this.transform = item.transform;
	}

	public static ArrayList<FSMovieObject> createNewTags( List<DisplayItem> list ) {
		ArrayList<FSMovieObject> placementList = new ArrayList<FSMovieObject>();
		if( list == null )
			return placementList;
		for (int i = 0; i < list.size(); i++) {
			DisplayItem item = list.get(i);
			// Place new object.
			FSPlaceObject2 placeObject = new FSPlaceObject2(i,item.getFSCoordTransform());
			placeObject.setPlaceType(FSPlaceObject2.New);
			placeObject.setIdentifier(item.id);
			placeObject.setName(item.name);
//			placeObject.setTransform(item.getFSCoordTransform());
			placeObject.setLayer(i);
			placementList.add(placeObject);
		}
		placementList.add(new FSShowFrame());
		return placementList;
	}	

	public static ArrayList<FSMovieObject> createSWFTags( List<DisplayItem> list1, List<DisplayItem> list2 ) {
		int max;
		if( list1 == null || list1.size() < list2.size() )
			max = list2.size();
		else
			max = list1.size();
		ArrayList<FSMovieObject> placementList = new ArrayList<FSMovieObject>();
		for (int i = 0; i < max; i++) {
//			FSMovieObject placementTag = createSWFTag(list1, list2, i, placementList);
			createSWFTag(list1, list2, i, placementList);
//			if( placementTag != null )
//				placementList.add(placementTag);
		}
		placementList.add(new FSShowFrame());
		return placementList;
	}	

	public static void createSWFTag( List<DisplayItem> list1, List<DisplayItem> list2, int i, List<FSMovieObject> placementList ) {
		if( i>= list1.size() && i >= list2.size() )
			throw new IndexOutOfBoundsException(String.valueOf(i));
		DisplayItem item1 = list1 == null || i >= list1.size() ? null : list1.get(i);
		DisplayItem item2 = list2 == null || i >= list2.size() ? null : list2.get(i);
		// Layer is empty in both frames.
		if( item1 == null && item2 == null )
			return;
		// Same item in both frames.
		if( equalOrBothNull( item1, item2 ) )
			return;
		// Item in frame1, empty layer in frame2.
		if( item1 != null && item2 == null ) {
			placementList.add( new FSRemoveObject2(i) );
			return;
		}

		// 2 different items need a place object tag.

		if( item1 == null ) {
			// Place new object.
			FSPlaceObject2 newObject = new FSPlaceObject2(i,(FSCoordTransform)null);
			newObject.setPlaceType(FSPlaceObject2.New);
			newObject.setIdentifier(item2.id);
			newObject.setName(item2.name);
			newObject.setTransform(item2.getFSCoordTransform());
			placementList.add( newObject );
		}
		else if( ! equalOrBothNull( item1.id, item2.id ) ) {
			
			// Replace existing object with new object.
			// It seems that the Flash player doesn't accept
			// replacements of different types (i.e. MovieClip for Shape).
			// That's why it's safer use Remove and New tags.
			placementList.add( new FSRemoveObject2(i) );
			FSPlaceObject2 newObject = new FSPlaceObject2(i,(FSCoordTransform)null);
			newObject.setPlaceType(FSPlaceObject2.New);
			newObject.setIdentifier(item2.id);
			newObject.setName(item2.name);
			newObject.setTransform(item2.getFSCoordTransform());
			placementList.add( newObject );
			
//			FSPlaceObject2 replaceObject = new FSPlaceObject2(i,(FSCoordTransform)null);
//			replaceObject.setPlaceType(FSPlaceObject2.Replace);
//			replaceObject.setIdentifier(item2.id);
//			if( ! equalOrBothNull( item1.name, item2.name ) )
//				replaceObject.setName(item2.name);
//			if( ! equalOrBothNull( item1.transform, item2.transform ) )
//				replaceObject.setTransform(item2.getFSCoordTransform());
//			placementList.add( replaceObject );
		}
		else {
			// Modify existing object.
			FSPlaceObject2 modifyObject = new FSPlaceObject2(i,(FSCoordTransform)null);
			modifyObject.setPlaceType(FSPlaceObject2.Modify);
			if( ! equalOrBothNull( item1.name, item2.name ) )
				modifyObject.setName(item2.name);
			if( ! equalOrBothNull( item1.transform, item2.transform ) )
				modifyObject.setTransform(item2.getFSCoordTransform());
			placementList.add( modifyObject );
		}
	}


	public boolean equals( Object object ) {
		if( object == null )
			return false;
		if( ! (object instanceof DisplayItem) )
			return false;
		DisplayItem item = (DisplayItem) object;
		return
		equalOrBothNull( id, item.id ) &&
		equalOrBothNull( name, item.name ) &&
		equalOrBothNull( transform, item.transform ) &&
		equalOrBothNull( events, item.events );
	}

	private static boolean equalOrBothNull(Object x, Object y) { 
		return ( x == null ? y == null : x.equals(y) ); 
	}

//	public Element createSVGUseElement(Document doc) {
//		Element use = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,SVGConstants.SVG_USE_TAG);
//		use.setAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI,"xlink:href","#"+(characterID == null? String.valueOf(id) : characterID));
//		if( transform != null )
//			use.setAttributeNS(null,SVGConstants.SVG_TRANSFORM_ATTRIBUTE,getSVGTransformAttribute());
//		if( name != null )
//			use.setAttributeNS(SWF2SVG.INKSCAPE_NAMESPACE_URI,"inkscape:label",name);
//		return use;
//	}

	public void setCharacterID( String characterID ) {
		this.characterID = characterID;
	}
	public void setIdentifier( int id ) {
		if( id == 0 )
			throw new NullPointerException();
		this.id = id;
	}

	public int getIdentifier() {
		return id;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
	}
	
	public void setTransform(FSCoordTransform transform) {
		float[][] m = transform.getMatrix();
//		this.transform = new AffineTransform( m[0][0], m[1][0], m[0][1], m[1][1], m[0][2]*0.05, m[1][2]*0.05 );
		this.transform = new AffineTransform( m[0][0], m[1][0], m[0][1], m[1][1], m[0][2]*0.05, m[1][2]*0.05 );
	}
//public void setTransform(FSCoordTransform transform) {
//float[][] m = transform.getMatrix();
//this.transform = new AffineTransform( m[0][0], m[1][0], m[0][1], m[1][1], m[0][2], m[1][2] );
//}
	public void setTransform(String transform) {
		if(parser == null)
			parser = new TransformListParser();
		AWTTransformProducer handler = new AWTTransformProducer();
		parser.setTransformListHandler(handler);
		parser.parse(transform);
		this.transform = handler.getAffineTransform();
	}

	public FSCoordTransform getFSCoordTransform() {
		double[] m1 = new double[6];
		if( transform != null )
			transform.getMatrix(m1);
		else {
			m1[0] = 1;
			m1[3] = 1;
		}
		float[][] m2 = new float[][] {
				new float[]{(float)m1[0],(float)m1[2],(float)m1[4]*20},
				new float[]{(float)m1[1],(float)m1[3],(float)m1[5]*20},
				new float[]{0,0,1}
			};
		FSCoordTransform coordTransform = new FSCoordTransform();
		coordTransform.setMatrix(m2);
		return coordTransform;
	}

	public String getSVGTransformAttribute() {
		double[] m = new double[6];
		transform.getMatrix(m);
		String string;
		if( m[0] == 1 && m[1] == 0 && m[2] == 0 && m[3] == 1 )
			string = "translate("+m[4]+" "+m[5]+")";
		else
			string = "matrix("+m[0]+" "+m[1]+" "+m[2]+" "+m[3]+" "+m[4]+" "+m[5]+")";
		return string;
	}
	
	public AffineTransform getAWTTransform() {
		return transform;
	}

	public void setEvents(List<FSClipEvent> events) {
		this.events = events;
	}

	public List<FSClipEvent> getEvents() {
		return events;
	}

}