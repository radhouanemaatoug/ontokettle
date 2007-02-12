 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/


package be.ibridge.kettle.job.entry.waitforfile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.i18n.GlobalMessages;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the Wait For File job entry settings.
 *
 * @author Sven Boden
 * @since  28-01-2007
 */
public class JobEntryWaitForFileDialog extends Dialog implements JobEntryDialogInterface
{
   private static final String[] FILETYPES = new String[] {
           Messages.getString("JobWaitForFile.Filetype.All") };
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;

    private Label        wlMaximumTimeout;
    private TextVar      wMaximumTimeout;
    private FormData     fdlMaximumTimeout, fdMaximumTimeout;

    private Label        wlCheckCycleTime;
    private TextVar      wCheckCycleTime;
    private FormData     fdlCheckCycleTime, fdCheckCycleTime;    
    
    private Label        wlSuccesOnTimeout;
    private Button       wSuccesOnTimeout;
    private FormData     fdlSuccesOnTimeout, fdSuccesOnTimeout;

    private Label        wlFileSizeCheck;
    private Button       wFileSizeCheck;
    private FormData     fdlFileSizeCheck, fdFileSizeCheck;   
    
	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;

	private JobEntryWaitForFile jobEntry;
	private Shell       	shell;
	private Props       	props;

	private SelectionAdapter lsDef;

	private boolean changed;

	public JobEntryWaitForFileDialog(Shell parent, JobEntryWaitForFile jobEntry, JobMeta jobMeta)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		this.jobEntry=jobEntry;

		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(Messages.getString("JobWaitForFile.Name.Default"));
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobWaitForFile.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobWaitForFile.Name.Label"));
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Filename line
		wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("JobWaitForFile.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wName, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(GlobalMessages.getSystemString("System.Button.Browse"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wName, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wName, margin);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename.setToolTipText(StringUtil.environmentSubstitute( wFilename.getText() ) );
				}
			}
		);

		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(StringUtil.environmentSubstitute(wFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

        // Maximum timeout
        wlMaximumTimeout = new Label(shell, SWT.RIGHT);
        wlMaximumTimeout.setText(Messages.getString("JobWaitForFile.MaximumTimeout.Label"));
        props.setLook(wlMaximumTimeout);
        fdlMaximumTimeout = new FormData();
        fdlMaximumTimeout.left = new FormAttachment(0, 0);
        fdlMaximumTimeout.top = new FormAttachment(wFilename, margin);
        fdlMaximumTimeout.right = new FormAttachment(middle, -margin);
        wlMaximumTimeout.setLayoutData(fdlMaximumTimeout);
        wMaximumTimeout = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wMaximumTimeout);
        wMaximumTimeout.setToolTipText(Messages.getString("JobWaitForFile.MaximumTimeout.Tooltip"));
        wMaximumTimeout.addModifyListener(lsMod);
        fdMaximumTimeout = new FormData();
        fdMaximumTimeout.left = new FormAttachment(middle, 0);
        fdMaximumTimeout.top = new FormAttachment(wFilename, margin);
        fdMaximumTimeout.right = new FormAttachment(100, 0);
        wMaximumTimeout.setLayoutData(fdMaximumTimeout);

        // Cycle time
        wlCheckCycleTime = new Label(shell, SWT.RIGHT);
        wlCheckCycleTime.setText(Messages.getString("JobWaitForFile.CheckCycleTime.Label"));
        props.setLook(wlCheckCycleTime);
        fdlCheckCycleTime = new FormData();
        fdlCheckCycleTime.left = new FormAttachment(0, 0);
        fdlCheckCycleTime.top = new FormAttachment(wMaximumTimeout, margin);
        fdlCheckCycleTime.right = new FormAttachment(middle, -margin);
        wlCheckCycleTime.setLayoutData(fdlCheckCycleTime);
        wCheckCycleTime = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCheckCycleTime);
        wCheckCycleTime.setToolTipText(Messages.getString("JobWaitForFile.CheckCycleTime.Tooltip"));
        wCheckCycleTime.addModifyListener(lsMod);
        fdCheckCycleTime = new FormData();
        fdCheckCycleTime.left = new FormAttachment(middle, 0);
        fdCheckCycleTime.top = new FormAttachment(wMaximumTimeout, margin);
        fdCheckCycleTime.right = new FormAttachment(100, 0);
        wCheckCycleTime.setLayoutData(fdCheckCycleTime);
	        
        // Success on timeout		
        wlSuccesOnTimeout = new Label(shell, SWT.RIGHT);
        wlSuccesOnTimeout.setText(Messages.getString("JobWaitForFile.SuccessOnTimeout.Label"));
        props.setLook(wlSuccesOnTimeout);
        fdlSuccesOnTimeout = new FormData();
        fdlSuccesOnTimeout.left = new FormAttachment(0, 0);
        fdlSuccesOnTimeout.top = new FormAttachment(wCheckCycleTime, margin);
        fdlSuccesOnTimeout.right = new FormAttachment(middle, -margin);
        wlSuccesOnTimeout.setLayoutData(fdlSuccesOnTimeout);
        wSuccesOnTimeout = new Button(shell, SWT.CHECK);
        props.setLook(wSuccesOnTimeout);
        wSuccesOnTimeout.setToolTipText(Messages.getString("JobWaitForFile.SuccessOnTimeout.Tooltip"));
        fdSuccesOnTimeout = new FormData();
        fdSuccesOnTimeout.left = new FormAttachment(middle, 0);
        fdSuccesOnTimeout.top = new FormAttachment(wCheckCycleTime, margin);
        fdSuccesOnTimeout.right = new FormAttachment(100, 0);
        wSuccesOnTimeout.setLayoutData(fdSuccesOnTimeout);
        wSuccesOnTimeout.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

        // Success on timeout		
        wlFileSizeCheck = new Label(shell, SWT.RIGHT);
        wlFileSizeCheck.setText(Messages.getString("JobWaitForFile.FileSizeCheck.Label"));
        props.setLook(wlFileSizeCheck);
        fdlFileSizeCheck = new FormData();
        fdlFileSizeCheck.left = new FormAttachment(0, 0);
        fdlFileSizeCheck.top = new FormAttachment(wSuccesOnTimeout, margin);
        fdlFileSizeCheck.right = new FormAttachment(middle, -margin);
        wlFileSizeCheck.setLayoutData(fdlFileSizeCheck);
        wFileSizeCheck = new Button(shell, SWT.CHECK);
        props.setLook(wFileSizeCheck);
        wFileSizeCheck.setToolTipText(Messages.getString("JobWaitForFile.FileSizeCheck.Tooltip"));
        fdFileSizeCheck = new FormData();
        fdFileSizeCheck.left = new FormAttachment(middle, 0);
        fdFileSizeCheck.top = new FormAttachment(wSuccesOnTimeout, margin);
        fdFileSizeCheck.right = new FormAttachment(100, 0);
        wFileSizeCheck.setLayoutData(fdFileSizeCheck);
        wFileSizeCheck.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });        
        
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(GlobalMessages.getSystemString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(GlobalMessages.getSystemString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFileSizeCheck);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wFilename.addSelectionListener( lsDef );
		wMaximumTimeout.addSelectionListener(lsDef);
		wCheckCycleTime.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName() != null) 
			wName.setText( jobEntry.getName() );
		wName.selectAll();

		wFilename.setText(Const.NVL(jobEntry.getFilename(), ""));
		wMaximumTimeout.setText(Const.NVL(jobEntry.getMaximumTimeout(), ""));
		wCheckCycleTime.setText(Const.NVL(jobEntry.getCheckCycleTime(), ""));
		wSuccesOnTimeout.setSelection(jobEntry.isSuccessOnTimeout());		
		wFileSizeCheck.setSelection(jobEntry.isFileSizeCheck());
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setFilename(wFilename.getText());
		jobEntry.setMaximumTimeout(wMaximumTimeout.getText());
		jobEntry.setCheckCycleTime(wCheckCycleTime.getText());
		jobEntry.setSuccessOnTimeout(wSuccesOnTimeout.getSelection());		
		jobEntry.setFileSizeCheck(wFileSizeCheck.getSelection());
		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}
}