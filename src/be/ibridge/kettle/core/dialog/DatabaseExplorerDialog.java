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

 


package be.ibridge.kettle.core.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Catalog;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.DatabaseMetaInformation;
import be.ibridge.kettle.core.database.Schema;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.test.EditDatabaseTable;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog represents an explorer type of interface on a given database connection.
 * It shows the tables defined in the visible schemas or catalogs on that connection.
 * The interface also allows you to get all kinds of information on those tables.
 * 
 * @author Matt
 * @since 18-05-2003
 *
 */
public class DatabaseExplorerDialog extends Dialog 
{
	private LogWriter log;
	private Props props;
	private DatabaseMeta dbMeta;
	private DBCache dbcache;
	
	private static final String STRING_CATALOG  = Messages.getString("DatabaseExplorerDialog.Catalogs.Label");
	private static final String STRING_SCHEMAS  = Messages.getString("DatabaseExplorerDialog.Schemas.Label");
	private static final String STRING_TABLES   = Messages.getString("DatabaseExplorerDialog.Tables.Label");
	private static final String STRING_VIEWS    = Messages.getString("DatabaseExplorerDialog.Views.Label");
	private static final String STRING_SYNONYMS = Messages.getString("DatabaseExplorerDialog.Synonyms.Label");
	
	private Shell     shell;
	private Tree      wTree;
	private TreeItem  tiTree;
	 
	private Button    wOK;
	private Button    wRefresh;
	private Button    wCancel;
	
	/** This is the return value*/
	private String    tableName; 
	
	private boolean justLook;
    private String  selectedSchema;
	private String  selectedTable;
	private List    databases;
    private boolean splitSchemaAndTable;
    private String schemaName;
    private Composite buttonsComposite;
    private Button bPrev;
    private Button bPrevN;
    private Button bCount;
    private Button bShow;
    private Button bDDL;
    private Button bDDL2;
    private Button bSQL;
    private String activeSchemaTable;
    private Button bTruncate;

    /** @deprecated */
    public DatabaseExplorerDialog(Shell par, Props pr, int style, DatabaseMeta conn, ArrayList databases)
    {
        this(par, style, conn, (List)databases);
    }

    /** @deprecated */
    public DatabaseExplorerDialog(Shell par, Props pr, int style, DatabaseMeta conn, List databases)
    {
        this(par, style, conn, databases);
    }

    /** @deprecated */
    public DatabaseExplorerDialog(Shell par, Props pr, int style, DatabaseMeta conn, ArrayList databases, boolean look)
    {
        this(par, style, conn, databases, look);
    }
    
    /** @deprecated */
    public DatabaseExplorerDialog(Shell par, Props pr, int style, DatabaseMeta conn, List databases, boolean look)
    {
        this(par, style, conn, databases, look);
    }
    
	public DatabaseExplorerDialog(Shell par, int style, DatabaseMeta conn, List databases)
	{
		this(par, style, conn, databases, false, false);
	}

    public DatabaseExplorerDialog(Shell par, int style, DatabaseMeta conn, List databases, boolean look)
    {
        this(par, style, conn, databases, look, false);
    }
    
    public DatabaseExplorerDialog(Shell par, int style, DatabaseMeta conn, List databases, boolean look, boolean splitSchemaAndTable)
    {
        super(par, style);
        this.dbMeta=conn;
        this.databases = databases;
        this.justLook=look;
        this.splitSchemaAndTable = splitSchemaAndTable;
                
        selectedSchema=null;
        selectedTable=null;
    
        props=Props.getInstance();
        log=LogWriter.getInstance();
        dbcache = DBCache.getInstance();
        
    }

	public void setSelectedTable(String selectedTable)
	{
		this.selectedTable = selectedTable;
	}
	
	public Object open() 
	{
		tableName = null;

		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		shell.setText(Messages.getString("DatabaseExplorerDialog.Title", dbMeta.toString()));
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setLayout (formLayout);
 		
        addButtons();
        refreshButtons(null);
        
 		// Tree
 		wTree = new Tree(shell, SWT.SINGLE | SWT.BORDER /*| (multiple?SWT.CHECK:SWT.NONE)*/);
 		props.setLook( 		wTree);
 				
		if (!getData()) return null;
 		
 		// Buttons
		wOK = new Button(shell, SWT.PUSH); 
		wOK.setText(Messages.getString("System.Button.OK"));

		wRefresh = new Button(shell, SWT.PUSH); 
		wRefresh.setText(Messages.getString("System.Button.Refresh"));
		
		if (!justLook) 
		{
			wCancel = new Button(shell, SWT.PUSH);
			wCancel.setText(Messages.getString("System.Button.Cancel"));
		}
		
		FormData fdTree      = new FormData(); 

		int margin =  10;

		fdTree.left   = new FormAttachment(0, 0); // To the right of the label
		fdTree.top    = new FormAttachment(0, 0);
		fdTree.right  = new FormAttachment(buttonsComposite, -margin);
		fdTree.bottom = new FormAttachment(100, -50);
		wTree.setLayoutData(fdTree);

		if (!justLook) 
		{
			BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wRefresh, wCancel}, margin, null);

			// Add listeners
			wCancel.addListener(SWT.Selection, new Listener ()
				{
					public void handleEvent (Event e) 
					{
						log.logDebug("SelectTableDialog", "CANCEL SelectTableDialog");
						dbMeta=null;
						dispose();
					}
				}
			);
		}
		else
		{
			BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wRefresh }, margin, null);		    
		}

		// Add listeners
		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					handleOK();
				}
			}
		);
		wRefresh.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					getData();
				}
			}
		);
		SelectionAdapter selAdapter=new SelectionAdapter()
			{
                public void widgetSelected(SelectionEvent e)
                {
                    refreshButtons(getSchemaTable());
                }
                
				public void widgetDefaultSelected(SelectionEvent e)
				{
					openSchema(e);	
				}
			};
		wTree.addSelectionListener(selAdapter);
		
		wTree.addMouseListener(new MouseAdapter()
			{
				public void mouseDown(MouseEvent e)
				{
					if (e.button == 3) // right click!
					{
						setTreeMenu();
					}
				}
			}
		);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { dispose(); } } );

		BaseStepDialog.setSize(shell, 320, 480, true);

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return tableName;
	}
    
    private void addButtons()
    {
        buttonsComposite = new Composite(shell, SWT.NONE);
        props.setLook(buttonsComposite);
        buttonsComposite.setLayout(new FormLayout());

        activeSchemaTable=null;
        
        bPrev  = new Button(buttonsComposite, SWT.PUSH); 
        bPrev.setText(Messages.getString("DatabaseExplorerDialog.Menu.Preview100", Const.NVL(activeSchemaTable, "?")));
        bPrev.setEnabled(activeSchemaTable!=null);
        bPrev.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { previewTable(activeSchemaTable, false); }});
        FormData prevData = new FormData();
        prevData.left = new FormAttachment(0, 0);
        prevData.right = new FormAttachment(100, 0);
        prevData.top = new FormAttachment(0, 0);
        bPrev.setLayoutData(prevData);
        
        bPrevN  = new Button(buttonsComposite, SWT.PUSH); 
        bPrevN.setText(Messages.getString("DatabaseExplorerDialog.Menu.PreviewN", Const.NVL(activeSchemaTable, "?")));
        bPrevN.setEnabled(activeSchemaTable!=null);
        bPrevN.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { previewTable(activeSchemaTable, true); }});
        FormData prevNData = new FormData();
        prevNData.left = new FormAttachment(0, 0);
        prevNData.right = new FormAttachment(100, 0);
        prevNData.top = new FormAttachment(bPrev, Const.MARGIN);
        bPrevN.setLayoutData(prevNData);
        
        bCount = new Button(buttonsComposite, SWT.PUSH); 
        bCount.setText(Messages.getString("DatabaseExplorerDialog.Menu.ShowSize", Const.NVL(activeSchemaTable, "?")));
        bCount.setEnabled(activeSchemaTable!=null);
        bCount.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showCount(activeSchemaTable); }});
        FormData countData = new FormData();
        countData.left = new FormAttachment(0, 0);
        countData.right = new FormAttachment(100, 0);
        countData.top = new FormAttachment(bPrevN, Const.MARGIN);
        bCount.setLayoutData(countData);

        bShow  = new Button(buttonsComposite, SWT.PUSH); 
        bShow.setText(Messages.getString("DatabaseExplorerDialog.Menu.ShowLayout", Const.NVL(activeSchemaTable, "?")));
        bShow.setEnabled(activeSchemaTable!=null);
        bShow.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showTable(activeSchemaTable); }});
        FormData showData = new FormData();
        showData.left = new FormAttachment(0, 0);
        showData.right = new FormAttachment(100, 0);
        showData.top = new FormAttachment(bCount, Const.MARGIN*7);
        bShow.setLayoutData(showData);
        
        bDDL  = new Button(buttonsComposite, SWT.PUSH); 
        bDDL.setText(Messages.getString("DatabaseExplorerDialog.Menu.GenDDL"));
        bDDL.setEnabled(activeSchemaTable!=null);
        bDDL.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getDDL(activeSchemaTable); }});
        FormData ddlData = new FormData();
        ddlData.left = new FormAttachment(0, 0);
        ddlData.right = new FormAttachment(100, 0);
        ddlData.top = new FormAttachment(bShow, Const.MARGIN);
        bDDL.setLayoutData(ddlData);
        
        bDDL2  = new Button(buttonsComposite, SWT.PUSH); 
        bDDL2.setText(Messages.getString("DatabaseExplorerDialog.Menu.GenDDLOtherConn"));
        bDDL2.setEnabled(activeSchemaTable!=null);
        bDDL2.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getDDLForOther(activeSchemaTable); }});
        bDDL2.setEnabled(databases!=null);
        FormData ddl2Data = new FormData();
        ddl2Data.left = new FormAttachment(0, 0);
        ddl2Data.right = new FormAttachment(100, 0);
        ddl2Data.top = new FormAttachment(bDDL, Const.MARGIN);
        bDDL2.setLayoutData(ddl2Data);

        bSQL  = new Button(buttonsComposite, SWT.PUSH); 
        bSQL.setText(Messages.getString("DatabaseExplorerDialog.Menu.OpenSQL", Const.NVL(activeSchemaTable, "?")));
        bSQL.setEnabled(activeSchemaTable!=null);
        bSQL.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getSQL(activeSchemaTable); }});
        FormData sqlData = new FormData();
        sqlData.left = new FormAttachment(0, 0);
        sqlData.right = new FormAttachment(100, 0);
        sqlData.top = new FormAttachment(bDDL2, Const.MARGIN);
        bSQL.setLayoutData(sqlData);

        bTruncate  = new Button(buttonsComposite, SWT.PUSH); 
        bTruncate.setText(Messages.getString("DatabaseExplorerDialog.Menu.Truncate", Const.NVL(activeSchemaTable, "?")));
        bTruncate.setEnabled(activeSchemaTable!=null);
        bTruncate.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTruncate(activeSchemaTable); }});
        FormData truncateData = new FormData();
        truncateData.left = new FormAttachment(0, 0);
        truncateData.right = new FormAttachment(100, 0);
        truncateData.top = new FormAttachment(bSQL, Const.MARGIN*7);
        bTruncate.setLayoutData(truncateData);

        FormData fdComposite = new FormData();
        fdComposite.right = new FormAttachment(100,0);
        fdComposite.top   = new FormAttachment(0, 20);
        buttonsComposite.setLayoutData(fdComposite);        
    }


    private void refreshButtons(String table)
    {
        activeSchemaTable=table;
        bPrev.setText(Messages.getString("DatabaseExplorerDialog.Menu.Preview100", Const.NVL(table, "?")));
        bPrev.setEnabled(table!=null);
        
        bPrevN.setText(Messages.getString("DatabaseExplorerDialog.Menu.PreviewN", Const.NVL(table, "?")));
        bPrevN.setEnabled(table!=null);
        
        bCount.setText(Messages.getString("DatabaseExplorerDialog.Menu.ShowSize", Const.NVL(table, "?")));
        bCount.setEnabled(table!=null);

        bShow.setText(Messages.getString("DatabaseExplorerDialog.Menu.ShowLayout", Const.NVL(table, "?")));
        bShow.setEnabled(table!=null);
        
        bDDL.setText(Messages.getString("DatabaseExplorerDialog.Menu.GenDDL"));
        bDDL.setEnabled(table!=null);
        
        bDDL2.setText(Messages.getString("DatabaseExplorerDialog.Menu.GenDDLOtherConn"));
        bDDL2.setEnabled(table!=null);

        bSQL.setText(Messages.getString("DatabaseExplorerDialog.Menu.OpenSQL", Const.NVL(table, "?")));
        bSQL.setEnabled(table!=null);
        
        bTruncate.setText(Messages.getString("DatabaseExplorerDialog.Menu.Truncate", Const.NVL(table, "?")));
        bTruncate.setEnabled(table!=null);

        shell.layout(true, true);
    }
	
	private boolean getData()
	{
		GetDatabaseInfoProgressDialog gdipd = new GetDatabaseInfoProgressDialog(shell, dbMeta);
		DatabaseMetaInformation dmi = gdipd.open();
		if (dmi!=null)
		{
			// Clear the tree top entry
			if (tiTree!=null && !tiTree.isDisposed()) tiTree.dispose();
				
			// New entry in the tree
			tiTree = new TreeItem(wTree, SWT.NONE); 
			tiTree.setText(dbMeta==null?"":dbMeta.getName());

			// Show the catalogs...
			Catalog[] catalogs = dmi.getCatalogs();
			if (catalogs!=null)
			{
				TreeItem tiCat = new TreeItem(tiTree, SWT.NONE); 
				tiCat.setText(STRING_CATALOG);
				
				for (int i=0;i<catalogs.length;i++)
				{
					TreeItem newCat = new TreeItem(tiCat, SWT.NONE);
					newCat.setText(catalogs[i].getCatalogName());
					
					for (int j=0;j<catalogs[i].getItems().length;j++)
					{
						String tableName = catalogs[i].getItems()[j];
	
						TreeItem ti = new TreeItem(newCat, SWT.NONE);
						ti.setText(tableName);
					}
				}
			}
				
			// The schema's
			Schema[] schemas= dmi.getSchemas();
			if (schemas!=null)
			{
				TreeItem tiSch = new TreeItem(tiTree, SWT.NONE); 
				tiSch.setText(STRING_SCHEMAS);
	
				for (int i=0;i<schemas.length;i++)
				{
					TreeItem newSch = new TreeItem(tiSch, SWT.NONE);
					newSch.setText(schemas[i].getSchemaName());
					
					for (int j=0;j<schemas[i].getItems().length;j++)
					{
						String tableName = schemas[i].getItems()[j];
	
						TreeItem ti = new TreeItem(newSch, SWT.NONE);
						ti.setText(tableName);
					}
				}
			}

			// The tables in general...
			TreeItem tiTab = null;
			String tabnames[] = dmi.getTables();
			if (tabnames!=null)
			{
				tiTab = new TreeItem(tiTree, SWT.NONE); 
				tiTab.setText(STRING_TABLES);
				tiTab.setExpanded(true);
				
				for (int i = 0; i < tabnames.length; i++)
				{
					TreeItem newTab = new TreeItem(tiTab, SWT.NONE);
					newTab.setText(tabnames[i]);
				}
			}
			
			// The views...
			TreeItem tiView = null;
			String views[] = dmi.getViews();
			if (views!=null)
			{
				tiView = new TreeItem(tiTree, SWT.NONE); 
				tiView.setText(STRING_VIEWS);
				for (int i = 0; i < views.length; i++)
				{
					TreeItem newView = new TreeItem(tiView, SWT.NONE);
					newView.setText(views[i]);
				}
			}
				

			// The synonyms
			TreeItem tiSyn = null;
			String[] syn = dmi.getSynonyms();
			if (syn!=null)
			{
				tiSyn = new TreeItem(tiTree, SWT.NONE); 
				tiSyn.setText(STRING_SYNONYMS);
				for (int i = 0; i < syn.length; i++)
				{
					TreeItem newSyn = new TreeItem(tiSyn, SWT.NONE);
					newSyn.setText(syn[i]);
				}
			}
				
			// Make sure the selected table is shown...
			// System.out.println("Selecting table "+k);
			if (!Const.isEmpty(selectedTable))
			{
				TreeItem ti = null;
                if (ti==null && tiTab!=null) ti = Const.findTreeItem(tiTab, selectedSchema, selectedTable);
				if (ti==null && tiView!=null) ti = Const.findTreeItem(tiView, selectedSchema, selectedTable);
				if (ti==null && tiTree!=null) ti = Const.findTreeItem(tiTree, selectedSchema, selectedTable);
				if (ti==null && tiSyn!=null) ti = Const.findTreeItem(tiSyn,  selectedSchema, selectedTable);
				
				if (ti!=null)
				{
					// System.out.println("Selection set on "+ti.getText());
					wTree.setSelection(new TreeItem[] { ti });
					wTree.showSelection();
                    refreshButtons(dbMeta.getQuotedSchemaTableCombination(selectedSchema, selectedTable));
				}
				
				selectedTable=null;
                
                
			}
			
			tiTree.setExpanded(true);
		}
		else
		{
			return false;
		}
		
		return true;
	}
    
    private String getSchemaTable()
    {
        TreeItem ti[]=wTree.getSelection();
        if (ti.length==1)
        {
            // Get the parent.
            TreeItem parent = ti[0].getParentItem();
            if (parent!=null)
            {
                String schemaName = parent.getText();
                String tableName  = ti[0].getText();

                if (ti[0].getItemCount()==0) // No children, only the tables themselves...
                {
                    String tab = null;
                    if (schemaName.equalsIgnoreCase(STRING_TABLES) ||
                        schemaName.equalsIgnoreCase(STRING_VIEWS) ||
                        schemaName.equalsIgnoreCase(STRING_SYNONYMS) ||
                        ( schemaName!=null && schemaName.length()==0 )
                        )
                    {
                        tab = tableName;
                    }
                    else
                    {
                        tab = dbMeta.getQuotedSchemaTableCombination(schemaName, tableName);
                    }
                    return tab;
                }
            }
        }
        return null;
    }

	public void setTreeMenu()
	{
        final String table = getSchemaTable();
        if (table!=null)
        {
            Menu mTree = null;
		
            if (mTree!=null && !mTree.isDisposed())
            {
                mTree.dispose();
            }
            mTree = new Menu(shell, SWT.POP_UP);
			MenuItem miPrev  = new MenuItem(mTree, SWT.PUSH); miPrev.setText(Messages.getString("DatabaseExplorerDialog.Menu.Preview100", table));
			miPrev.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { previewTable(table, false); }});
			MenuItem miPrevN  = new MenuItem(mTree, SWT.PUSH); miPrevN.setText(Messages.getString("DatabaseExplorerDialog.Menu.PreviewN", table));
			miPrevN.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { previewTable(table, true); }});
			//MenuItem miEdit   = new MenuItem(mTree, SWT.PUSH); miEdit.setText("Open ["+table+"] for editing");
			//miEdit.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editTable(table); }});
			MenuItem miCount = new MenuItem(mTree, SWT.PUSH); miCount.setText(Messages.getString("DatabaseExplorerDialog.Menu.ShowSize", table));
			miCount.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showCount(table); }});

			new MenuItem(mTree, SWT.SEPARATOR);
			
			MenuItem miShow  = new MenuItem(mTree, SWT.PUSH); miShow.setText(Messages.getString("DatabaseExplorerDialog.Menu.ShowLayout", table));
			miShow.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showTable(table); }});
			MenuItem miDDL  = new MenuItem(mTree, SWT.PUSH); miDDL.setText(Messages.getString("DatabaseExplorerDialog.Menu.GenDDL"));
			miDDL.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getDDL(table); }});
            MenuItem miDDL2  = new MenuItem(mTree, SWT.PUSH); miDDL2.setText(Messages.getString("DatabaseExplorerDialog.Menu.GenDDLOtherConn"));
			miDDL2.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getDDLForOther(table); }});
            miDDL2.setEnabled(databases!=null);
			MenuItem miSQL  = new MenuItem(mTree, SWT.PUSH); miSQL.setText(Messages.getString("DatabaseExplorerDialog.Menu.OpenSQL", table));
			miSQL.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getSQL(table); }});
            
            wTree.setMenu(mTree);
		}
        else
        {
            wTree.setMenu(null);
        }
	}

	public void previewTable(String tableName, boolean asklimit)
	{
		int limit = 100;
		if (asklimit)
		{
			// Ask how many lines we should preview.
			String shellText = Messages.getString("DatabaseExplorerDialog.PreviewTable.Title");
			String lineText = Messages.getString("DatabaseExplorerDialog.PreviewTable.Message");
			EnterNumberDialog end = new EnterNumberDialog(shell, limit, shellText, lineText);
			int samples = end.open();
			if (samples>=0) limit=samples;
		}

	    GetPreviewTableProgressDialog pd = new GetPreviewTableProgressDialog(shell, dbMeta, tableName, limit);
	    List rows = pd.open();
	    if (rows!=null) // otherwise an already shown error...
	    {
			if (rows.size()>0)
			{
				PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, tableName, rows);
				prd.open();
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				mb.setMessage(Messages.getString("DatabaseExplorerDialog.NoRows.Message"));
				mb.setText(Messages.getString("DatabaseExplorerDialog.NoRows.Title"));
				mb.open();
			}
	    }
	}

	public void editTable(String tableName)
	{
		EditDatabaseTable edt = new EditDatabaseTable(shell, SWT.NONE, props, dbMeta, tableName, 20);
		edt.open();
	}

	public void showTable(String tableName)
	{
	    String sql = dbMeta.getSQLQueryFields(tableName);
	    GetQueryFieldsProgressDialog pd = new GetQueryFieldsProgressDialog(shell, dbMeta, sql);
	    Row result = pd.open();         
		if (result!=null)
		{
			StepFieldsDialog sfd = new StepFieldsDialog(shell, SWT.NONE, tableName, result);
			sfd.open();
		}
	}

	public void showCount(String tableName)
	{
	    GetTableSizeProgressDialog pd = new GetTableSizeProgressDialog(shell, dbMeta, tableName);
		Row r = pd.open();
		if (r!=null)
		{
            MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
            mb.setMessage(Messages.getString("DatabaseExplorerDialog.TableSize.Message", tableName, Long.toString(r.getValue(0).getInteger())));
            mb.setText(Messages.getString("DatabaseExplorerDialog.TableSize.Title"));
            mb.open();
		}
	}

	public void getDDL(String tableName)
	{
		Database db = new Database(dbMeta);
		try
		{
			db.connect();
			Row r = db.getTableFields(tableName);
			String sql = db.getCreateTableStatement(tableName, r, null, false, null, true);
			SQLEditor se = new SQLEditor(shell, SWT.NONE, dbMeta, dbcache, sql);
			se.open();
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, Messages.getString("Dialog.Error.Header"),
                Messages.getString("DatabaseExplorerDialog.Error.RetrieveLayout"), dbe);
		}
		finally
		{
			db.disconnect();
		}
	}
	
	public void getDDLForOther(String tableName)
	{
        if (databases!=null)
        {
    		Database db = new Database(dbMeta);
    		try
    		{
    			db.connect();
    			
    			Row r = db.getTableFields(tableName);
    
    			// Now select the other connection...
                
                // Only take non-SAP R/3 connections....
                List dbs = new ArrayList();
                for (int i=0;i<databases.size();i++) 
                    if (((DatabaseMeta)databases.get(i)).getDatabaseType()!=DatabaseMeta.TYPE_DATABASE_SAPR3) dbs.add(databases.get(i));
                
                String conn[] = new String[dbs.size()];
    			for (int i=0;i<conn.length;i++) conn[i] = ((DatabaseMeta)dbs.get(i)).getName();
    			
    			EnterSelectionDialog esd = new EnterSelectionDialog(shell, conn, Messages.getString("DatabaseExplorerDialog.TargetDatabase.Title"),
                    Messages.getString("DatabaseExplorerDialog.TargetDatabase.Message"));
    			String target = esd.open();
    			if (target!=null)
    			{
    				DatabaseMeta targetdbi = DatabaseMeta.findDatabase(dbs, target);
    				Database targetdb = new Database(targetdbi);
    
    				String sql = targetdb.getCreateTableStatement(tableName, r, null, false, null, true);
    				SQLEditor se = new SQLEditor(shell, SWT.NONE, dbMeta, dbcache, sql);
    				se.open();
    			}
    		}
    		catch(KettleDatabaseException dbe)
    		{
    			new ErrorDialog(shell, Messages.getString("Dialog.Error.Header"),
                    Messages.getString("DatabaseExplorerDialog.Error.GenDDL"), dbe);
    		}
    		finally
    		{
    			db.disconnect();
    		}
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.NONE | SWT.ICON_INFORMATION);
            mb.setMessage(Messages.getString("DatabaseExplorerDialog.NoConnectionsKnown.Message"));
            mb.setText(Messages.getString("DatabaseExplorerDialog.NoConnectionsKnown.Title"));
            mb.open();
        }
	}

	
	public void getSQL(String tableName)
	{
		SQLEditor sql = new SQLEditor(shell, SWT.NONE, dbMeta, dbcache, "SELECT * FROM "+tableName);
		sql.open();
	}
    
    
    public void getTruncate(String activeSchemaTable)
    {
        SQLEditor sql = new SQLEditor(shell, SWT.NONE, dbMeta, dbcache, "-- TRUNCATE TABLE "+activeSchemaTable);
        sql.open();
    }

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void handleOK()
	{
		if (justLook) 
		{
			dispose();
			return;
		} 
		TreeItem ti[]=wTree.getSelection();
		if (ti.length==1)
		{
			// Get the parent.
            String table = ti[0].getText();
            String[] path = Const.getTreeStrings(ti[0]);
			if (path.length==3)
			{
 				if (STRING_TABLES.equalsIgnoreCase(path[1]) ||
 					STRING_VIEWS.equalsIgnoreCase(path[1]) ||
 					STRING_SYNONYMS.equalsIgnoreCase(path[1]))
				{
                    schemaName = null;
					tableName = table;
                    dispose();
				}
            }
            if (path.length==4)
            {
				if (STRING_SCHEMAS.equals(path[1]) || STRING_CATALOG.equals(path[1])) 
				{
                    if (splitSchemaAndTable)
                    {
                        schemaName = path[2];
                        tableName = path[3];
                    }
                    else
                    {
                        schemaName = null;
                        tableName = dbMeta.getQuotedSchemaTableCombination(path[2], path[3]);
                    }
                    dispose();
				}
			}
		}
	}
	
	public void openSchema(SelectionEvent e)
	{
		TreeItem sel = (TreeItem)e.item;
		
		log.logDebug("SelectTableDialog", "Open :"+sel.getText());
		
		TreeItem up1 = sel.getParentItem();
		if (up1 != null)
		{
			TreeItem up2 = up1.getParentItem();
			if (up2 != null)
			{
				TreeItem up3 = up2.getParentItem();
				if (up3 != null)
				{
					tableName = sel.getText();
					if (!justLook) handleOK();
					else previewTable(tableName, false);
				}
			}
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

    /**
     * @return the schemaName
     */
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    /**
     * @return the splitSchemaAndTable
     */
    public boolean isSplitSchemaAndTable()
    {
        return splitSchemaAndTable;
    }

    /**
     * @param splitSchemaAndTable the splitSchemaAndTable to set
     */
    public void setSplitSchemaAndTable(boolean splitSchemaAndTable)
    {
        this.splitSchemaAndTable = splitSchemaAndTable;
    }

    /**
     * @return the selectSchema
     */
    public String getSelectedSchema()
    {
        return selectedSchema;
    }

    /**
     * @param selectSchema the selectSchema to set
     */
    public void setSelectedSchema(String selectSchema)
    {
        this.selectedSchema = selectSchema;
    }
}