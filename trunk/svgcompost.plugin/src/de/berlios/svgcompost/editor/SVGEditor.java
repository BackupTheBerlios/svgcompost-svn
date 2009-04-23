/**
 * Copyright 2009 Gerrit Karius
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.berlios.svgcompost.editor;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.EventObject;
import java.util.Properties;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;


import com.sun.org.apache.xml.internal.serialize.OutputFormat;

import de.berlios.svgcompost.model.ParentElement;
import de.berlios.svgcompost.part.SingleLevelFactory;
import de.berlios.svgcompost.provider.SVGEditorContextMenuProvider;
import de.berlios.svgcompost.provider.SVGEditorPaletteFactory;
import de.berlios.svgcompost.provider.SVGTreeOutlinePage;


/**
 * An Editor for SVG documents. 
 * @author Gerrit Karius
 *
 */
public class SVGEditor extends GraphicalEditorWithFlyoutPalette {

	private static PaletteRoot PALETTE_MODEL;
	
	private SVGDocument doc;
	private BridgeContext ctx;

	
	private SVGTreeOutlinePage outline;
	
	private SingleLevelFactory factory;
	

	public SVGEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();

		viewer.setRootEditPart(new ScalableFreeformRootEditPart());

		factory = new SingleLevelFactory();
		viewer.setEditPartFactory(factory);
		factory.setViewer(viewer);
		
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));

		// configure the context menu provider
		ContextMenuProvider cmProvider =
			new SVGEditorContextMenuProvider(viewer, getActionRegistry());
		viewer.setContextMenu(cmProvider);
		getSite().registerContextMenu(cmProvider, viewer);
	}


	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
			}
		};
	}

	private TransferDropTargetListener createTransferDropTargetListener() {
		return new TemplateTransferDropTargetListener(getGraphicalViewer()) {
			protected CreationFactory getFactory(Object template) {
				return new SimpleFactory((Class) template);
			}
		};
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IFile file = ((IFileEditorInput) getEditorInput()).getFile();

		if( doc == null ) {
			System.err.println( "SVG Document is null." );
			return;
		}
		saveToFile(file);
	}
	
	protected void saveToFile(IFile file) {
        try {

            OutputStream output = new FileOutputStream( file.getRawLocation().toString() );
    		Writer writer = new OutputStreamWriter( output, "UTF-8" );//$NON-NLS-1$

			OutputFormat format = new OutputFormat(doc);
			format.setIndenting(true);
			format.setLineWidth(0);
			format.setPreserveSpace(true);
        	Properties props = OutputPropertiesFactory.getDefaultMethodProperties(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT);
			Serializer serializer = SerializerFactory.getSerializer(props);
			serializer.asDOMSerializer();
			serializer.setWriter(writer);
			((DOMSerializer) serializer).serialize(doc);

			getCommandStack().markSaveLocation();
			fireChange();
			
			// TODO: refresh?

        } catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(RuntimeException e) {
			e.printStackTrace();
		}

	}
	
	public void fireChange() {
		firePropertyChange(PROP_DIRTY);
	}


	public void doSaveAs() {
		// Show a SaveAs dialog
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();

		IPath path = dialog.getResult();	
		if (path != null) {
			// try to save the editor's contents under a different file name
			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			saveToFile(file);
			//TODO: use file.create(InputStream) instead

		}
	}

	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class) {
			outline = new SVGTreeOutlinePage();
			System.out.println( "outline: "+outline );
			outline.setInput(doc);
			return outline;
		}
		return super.getAdapter(type);
	}

	protected PaletteRoot getPaletteRoot() {
		if (PALETTE_MODEL == null)
			PALETTE_MODEL = SVGEditorPaletteFactory.createPalette();
		return PALETTE_MODEL;
	}

	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		initializeSVGCanvas();
	}
	
	protected void initializeSVGCanvas() {
		GraphicalViewer viewer = getGraphicalViewer();

		// listen for dropped parts
		viewer.addDropTargetListener(createTransferDropTargetListener());
		
		IFile file = ((IFileEditorInput) getEditorInput()).getFile();
		String fileUri = null;
		try {
			fileUri = file.getLocationURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		System.out.println( "fileUri: "+fileUri );

        String xmlReaderClassName = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory docFactory = new SAXSVGDocumentFactory(xmlReaderClassName);

		try {
			// Parse SVG document.
			doc = (SVGDocument) docFactory.createSVGDocument( fileUri );
			ctx = new BridgeContext( new UserAgentAdapter() );
			ctx.setDynamic( true );
			GVTBuilder builder = new GVTBuilder();
			// Build GVT tree.
			builder.build( ctx, doc );
		}
		catch( IOException exc ) {
			exc.printStackTrace();
		}
		
		this.factory.setBridgeContext(ctx);

		System.out.println("set contents for outline: "+outline);
		if( outline != null )
			outline.setInput(doc);

		Element root = doc.getRootElement();
		System.out.println( "gNode for root: "+ctx.getGraphicsNode(root) );
		viewer.setContents( new ParentElement( doc.getRootElement(), ctx ) );
		
	}



	public boolean isSaveAsAllowed() {
		return true;
	}


}