package de.berlios.svgcompost.animation.export.binary;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import org.apache.batik.gvt.ShapePainter;

public class PainterAccess extends Graphics2D {
	
	protected Paint paint;
	protected Stroke stroke;
	
	public Paint getPaint(ShapePainter painter) {
		paint = null;
		painter.paint(this);
		return paint;
	}

	public Stroke getStroke(ShapePainter painter) {
		stroke = null;
		painter.paint(this);
		return stroke;
	}

	@Override
	public void addRenderingHints(Map<?, ?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clip(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean drawImage(Image arg0, AffineTransform arg1,
			ImageObserver arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(String arg0, float arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(AttributedCharacterIterator arg0, float arg1,
			float arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fill(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Color getBackground() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Composite getComposite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Paint getPaint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRenderingHint(Key arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RenderingHints getRenderingHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stroke getStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AffineTransform getTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void rotate(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rotate(double arg0, double arg1, double arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scale(double arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBackground(Color arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setComposite(Composite arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	@Override
	public void setRenderingHint(Key arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRenderingHints(Map<?, ?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	@Override
	public void setTransform(AffineTransform arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shear(double arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transform(AffineTransform arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void translate(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void translate(double arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearRect(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clipRect(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Graphics create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
			ImageObserver arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, ImageObserver arg5) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, Color arg5, ImageObserver arg6) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9,
			ImageObserver arg10) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawLine(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawOval(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillOval(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillRect(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Shape getClip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getClipBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font getFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FontMetrics getFontMetrics(Font arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClip(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColor(Color arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFont(Font arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPaintMode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setXORMode(Color arg0) {
		// TODO Auto-generated method stub
		
	}

}
