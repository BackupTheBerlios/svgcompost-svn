/******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 *    Gerrit Karius   - adaption to single GVT node
 ****************************************************************************/

package de.berlios.svgcompost.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.Rectangle2DKey;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

import de.berlios.svgcompost.freetransform.FreeTransformHelper;

//import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.graphics.Graphics2DToGraphicsAdaptor;


/**
 * Renders a GVT GraphicsNode to a bitmap image.
 * This GraphicsNode can be anywhere within the GVT tree.
 * The image will have the global bounds of this node.
 * Based on the GMF SWTImageTranscoder class by sshaw,
 * which is sadly made internal and cannot be reused directly.
 * @see org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.SWTImageTranscoder 
 *
 */
public class GVTRenderer {

	private static final RGB TRANSPARENT_COLOR = new RGB(254, 255, 254);

	private static final RGB REPLACE_TRANSPARENT_COLOR = new RGB(255, 255, 255);
	
	public static final TranscodingHints.Key KEY_MAINTAIN_ASPECT_RATIO = new BooleanKey();

	public static final TranscodingHints.Key KEY_ANTI_ALIASING = new BooleanKey();
					
    public static final TranscodingHints.Key KEY_AOI = new Rectangle2DKey();

    public static final TranscodingHints.Key KEY_WIDTH = new LengthKey();
    
    public static final TranscodingHints.Key KEY_MAX_WIDTH = new LengthKey();
    
    public static final TranscodingHints.Key KEY_HEIGHT = new LengthKey();
    
    public static final TranscodingHints.Key KEY_MAX_HEIGHT = new LengthKey();
    
	private Image swtImage = null;
	
	private GC swtGC = null;
	
    protected float width = 400, height = 400;
    
    protected AffineTransform curTxf;

    protected TranscodingHints hints = new TranscodingHints();

	public Image transcode( BridgeContext ctx, GraphicsNode gvtRoot)
	throws TranscoderException {
		
		// get the 'width' and 'height' attributes of the SVG document
		float docWidth = (float)ctx.getDocumentSize().getWidth();
		float docHeight = (float)ctx.getDocumentSize().getHeight();

		Rectangle2D gvtBounds = FreeTransformHelper.getGlobalBounds( gvtRoot );
		float gvtWidth = (float) gvtBounds.getWidth();
		float gvtHeight = (float) gvtBounds.getHeight();

		setImageSize(docWidth, docHeight);
		

		//compute the transformation matrix
		AffineTransform Px = AffineTransform.getTranslateInstance(0, 0);
		
//		Element el = ctx.getElement(gvtRoot);
//		String id = el.getAttribute("id");
//		System.out.println( id+".tx: "+Px.getTranslateX() );
//		System.out.println( id+".ty: "+Px.getTranslateY() );

		gvtRoot = renderImage( gvtRoot, Px, (int)gvtWidth, (int)gvtHeight);

		return swtImage;
	}

	public GraphicsNode renderImage( GraphicsNode gvtRoot, AffineTransform Px, int w, int h)
	throws TranscoderException {
		
		Graphics2D g2d = createGraphics(w, h);
	
		// Check anti-aliasing preference
		if (hints.containsKey(KEY_ANTI_ALIASING)) {	
			boolean antialias = ((Boolean)hints.get(KEY_ANTI_ALIASING)).booleanValue();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		} else {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		g2d.clip(new java.awt.Rectangle(0, 0, w, h));
		
		g2d.transform(Px);
		
		gvtRoot.paint(g2d);
		
		postRenderImage(g2d);
		
		return null;
	}

	protected AffineTransform calculateSizeTransform(/*SVGSVGElement*/Element svgRoot, GraphicsNode gvtRoot, String uri, 
			float docWidth, float docHeight, 
			float newWidth, float newHeight) 
	throws TranscoderException {
		AffineTransform Px;
		String ref = null;
		try {
			ref = new URL(uri == null ? "": uri).getRef(); //$NON-NLS-1$
		} catch (MalformedURLException ex) {

		}
		
		boolean maintainAspectRatio = true;
		if (hints.containsKey(KEY_MAINTAIN_ASPECT_RATIO)) {
			maintainAspectRatio = ((Boolean)hints.get(KEY_MAINTAIN_ASPECT_RATIO)).booleanValue();
		}
		
		if (maintainAspectRatio) {
			try {
				Px = ViewBox.getViewTransform(ref, svgRoot, newWidth, newHeight);
			} catch (BridgeException ex) {
				ex.printStackTrace();
				throw new TranscoderException(ex);
			}
			
			if (Px.isIdentity() && (newWidth != docWidth || newHeight != docHeight)) {
				// The document has no viewBox, we need to resize it by hand.
				// we want to keep the document size ratio
				float xscale = newWidth / docWidth;
                float yscale = newHeight / docHeight;
                if (docHeight / docWidth > newHeight / newWidth) {
                    xscale = yscale;
                } else {
                    yscale = xscale;
                }
                
				Px = AffineTransform.getScaleInstance(xscale, yscale);
			}
		}
		else {
			float xscale = newWidth / docWidth;
			float yscale = newHeight / docHeight; 
			Px = AffineTransform.getScaleInstance(xscale, yscale);
		}
		
		// take the AOI into account if any
		if (hints.containsKey(KEY_AOI)) {
			Rectangle2D aoi = (Rectangle2D)hints.get(KEY_AOI);
			// transform the AOI into the image's coordinate system
			aoi = Px.createTransformedShape(aoi).getBounds2D();
			AffineTransform Mx = new AffineTransform();
			double sx = newWidth / aoi.getWidth();
			double sy = newHeight / aoi.getHeight();
			Mx.scale(sx, sy);
			double tx = -aoi.getX();
			double ty = -aoi.getY();
			Mx.translate(tx, ty);

			// take the AOI transformation matrix into account
			// we apply first the preserveAspectRatio matrix
			Px.preConcatenate(Mx);
		}
		
		CanvasGraphicsNode cgn = getCanvasGraphicsNode(gvtRoot);
        if (cgn != null) {
            cgn.setViewingTransform(Px);
            curTxf = new AffineTransform();
        } else {
            curTxf = Px;
        }
        
		return curTxf;
	}
	
	protected Graphics2D createGraphics(int w, int h) {
		Display display = Display.getDefault();

		ImageData imgData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
		imgData.transparentPixel = FigureUtilities.RGBToInteger(TRANSPARENT_COLOR).intValue();
		
		swtImage = new Image(display, imgData);
		swtGC = new GC(swtImage);
		
		Color transparentColor = new Color(null, TRANSPARENT_COLOR);
		swtGC.setBackground(transparentColor);
		swtGC.fillRectangle(0, 0, w, h);
		transparentColor.dispose();
		
        Graphics2D g2d = new Graphics2DToGraphicsAdaptor(swtGC, TRANSPARENT_COLOR, REPLACE_TRANSPARENT_COLOR);
        // needed to avoid erroneous error being dumped to console
        g2d.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING,
            RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING);
        
        return g2d;
	}
	
    protected void setImageSize(float docWidth, float docHeight) {

        // Compute the image's width and height according the hints
        float imgWidth = -1;
        if (hints.containsKey(KEY_WIDTH)) {
            imgWidth = ((Float)hints.get(KEY_WIDTH)).floatValue();
        }
        float imgHeight = -1;
        if (hints.containsKey(KEY_HEIGHT)) {
            imgHeight = ((Float)hints.get(KEY_HEIGHT)).floatValue();
        }

        if (imgWidth > 0 && imgHeight > 0) {
            width = imgWidth;
            height = imgHeight;
        } else if (imgHeight > 0) {
            width = (docWidth * imgHeight) / docHeight;
            height = imgHeight;
        } else if (imgWidth > 0) {
            width = imgWidth;
            height = (docHeight * imgWidth) / docWidth;
        } else {
            width = docWidth;
            height = docHeight;
        }

        // Limit image size according to the maximum size hints.
        float imgMaxWidth = -1;
        if (hints.containsKey(KEY_MAX_WIDTH)) {
            imgMaxWidth = ((Float)hints.get(KEY_MAX_WIDTH)).floatValue();
        }
        float imgMaxHeight = -1;
        if (hints.containsKey(KEY_MAX_HEIGHT)) {
            imgMaxHeight = ((Float)hints.get(KEY_MAX_HEIGHT)).floatValue();
        }

        if ((imgMaxHeight > 0) && (height > imgMaxHeight)) {
            width = (docWidth * imgMaxHeight) / docHeight;
            height = imgMaxHeight;
        }
        if ((imgMaxWidth > 0) && (width > imgMaxWidth)) {
            width = imgMaxWidth;
            height = (docHeight * imgMaxWidth) / docWidth;
        }
    }

    protected CanvasGraphicsNode getCanvasGraphicsNode(GraphicsNode gn) {
        if (!(gn instanceof CompositeGraphicsNode))
            return null;
        CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
        List children = cgn.getChildren();
        if (children.size() == 0) 
            return null;
        gn = (GraphicsNode)children.get(0);
        if (!(gn instanceof CanvasGraphicsNode))
            return null;
        return (CanvasGraphicsNode)gn;
    }

    protected void postRenderImage(Graphics2D g2d) {
		g2d.dispose();
	}
}
