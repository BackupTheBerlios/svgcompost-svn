package de.berlios.svgcompost.plugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * A utility class that provides functions to create grouped SWT widgets.
 * @author Gerrit Karius
 *
 */
public class WidgetHelper {
	
	
	public static Composite addContainer(Composite parent, int numColumns) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		container.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		container.setLayoutData(data);
		return container;
	}

	public static Group addGroup(Composite parent, String title, int numColumns) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(title);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		group.setLayout(layout);
		return group;
	}
	
	public static Combo addCombo(Composite parent, String[] choices, int selection) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(choices);
		combo.select(selection);
		combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		return combo;
	}

	public static Text addTextField(Composite parent) {
		Text textField = new Text(parent, SWT.SINGLE);
		textField.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		return textField;
	}

	public static Button addCheckButton(Composite parent, String label, boolean checked) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		button.setSelection(checked);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		return button;
	}

	public static Button addRadioButton(Composite parent, String label, boolean checked) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(label);
		button.setSelection(checked);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		return button;
	}

	public static void addLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	}

	public static void addEmptyLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("");
		label.setVisible(false);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	}

	public static void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL ));
	}

}
