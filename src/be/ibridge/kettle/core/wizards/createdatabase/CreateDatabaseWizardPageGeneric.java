/***********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/


package be.ibridge.kettle.core.wizards.createdatabase;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.GenericDatabaseMeta;


/**
 * 
 * On page one we select the database connection SAP/R3 specific settings
 * 1) The data tablespace
 * 2) The index tablespace
 * 
 * @author Jens Bleuel
 * @since  22-mar-2006
 */
public class CreateDatabaseWizardPageGeneric extends WizardPage
{
	private Label    wlURL;
	private Text     wURL;
	private FormData fdlURL, fdURL;

	private Label    wlDriverClass;
	private Text     wDriverClass;
	private FormData fdlDriverClass, fdDriverClass;
    
	private Props props;
	private DatabaseMeta info;
	
	public CreateDatabaseWizardPageGeneric(String arg, Props props, DatabaseMeta info)
	{
		super(arg);
		this.props=props;
		this.info = info;
		
		setTitle(Messages.getString("CreateDatabaseWizardPageGeneric.DialogTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("CreateDatabaseWizardPageGeneric.DialogMessage")); //$NON-NLS-1$
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		int margin = Const.MARGIN;
		int middle = props.getMiddlePct();
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// URL
		wlURL = new Label(composite, SWT.RIGHT);
		wlURL.setText(Messages.getString("CreateDatabaseWizardPageGeneric.URL.Label")); //$NON-NLS-1$
 		props.setLook(wlURL);
		fdlURL = new FormData();
		fdlURL.top    = new FormAttachment(0, 0);
		fdlURL.left   = new FormAttachment(0, 0);
		fdlURL.right  = new FormAttachment(middle,0);
		wlURL.setLayoutData(fdlURL);
		wURL = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wURL);
		fdURL = new FormData();
		fdURL.top     = new FormAttachment(0, 0);
		fdURL.left    = new FormAttachment(middle, margin);
		fdURL.right   = new FormAttachment(100, 0);
		wURL.setLayoutData(fdURL);
		wURL.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		// DRIVER CLASS
		wlDriverClass = new Label(composite, SWT.RIGHT);
		wlDriverClass.setText(Messages.getString("CreateDatabaseWizardPageGeneric.DriverClass.Label")); //$NON-NLS-1$
 		props.setLook(wlDriverClass);
		fdlDriverClass = new FormData();
		fdlDriverClass.top    = new FormAttachment(wURL, margin);
		fdlDriverClass.left   = new FormAttachment(0, 0);
		fdlDriverClass.right  = new FormAttachment(middle,0);
		wlDriverClass.setLayoutData(fdlDriverClass);
		wDriverClass = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wDriverClass);
		fdDriverClass = new FormData();
		fdDriverClass.top     = new FormAttachment(wURL, margin);
		fdDriverClass.left    = new FormAttachment(middle, margin);
		fdDriverClass.right   = new FormAttachment(100, 0);
		wDriverClass.setLayoutData(fdDriverClass);
		wDriverClass.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		
		// set the composite as the control for this page
		setControl(composite);
	}

	public boolean canFlipToNextPage()
	{
		String url = wURL.getText()!=null?wURL.getText().length()>0?wURL.getText():null:null;
		String driverClass   = wDriverClass.getText()!=null?wDriverClass.getText().length()>0?wDriverClass.getText():null:null;
		
		if (url==null || driverClass==null)
		{
			setErrorMessage(Messages.getString("CreateDatabaseWizardPageGeneric.ErrorMessage.URLAndDriverClassRequired")); //$NON-NLS-1$
			return false;
		}
		else
		{
			getDatabaseInfo();
			setErrorMessage(null);
			setMessage(Messages.getString("CreateDatabaseWizardPageGeneric.Message.Next")); //$NON-NLS-1$
			return true;
		}

	}	
	
	public DatabaseMeta getDatabaseInfo()
	{

		if (wURL.getText()!=null && wURL.getText().length()>0) 
		{
	        info.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL,     wURL.getText());
		}
		
		if (wDriverClass.getText()!=null && wDriverClass.getText().length()>0)
		{
			info.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, wDriverClass.getText());
		}

		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		return wiz.getPage("2"); //$NON-NLS-1$
	}
	
}
