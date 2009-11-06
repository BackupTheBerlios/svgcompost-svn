package de.berlios.svgcompost.util;

import org.apache.batik.dom.util.XLinkSupport;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class LinkHelper {
	
	public static void refactorLinks( Node node, String oldBaseUri, String newBaseUri ) {
		IPath oldFile = Path.fromOSString(oldBaseUri);
		IPath oldFolder = oldFile.removeLastSegments(1);
		IPath newFolder = Path.fromOSString(newBaseUri).removeLastSegments(1);
		refactorLinks(node, oldFile, oldFolder, newFolder);
	}
	
	public static void refactorLinks( Node node, IPath oldFile, IPath oldFolder, IPath newFolder ) {
		if( ! (node instanceof Element) )
			return;
		
		Element element = (Element) node;
		Attr attr = element.getAttributeNodeNS(XLinkSupport.XLINK_NAMESPACE_URI, "href");
		if( attr != null ) {
			refactorLink(attr, oldFile, oldFolder, newFolder);
		}
		
		NodeList list = element.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			refactorLinks( list.item(i), oldFile, oldFolder, newFolder );
		}
	}
	
	public static void refactorLink(Attr refNode, IPath oldFile, IPath oldFolder, IPath newFolder) {
	
		String oldValue = refNode.getValue();
		String refValue = oldValue;
		String refAnchor = null;
		String newValue;
		
		int anchorIndex = oldValue.indexOf('#');
		if( anchorIndex != -1 ) {
			refValue = oldValue.substring(0,anchorIndex);
			refAnchor = oldValue.substring(anchorIndex+1);
		}
		if( refValue.equals("") ) {
			newValue = createRelativePath(newFolder, oldFile).toString();
		}
		else {
			IPath hrefPath = oldFolder.append(refValue);
			IPath newHrefPath = createRelativePath( newFolder, hrefPath );
			newValue = newHrefPath.toString();
		}
		if( refAnchor != null )
			newValue += '#'+refAnchor;

		refNode.setValue(newValue);
	}
	
	public static String createRelativePath( String fromFile, String toFile ) {
		IPath oldFile = Path.fromOSString(fromFile);
		IPath oldFolder = oldFile.removeLastSegments(1);
		IPath toPath = Path.fromOSString(toFile);
		return createRelativePath( oldFolder, toPath ).toString();
	}

	public static IPath createRelativePath( IPath absFromFolder, IPath absTo ) {
		int matchingSegs = absFromFolder.matchingFirstSegments(absTo);
		IPath relPath = absTo.removeFirstSegments(matchingSegs);
		int pathUp = absFromFolder.segmentCount() - matchingSegs;
		if( pathUp > 0 ) {
			String up = "";
			for( int i=0; i<pathUp; i++ ) {
				up += "../";
			}
			relPath = new Path(up).append(relPath);
		}
		return relPath;
	}

}
