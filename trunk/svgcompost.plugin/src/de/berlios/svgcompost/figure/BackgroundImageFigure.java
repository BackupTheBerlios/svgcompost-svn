package de.berlios.svgcompost.figure;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class BackgroundImageFigure extends FreeformLayer {

	protected Image img;
	protected Dimension size = new Dimension();
	protected int alignment;
	private int[] origin = new int[2];
	private static final int crosshairSize = 5;

	public void setImage(Image image) {
		if (img == image)
			return;
		else if ( img != null )
			img.dispose();
		img = image;
		if (img != null)
			size = new Rectangle(image.getBounds()).getSize();
		else
			size = new Dimension();
		revalidate();
		repaint();
	}
	
	public Image getImage() {
		return img;
	}
	

	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		
		if (getImage() == null)
			return;

		int x = 0;
		int y = 0;
//		Rectangle area = getClientArea();
//		switch (alignment & PositionConstants.NORTH_SOUTH) {
//			case PositionConstants.NORTH:
//				y = area.y;
//				break;
//			case PositionConstants.SOUTH:
//				y = area.y + area.height - size.height;
//				break;
//			default:
//				y = (area.height - size.height) / 2 + area.y;
//				break;
//		}
//		switch (alignment & PositionConstants.EAST_WEST) {
//			case PositionConstants.EAST:
//				x = area.x + area.width - size.width;
//				break;
//			case PositionConstants.WEST:
//				x = area.x;
//				break;
//			default:
//				x = (area.width - size.width) / 2 + area.x;
//				break;
//		}
		graphics.drawImage(getImage(), x, y);
		drawCrosshair(graphics);
	}

	protected void drawCrosshair(Graphics graphics) {
		graphics.setForegroundColor(org.eclipse.draw2d.ColorConstants.black);
		graphics.setLineWidth(1);
		graphics.setLineStyle(SWT.LINE_SOLID);
		graphics.drawLine(origin[0]-crosshairSize, origin[1], origin[0]+crosshairSize, origin[1]);
		graphics.drawLine(origin[0], origin[1]-crosshairSize, origin[0], origin[1]+crosshairSize);
	}
	
	protected void __paintFigure(Graphics graphics) {
		if (img != null) {
			org.eclipse.draw2d.geometry.Rectangle targetRect = getBounds().getCopy();
			org.eclipse.swt.graphics.Rectangle imgBox = img.getBounds();
			graphics.drawImage(img, 0, 0, imgBox.width,imgBox.height, targetRect.x, targetRect.y, targetRect.width, targetRect.height);
		}
		super.paintFigure(graphics);
	}

	public void setCrosshair(float[] origin) {
		this.origin[0] = (int)origin[0];
		this.origin[1] = (int)origin[1];
	}
}
