package de.berlios.svgcompost.animation.anim.composite;

import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.canvas.CanvasNode;

public class Scene extends Sequence {

//	private static Logger log = Logger.getLogger(Scene.class);

	protected Canvas canvas;
	public CanvasNode container;
//	public CanvasNode stage;
	public CanvasNode background;
	public CanvasNode overlay;
	public CanvasNode mask;
	
//	protected HashMap<String,Skeleton> models;
//	protected HashMap<String,Walk> walks;
	
	public Scene(Canvas canvas) {
		this.canvas = canvas;
	}

//	public void addModel( Skeleton model ) {
//		if( models == null )
//			models = new HashMap<String,Skeleton>();
//		log.debug( "Scene.addModel: "+model.getName() );
//		models.put( model.getName(), model );
//	}
//	public Skeleton getModel( String name ) {
//		if( models != null )
//			return (Skeleton) models.get( name );
//		return null;
//	}
//	
//	public void addWalk( Walk walk ) {
//		if( walks == null )
//			walks = new HashMap<String,Walk>();
//		walks.put( walk.name, walk );
//	}
//	public Walk getWalk( String name ) {
//		if( walks != null )
//			return (Walk) walks.get( name );
//		return null;
//	}

	public void prepare() {
		// Create a stage.
//		if( container == null ) {
//			if( canvas == null && log.isDebugEnabled() )
//				log.debug( "canvas is null" );
//			container = canvas.getRoot().addEmptyChild( name );
//			container.setX( canvas.width / 2 );
//			container.setY( canvas.height / 2 );
//			background = container.addEmptyChild( "background" );
//			stage = container.addEmptyChild( "stage" );
//		}

		setScene( this );
		// Init the anims.
		super.prepare();
		// Set the duration.
		if( duration == 0 )
			duration = innerDuration;
	}
	
	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}
	public Canvas getCanvas() {
		return canvas;
	}
}
