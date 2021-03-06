 /**********************************************************************
 **                                                                   **
 ** This Script has been developed for more StyledText Enrichment     **
 ** December-2006 by proconis GmbH / Germany                          **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package de.proconis.StyledText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class StyledTextCompFind extends org.eclipse.swt.widgets.Dialog {


	private Shell sShell = null;
	private Text searchText;
	private StyledText text;
	private String strHeader;
	
	private Button btnNext;
	private Button btnCancel;
	
	private Button btnIgnoreCase;
	private Button optForward; 
	
	public StyledTextCompFind(Shell parent, StyledText text, String strHeader){
		super(parent);
		this.text = text;
		this.strHeader = "Find";
	}
	
	public void open() {
		
		Shell parent = getParent();
		sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.NO_REDRAW_RESIZE | SWT.APPLICATION_MODAL  );

		sShell.setText(strHeader);
		sShell.setSize(new Point(360, 126));
		FormLayout formLayout = new FormLayout ();
		sShell.setLayout(formLayout);
		
		searchText = new Text(sShell,  SWT.SINGLE | SWT.BORDER);
		FormData frmData = new FormData();
		frmData.left = new FormAttachment(0, 70);
		frmData.top  = new FormAttachment(12, 0);
		frmData.width = 178;
		searchText.setLayoutData(frmData);

		Label lblFind = new Label(sShell,SWT.LEFT);
		lblFind.setText("Find:");
		frmData = new FormData();
		frmData.right = new FormAttachment(searchText,  -8);
		frmData.top  = new FormAttachment(12, 0);
		lblFind.setLayoutData(frmData);

		btnNext = new Button(sShell,SWT.PUSH);
		btnNext.setText("Find &Next");
		btnNext.setEnabled(false);
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(9, 0);
		btnNext.setLayoutData(frmData);
		
		btnCancel = new Button(sShell,SWT.PUSH);
		btnCancel.setText("Close");
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(btnNext, 6);
		btnCancel.setLayoutData(frmData);
		
		btnIgnoreCase = new Button(sShell, SWT.CHECK);
		btnIgnoreCase.setText("&Case Sensitive");
		frmData = new FormData();
		frmData.left = new FormAttachment(5,0);
		frmData.top  = new FormAttachment(72, 0);
		btnIgnoreCase.setLayoutData(frmData);
		
		Group grpDir = new Group(sShell, SWT.SHADOW_IN);
		grpDir.setText("Direction");
		
		optForward = new Button(grpDir, SWT.RADIO);
		optForward.setText("F&orward");
		optForward.setBounds(5, 15, 75, 15);
		Button optBackward = new Button(grpDir, SWT.RADIO);
		optBackward.setBounds(5, 33, 75, 15);
		optBackward.setSelection(true);
		optBackward.setText("&Backward");

		frmData = new FormData();
		frmData.top = new FormAttachment(searchText,8);
		frmData.right  = new FormAttachment(btnNext, -7);
		frmData.bottom = new FormAttachment(100,-10);
		grpDir.setLayoutData(frmData);
		
		btnNext.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(!findText()){
					MessageBox messageBox = new MessageBox(sShell, SWT.ICON_INFORMATION | SWT.OK);
	        		messageBox.setText("Find Item");
	        		messageBox.setMessage("\""+ searchText.getText() + "\" was not found?");
	        		messageBox.open();
				}
			}
		});
		
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				sShell.dispose();
			}
		});
		
		searchText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				if(searchText.getText()!=null && searchText.getText().length()>0) btnNext.setEnabled(true);
				else btnNext.setEnabled(false);
			}
		});
		
		
		sShell.open();
		Display display = parent.getDisplay();
		while (!parent.isDisposed() && !sShell.isDisposed() && !text.isDisposed()){
			if (!display.readAndDispatch()) display.sleep();
		}
		sShell.dispose();
		
	}
		   
	private boolean findText() {
		String searchString = searchText.getText();
		String textString = text.getText();
		int offset = text.getCaretOffset();	
		int start = -1;
		
		if (!btnIgnoreCase.getSelection()) {
			searchString = searchString.toLowerCase();
			textString = textString.toLowerCase();
		}
		
		if (optForward.getSelection()) {
			start = textString.indexOf(searchString, offset);
			
		}else if (text.getSelectionRange().y > searchString.length()) {
			start = textString.lastIndexOf(searchString, offset - 1);
		}else {
			start = textString.lastIndexOf(searchString, offset - text.getSelectionRange().y - 1);
		}
		if (start > -1) {
			text.setSelection(start, start + searchString.length());
			return true;
		}
		return false;
	}
}
