package org.gvt.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.util.CommonStreamOptionsPack;
import org.gvt.util.EntityHolder;

/**
 * This class maintains Common Stream Query Dialog for TopMenuBar
 *
 * @author Shatlyk Ashyralyev
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CommonStreamQueryParamWithEntitiesDialog extends AbstractQueryParamWithStreamDialog
{
	/**
	 * All entities of graph
	 */
	ArrayList<EntityHolder> allEntities;

	/**
	 * Entities which are added
	 */
	ArrayList<EntityHolder> addedEntities;

	/**
	 * Getter
	 */
	public ArrayList<EntityHolder> getAddedEntities()
	{
		return this.addedEntities;
	}

	/**
	 * Create the dialog
	 */
	public CommonStreamQueryParamWithEntitiesDialog(ChisioMain main)
	{
		super(main);
		this.allEntities = main.getAllEntities();
		this.addedEntities = new ArrayList<EntityHolder>();
	}

	/**
	 * Open the dialog
	 */
	public CommonStreamOptionsPack open(CommonStreamOptionsPack opt)
	{
		createContents(opt);

		shell.setLocation(
			getParent().getLocation().x + (getParent().getSize().x / 2) -
			(shell.getSize().x / 2),
			getParent().getLocation().y + (getParent().getSize().y / 2) -
			(shell.getSize().y / 2));

		shell.open();

		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return opt;
	}

	/**
	 * Create contents of the dialog.
	 * Buttons, List, Text Field, Radio Buttons, etc
	 */
	protected void createContents(final CommonStreamOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Common Stream Query Properties");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			CommonStreamQueryParamWithEntitiesDialog.class,
			"/org/gvt/icon/cbe-icon.png");
		shell.setImage(id.createImage());

		//layout of shell contains 6 columns

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		shell.setLayout(gridLayout);

		GridData gridData;

		//Entity list
		createList(2, 2, 300, 5);

		//Group for currentViewButton and newViewButton
		createResultViewGroup(2, 2);

		//Group for downstreamButton, upstreamButton and bothBotton
		createStreamDirectionGroup(2, 2, false);

		//Add Entity Button
		
		addButton = new Button(shell, SWT.NONE);
		addButton.setText("Add...");
		gridData =
			new GridData(GridData.END, GridData.BEGINNING, true, false);
		gridData.minimumWidth = 100;
		gridData.horizontalIndent = 5;
		addButton.setLayoutData(gridData);
		addButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//new addEntityDialog
				AddEntityDialog addEntityDialog = 
					new AddEntityDialog(new Shell(), allEntities);

				//open dialog
				boolean addPressed = addEntityDialog.open();

				//if add button is pressed
				if (addPressed)
				{
					//for each selected entity
					for (EntityHolder entity : addEntityDialog.getSelectedEntities())
					{
						//check if entity has been added before
						if (!previouslyAdded(entity))
						{
							//add entity keyName to List
							entityList.add(entity.getName());
							
							//add entity to addedEntities ArrayList
							addedEntities.add(entity);
						}
					}
				}
			}
		});

		//Remove Entity Button

		removeButton = new Button(shell, SWT.NONE);
		removeButton.setText("Remove");
		gridData = 
			new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false);
		gridData.horizontalIndent = 5;
		gridData.minimumWidth = 100;
		removeButton.setLayoutData(gridData);
		removeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				String[] selectionResult = entityList.getSelection();
				
				//for each selected string
				for (String selected : selectionResult)
				{
					//search among all addedEntities
					for (int j = 0 ; j < addedEntities.size() ; j++)
					{
						EntityHolder entity = addedEntities.get(j);
						
						//if corresponding entity is found
						if (selected != null &&
							selected.equals(entity.getName()))
						{
							//remove entity from addedEntities ArrayList
							addedEntities.remove(j);
							
							//remove entity keyName from from List
							entityList.remove(selected);
						}
					}
				}
			}
		});

		//Length Limit Label and Text
		createLengthLimit(1, 1, 1, 1, 50);

		// Group for execute, cancel and default buttons

		exeCancelDefaultGroup = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gridData.horizontalSpan = 6;
		exeCancelDefaultGroup.setLayoutData(gridData);
		exeCancelDefaultGroup.setLayout(new GridLayout(3, true));

		//Execute Button
		
		executeButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		executeButton.setText("Execute");
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		executeButton.setLayoutData(gridData);
		executeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//if no entity is added, show error
				if (getAddedEntities().isEmpty())
				{
					MessageDialog.openError(main.getShell(), "Error!",
					"Add Entity!");
					
					return;
				}
			
				//store values in dialog to optionsPack
				storeValuesToOptionsPack(opt);

				//ok is selected
				opt.setCancel(false);

				shell.close();
			}
		});
		

		//Cancel Button
		
		cancelButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		gridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
		createCancelButton(gridData);
		
		//Default Button
		
		defaultButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		defaultButton.setText("Default");
		gridData = 
			new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		defaultButton.setLayoutData(gridData);
		defaultButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//set default values of dialog 
				setDefaultQueryDialogOptions();
			}
		});

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
	}

	/**
	 * This method checks whether physicalEntity is added before.
	 */
	private boolean previouslyAdded(EntityHolder pe)
	{
		for (EntityHolder addedBefore : this.addedEntities)
		{
			//if entity has been added before, return true
			if (pe == addedBefore)
			{
				return true;
			}
		}
		//if entity is not found in added List, then return false
		return false;
	}
	
	/**
	 * After clicking OK button,
	 * all data in dialog is saved to CommonStreamOptionsPack
	 */
	public void storeValuesToOptionsPack(CommonStreamOptionsPack opt)
	{
		//store Length Limit
		opt.setLengthLimit(Integer.parseInt(lengthLimit.getText()));

		//if downstream is selected
		if (downstreamButton.getSelection())
		{
			opt.setDownstream(true);
		}
		//if upstream is selected
		else
		{
			opt.setDownstream(false);
		}

		//if currentView is selected
		if (currentViewButton.getSelection())
		{
			opt.setCurrentView(true);
		}
		//if newView is selected
		else
		{
			opt.setCurrentView(false);
		}
	}

	/**
	 * After creating the dialog box, 
	 * fields are completed with data in opt OptionsPack
	 */
	public void setInitialValues(CommonStreamOptionsPack opt)
	{
		super.setInitialValues(opt);

		//Downstream or Upstream
		
		if (opt.isDownstream())
		{
			downstreamButton.setSelection(true);
		}
		else
		{
			upstreamButton.setSelection(true);	
		}
	}

	/**
	 * Set default values into dialog
	 */
	public void setDefaultQueryDialogOptions()
	{
		super.setDefaultQueryDialogOptions();

		downstreamButton.setSelection(DOWNSTREAM);
		upstreamButton.setSelection(!DOWNSTREAM);

	}
}