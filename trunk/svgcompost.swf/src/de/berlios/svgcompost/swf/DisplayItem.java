package de.berlios.svgcompost.swf;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.TransformListParser;

import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSMovieObject;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSRemoveObject2;
import com.flagstone.transform.Transform;

public class DisplayItem {

	protected Integer id = Transform.VALUE_NOT_SET;
	protected String characterID;
	protected String name;
	protected AffineTransform transform = new AffineTransform();

	protected TransformListParser parser;

//public DisplayItem( String id ) {
//this.id = id;
//}

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

	public static FSMovieObject createSWFTag( ArrayList<DisplayItem> list1, ArrayList<DisplayItem> list2, int i ) {
		if( i>= list1.size() && i >= list2.size() )
			throw new IndexOutOfBoundsException(String.valueOf(i));
		DisplayItem item1 = list1.size() > i ? null : list1.get(i);
		DisplayItem item2 = list2.size() > i ? null : list2.get(i);
		if( item1 == null && item2 == null )
			return null;
		if( equalOrBothNull( item1, item2 ) )
			return null;
		if( item1 != null && item2 == null )
			return new FSRemoveObject2(i);

		// 2 different items need a place object tag.

		FSPlaceObject2 placeObject = new FSPlaceObject2(i,(FSCoordTransform)null);

		if( item1 == null ) {
			// Place new object.
			placeObject.setPlaceType(FSPlaceObject2.New);
			placeObject.setIdentifier(item2.id);
			placeObject.setName(item2.name);
			placeObject.setTransform(item2.getFSCoordTransform());
		}
		else if( ! item1.id.equals( item2.id ) ) {
			// Replace existing object with new object.
			placeObject.setPlaceType(FSPlaceObject2.Replace);
			placeObject.setIdentifier(item2.id);
			if( ! equalOrBothNull( item1.name, item2.name ) )
				placeObject.setName(item2.name);
			if( ! equalOrBothNull( item1.transform, item2.transform ) )
				placeObject.setTransform(item2.getFSCoordTransform());
		}
		else {
			// Modify existing object.
			placeObject.setPlaceType(FSPlaceObject2.Modify);
			if( ! equalOrBothNull( item1.name, item2.name ) )
				placeObject.setName(item2.name);
			if( ! equalOrBothNull( item1.transform, item2.transform ) )
				placeObject.setTransform(item2.getFSCoordTransform());
		}
		
		return placeObject;
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
		equalOrBothNull( transform, item.transform );
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
		transform.getMatrix(m1);
		float[][] m2 = new float[][] {
				new float[]{(float)m1[0],(float)m1[2],(float)m1[4]*20},
				new float[]{(float)m1[1],(float)m1[3],(float)m1[5]*20},
				new float[]{1,1,1}
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

}