package de.berlios.svgcompost.animation.anim.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.anim.Tweener;
import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.canvas.PathConverter;
import de.berlios.svgcompost.animation.util.Polar;

/**
 * Tweens 2 matrices and rotates them where necessary, rather than tweening linear.
 * @author User
 *
 */
public class RotationMatrixTweener extends Tweener {

	private static Logger log = Logger.getLogger(RotationMatrixTweener.class);

	public AffineTransform[] matrix;
	protected Polar[] xAxis;
	protected Polar[] yAxis;
	protected Point2D.Float xAxisCart = new Point2D.Float(0,0);
	protected Point2D.Float yAxisCart = new Point2D.Float(0,0);
//	protected Point2D.Float[] origin;
	protected int[] left;
	protected boolean flip = false;
	protected boolean nullPointer = false;
	protected boolean simpleTweening = false;
	protected boolean noTweening = false;
	
	protected CanvasNode node;

	public RotationMatrixTweener( AffineTransform matrix1, AffineTransform matrix2, CanvasNode node ) {
		this.node = node;
		matrix = new AffineTransform[2];
		xAxis = new Polar[2];
		yAxis = new Polar[2];
		left = new int[2];
		xAxisCart = new Point2D.Float(0,0);
		yAxisCart = new Point2D.Float(0,0);
		if( ! (matrix1 == null && matrix2 == null) )
			load( matrix1, matrix2 );
	}
	
	public void load( AffineTransform matrix1, AffineTransform matrix2 ) {
//		if( node != null )
//			log.debug("load( "+matrix1+", "+matrix2+" ) for "+node.getName());
		matrix[0] = matrix1;
		matrix[1] = matrix2;
		nullPointer = false;
		simpleTweening = false;
		noTweening = false;
		// Check if both matrices are null.
		if( matrix[0] == null && matrix[1] == null ) {
//			log.debug( "Both key matrices null for "+name );
			nullPointer = true;
			return;
		}
		// Check for a missing matrix.
		for (int i = 0; i < 2; i++) {
			if( matrix[i] == null ) {
				if( node != null )
					log.debug( "One key matrix null for "+node.getName() );
				matrix[i] = (AffineTransform) matrix[1-i].clone();
				noTweening = true;
				return;
			}
		}
		// Check if both matrices are equal.
		if( matrix[0].equals( matrix[1] )  ) {
				noTweening = true;
				if( node != null )
					log.debug("Both matrices are equal for "+node.getName());
				return;
		}
//		if( matrix[0].a == matrix[1].a && matrix[0].b == matrix[1].b &&
//				matrix[0].c == matrix[1].c && matrix[0].d == matrix[1].d &&
//				matrix[0].tx == matrix[1].tx && matrix[0].ty == matrix[1].ty  ) {
//				noTweening = true;
//				return;
//		}
		// Check if there is no rotation.
//		if( matrix[0].b == 0 && matrix[1].b == 0 && matrix[0].c == 0 && matrix[1].c == 0 ) {
//			simpleTweening = true;
//			return;
//		}
		// TODO: check if there is rotation, but no skewing.
		// Get data for actual rotation.
		for (int i = 0; i < 2; i++) {
//			Point2D.Float xAxisCart = new Point2D.Float( matrix[i].a, matrix[i].b );
//			Point2D.Float yAxisCart = new Point2D.Float( matrix[i].c, matrix[i].d );
			Point2D.Float xAxisCart = new Point2D.Float( (float) matrix[i].getScaleX(), (float) matrix[i].getShearX() );
			Point2D.Float yAxisCart = new Point2D.Float( (float) matrix[i].getShearY(), (float) matrix[i].getScaleY() );
			xAxis[i] = Polar.fromCartesian( xAxisCart );
			yAxis[i] = Polar.fromCartesian( yAxisCart );
			
			// TODO: check tweening direction, should be nearest path. check range, should be -pi to pi.
			xAxis[i].a -= yAxis[i].a;
//			xAxis[i].a %= Math.PI;
//			left[i] = xAxis[i].a < 0;
			left[i] = PathConverter.triangleOrientation( new float[]{0,0}, new float[]{yAxisCart.x,yAxisCart.y}, new float[]{xAxisCart.x,xAxisCart.y} );
			
//			System.out.println( "matrix["+i+"]: "+matrix[i] );
//			System.out.println( "xAxisCart: "+xAxisCart );
//			System.out.println( "yAxisCart: "+yAxisCart );
//			System.out.println( "xAxis["+i+"]: "+xAxis[i] );
//			System.out.println( "yAxis["+i+"]: "+yAxis[i] );
//			Point2D.Float xAxisCartCheck = Polar.toCartesian( xAxis[i].r, xAxis[i].a + yAxis[i].a );
//			Point2D.Float yAxisCartCheck = Polar.toCartesian( yAxis[i] );
//			System.out.println( "xAxisCartCheck: "+xAxisCartCheck );
//			System.out.println( "yAxisCartCheck: "+yAxisCartCheck );
//			AffineTransform matrixCheck = new AffineTransform();
//			matrixCheck.setTransform( xAxisCartCheck.x, yAxisCartCheck.x, xAxisCartCheck.y, yAxisCartCheck.y, matrix[i].getTranslateX(), matrix[i].getTranslateY() );
//			System.out.println( "matrixChk: "+matrixCheck );
//			System.out.println();
		}
		adjustAnglesToMinDist( xAxis[0], xAxis[1] );
		adjustAnglesToMinDist( yAxis[0], yAxis[1] );
		if( left[0] != left[1] ) {
			log.debug( "flip" );
			xAxis[1].a *= -1;
			flip = true;
		}
	}
	
	static final float dblPI = (float)(2*Math.PI);
	
	public static void adjustAnglesToMinDist( Polar p1, Polar p2 ) {
		p1.a = normAngle( p1.a );
		p2.a = normAngle( p2.a );
//		float dist12 = p2.a - p1.a;
//		float dist21 = p1.a - p2.a;
		float dist = Math.abs( p2.a - p1.a );
		if( dist > Math.PI ) {
			boolean smaller1 = p1.a < Math.PI;
			boolean smaller2 = p2.a < Math.PI;
			if( smaller1 && ! smaller2 )
				p2.a -= dblPI;
			else if( ! smaller1 && smaller2 )
				p1.a -= dblPI;
		}
		// check
		float checkDist = Math.abs( p2.a - p1.a );
		if( checkDist > Math.PI )
			System.err.println( "Angle dist still larger that PI." );

	}
	
	public static float normAngle( float angle ) {
		if( angle < 0 ) {
			angle %= dblPI;
			angle += dblPI;
		}
		if( angle > dblPI )
			angle %= dblPI;
		return angle;
	}
	
	public AffineTransform tween( double p ) {
		if( nullPointer ) {
			System.out.println( "RotationMatrixTweener.tween: nullPointer" );
			return null;
		}
				
		if( noTweening ) {
			return (AffineTransform) matrix[0].clone();
		}
//		p = p < 0.5 ? 0 : 1;
		double pInv = 1 - p;
		if( simpleTweening ) {
			AffineTransform tween = new AffineTransform();
			tween.scale( matrix[0].getScaleX()*pInv + matrix[1].getScaleX()*p, matrix[0].getScaleY()*pInv + matrix[1].getScaleY()*p );
			tween.translate( matrix[0].getTranslateX()*pInv + matrix[1].getTranslateX()*p, matrix[0].getTranslateY()*pInv + matrix[1].getTranslateY()*p );
//			tween.a = matrix[0].a*pInv + matrix[1].a*p;
//			tween.d = matrix[0].d*pInv + matrix[1].d*p;
//			tween.tx = matrix[0].tx*pInv + matrix[1].tx*p;
//			tween.ty = matrix[0].ty*pInv + matrix[1].ty*p;
			return tween;
		}
		// TODO: check for flip, adjust tween according to alignment.
		// alignment needs to be given externally.
//		log.debug( node.getPath()+" has bone "+node.getBoneLink().getBone() );
//		log.debug(node.getName()+".xAxis[0]: "+xAxis[0]);
		Polar xAxisTween = new Polar( xAxis[0].r*pInv + xAxis[1].r*p, xAxis[0].a*pInv + xAxis[1].a*p );
		Polar yAxisTween = new Polar( yAxis[0].r*pInv + yAxis[1].r*p, yAxis[0].a*pInv + yAxis[1].a*p );
		xAxisTween.a += yAxisTween.a;
		Point2D.Float xAxisCart = Polar.toCartesian( xAxisTween );
		Point2D.Float yAxisCart = Polar.toCartesian( yAxisTween );
		
		xAxisCart = Polar.toCartesian( xAxisTween );
		yAxisCart = Polar.toCartesian( yAxisTween );
//		xAxisCart = Polar.toCartesian( xAxisTween );
//		yAxisCart = Polar.toCartesian( yAxisTween );
		
//		Polar.copyToCartesian( xAxisTween.a, xAxisTween.r, xAxisCart );
//		Polar.copyToCartesian( yAxisTween.a, yAxisTween.r, yAxisCart );
		
//		double yAxisTween_a = yAxis[0].a*pInv + yAxis[1].a*p;
//		Polar.copyToCartesian( xAxis[0].r*pInv + xAxis[1].r*p, xAxis[0].a*pInv + xAxis[1].a*p + yAxisTween_a, xAxisCart );
//		Polar.copyToCartesian( yAxis[0].r*pInv + yAxis[1].r*p, yAxis[0].a*pInv + yAxis[1].a*p, yAxisCart );
				
		AffineTransform tween = new AffineTransform();
		double tx = matrix[0].getTranslateX()*pInv + matrix[1].getTranslateX()*p;
		double ty = matrix[0].getTranslateY()*pInv + matrix[1].getTranslateY()*p;
//		tween.setTransform( xAxisCart.x, yAxisCart.x, xAxisCart.y, yAxisCart.y, tx, ty );

//		tween.setTransform( yAxisCart.y, yAxisCart.x, xAxisCart.y, xAxisCart.x, tx, ty );
		tween.setTransform( xAxisCart.x, yAxisCart.x, xAxisCart.y, yAxisCart.y, tx, ty );
		
//		System.out.println();
//		System.out.println( "matrix["+(p<0.5?0:1)+"] at percentage: "+p );
//		double[] m = new double[6];
//		matrix[p<0.5?0:1].getMatrix(m);
//		for (int i = 0; i < m.length; i++) {
//			System.out.print( m[i]+"  " );
//		}
//		System.out.println();
//		System.out.println( "xAxisCart: "+xAxisCart );
//		System.out.println( "yAxisCart: "+yAxisCart );
//		tween.getMatrix(m);
//		for (int i = 0; i < m.length; i++) {
//			System.out.print( m[i]+"  " );
//		}
//		System.out.println();
		
//		if(0==0)
//			return p < 0.5 ? matrix[0].clone() : matrix[1].clone();
		
//		tween.a = xAxisCart.x;
//		tween.b = xAxisCart.y;
//		tween.c = yAxisCart.x;
//		tween.d = yAxisCart.y;
//		tween.tx = matrix[0].tx*pInv + matrix[1].tx*p;
//		tween.ty = matrix[0].ty*pInv + matrix[1].ty*p;
		return tween;
	}

}
