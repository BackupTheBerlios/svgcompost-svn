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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.EventObject;
import java.util.List;
import java.util.Properties;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;

import de.berlios.svgcompost.copy.CopyAction;
import de.berlios.svgcompost.copy.DeleteAction;
import de.berlios.svgcompost.copy.PasteAction;
import de.berlios.svgcompost.layers.BreakApartAction;
import de.berlios.svgcompost.layers.FlipBackwardAction;
import de.berlios.svgcompost.layers.FlipForwardAction;
import de.berlios.svgcompost.layers.HideShowLayersAction;
import de.berlios.svgcompost.layers.LowerNodeAction;
import de.berlios.svgcompost.layers.RaiseNodeAction;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.SVGScalableFreeformRootEditPart;
import de.berlios.svgcompost.part.SingleLevelFactory;
import de.berlios.svgcompost.provider.SVGDropTargetListener;
import de.berlios.svgcompost.provider.SVGEditorContextMenuProvider;
import de.berlios.svgcompost.provider.SVGEditorPaletteFactory;
import de.berlios.svgcompost.provider.SVGTreeOutlinePage;


/**
 * An Editor for SVG documents. 
 * @author Gerrit Karius
 *
 */
public class SVGEditor extends GraphicalEditorWithFlyoutPalette implements IDoubleClickListener {

	private static PaletteRoot PALETTE_MODEL;
	
	private SVGDocument doc;
	private BridgeContext ctx;

	
	private SVGTreeOutlinePage outline;
	private PropertySheetPage propertySheetPage;
	
	private SingleLevelFactory factory;
	

	public SVGEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}
	public DefaultEditDomain getEditDomain() {
		return super.getEditDomain();
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();

		viewer.setRootEditPart(new SVGScalableFreeformRootEditPart());

		factory = new SingleLevelFactory();
		viewer.setEditPartFactory(factory);
		
		ActionRegistry registry = getActionRegistry();
		KeyHandler keyHandler = new GraphicalViewerKeyHandler(viewer);
		
		keyHandler.put( KeyStroke.getPressed( SWT.PAGE_DOWN, 0), registry.getAction(LowerNodeAction.LOWER_NODE));
		keyHandler.put( KeyStroke.getPressed( SWT.PAGE_UP, 0), registry.getAction(RaiseNodeAction.RAISE_NODE));
		keyHandler.put( KeyStroke.getPressed( SWT.DEL, 0), registry.getAction(ActionFactory.DELETE.getId()));
		keyHandler.put( KeyStroke.getPressed( 'h', 0x68, 0), registry.getAction(HideShowLayersAction.HIDE_SHOW_LAYERS));
		keyHandler.put( KeyStroke.getPressed( 'l', 0x6c, 0), registry.getAction(FlipForwardAction.FLIP_FORWARD));
		keyHandler.put( KeyStroke.getPressed( 'k', 0x6b, 0), registry.getAction(FlipBackwardAction.FLIP_BACKWARD));
		keyHandler.put( KeyStroke.getPressed( 'b', 0x62, 0), registry.getAction(BreakApartAction.BREAK_APART));

		viewer.setKeyHandler(keyHandler);

		// configure the context menu provider
		ContextMenuProvider cmProvider =
			new SVGEditorContextMenuProvider(viewer, registry);
		viewer.setContextMenu(cmProvider);
		getSite().registerContextMenu(cmProvider, viewer);

		viewer.addDropTargetListener(new SVGDropTargetListener(getGraphicalViewer()));
	}


	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	@Override
	public void createActions() {
		super.createActions();

		registerAction( new CopyAction(this) );
		registerAction( new PasteAction(this) );
		registerAction( new DeleteAction(this) );
		registerAction( new RaiseNodeAction(this) );
		registerAction( new LowerNodeAction(this) );
		registerAction( new HideShowLayersAction(this) );
		registerAction( new FlipForwardAction(this) );
		registerAction( new FlipBackwardAction(this) );
		registerAction( new BreakApartAction(this) );
		}
	
	protected void registerAction( IAction action ) {
		getActionRegistry().registerAction(action);
		getSelectionActions().add(action.getId());
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
		if( doc == null ) {
			System.err.println( "SVG Document is null." );
			return;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			createOutputStream(out);
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true, false, monitor);
			getCommandStack().markSaveLocation();
		} catch (CoreException e) { 
			e.printStackTrace();
		}
	}
	
	protected void createOutputStream(ByteArrayOutputStream output) {
        try {

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
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();

		IPath path = dialog.getResult();	
		if (path != null) {
			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			try {
				new ProgressMonitorDialog(shell).run(false, false, new WorkspaceModifyOperation() {
							public void execute(final IProgressMonitor monitor) {
								try {
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									createOutputStream(out);
									file.create(new ByteArrayInputStream(out.toByteArray()), true, monitor);
								} catch (CoreException ce) {
									ce.printStackTrace();
								} 
							}
						});
				setInput(new FileEditorInput(file));
				getCommandStack().markSaveLocation();
			} catch (InterruptedException e) {
				e.printStackTrace(); 
			} catch (InvocationTargetException e) { 
				e.printStackTrace(); 
			}
		}
	}

	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class) {
			outline = new SVGTreeOutlinePage(this);
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

		if( outline != null )
			outline.setInput(doc);

		Element root = doc.getRootElement();
		viewer.setContents( root );
		
	}


	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	public Element getFirstSelectedElement(ISelection selection) {
		if( selection instanceof StructuredSelection ) {
			StructuredSelection structured = (StructuredSelection) selection;
			Object selected = structured.getFirstElement();
			Element element = null;
			if( selected instanceof Element )
				element = (Element) selected;
			else if( selected instanceof EditPart )
				element = (Element) ( (EditPart)selected ).getModel();
			return element;
		}
		return null;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		Element element = getFirstSelectedElement(selection);
		GraphicalViewer viewer = getGraphicalViewer();
		if( element != null ) {
//			EditPart part = (EditPart) viewer.getEditPartRegistry().get(element);
			List<EditPart> children = viewer.getRootEditPart().getChildren();
			if( children.size() > 0 && children.get(0) instanceof BackgroundPart ) {
				BackgroundPart bgPart = (BackgroundPart) children.get(0);
				bgPart.setEditRoot( (SVGOMElement) element );
				viewer.setSelection( new StructuredSelection(bgPart) );
			}
		}
		else
			viewer.setSelection( new StructuredSelection() );
	}


}