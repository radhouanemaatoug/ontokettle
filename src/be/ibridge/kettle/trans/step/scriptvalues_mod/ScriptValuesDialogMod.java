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
 /**********************************************************************
 **                                                                   **
 ** This Script has been modified for higher performance              **
 ** and more functionality in December-2006,                          **
 ** by proconis GmbH / Germany                                        **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.trans.step.scriptvalues_mod;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import de.proconis.StyledText.StyledTextComp;



public class ScriptValuesDialogMod extends BaseStepDialog implements StepDialogInterface
{
	private ModifyListener lsMod;
	private SashForm     wSash;
	private FormData     fdSash;
	
	private Composite    wTop, wBottom;
	private FormData     fdTop, fdBottom;
	
	private Label        wlScript;
	private FormData     fdlScript, fdScript;

	private Label        wSeparator;
	private FormData     fdSeparator;
	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Label        wlPosition;
	private FormData     fdlPosition;
	
	private Text		wlHelpLabel;
	
	private Button wVars, wTest;
	private Listener lsVars, lsTest;
	
	// private Button wHelp;
	
	private Label		wlScriptFunctions;
	private FormData	fdlScriptFunctions;
	
	private Tree		wTree;
	private TreeItem	wTreeScriptsItem;
	private TreeItem	wTreeClassesitem;
	private	FormData	fdlTree;
	private Listener	lsTree;
	// private Listener	lsHelp;
	private FormData 	fdHelpLabel;
	
	private Image imageActiveScript=null;
	private Image imageInactiveScript=null;
	private Image imageActiveStartScript=null;
	private Image imageActiveEndScript=null;

	
	private static CTabFolder 	folder;
	private static Menu 		cMenu;
	private static Menu 		tMenu;
	
	// Suport for Rename Tree
	private TreeItem [] lastItem;
	private TreeEditor editor;
	
	private static final int DELETE_ITEM = 0;
	private static final int ADD_ITEM = 1;
	private static final int RENAME_ITEM = 2;
	private static final int SET_ACTIVE_ITEM = 3;
	
	private static final int ADD_COPY = 2;
	private static final int ADD_BLANK = 1;
	private static final int ADD_DEFAULT = 0;
	
	
	private String strActiveScript;
	private String strActiveStartScript;
	private String strActiveEndScript;
	
	private static String[] jsFunctionList = ScriptValuesAddedFunctions.jsFunctionList;
	
	public final static int SKIP_TRANSFORMATION = 1;
	private final static int ABORT_TRANSFORMATION = -1;
	private final static int ERROR_TRANSFORMATION = -2;
	private final static int CONTINUE_TRANSFORMATION = 0;
	
	
	private ScriptValuesMetaMod input;
	private ScriptValuesHelp scVHelp;
	private ScriptValuesHighlight lineStyler = new ScriptValuesHighlight();

	
	public ScriptValuesDialogMod(Shell parent, Object in, TransMeta transMeta, String sname){

		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ScriptValuesMetaMod)in;
		try{
			ImageLoader xl = new ImageLoader();
			imageActiveScript = new Image(parent.getDisplay(),xl.load(this.getClass().getResourceAsStream("/images/faScript.png"))[0]);
			imageInactiveScript = new Image(parent.getDisplay(),xl.load(this.getClass().getResourceAsStream("/images/fScript.png"))[0]);
			imageActiveStartScript = new Image(parent.getDisplay(),xl.load(this.getClass().getResourceAsStream("/images/sScript.png"))[0]);
			imageActiveEndScript = new Image(parent.getDisplay(),xl.load(this.getClass().getResourceAsStream("/images/eScript.png"))[0]);
		}catch(Exception e){
			imageActiveScript = new Image(parent.getDisplay(), 16, 16);
			imageInactiveScript = new Image(parent.getDisplay(), 16, 16);
			imageActiveStartScript = new Image(parent.getDisplay(), 16, 16);
			imageActiveEndScript = new Image(parent.getDisplay(), 16, 16);
		}
		
        try
        {
            scVHelp = new ScriptValuesHelp("/be/ibridge/kettle/trans/step/scriptvalues_mod/jsFunctionHelp.xml");
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
		
	}

	public String open(){
		
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);
 		
		lsMod = new ModifyListener() 
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
		shell.setText(Messages.getString("ScriptValuesDialogMod.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("ScriptValuesDialogMod.Stepname.Label")); //$NON-NLS-1$
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

		wSash = new SashForm(shell, SWT.VERTICAL );
 		props.setLook(wSash);
		
		// Top sash form
		//
		wTop = new Composite(wSash, SWT.NONE);
 		props.setLook(wTop);

		FormLayout topLayout  = new FormLayout ();
		topLayout.marginWidth  = Const.FORM_MARGIN;
		topLayout.marginHeight = Const.FORM_MARGIN;
		wTop.setLayout(topLayout);
		
		// Script line
		wlScriptFunctions=new Label(wTop, SWT.NONE);
		wlScriptFunctions.setText(Messages.getString("ScriptValuesDialogMod.JavascriptFunctions.Label")); //$NON-NLS-1$
		props.setLook(wlScriptFunctions);
		fdlScriptFunctions=new FormData();
		fdlScriptFunctions.left = new FormAttachment(0, 0);
		fdlScriptFunctions.top  = new FormAttachment(0, 0);
		wlScriptFunctions.setLayoutData(fdlScriptFunctions);
				
		// Tree View Test
		wTree = new Tree(wTop, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		props.setLook(wTree);
	    fdlTree=new FormData();
		fdlTree.left = new FormAttachment(0, 0);
		fdlTree.top  = new FormAttachment(wlScriptFunctions, margin);
		fdlTree.right = new FormAttachment(20, 0);
		fdlTree.bottom = new FormAttachment(100, -margin);
		wTree.setLayoutData(fdlTree);
		
		// Script line
		wlScript=new Label(wTop, SWT.NONE);
		wlScript.setText(Messages.getString("ScriptValuesDialogMod.Javascript.Label")); //$NON-NLS-1$
		props.setLook(wlScript);
		fdlScript=new FormData();
		fdlScript.left = new FormAttachment(wTree, margin);
		fdlScript.top  = new FormAttachment(0, 0);
		wlScript.setLayoutData(fdlScript);
		
		folder = new CTabFolder(wTop, SWT.BORDER | SWT.RESIZE);
		folder.setSimple(false);
		folder.setUnselectedImageVisible(true);
		folder.setUnselectedCloseVisible(true);
		fdScript=new FormData();
		fdScript.left   = new FormAttachment(wTree, margin);
		fdScript.top    = new FormAttachment(wlScript, margin);
		fdScript.right  = new FormAttachment(100, -5);
		fdScript.bottom = new FormAttachment(100, -50);
		folder.setLayoutData(fdScript);
		
		wlPosition=new Label(wTop, SWT.NONE);
		wlPosition.setText(Messages.getString("ScriptValuesDialogMod.Position.Label")); //$NON-NLS-1$
		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left  = new FormAttachment(wTree, margin);
		fdlPosition.right = new FormAttachment(30, 0);
		fdlPosition.top   = new FormAttachment(folder, margin);
		wlPosition.setLayoutData(fdlPosition);
		
		wlHelpLabel = new Text(wTop, SWT.V_SCROLL |   SWT.LEFT);
		wlHelpLabel.setEditable(false);
		wlHelpLabel.setText("Hallo");
		props.setLook(wlHelpLabel);
		fdHelpLabel = new FormData();
		fdHelpLabel.left = new FormAttachment(wlPosition, margin);
		fdHelpLabel.top = new FormAttachment(folder, margin);
		fdHelpLabel.right = new FormAttachment(100, -5);
		fdHelpLabel.bottom = new FormAttachment(100,0);
		wlHelpLabel.setLayoutData(fdHelpLabel);
		wlHelpLabel.setVisible(false);
		
		
		

		fdTop=new FormData();
		fdTop.left  = new FormAttachment(0, 0);
		fdTop.top   = new FormAttachment(0, 0);
		fdTop.right = new FormAttachment(100, 0);
		fdTop.bottom= new FormAttachment(100, 0);
		wTop.setLayoutData(fdTop);
		
		wBottom = new Composite(wSash, SWT.NONE);
 		props.setLook(wBottom);
		
		FormLayout bottomLayout  = new FormLayout ();
		bottomLayout.marginWidth  = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);
		
		wSeparator = new Label(wBottom, SWT.SEPARATOR | SWT.HORIZONTAL);
		fdSeparator= new FormData();
		fdSeparator.left  = new FormAttachment(0, 0);
		fdSeparator.right = new FormAttachment(100, 0);
		fdSeparator.top   = new FormAttachment(0, -margin+2);
		wSeparator.setLayoutData(fdSeparator);
		
		wlFields=new Label(wBottom, SWT.NONE);
		wlFields.setText(Messages.getString("ScriptValuesDialogMod.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wSeparator, 0);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.getName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[]
           {
    		 new ColumnInfo(Messages.getString("ScriptValuesDialogMod.ColumnInfo.Filename"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialogMod.ColumnInfo.RenameTo"),  ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialogMod.ColumnInfo.Type"),       ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() ), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialogMod.ColumnInfo.Length"),     ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialogMod.ColumnInfo.Precision"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
           };
		
		wFields=new TableView(wBottom, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );
		
		fdFields=new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);

		fdBottom=new FormData();
		fdBottom.left  = new FormAttachment(0, 0);
		fdBottom.top   = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom= new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);

		fdSash = new FormData();
		fdSash.left  = new FormAttachment(0, 0);
		fdSash.top   = new FormAttachment(wStepname, 0);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom= new FormAttachment(100, -50);
		wSash.setLayoutData(fdSash);
		
		wSash.setWeights(new int[] {75,25});

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wVars=new Button(shell, SWT.PUSH);
		wVars.setText(Messages.getString("ScriptValuesDialogMod.GetVariables.Button")); //$NON-NLS-1$
		wTest=new Button(shell, SWT.PUSH);
		wTest.setText(Messages.getString("ScriptValuesDialogMod.TestScript.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK,  wVars, wTest, wCancel }, margin, null);


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();          } };
		//lsGet      = new Listener() { public void handleEvent(Event e) { get();             } };
		lsTest     = new Listener() { public void handleEvent(Event e) { test(false, true); } };
		lsVars     = new Listener() { public void handleEvent(Event e) { test(true, true);  } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();              } };
		lsTree	   = new Listener() { public void handleEvent(Event e) { treeDblClick(e);       } };
		// lsHelp		= new Listener(){public void handleEvent(Event e){ wlHelpLabel.setVisible(true); }};

		wCancel.addListener(SWT.Selection, lsCancel);
		//wGet.addListener   (SWT.Selection, lsGet   );
		wTest.addListener (SWT.Selection, lsTest  );
		wVars.addListener  (SWT.Selection, lsVars  );
		wOK.addListener    (SWT.Selection, lsOK    );
		wTree.addListener(SWT.MouseDoubleClick, lsTree);
		
		
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wStepname.addSelectionListener( lsDef );
				
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
	        	CTabItem cItem = (CTabItem)event.item;
	        	event.doit=false;
	        	if(cItem!=null && folder.getItemCount()>1){
	        		MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
	        		messageBox.setText("Delete Item");
	        		messageBox.setMessage("Do you really want to delete "+cItem.getText() + "?");
		            switch(messageBox.open()){
		            	case SWT.YES:
		            		modifyScriptTree(cItem,DELETE_ITEM);
		            		event.doit=true;
		            		break;
		            }
	        	}
			}
		});

		cMenu = new Menu(shell, SWT.POP_UP);
		buildingFolderMenu();
		tMenu = new Menu(shell, SWT.POP_UP);
		buildingTreeMenu();
		
		// Adding the Default Transform Scripts Item to the Tree
		wTreeScriptsItem = new TreeItem(wTree, SWT.NULL);
		wTreeScriptsItem.setText("Transform Scripts");
		
		// Set the shell size, based upon previous time...
		setSize();
		getData();
		
		// Adding the Rest (Functions, InputItems, etc.) to the Tree
		buildSpecialFunctionsTree();
		buildInputFieldsTree();
		buildOutputFieldsTree();
		buildAddClassesListTree();
		addRenameTowTreeScriptItems();
		input.setChanged(changed);
		
		// Create the drag source on the tree
	    DragSource ds = new DragSource(wTree, DND.DROP_MOVE);
	    ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
	    ds.addDragListener(new DragSourceAdapter() {

	    	public void dragStart(DragSourceEvent event) {
	    		TreeItem item = wTree.getSelection()[0];

	    		// Qualifikation where the Drag Request Comes from
	            if(item !=null && item.getParentItem()!=null){
	            	if(item.getParentItem().equals(wTreeScriptsItem)){
	            		event.doit=false;
	            	}else if(!item.getData().equals("Function")){
	                	String strInsert =(String)item.getData();
	                	if(strInsert.equals("jsFunction")) event.doit=true;
	                	else event.doit=false;
	            	}else{
	            		event.doit=false;
	            	}
	            }else{
	            	event.doit=false;
	            }
	    		
			}
	    	
	    	public void dragSetData(DragSourceEvent event) {
	    		// Set the data to be the first selected item's text
	    		event.data = wTree.getSelection()[0].getText();
	    	}
	    });
		
		
		shell.open();
		while (!shell.isDisposed()){
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	private void setActiveCtab(String strName){
		if(strName.length()==0){
			folder.setSelection(0);
		}
		else folder.setSelection(getCTabPosition(strName));
	}
	
	private  void addCtab(String cScriptName, String strScript, int iType){
		CTabItem item = new CTabItem(folder, SWT.CLOSE);
		
		switch(iType){
			case ADD_DEFAULT: item.setText(cScriptName);
				break;
			default:
				item.setText(getNextName(cScriptName));
				break;
		}
		StyledTextComp wScript=new StyledTextComp(item.getParent(), SWT.MULTI | SWT.LEFT |  SWT.H_SCROLL | SWT.V_SCROLL, item.getText());
		if(strScript.length()>0) wScript.setText(strScript);
		else wScript.setText("//Script here"); 
		item.setImage(imageInactiveScript);
 		props.setLook(wScript, Props.WIDGET_STYLE_FIXED);
		
		wScript.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e) { setPosition(); }
				public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
		wScript.addFocusListener(new FocusAdapter(){
				public void focusGained(FocusEvent e) { setPosition(); }
				public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
		wScript.addMouseListener(new MouseAdapter(){
				public void mouseDoubleClick(MouseEvent e) { setPosition(); }
				public void mouseDown(MouseEvent e) { setPosition(); }
				public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
		
		wScript.addModifyListener(lsMod);
		
		// Text Higlighting
		lineStyler = new ScriptValuesHighlight(ScriptValuesAddedFunctions.jsFunctionList);
		wScript.addLineStyleListener(lineStyler);
 		item.setControl(wScript);
 		
 		// Adding new Item to Tree
 		modifyScriptTree(item, ADD_ITEM );
	}
	
	private void modifyScriptTree(CTabItem ctabitem, int iModType){
		
		switch(iModType){
			case DELETE_ITEM :
				TreeItem dItem = getTreeItemByName(ctabitem.getText());
		        if(dItem!=null){
		        	dItem.dispose();
		        	input.setChanged();
		        }
				break;
			case ADD_ITEM :
				TreeItem item = new TreeItem(wTreeScriptsItem, SWT.NULL);
				item.setText(ctabitem.getText());
				input.setChanged();
				break;
			
			case RENAME_ITEM :
				input.setChanged();
				break;
			case SET_ACTIVE_ITEM :
	        	input.setChanged();
				break;
		}
	}
	
	private TreeItem getTreeItemByName(String strTabName){
		TreeItem[] tItems = wTreeScriptsItem.getItems();
		for(int i=0;i<tItems.length;i++){
			if(tItems[i].getText().equals(strTabName)) return tItems[i];
		}
		return null;
	}
	
	private int getCTabPosition(String strTabName){
		CTabItem[] cItems = folder.getItems();
		for(int i=0;i<cItems.length;i++){
			if(cItems[i].getText().equals(strTabName)) return i;
		}
		return -1;
	}
	
	private CTabItem getCTabItemByName(String strTabName){
		CTabItem[] cItems = folder.getItems();
		for(int i=0;i<cItems.length;i++){
			if(cItems[i].getText().equals(strTabName)) return cItems[i];
		}
		return null;
	}
	
	
	private void modifyCTabItem(TreeItem tItem, int iModType, String strOption){
		
		switch(iModType){
			case DELETE_ITEM :
				CTabItem dItem = folder.getItem(getCTabPosition(tItem.getText()));
				if(dItem!=null){
					dItem.dispose();
					input.setChanged();
				}
				break;
			
			case RENAME_ITEM : 
				CTabItem rItem = folder.getItem(getCTabPosition(tItem.getText()));
				if(rItem!=null){
					rItem.setText(strOption);
					input.setChanged();
					if(rItem.getImage().equals(imageActiveScript)) strActiveScript = strOption;
					else if(rItem.getImage().equals(imageActiveStartScript)) strActiveStartScript = strOption;
					else if(rItem.getImage().equals(imageActiveEndScript)) strActiveEndScript = strOption;
				}
				break;
			case SET_ACTIVE_ITEM : 
				CTabItem aItem = folder.getItem(getCTabPosition(tItem.getText()));
				if(aItem!=null){
					input.setChanged();
					strActiveScript = tItem.getText();
					for(int i=0;i<folder.getItemCount();i++){
						if(folder.getItem(i).equals(aItem))aItem.setImage(imageActiveScript);
						else folder.getItem(i).setImage(imageInactiveScript);
					}
				}
				break;
		}
		
	}
	
	
	private StyledTextComp getStyledTextComp(){
		CTabItem item = folder.getSelection();
		if(item.getControl().isDisposed()) return null;
		else return (StyledTextComp)item.getControl();
	}

	private StyledTextComp getStyledTextComp(CTabItem item){
		return (StyledTextComp)item.getControl();
	}
	
    /*
	private void setStyledTextComp(String strText){
		CTabItem item = folder.getSelection();
		((StyledTextComp)item.getControl()).setText(strText);
	}

	private void setStyledTextComp(String strText, CTabItem item){
		((StyledTextComp)item.getControl()).setText(strText);
	}
    */
	
	private String getNextName(String strActualName){
		String strRC = "";
		if(strActualName.length()==0){
			strActualName = "Item";
		}
			
		int i=0;
		strRC = strActualName + "_" + i;
		while(getCTabItemByName(strRC)!=null){
			i++;
			strRC = strActualName + "_" + i;
		}
		return strRC; 
	}
	
	public void setPosition(){
		
		StyledTextComp wScript = getStyledTextComp();
		String scr = wScript.getText();
		int linenr = wScript.getLineAtOffset(wScript.getCaretOffset())+1;
		int posnr  = wScript.getCaretOffset();
		
		
		
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(Messages.getString("ScriptValuesDialogMod.Position.Label2")+linenr+", "+colnr); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		for (int i=0;i<input.getName().length;i++)
		{
			if (input.getName()[i]!=null && input.getName()[i].length()>0)
			{	
				TableItem item = wFields.table.getItem(i);
				item.setText(1, input.getName()[i]);
				if (input.getRename()[i]!=null && !input.getName()[i].equals(input.getRename()[i]))
					item.setText(2, input.getRename()[i]);
				item.setText(3, Value.getTypeDesc(input.getType()[i]));
				if (input.getLength()[i]>=0) item.setText(4, ""+input.getLength()[i]); //$NON-NLS-1$
                if (input.getPrecision()[i]>=0) item.setText(5, ""+input.getPrecision()[i]); //$NON-NLS-1$
			}
		}

		
		ScriptValuesScript[] jsScripts = input.getJSScripts();
		if(jsScripts.length>0){
			for(int i=0;i<jsScripts.length;i++){
				if(jsScripts[i].isTransformScript()) strActiveScript =jsScripts[i].getScriptName();
				else if(jsScripts[i].isStartScript()) strActiveStartScript =jsScripts[i].getScriptName(); 
				else if(jsScripts[i].isEndScript()) strActiveEndScript =jsScripts[i].getScriptName();
				addCtab(jsScripts[i].getScriptName(), jsScripts[i].getScript(), ADD_DEFAULT);
			}
		}else{
			addCtab("", "", ADD_DEFAULT);
		}
		setActiveCtab(strActiveScript);
		refresh();
		
		wFields.setRowNums();
		wFields.optWidth(true);
		wStepname.selectAll();
	}
	
	// Setting default active Script
	private void refresh(){
		//CTabItem item = getCTabItemByName(strActiveScript);
		for(int i=0;i<folder.getItemCount();i++){
			CTabItem item = folder.getItem(i);
			if(item.getText().equals(strActiveScript))item.setImage(imageActiveScript);
			else if(item.getText().equals(strActiveStartScript))item.setImage(imageActiveStartScript);
			else if(item.getText().equals(strActiveEndScript))item.setImage(imageActiveEndScript);
			else item.setImage(imageInactiveScript);
		}
		//modifyScriptTree(null, SET_ACTIVE_ITEM);
	}
	
	private void refreshScripts(){
		CTabItem[] cTabs = folder.getItems();
		for(int i =0;i<cTabs.length;i++){
			if(cTabs[i].getImage().equals(imageActiveStartScript)) strActiveStartScript = cTabs[i].getText();
			else if(cTabs[i].getImage().equals(imageActiveEndScript)) strActiveEndScript = cTabs[i].getText();
		}
	}
	private void cancel(){
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		boolean bInputOK = false;
		
		// Check if Active Script has set, otherwise Ask
		if(getCTabItemByName(strActiveScript)==null){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_ERROR );
			mb.setMessage("No active Script has been set! Should the first tab set as acitve Script?");
			mb.setText("ERROR"); //$NON-NLS-1$
			switch(mb.open()){
				case SWT.OK:
					strActiveScript = folder.getItem(0).getText();
					refresh();
					bInputOK = true;
				break;
				case SWT.CANCEL: bInputOK = false;
					break;
			}
		}else{
			bInputOK = true;
		}
		
		if(bInputOK){

			//StyledTextComp wScript = getStyledTextComp();
			//input.setScript( wScript.getText() );
			int nrfields = wFields.nrNonEmpty();
			input.allocate(nrfields);
			for (int i=0;i<nrfields;i++){
				TableItem item = wFields.getNonEmpty(i);
				input.getName()  [i] = item.getText(1);
				input.getRename()[i] = item.getText(2);
				if (input.getRename()[i]==null || 
						input.getRename()[i].length()==0 || 
						input.getRename()[i].equalsIgnoreCase(input.getName()[i])
				)
				{
					input.getRename()[i] = input.getName()[i];
				}
				input.getType()  [i] = Value.getType(item.getText(3));
				String slen = item.getText(4);
				String sprc = item.getText(5);
				input.getLength()   [i]=Const.toInt(slen, -1);
				input.getPrecision()[i]=Const.toInt(sprc, -1);
			}
		
			
			
			//input.setActiveJSScript(strActiveScript);
			CTabItem[] cTabs = folder.getItems();
			if(cTabs.length>0){
				ScriptValuesScript[] jsScripts = new ScriptValuesScript[cTabs.length];
				for(int i=0;i<cTabs.length;i++){
					ScriptValuesScript jsScript = new ScriptValuesScript(
							ScriptValuesScript.NORMAL_SCRIPT,
							cTabs[i].getText(),
							getStyledTextComp(cTabs[i]).getText()
						);
					if(cTabs[i].getImage().equals(imageActiveScript)) jsScript.setScriptType(ScriptValuesScript.TRANSFORM_SCRIPT);
					else if(cTabs[i].getImage().equals(imageActiveStartScript)) jsScript.setScriptType(ScriptValuesScript.START_SCRIPT);
					else if(cTabs[i].getImage().equals(imageActiveEndScript)) jsScript.setScriptType(ScriptValuesScript.END_SCRIPT);
					jsScripts[i] = jsScript;
				}
				input.setJSScripts(jsScripts);
			}
			dispose();
		}
	}
	
    /*
	private void get()
	{
		try
		{
			StyledTextComp wScript = getStyledTextComp();
			String script = wScript.getText();
			script+=Const.CR;
	
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					
					switch(v.getType())
					{
					case Value.VALUE_TYPE_STRING : script+=v.getName()+".getString()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_NUMBER : script+=v.getName()+".getNumber()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_INTEGER: script+=v.getName()+".getInt()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_DATE   : script+=v.getName()+".getDate()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_BOOLEAN: script+=v.getName()+".getBool()"; break; //$NON-NLS-1$
					default: script+=v.getName(); break;
					}
					script+=";"+Const.CR; //$NON-NLS-1$
				}
				wScript.setText(script);
			}
		}
		catch(KettleException ke)
		{
			//new ErrorDialog(shell, props, Messages.getString("ScriptValuesDialogMod.FailedToGetFields.DialogTitle"), Messages.getString("ScriptValuesDialogMod.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
    */
	
	public boolean test()
	{
		return test(false, false);
	}
	
	private boolean test(boolean getvars, boolean popup)
	{
		boolean retval=true;
		StyledTextComp wScript = getStyledTextComp();
		String scr = wScript.getText();
		String errorMessage = ""; //$NON-NLS-1$
		
		Context jscx;
		Scriptable jsscope;
		// Script jsscript;

		// Making Refresh to get Active Script State
		refreshScripts();
		
		jscx = Context.enter();
		jscx.setOptimizationLevel(-1);
		jsscope = jscx.initStandardObjects(null);
		
		// Adding the existing Scripts to the Context
		for(int i=0;i<folder.getItemCount();i++){
			StyledTextComp sItem = getStyledTextComp(folder.getItem(i));
			Scriptable jsR = Context.toObject(sItem.getText(), jsscope);
			jsscope.put(folder.getItem(i).getText(), jsscope, jsR); //$NON-NLS-1$
		}
		
		// Adding the Name of the Transformation to the Context
		jsscope.put("_TransformationName_", jsscope, new String(this.stepname));
		
			
		try{
			
			Row row = transMeta.getPrevStepFields(stepname);
			if (row!=null){
				// Modification for Additional Script parsing
				try{
                    if (input.getAddClasses()!=null)
                    {
    					for(int i=0;i<input.getAddClasses().length;i++){
    						Object jsOut = Context.javaToJS(input.getAddClasses()[i].getAddObject(), jsscope);
    						ScriptableObject.putProperty(jsscope, input.getAddClasses()[i].getJSName(), jsOut);
    					}
                    }
				}catch(Exception e){
					errorMessage="Couldn't add JavaClasses to Context! Error:"+Const.CR+e.toString(); //$NON-NLS-1$
					retval = false;
				}
				
				// Adding some default JavaScriptFunctions to the System
				try {
					Context.javaToJS(ScriptValuesAddedFunctions.class, jsscope);
					((ScriptableObject)jsscope).defineFunctionProperties(jsFunctionList, ScriptValuesAddedFunctions.class, ScriptableObject.DONTENUM);
				} catch (Exception ex) {
					errorMessage="Couldn't add Default Functions! Error:"+Const.CR+ex.toString(); //$NON-NLS-1$
					retval = false;
				};

				// Adding some Constants to the JavaScript
				try {
					jsscope.put("SKIP_TRANSFORMATION", jsscope, new Integer(SKIP_TRANSFORMATION));
					jsscope.put("ABORT_TRANSFORMATION", jsscope, new Integer(ABORT_TRANSFORMATION));
					jsscope.put("ERROR_TRANSFORMATION", jsscope, new Integer(ERROR_TRANSFORMATION));
					jsscope.put("CONTINUE_TRANSFORMATION", jsscope, new Integer(CONTINUE_TRANSFORMATION));
				} catch (Exception ex) {
					errorMessage="Couldn't add Transformation Constants! Error:"+Const.CR+ex.toString(); //$NON-NLS-1$
					retval = false;
				};
				
				try{
	   			    Scriptable jsrow = Context.toObject(row, jsscope);
				    jsscope.put("row", jsscope, jsrow); //$NON-NLS-1$
				    for (int i=0;i<row.size();i++)
				    {
	  				    Value val = row.getValue(i); 
					    // Set date and string values to something to simulate real thing
					    if (val.isDate()) val.setValue(new Date());
					    if (val.isString()) val.setValue("test value test value test value test value test value test value test value test value test value test value"); //$NON-NLS-1$
					    Scriptable jsarg = Context.toObject(val, jsscope);
					    jsscope.put(val.getName(), jsscope, jsarg);
				    }
				    // Add support for Value class (new Value())
				    Scriptable jsval = Context.toObject(Value.class, jsscope);
				    jsscope.put("Value", jsscope, jsval); //$NON-NLS-1$
				}catch(Exception ev){
					errorMessage="Couldn't add Input fields to Script! Error:"+Const.CR+ev.toString(); //$NON-NLS-1$
					retval = false;
				}
				
				try{
					// Checking for StartScript
					if(strActiveStartScript != null && !folder.getSelection().getText().equals(strActiveStartScript) && strActiveStartScript.length()>0){
						String strStartScript = getStyledTextComp(folder.getItem(getCTabPosition(strActiveStartScript))).getText();
						/* Object startScript = */ jscx.evaluateString(jsscope, strStartScript, "trans_Start", 1, null);
					}
				}catch(Exception e){
					errorMessage="Couldn't process Start Script! Error:"+Const.CR+e.toString(); //$NON-NLS-1$
					retval = false;					
				};
				
				try{
					
					Script evalScript = jscx.compileString(scr, "script", 1, null);
					evalScript.exec(jscx, jsscope);
					//Object tranScript = jscx.evaluateString(jsscope, scr, "script", 1, null);
						
					if (getvars){
						ScriptOrFnNode tree = parseVariables(jscx, jsscope, scr, "script", 1, null); 
						for (int i=0;i<tree.getParamAndVarCount();i++){
							String varname = tree.getParamOrVarName(i);
							if (!varname.equalsIgnoreCase("row") && !varname.equalsIgnoreCase("trans_Status") && row.searchValueIndex(varname)<0){
								int type=Value.VALUE_TYPE_STRING;
								int length=-1, precision=-1;
								Object result = jsscope.get(varname, jsscope);
								if (result!=null){
									String classname = result.getClass().getName();	
									if (classname.equalsIgnoreCase("java.lang.Byte")){
										// MAX = 127
										type=Value.VALUE_TYPE_INTEGER;
										length=3;
										precision=0;
									}else if (classname.equalsIgnoreCase("java.lang.Integer")){
										// MAX = 2147483647
										type=Value.VALUE_TYPE_INTEGER;
										length=9;
										precision=0;
									}else if (classname.equalsIgnoreCase("java.lang.Long")){
										// MAX = 9223372036854775807
										type=Value.VALUE_TYPE_INTEGER;
										length=18;
										precision=0;
									}else if (classname.equalsIgnoreCase("java.lang.Double")){
										type=Value.VALUE_TYPE_NUMBER;
										length=16;
										precision=2;
										
									}else if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeDate") || classname.equalsIgnoreCase("java.util.Date")){
										type=Value.VALUE_TYPE_DATE;
									}else if (classname.equalsIgnoreCase("java.lang.Boolean")){
										type=Value.VALUE_TYPE_BOOLEAN;
									}
								}
								TableItem ti = new TableItem(wFields.table, SWT.NONE);
								ti.setText(1, varname);
								ti.setText(2, varname);
								ti.setText(3, Value.getTypeDesc(type));
								ti.setText(4, ""+length); //$NON-NLS-1$
								ti.setText(5, ""+precision); //$NON-NLS-1$
							}
						}
						wFields.removeEmptyRows();
						wFields.setRowNums();
						wFields.optWidth(true);
					}
					
					// End Script!
				}
				catch(JavaScriptException jse){
					errorMessage=Messages.getString("ScriptValuesDialogMod.Exception.CouldNotExecuteScript")+Const.CR+jse.toString(); //$NON-NLS-1$
					retval=false;
				}
				catch(Exception e){
					errorMessage=Messages.getString("ScriptValuesDialogMod.Exception.CouldNotExecuteScript2")+Const.CR+e.toString(); //$NON-NLS-1$
					retval=false;
				}
			}else{
				errorMessage = Messages.getString("ScriptValuesDialogMod.Exception.CouldNotGetFields"); //$NON-NLS-1$
				retval=false;
			}
	
			if (popup){
				if (retval){
					if (!getvars){
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
						mb.setMessage("This script compiled without problems."+Const.CR); //$NON-NLS-1$
						mb.setText("OK"); //$NON-NLS-1$
						mb.open();
					}
				}else{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(errorMessage);
					mb.setText("ERROR"); //$NON-NLS-1$
					mb.open(); 
				}
			}
		}catch(KettleException ke){
			retval=false;
			new ErrorDialog(shell, Messages.getString("ScriptValuesDialogMod.TestFailed.DialogTitle"), Messages.getString("ScriptValuesDialogMod.TestFailed.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}finally{
			if (jscx!=null) Context.exit();
		}
		return retval;
	}
		
	public String toString(){
		return this.getClass().getName();
	}
	
	private void buildSpecialFunctionsTree(){

		TreeItem item = new TreeItem(wTree, SWT.NULL);
		item.setText("Transform Constants");
		TreeItem itemT = new TreeItem(item, SWT.NULL);
		itemT.setText("SKIP_TRANSFORMATION");
		itemT.setData("SKIP_TRANSFORMATION");
		//itemT = new TreeItem(item, SWT.NULL);
		//itemT.setText("ABORT_TRANSFORMATION");
		//itemT.setData("ABORT_TRANSFORMATION");
		itemT = new TreeItem(item, SWT.NULL);
		itemT.setText("ERROR_TRANSFORMATION");
		itemT.setData("ERROR_TRANSFORMATION");
		itemT = new TreeItem(item, SWT.NULL);
		itemT.setText("CONTINUE_TRANSFORMATION");
		itemT.setData("CONTINUE_TRANSFORMATION");
		
		
		item = new TreeItem(wTree, SWT.NULL);
		item.setText("Transform Functions");
		String strData = "";
		
		// Adding the Grouping Items to the Tree
		TreeItem itemStringFunctionsGroup = new TreeItem(item, SWT.NULL);
		itemStringFunctionsGroup.setText("String Functions");
		itemStringFunctionsGroup.setData("Function");
		TreeItem itemNumericFunctionsGroup = new TreeItem(item, SWT.NULL);
		itemNumericFunctionsGroup.setText("Numeric Functions");
		itemNumericFunctionsGroup.setData("Function");
		TreeItem itemDateFunctionsGroup = new TreeItem(item, SWT.NULL);
		itemDateFunctionsGroup.setText("Date Functions");
		itemDateFunctionsGroup.setData("Function");
		TreeItem itemLogicFunctionsGroup = new TreeItem(item, SWT.NULL);
		itemLogicFunctionsGroup.setText("Logic Functions");
		itemLogicFunctionsGroup.setData("Function");
		TreeItem itemSpecialFunctionsGroup = new TreeItem(item, SWT.NULL);
		itemSpecialFunctionsGroup.setText("Special Functions");
		itemSpecialFunctionsGroup.setData("Function");
		
		// Loading the Default delivered JScript Functions
		//Method[] methods = ScriptValuesAddedFunctions.class.getMethods();
		//String strClassType = ScriptValuesAddedFunctions.class.toString();
		
		
		
		
		Hashtable hatFunctions =scVHelp.getFunctionList(); 
	    Vector v = new Vector(hatFunctions.keySet());
	    Collections.sort(v);
	    Iterator it = v.iterator();
	    while (it.hasNext()) {
	       String strFunction =  (String)it.next();
	       String strFunctionType =(String)hatFunctions.get(strFunction);
	       int iFunctionType = Integer.valueOf(strFunctionType).intValue();
	       //System.out.println( element + " " + (String)hatFunctions.get(element));
	       TreeItem itemFunction=null;
			switch(iFunctionType){
				case ScriptValuesAddedFunctions.STRING_FUNCTION: itemFunction = new TreeItem(itemStringFunctionsGroup,SWT.NULL); break;
				case ScriptValuesAddedFunctions.NUMERIC_FUNCTION:itemFunction = new TreeItem(itemNumericFunctionsGroup,SWT.NULL); break;
				case ScriptValuesAddedFunctions.DATE_FUNCTION:itemFunction = new TreeItem(itemDateFunctionsGroup,SWT.NULL); break;
				case ScriptValuesAddedFunctions.LOGIC_FUNCTION:itemFunction = new TreeItem(itemLogicFunctionsGroup,SWT.NULL); break;
				case ScriptValuesAddedFunctions.SPECIAL_FUNCTION:itemFunction = new TreeItem(itemSpecialFunctionsGroup,SWT.NULL); break;
			}
			if(itemFunction !=null){
				itemFunction.setText(strFunction);
				strData = "jsFunction";
				itemFunction.setData(strData);
			}
	    }		
	}
	
	public boolean TreeItemExist(TreeItem itemToCheck, String strItemName){
		boolean bRC=false;
		if(itemToCheck.getItemCount()>0){
			TreeItem[] items = itemToCheck.getItems();
			for(int i=0;i<items.length;i++){
				if(items[i].getText().equals(strItemName)) return true;
			}
		}
		return bRC;
	}
	
	private void buildOutputFieldsTree(){
		try{
		
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null){
				TreeItem item = new TreeItem(wTree, SWT.NULL);
				item.setText("Output Fields");
				String strItemToAdd="";
				for (int i=0;i<r.size();i++){
						Value v = r.getValue(i);
						switch(v.getType()){
							case Value.VALUE_TYPE_STRING : strItemToAdd=v.getName()+".setString(var)"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_NUMBER : strItemToAdd=v.getName()+".setNumber(var)"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_INTEGER: strItemToAdd=v.getName()+".setInt(var)"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_DATE   : strItemToAdd=v.getName()+".setDate(var)"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_BOOLEAN: strItemToAdd=v.getName()+".setBool(var)"; break; //$NON-NLS-1$
							default: strItemToAdd=v.getName(); break;
						}
						TreeItem itemInputFields = new TreeItem(item, SWT.NULL);
						itemInputFields.setText(strItemToAdd);
						itemInputFields.setData(strItemToAdd);
					}
			}
		}catch(KettleException ke){
			new ErrorDialog(shell, Messages.getString("ScriptValuesDialogMod.FailedToGetFields.DialogTitle"), Messages.getString("ScriptValuesDialogMod.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	
	private void buildInputFieldsTree(){
		try{
		
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null){
				TreeItem item = new TreeItem(wTree, SWT.NULL);
				item.setText("Input Fields");
				String strItemToAdd="";
				for (int i=0;i<r.size();i++){
						Value v = r.getValue(i);
						switch(v.getType()){
							case Value.VALUE_TYPE_STRING : strItemToAdd=v.getName()+".getString()"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_NUMBER : strItemToAdd=v.getName()+".getNumber()"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_INTEGER: strItemToAdd=v.getName()+".getInt()"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_DATE   : strItemToAdd=v.getName()+".getDate()"; break; //$NON-NLS-1$
							case Value.VALUE_TYPE_BOOLEAN: strItemToAdd=v.getName()+".getBool()"; break; //$NON-NLS-1$
							default: strItemToAdd=v.getName(); break;
						}
						TreeItem itemInputFields = new TreeItem(item, SWT.NULL);
						itemInputFields.setText(strItemToAdd);
						itemInputFields.setData(strItemToAdd);
					}
			}
		}catch(KettleException ke){
			new ErrorDialog(shell, Messages.getString("ScriptValuesDialogMod.FailedToGetFields.DialogTitle"), Messages.getString("ScriptValuesDialogMod.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	// Adds the Current item to the current Position
	private void treeDblClick(Event event){
		StyledTextComp wScript = getStyledTextComp();
		Point point = new Point(event.x, event.y);
        TreeItem item = wTree.getItem(point);
        
        // Qualifikation where the Click comes from
        if(item !=null && item.getParentItem()!=null){
        	if(item.getParentItem().equals(wTreeScriptsItem)){
        		setActiveCtab(item.getText());
        	}else if(!item.getData().equals("Function")){
        		int iStart = wScript.getCaretOffset();
            	String strInsert =(String)item.getData();
            	if(strInsert.equals("jsFunction")) strInsert = (String)item.getText();
            	wScript.insert(strInsert);
            	wScript.setSelection(iStart,iStart+strInsert.length());
        	}
        }
        /*
        if (item != null && item.getParentItem()!=null && !item.getData().equals("Function")) {
        	int iStart = wScript.getCaretOffset();
        	String strInsert =(String)item.getData();
        	if(strInsert.equals("jsFunction")) strInsert = (String)item.getText();
        	wScript.insert(strInsert);
        	wScript.setSelection(iStart,iStart+strInsert.length());
        }*/
	}
	
	// Building the Tree for Additional Classes
	private void buildAddClassesListTree(){
		if(wTreeClassesitem!=null){
			wTreeClassesitem.dispose();
		}
        if (input.getAddClasses()!=null)
        {
    		for(int i=0;i<input.getAddClasses().length;i++){
    			//System.out.println(input.getAddClasses().length);
    			
    			try{
    				Method[] methods = input.getAddClasses()[i].getAddClass().getMethods();
    				String strClassType = input.getAddClasses()[i].getAddClass().toString();
    				String strParams;
    				wTreeClassesitem = new TreeItem(wTree, SWT.NULL);
    				wTreeClassesitem.setText(input.getAddClasses()[i].getJSName());
    				for (int j=0; j<methods.length; j++){
    					String strDeclaringClass = methods[j].getDeclaringClass().toString();
    					if(strClassType.equals(strDeclaringClass)){
    						TreeItem item2 = new TreeItem(wTreeClassesitem, SWT.NULL);
    						strParams = buildAddClassFunctionName(methods[j]);
    						item2.setText(methods[j].getName() + "("+ strParams  +")");
    						String strData = input.getAddClasses()[i].getJSName() + "." +methods[j].getName() + "("+strParams+")";
    						item2.setData(strData);
    					}
    				}
    			}
                catch(Exception e){
    			}
    		}
        }
	}
	
	
	private String buildAddClassFunctionName(Method metForParams){
		StringBuffer sbRC = new StringBuffer();
		String strRC = "";
		Class[] clsParamType = metForParams.getParameterTypes();
		String strParam;
		

		for(int x=0;x<clsParamType.length;x++){
			strParam = clsParamType[x].getName();
			if(strParam.toLowerCase().indexOf("javascript")>0){
			}else if(strParam.toLowerCase().indexOf("object")>0){
				sbRC.append("var");
				sbRC.append(", ");
			}else if(strParam.equals("java.lang.String")){
				sbRC.append("String");
				sbRC.append(", ");
			}else{
				sbRC.append(strParam);
				sbRC.append(", ");
			}
			
		}
		strRC = sbRC.toString();
		if(strRC.length()>0) strRC = strRC.substring(0,sbRC.length()-2);
		return strRC;
	}
	
	private void buildingFolderMenu(){
		//styledTextPopupmenu = new Menu(, SWT.POP_UP);
		MenuItem addNewItem = new MenuItem(cMenu, SWT.PUSH);
		addNewItem.setText("Add New");
		addNewItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addCtab("","", ADD_BLANK);
			}
		});
		
		MenuItem copyItem = new MenuItem(cMenu, SWT.PUSH);
		copyItem.setText("Add Copy");
		copyItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				StyledTextComp st = (StyledTextComp)item.getControl();
				addCtab(item.getText(),st.getText(), ADD_COPY);
			}
		});
		new MenuItem(cMenu, SWT.SEPARATOR);
		
		MenuItem setActiveScriptItem = new MenuItem(cMenu, SWT.PUSH);
		setActiveScriptItem.setText("Set Transform Script");
		setActiveScriptItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				for(int i=0;i<folder.getItemCount();i++){
					if(folder.getItem(i).equals(item)){
						if(item.getImage().equals(imageActiveScript)) strActiveScript="";
						else if(item.getImage().equals(imageActiveStartScript)) strActiveStartScript="";
						else if(item.getImage().equals(imageActiveEndScript)) strActiveEndScript="";
						item.setImage(imageActiveScript);
						strActiveScript = item.getText();
					}
					else if(folder.getItem(i).getImage().equals(imageActiveScript)){
						folder.getItem(i).setImage(imageInactiveScript);
					}
				}
				modifyScriptTree(item, SET_ACTIVE_ITEM);
			}
		});
		
		MenuItem setStartScriptItem = new MenuItem(cMenu, SWT.PUSH);
		setStartScriptItem.setText("Set Start Script");
		setStartScriptItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				for(int i=0;i<folder.getItemCount();i++){
					if(folder.getItem(i).equals(item)){
						if(item.getImage().equals(imageActiveScript)) strActiveScript="";
						else if(item.getImage().equals(imageActiveStartScript)) strActiveStartScript="";
						else if(item.getImage().equals(imageActiveEndScript)) strActiveEndScript="";
						item.setImage(imageActiveStartScript);
						strActiveStartScript = item.getText();
					}
					else if(folder.getItem(i).getImage().equals(imageActiveStartScript)){
						folder.getItem(i).setImage(imageInactiveScript);
					}
				}
				modifyScriptTree(item, SET_ACTIVE_ITEM);
			}
		});
		
		MenuItem setEndScriptItem = new MenuItem(cMenu, SWT.PUSH);
		setEndScriptItem.setText("Set End Script");
		setEndScriptItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				for(int i=0;i<folder.getItemCount();i++){
					if(folder.getItem(i).equals(item)){
						if(item.getImage().equals(imageActiveScript)) strActiveScript="";
						else if(item.getImage().equals(imageActiveStartScript)) strActiveStartScript="";
						else if(item.getImage().equals(imageActiveEndScript)) strActiveEndScript="";
						item.setImage(imageActiveEndScript);
						strActiveEndScript = item.getText();
					}
					else if(folder.getItem(i).getImage().equals(imageActiveEndScript)){
						folder.getItem(i).setImage(imageInactiveScript);
					}
				}
				modifyScriptTree(item, SET_ACTIVE_ITEM);
			}
		});
		new MenuItem(cMenu, SWT.SEPARATOR);
		MenuItem setRemoveScriptItem = new MenuItem(cMenu, SWT.PUSH);
		setRemoveScriptItem.setText("Remove Script Type");
		setRemoveScriptItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				input.setChanged(true);
				if(item.getImage().equals(imageActiveScript)) strActiveScript="";
				else if(item.getImage().equals(imageActiveStartScript)) strActiveStartScript="";
				else if(item.getImage().equals(imageActiveEndScript)) strActiveEndScript="";
				item.setImage(imageInactiveScript);
			}
		});
		
		folder.setMenu(cMenu);
	}	

	private void buildingTreeMenu(){
		//styledTextPopupmenu = new Menu(, SWT.POP_UP);
		MenuItem addDeleteItem = new MenuItem(tMenu, SWT.PUSH);
		addDeleteItem.setText("Delete");
		addDeleteItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TreeItem tItem = wTree.getSelection()[0];
		        if(tItem!=null){
		        	MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
		            messageBox.setText("Delete Item");
		            messageBox.setMessage("Do you really want to delete "+tItem.getText() + "?");
		            switch(messageBox.open()){
		            	case SWT.YES:
				        	modifyCTabItem(tItem,DELETE_ITEM,"");
				        	tItem.dispose();
				        	input.setChanged();
		            		break;
		            }
		        }
			}
		});
		
		MenuItem renItem = new MenuItem(tMenu, SWT.PUSH);
		renItem.setText("Rename");
		renItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				renameFunction(wTree.getSelection()[0]);
			}
		});
		
		new MenuItem(tMenu, SWT.SEPARATOR);
		MenuItem helpItem = new MenuItem(tMenu, SWT.PUSH);
		helpItem.setText("Sample");
		helpItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String strFunctionName = wTree.getSelection()[0].getText();
				String strFunctionNameWithArgs = strFunctionName;
				strFunctionName = strFunctionName.substring(0,strFunctionName.indexOf('('));
				String strHelpTabName = strFunctionName + "_Sample";
				
				if(getCTabPosition(strHelpTabName)==-1)
				addCtab(strHelpTabName, scVHelp.getSample(strFunctionName,strFunctionNameWithArgs), 0);
				
				if(getCTabPosition(strHelpTabName)!=-1)
				setActiveCtab(strHelpTabName);
			}
		});
		
		
		wTree.addListener(SWT.MouseDown, new Listener(){
			public void handleEvent(Event e){
				TreeItem tItem = wTree.getSelection()[0];
				if(tItem != null){
					TreeItem pItem = tItem.getParentItem();
					
					if(pItem !=null && pItem.equals(wTreeScriptsItem)){
						if(folder.getItemCount()>1)	tMenu.getItem(0).setEnabled(true);
						else tMenu.getItem(0).setEnabled(false);
						tMenu.getItem(1).setEnabled(true);
						tMenu.getItem(3).setEnabled(false);
					}else if(tItem.equals(wTreeClassesitem)){
						tMenu.getItem(0).setEnabled(false);	
						tMenu.getItem(1).setEnabled(false);
						tMenu.getItem(3).setEnabled(false);
					}else if(tItem.getData() != null && tItem.getData().equals("jsFunction")){
						tMenu.getItem(0).setEnabled(false);	
						tMenu.getItem(1).setEnabled(false);
						tMenu.getItem(3).setEnabled(true);
					}else{
						tMenu.getItem(0).setEnabled(false);	
						tMenu.getItem(1).setEnabled(false);
						tMenu.getItem(3).setEnabled(false);
					}
				}
			}
		});
		wTree.setMenu(tMenu);
	}
	
	private void addRenameTowTreeScriptItems(){
		lastItem = new TreeItem [1];
		editor = new TreeEditor (wTree);
		wTree.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
				final TreeItem item = (TreeItem)event.item;
				renameFunction(item);
			}
		});
	}
	
	// This function is for a Windows Like renaming inside the tree
	private void renameFunction(TreeItem tItem){
		final TreeItem item = tItem;
		if(item.getParentItem()!=null && item.getParentItem().equals(wTreeScriptsItem)){
			if (item != null && item == lastItem [0]) {
				boolean isCarbon = SWT.getPlatform ().equals ("carbon");
				final Composite composite = new Composite (wTree, SWT.NONE);
				if (!isCarbon) composite.setBackground(shell.getDisplay().getSystemColor (SWT.COLOR_BLACK));
				final Text text = new Text (composite, SWT.NONE);
				final int inset = isCarbon ? 0 : 1;
				composite.addListener (SWT.Resize, new Listener () {
					public void handleEvent (Event e) {
						Rectangle rect = composite.getClientArea ();
						text.setBounds (rect.x + inset, rect.y + inset, rect.width - inset * 2, rect.height - inset * 2);
					}
				});
				Listener textListener = new Listener () {
					public void handleEvent (final Event e) {
						switch (e.type) {
							case SWT.FocusOut:
								if(text.getText().length()>0){
									// Check if the name Exists
									if(getCTabItemByName(text.getText())==null){
										modifyCTabItem(item,RENAME_ITEM, text.getText());
										item.setText (text.getText ());
									}
								}
								composite.dispose ();
							break;
							case SWT.Verify:
								String newText = text.getText ();
								String leftText = newText.substring (0, e.start);
								String rightText = newText.substring (e.end, newText.length ());
								GC gc = new GC (text);
								Point size = gc.textExtent (leftText + e.text + rightText);
								gc.dispose ();
								size = text.computeSize (size.x, SWT.DEFAULT);
								editor.horizontalAlignment = SWT.LEFT;
								Rectangle itemRect = item.getBounds (), rect = wTree.getClientArea ();
								editor.minimumWidth = Math.max (size.x, itemRect.width) + inset * 2;
								int left = itemRect.x, right = rect.x + rect.width;
								editor.minimumWidth = Math.min (editor.minimumWidth, right - left);
								editor.minimumHeight = size.y + inset * 2;
								editor.layout ();
								break;
							case SWT.Traverse:
								switch (e.detail) {
									case SWT.TRAVERSE_RETURN:
										if(text.getText().length()>0){
											// Check if the name Exists
											if(getCTabItemByName(text.getText())==null){
												modifyCTabItem(item,RENAME_ITEM, text.getText());
												item.setText (text.getText ());
											}
										}
									case SWT.TRAVERSE_ESCAPE:
										composite.dispose ();
										e.doit = false;
								}
								break;
						}
					}
				};
				text.addListener (SWT.FocusOut, textListener);
				text.addListener (SWT.Traverse, textListener);
				text.addListener (SWT.Verify, textListener);
				editor.setEditor (composite, item);
				text.setText (item.getText ());
				text.selectAll ();
				text.setFocus ();
		
			}
		}
		lastItem [0] = item;
	}
	
	// This could be useful for further improvements
	public static ScriptOrFnNode parseVariables(Context cx, Scriptable scope, String source, String sourceName, int lineno, Object securityDomain){
    	// Interpreter compiler = new Interpreter();
    	CompilerEnvirons evn = new CompilerEnvirons();
    	evn.setLanguageVersion(Context.VERSION_1_5);
    	evn.setOptimizationLevel(-1);
    	evn.setGeneratingSource(true);
    	evn.setGenerateDebugInfo(true);
    	Parser p = new Parser(evn);
    	ScriptOrFnNode tree = p.parse(source, "",0); // IOException
    	//Script result = (Script)compiler.compile(scope, evn, tree, p.getEncodedSource(),false, null);
    	return tree;
	}
}