 /**********************************************************************
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

/*
 * Created on 18-mei-2003
 *
 */

package be.ibridge.kettle.trans.step.tableinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.EnterNumberDialog;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.dialog.TransPreviewProgressDialog;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class TableInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlSQL;
	private Text         wSQL;
	private FormData     fdlSQL, fdSQL;

	private Label        wlDatefrom;
	private CCombo       wDatefrom;
	private FormData     fdlDatefrom, fdDatefrom;
    private Listener     lsDateform;

	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
    
    private Label        wlEachRow;
    private Button       wEachRow;
    private FormData     fdlEachRow, fdEachRow; 

    private Label        wlVariables;
    private Button       wVariables;
    private FormData     fdlVariables, fdVariables; 

	private Button wbTable;
	private FormData fdbTable;
	private Listener lsbTable;

	private TableInputMeta input;

	public TableInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(TableInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("TableInputDialog.TableInput")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

        // Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("TableInputDialog.StepName")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(Messages.getString("System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, null);

		// Limit input ...
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(Messages.getString("TableInputDialog.LimitSize")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.right= new FormAttachment(middle, -margin);
		fdlLimit.bottom = new FormAttachment(wOK, -2*margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.right= new FormAttachment(100, 0);
		fdLimit.bottom = new FormAttachment(wOK, -2*margin);
		wLimit.setLayoutData(fdLimit);

        // Execute for each row?
        wlEachRow = new Label(shell, SWT.RIGHT);
        wlEachRow.setText(Messages.getString("TableInputDialog.ExecuteForEachRow")); //$NON-NLS-1$
        props.setLook(wlEachRow);
        fdlEachRow = new FormData();
        fdlEachRow.left = new FormAttachment(0, 0);
        fdlEachRow.right = new FormAttachment(middle, -margin);
        fdlEachRow.bottom = new FormAttachment(wLimit, -margin);
        wlEachRow.setLayoutData(fdlEachRow);
        wEachRow = new Button(shell, SWT.CHECK);
        props.setLook(wEachRow);
        fdEachRow = new FormData();
        fdEachRow.left = new FormAttachment(middle, 0);
        fdEachRow.right = new FormAttachment(100, 0);
        fdEachRow.bottom = new FormAttachment(wLimit, -margin);
        wEachRow.setLayoutData(fdEachRow);


		// Read date from...
		wlDatefrom=new Label(shell, SWT.RIGHT);
		wlDatefrom.setText(Messages.getString("TableInputDialog.InsertDataFromStep")); //$NON-NLS-1$
 		props.setLook(wlDatefrom);
		fdlDatefrom=new FormData();
		fdlDatefrom.left = new FormAttachment(0, 0);
		fdlDatefrom.right= new FormAttachment(middle, -margin);
		fdlDatefrom.bottom = new FormAttachment(wEachRow, -margin);
		wlDatefrom.setLayoutData(fdlDatefrom);
		wDatefrom=new CCombo(shell, SWT.BORDER );
 		props.setLook(wDatefrom);

		for (int i=0;i<transMeta.findNrPrevSteps(stepname);i++)
		{
			StepMeta stepMeta = transMeta.findPrevStep(stepname, i);
			wDatefrom.add(stepMeta.getName());
		}
		
		wDatefrom.addModifyListener(lsMod);
		fdDatefrom=new FormData();
		fdDatefrom.left = new FormAttachment(middle, 0);
		fdDatefrom.right= new FormAttachment(100, 0);
		fdDatefrom.bottom = new FormAttachment(wEachRow, -margin);
		wDatefrom.setLayoutData(fdDatefrom);

        // Execute for each row?
        wlVariables = new Label(shell, SWT.RIGHT);
        wlVariables.setText(Messages.getString("TableInputDialog.ReplaceVariables")); //$NON-NLS-1$
        props.setLook(wlVariables);
        fdlVariables = new FormData();
        fdlVariables.left = new FormAttachment(0, 0);
        fdlVariables.right = new FormAttachment(middle, -margin);
        fdlVariables.bottom = new FormAttachment(wDatefrom, -margin);
        wlVariables.setLayoutData(fdlVariables);
        wVariables = new Button(shell, SWT.CHECK);
        props.setLook(wVariables);
        fdVariables = new FormData();
        fdVariables.left = new FormAttachment(middle, 0);
        fdVariables.right = new FormAttachment(100, 0);
        fdVariables.bottom = new FormAttachment(wDatefrom, -margin);
        wVariables.setLayoutData(fdVariables);
        wVariables.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { setSQLToolTip(); } });

		// Table line...
		wlSQL=new Label(shell, SWT.NONE);
		wlSQL.setText(Messages.getString("TableInputDialog.SQL")); //$NON-NLS-1$
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(wConnection, margin*2);
		wlSQL.setLayoutData(fdlSQL);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("TableInputDialog.GetSQLAndSelectStatement")); //$NON-NLS-1$
		fdbTable=new FormData();
		fdbTable.right = new FormAttachment(100, 0);
		fdbTable.top   = new FormAttachment(wConnection, margin*2);
		wbTable.setLayoutData(fdbTable);

		wSQL=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		fdSQL=new FormData();
		fdSQL.left  = new FormAttachment(0, 0);
		fdSQL.top   = new FormAttachment(wbTable, margin );
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom= new FormAttachment(wVariables, 0 );
		wSQL.setLayoutData(fdSQL);
		wSQL.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent arg0)
                {
                    setSQLToolTip();
                }
            }
        );

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();      } };
		lsbTable   = new Listener() { public void handleEvent(Event e) { getSQL();  } };
        lsDateform = new Listener() { public void handleEvent(Event e) { setFags(); } };
        
		wCancel.addListener  (SWT.Selection, lsCancel);
        wPreview.addListener (SWT.Selection, lsPreview);
		wOK.addListener      (SWT.Selection, lsOK    );
		wbTable.addListener  (SWT.Selection, lsbTable);
        wDatefrom.addListener(SWT.Selection, lsDateform);
        wDatefrom.addListener(SWT.FocusOut,  lsDateform);

		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setSQLToolTip()
    {
       if (wVariables.getSelection())
       {
           wSQL.setToolTipText(StringUtil.environmentSubstitute(wSQL.getText()));
       }
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getSQL() != null) wSQL.setText(input.getSQL());
		if (input.getDatabaseMeta() != null) wConnection.setText(input.getDatabaseMeta().getName());
		wLimit.setText(""+(int)input.getRowLimit()); //$NON-NLS-1$
		
        if (input.getLookupStepname() != null)
        {
            wDatefrom.setText(input.getLookupStepname());
            wEachRow.setSelection(input.isExecuteEachInputRow());
        }
        else
        {
            wEachRow.setEnabled(false);
            wlEachRow.setEnabled(false);
        }
        
        wVariables.setSelection(input.isVariableReplacementActive());
               
		wStepname.selectAll();
        setSQLToolTip();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
    private void getInfo(TableInputMeta meta)
    {
        meta.setSQL( wSQL.getText() );
        meta.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
        meta.setRowLimit( Const.toInt(wLimit.getText(), 0) );
        meta.setLookupFromStep( transMeta.findStep( wDatefrom.getText() ) );
        meta.setExecuteEachInputRow(wEachRow.getSelection());
        meta.setVariableReplacementActive(wVariables.getSelection());
    }
    
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		// copy info to TextFileInputMeta class (input)
        
        getInfo(input);
        
		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("TableInputDialog.SelectValidConnection")); //$NON-NLS-1$
			mb.setText(Messages.getString("TableInputDialog.DialogCaptionError")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}
	
	private void getSQL()
	{
		DatabaseMeta inf = transMeta.findDatabase(wConnection.getText());
		if (inf!=null)
		{	
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
            std.setSplitSchemaAndTable(true);
			if (std.open()!= null)
			{
				String sql = "SELECT *"+Const.CR+"FROM "+inf.getQuotedSchemaTableCombination(std.getSchemaName(), std.getTableName())+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
				wSQL.setText(sql);

				MessageBox yn = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
				yn.setMessage(Messages.getString("TableInputDialog.IncludeFieldNamesInSQL")); //$NON-NLS-1$
				yn.setText(Messages.getString("TableInputDialog.DialogCaptionQuestion")); //$NON-NLS-1$
				int id = yn.open();
				switch(id)
				{
				case SWT.CANCEL: break;
				case SWT.NO:     wSQL.setText(sql); break;
				case SWT.YES:
					Database db = new Database(inf);
					try
					{
						db.connect();
						Row fields = db.getQueryFields(sql, false);
						if (fields!=null)
						{
							sql = "SELECT"+Const.CR; //$NON-NLS-1$
							for (int i=0;i<fields.size();i++)
							{
								Value field=fields.getValue(i);
								if (i==0) sql+="  "; else sql+=", "; //$NON-NLS-1$ //$NON-NLS-2$
								sql+=inf.quoteField(field.getName())+Const.CR;
							}
							sql+="FROM "+inf.getQuotedSchemaTableCombination(std.getSchemaName(), std.getTableName())+Const.CR; //$NON-NLS-1$
							wSQL.setText(sql);
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
							mb.setMessage(Messages.getString("TableInputDialog.ERROR_CouldNotRetrieveFields")+Const.CR+Messages.getString("TableInputDialog.PerhapsNoPermissions")); //$NON-NLS-1$ //$NON-NLS-2$
							mb.setText(Messages.getString("TableInputDialog.DialogCaptionError2")); //$NON-NLS-1$
							mb.open();
						}
					}
					catch(KettleException e)
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setText(Messages.getString("TableInputDialog.DialogCaptionError3")); //$NON-NLS-1$
						mb.setMessage(Messages.getString("TableInputDialog.AnErrorOccurred")+Const.CR+e.getMessage()); //$NON-NLS-1$
						mb.open(); 
					}
					finally
					{
						db.disconnect();
					}
					break;
				}
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("TableInputDialog.ConnectionNoLongerAvailable")); //$NON-NLS-1$
			mb.setText(Messages.getString("TableInputDialog.DialogCaptionError4")); //$NON-NLS-1$
			mb.open();
		}
					
	}
	
    private void setFags()
    {
        if (wDatefrom.getText() != null && wDatefrom.getText().length() > 0)
        {
            // The foreach check box... 
            wEachRow.setEnabled(true);
            wlEachRow.setEnabled(true);
            
            // The preview button...
            wPreview.setEnabled(false);
        }
        else
        {
            // The foreach check box... 
            wEachRow.setEnabled(false);
            wEachRow.setSelection(false);
            wlEachRow.setEnabled(false);
            
            // The preview button...
            wPreview.setEnabled(true);
        }
        
    }

    /**
     * Preview the data generated by this step.
     * This generates a transformation using this step & a dummy and previews it.
     *
     */
    private void preview()
    {
        // Create the table input reader step...
        TableInputMeta oneMeta = new TableInputMeta();
        getInfo(oneMeta);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 500, Messages.getString("TableInputDialog.EnterPreviewSize"), Messages.getString("TableInputDialog.NumberOfRowsToPreview")); //$NON-NLS-1$ //$NON-NLS-2$
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled())
            {
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  
                			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            PreviewRowsDialog prd =new PreviewRowsDialog(shell, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
            prd.open();
        }
    }
}
