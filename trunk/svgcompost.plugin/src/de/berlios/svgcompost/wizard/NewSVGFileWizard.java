package de.berlios.svgcompost.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewSVGFileWizard extends Wizard implements INewWizard {
	
	private IStructuredSelection selection;
    private NewSVGFileCreationPage newFileWizardPage;
    private IWorkbench workbench;
    
    public NewSVGFileWizard() {

        setWindowTitle("New SVG File");

    } 

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }
	
    @Override
    public void addPages() {

        newFileWizardPage = new NewSVGFileCreationPage(selection);
        addPage(newFileWizardPage);
    }
    
	@Override
    public boolean performFinish() {
       
        IFile file = newFileWizardPage.createNewFile();
        if (file != null)
            return true;
        else
            return false;
    }
	
}
