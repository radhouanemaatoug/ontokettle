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

package be.ibridge.kettle.trans.step.insertupdate;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.DatabaseImpact;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/*
 * Created on 26-apr-2003
 *
 */
public class InsertUpdateMeta extends BaseStepMeta implements StepMetaInterface
{
    /** what's the lookup schema? */
    private String schemaName;
    
    /** what's the lookup table? */
	private String tableName;
	
    /** database connection */
	private DatabaseMeta databaseMeta;
	
    /** which field in input stream to compare with? */
	private String keyStream[];
	
    /** field in table */
	private String keyLookup[];
	
    /** Comparator: =, <>, BETWEEN, ... */
	private String keyCondition[];
	
    /** Extra field for between... */
	private String keyStream2[];
	
    /** Field value to update after lookup */
	private String updateLookup[];
	
    /** Stream name to update value with */
	private String updateStream[];
	
    /** boolean indicating if field needs to be updated */
	private Boolean update[];

	/** Commit size for inserts/updates */
	private int    commitSize;

    /** Bypass any updates */
	private boolean updateBypassed;

	public InsertUpdateMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	
	
    /**
     * @return Returns the commitSize.
     */
    public int getCommitSize()
    {
        return commitSize;
    }
    
    /**
     * @param commitSize The commitSize to set.
     */
    public void setCommitSize(int commitSize)
    {
        this.commitSize = commitSize;
    }
    
    /**
     * @return Returns the database.
     */
    public DatabaseMeta getDatabaseMeta()
    {
        return databaseMeta;
    }
    
    /**
     * @param database The database to set.
     */
    public void setDatabaseMeta(DatabaseMeta database)
    {
        this.databaseMeta = database;
    }
    
    /**
     * @return Returns the keyCondition.
     */
    public String[] getKeyCondition()
    {
        return keyCondition;
    }
    
    /**
     * @param keyCondition The keyCondition to set.
     */
    public void setKeyCondition(String[] keyCondition)
    {
        this.keyCondition = keyCondition;
    }
    
    /**
     * @return Returns the keyLookup.
     */
    public String[] getKeyLookup()
    {
        return keyLookup;
    }
    
    /**
     * @param keyLookup The keyLookup to set.
     */
    public void setKeyLookup(String[] keyLookup)
    {
        this.keyLookup = keyLookup;
    }
    
    /**
     * @return Returns the keyStream.
     */
    public String[] getKeyStream()
    {
        return keyStream;
    }
    
    /**
     * @param keyStream The keyStream to set.
     */
    public void setKeyStream(String[] keyStream)
    {
        this.keyStream = keyStream;
    }
    
    /**
     * @return Returns the keyStream2.
     */
    public String[] getKeyStream2()
    {
        return keyStream2;
    }
    
    /**
     * @param keyStream2 The keyStream2 to set.
     */
    public void setKeyStream2(String[] keyStream2)
    {
        this.keyStream2 = keyStream2;
    }
    
    /**
     * @return Returns the tableName.
     */
    public String getTableName()
    {
        return tableName;
    }
    
    /**
     * @param tableName The tableName to set.
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    
    /**
     * @return Returns the updateLookup.
     */
    public String[] getUpdateLookup()
    {
        return updateLookup;
    }
    
    /**
     * @param updateLookup The updateLookup to set.
     */
    public void setUpdateLookup(String[] updateLookup)
    {
        this.updateLookup = updateLookup;
    }
    
    /**
     * @return Returns the updateStream.
     */
    public String[] getUpdateStream()
    {
        return updateStream;
    }
    
    /**
     * @param updateStream The updateStream to set.
     */
    public void setUpdateStream(String[] updateStream)
    {
        this.updateStream = updateStream;
    }
    
	public Boolean[] getUpdate() {
		return update;
	}

	public void setUpdate(Boolean[] update) {
		this.update = update;
	}

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public void allocate(int nrkeys, int nrvalues)
	{
		keyStream          = new String[nrkeys];
		keyLookup    = new String[nrkeys];
		keyCondition = new String[nrkeys];
		keyStream2         = new String[nrkeys];
		updateLookup        = new String[nrvalues];
		updateStream    = new String[nrvalues];
		update    = new Boolean[nrvalues];
	}

	public Object clone()
	{
		InsertUpdateMeta retval = (InsertUpdateMeta)super.clone();
		int nrkeys    = keyStream.length;
		int nrvalues  = updateLookup.length;

		retval.allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			retval.keyStream         [i] = keyStream[i];
			retval.keyLookup   [i] = keyLookup[i];
			retval.keyCondition[i] = keyCondition[i];
			retval.keyStream2        [i] = keyStream2[i];
		}

		for (int i=0;i<nrvalues;i++)
		{
			retval.updateLookup[i]        = updateLookup[i];
			retval.updateStream[i]    = updateStream[i];
			retval.update[i]    = update[i];
		}
		return retval;
	}
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String csize;
			int nrkeys, nrvalues;
			
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, con);
			csize      = XMLHandler.getTagValue(stepnode, "commit"); //$NON-NLS-1$
			commitSize=Const.toInt(csize, 0);
            schemaName      = XMLHandler.getTagValue(stepnode, "lookup", "schema"); //$NON-NLS-1$ //$NON-NLS-2$
			tableName      = XMLHandler.getTagValue(stepnode, "lookup", "table"); //$NON-NLS-1$ //$NON-NLS-2$
			updateBypassed = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "update_bypassed")); //$NON-NLS-1$ //$NON-NLS-2$
	
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup"); //$NON-NLS-1$
			nrkeys    = XMLHandler.countNodes(lookup, "key"); //$NON-NLS-1$
			nrvalues  = XMLHandler.countNodes(lookup, "value"); //$NON-NLS-1$
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(lookup, "key", i); //$NON-NLS-1$
				
				keyStream         [i] = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
				keyLookup   [i] = XMLHandler.getTagValue(knode, "field"); //$NON-NLS-1$
				keyCondition[i] = XMLHandler.getTagValue(knode, "condition"); //$NON-NLS-1$
				if (keyCondition[i]==null) keyCondition[i]="="; //$NON-NLS-1$
				keyStream2        [i] = XMLHandler.getTagValue(knode, "name2"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(lookup, "value", i); //$NON-NLS-1$
				
				updateLookup[i]        = XMLHandler.getTagValue(vnode, "name"); //$NON-NLS-1$
				updateStream[i]    = XMLHandler.getTagValue(vnode, "rename"); //$NON-NLS-1$
				if (updateStream[i]==null) updateStream[i]=updateLookup[i]; // default: the same name!
				String updateValue = XMLHandler.getTagValue(vnode, "update"); //$NON-NLS-1$
				if(updateValue==null) {
					//default TRUE
					update[i] = Boolean.TRUE;
				} else 
                {
                    if (updateValue.equalsIgnoreCase("Y"))
                        update[i] = Boolean.TRUE;
                    else
                        update[i] = Boolean.FALSE; 
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("InsertUpdateMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		keyStream    = null;
		updateLookup = null;
		databaseMeta = null;
		commitSize   = 100;
        schemaName   = ""; //$NON-NLS-1$
		tableName    = Messages.getString("InsertUpdateMeta.DefaultTableName"); //$NON-NLS-1$

		int nrkeys   = 0;
		int nrvalues = 0;

		allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			keyLookup[i]   = "age"; //$NON-NLS-1$
			keyCondition[i]= "BETWEEN"; //$NON-NLS-1$
			keyStream[i]         = "age_from"; //$NON-NLS-1$
			keyStream2[i]        = "age_to"; //$NON-NLS-1$
		}

		for (int i=0;i<nrvalues;i++)
		{
			updateLookup[i]=Messages.getString("InsertUpdateMeta.ColumnName.ReturnField")+i; //$NON-NLS-1$
			updateStream[i]=Messages.getString("InsertUpdateMeta.ColumnName.NewName")+i; //$NON-NLS-1$
			update[i]=Boolean.TRUE; //$NON-NLS-1$
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
		
		retval.append("    ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    ").append(XMLHandler.addTagValue("commit", commitSize)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("update_bypassed", updateBypassed)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    <lookup>").append(Const.CR); //$NON-NLS-1$
        retval.append("      ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$

		for (int i=0;i<keyStream.length;i++)
		{
			retval.append("      <key>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name", keyStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("field", keyLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("condition", keyCondition[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("name2", keyStream2[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </key>").append(Const.CR); //$NON-NLS-1$
		}

		for (int i=0;i<updateLookup.length;i++)
		{
			retval.append("      <value>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name", updateLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("rename", updateStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("update", update[i].booleanValue())); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </value>").append(Const.CR); //$NON-NLS-1$
		}

		retval.append("    </lookup>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection");  //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase( databases, id_connection);
			
			commitSize     = (int)rep.getStepAttributeInteger(id_step, "commit"); //$NON-NLS-1$
            schemaName     =      rep.getStepAttributeString(id_step, "schema"); //$NON-NLS-1$
			tableName      =      rep.getStepAttributeString(id_step, "table"); //$NON-NLS-1$
			updateBypassed =      rep.getStepAttributeBoolean(id_step, "update_bypassed"); //$NON-NLS-1$
	
			int nrkeys   = rep.countNrStepAttributes(id_step, "key_name"); //$NON-NLS-1$
			int nrvalues = rep.countNrStepAttributes(id_step, "value_name"); //$NON-NLS-1$
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				keyStream[i]    = rep.getStepAttributeString(id_step, i, "key_name"); //$NON-NLS-1$
				keyLookup[i]    = rep.getStepAttributeString(id_step, i, "key_field"); //$NON-NLS-1$
				keyCondition[i] = rep.getStepAttributeString(id_step, i, "key_condition"); //$NON-NLS-1$
				keyStream2[i]   = rep.getStepAttributeString(id_step, i, "key_name2"); //$NON-NLS-1$
			}
			
			for (int i=0;i<nrvalues;i++)
			{
				updateLookup[i]  = rep.getStepAttributeString(id_step, i, "value_name"); //$NON-NLS-1$
				updateStream[i]  = rep.getStepAttributeString(id_step, i, "value_rename"); //$NON-NLS-1$
				update[i]        = Boolean.valueOf(rep.getStepAttributeBoolean(id_step, i, "value_update",true)); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("InsertUpdateMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "id_connection", databaseMeta==null?-1:databaseMeta.getID()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "commit",        commitSize); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "schema",        schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "table",         tableName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "update_bypassed", updateBypassed); //$NON-NLS-1$
	
			for (int i=0;i<keyStream.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "key_name",      keyStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "key_field",     keyLookup[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "key_condition", keyCondition[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "key_name2",     keyStream2[i]); //$NON-NLS-1$
			}
	
			for (int i=0;i<updateLookup.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "value_name",    updateLookup[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "value_rename",  updateStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "value_update",  update[i].booleanValue()); //$NON-NLS-1$
			}
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("InsertUpdateMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		
		if (databaseMeta!=null)
		{
			Database db = new Database(databaseMeta);
			try
			{
				db.connect();
				
				if (!Const.isEmpty(tableName))
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.TableNameOK"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
			
					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$
					
					// Check fields in table
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
					Row r = db.getTableFields(schemaTable);
					if (r!=null)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.TableExists"), stepMeta); //$NON-NLS-1$
						remarks.add(cr);
			
						for (int i=0;i<keyLookup.length;i++)
						{
							String lufield = keyLookup[i];

							Value v = r.searchValue(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("InsertUpdateMeta.CheckResult.MissingCompareFieldsInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.AllLookupFieldsFound"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);
						
						// How about the fields to insert/update in the table?
						first=true;
						error_found=false;
						error_message = ""; //$NON-NLS-1$

						for (int i=0;i<updateLookup.length;i++)
						{
							String lufield = updateLookup[i];

							Value v = r.searchValue(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("InsertUpdateMeta.CheckResult.MissingFieldsToUpdateInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.AllFieldsToUpdateFoundInTargetTable"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);
					}
					else
					{
						error_message=Messages.getString("InsertUpdateMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.StepReceivingDatas",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);
			
					boolean first=true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;
					
					for (int i=0;i<keyStream.length;i++)
					{
						Value v = prev.searchValue(keyStream[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=Messages.getString("InsertUpdateMeta.CheckResult.MissingFieldsInInput")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+keyStream[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					for (int i=0;i<keyStream2.length;i++)
					{
						if (keyStream2[i]!=null && keyStream2[i].length()>0)
						{
							Value v = prev.searchValue(keyStream2[i]);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("InsertUpdateMeta.CheckResult.MissingFieldsInInput")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+keyStream[i]+Const.CR;  //$NON-NLS-1$
							}
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.AllFieldsFoundInInput"), stepMeta); //$NON-NLS-1$
					}
					remarks.add(cr);

					// How about the fields to insert/update the table with?
					first=true;
					error_found=false;
					error_message = ""; //$NON-NLS-1$

					for (int i=0;i<updateStream.length;i++)
					{
						String lufield = updateStream[i];

						Value v = prev.searchValue(lufield);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=Messages.getString("InsertUpdateMeta.CheckResult.MissingInputStreamFields")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.AllFieldsFoundInInput2"), stepMeta); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=Messages.getString("InsertUpdateMeta.CheckResult.MissingFieldsInInput3")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				error_message = Messages.getString("InsertUpdateMeta.CheckResult.DatabaseErrorOccurred")+e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			error_message = Messages.getString("InsertUpdateMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("InsertUpdateMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("InsertUpdateMeta.CheckResult.NoInputError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, Row prev) throws KettleStepException
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!
	
		if (databaseMeta!=null)
		{
			if (prev!=null && prev.size()>0)
			{
                // Copy the row
                Row tableFields = new Row();
                
                // Now change the field names
                for (int i=0;i<updateLookup.length;i++)
                {
                    Value v = prev.searchValue(updateStream[i]);
                    if (v!=null)
                    {
                        Value tableField = new Value(v);
                        tableField.setName(updateLookup[i]);
                        tableFields.addValue(tableField);                        
                    }
                    else
                    {
                        throw new KettleStepException("Unable to find field ["+updateStream[i]+"] in the input rows");
                    }
                }
                
				if (!Const.isEmpty(tableName))
				{
                    Database db = new Database(databaseMeta);
					try
					{
						db.connect();
						
                        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
						String cr_table = db.getDDL(schemaTable, 
													tableFields, 
													null, 
													false, 
													null,
													true
													);
						
						String cr_index = ""; //$NON-NLS-1$
						String idx_fields[] = null;
						
						if (keyLookup!=null && keyLookup.length>0)
						{
							idx_fields = new String[keyLookup.length];
							for (int i=0;i<keyLookup.length;i++) idx_fields[i] = keyLookup[i];
						}
						else
						{
							retval.setError(Messages.getString("InsertUpdateMeta.CheckResult.MissingKeyFields")); //$NON-NLS-1$
						}

						// Key lookup dimensions...
						if (idx_fields!=null && idx_fields.length>0 &&  !db.checkIndexExists(schemaName, tableName, idx_fields)
						   )
						{
							String indexname = "idx_"+tableName+"_lookup"; //$NON-NLS-1$ //$NON-NLS-2$
							cr_index = db.getCreateIndexStatement(schemaTable, indexname, idx_fields, false, false, false, true);
						}
						
						String sql = cr_table+cr_index;
						if (sql.length()==0) retval.setSQL(null); else retval.setSQL(sql);
					}
					catch(KettleException e)
					{
						retval.setError(Messages.getString("InsertUpdateMeta.ReturnValue.ErrorOccurred")+e.getMessage()); //$NON-NLS-1$
					}
				}
				else
				{
					retval.setError(Messages.getString("InsertUpdateMeta.ReturnValue.NoTableDefinedOnConnection")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(Messages.getString("InsertUpdateMeta.ReturnValue.NotReceivingAnyFields")); //$NON-NLS-1$
			}
		}
		else
		{
			retval.setError(Messages.getString("InsertUpdateMeta.ReturnValue.NoConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}
	
	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev, String[] input,
			String[] output, Row info)
            throws KettleStepException
    {
        if (prev != null)
        {
            // Lookup: we do a lookup on the natural keys 
            for (int i = 0; i < keyLookup.length; i++)
            {
                Value v = prev.searchValue(keyStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), databaseMeta
                        .getDatabaseName(), tableName, keyLookup[i], keyStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                impact.add(ii);
            }

            // Insert update fields : read/write
            for (int i = 0; i < updateLookup.length; i++)
            {
                Value v = prev.searchValue(updateStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
                        .getDatabaseName(), tableName, updateLookup[i], updateStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                impact.add(ii);
            }
        }
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new InsertUpdateDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new InsertUpdate(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new InsertUpdateData();
	}

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (databaseMeta!=null) 
        {
            return new DatabaseMeta[] { databaseMeta };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }



	/**
	 * @return Returns the updateBypassed.
	 */
	public boolean isUpdateBypassed()
	{
		return updateBypassed;
	}



	/**
	 * @param updateBypassed The updateBypassed to set.
	 */
	public void setUpdateBypassed(boolean updateBypassed)
	{
		this.updateBypassed = updateBypassed;
	}

    
    public Row getRequiredFields() throws KettleException
    {
        if (databaseMeta!=null)
        {
            Database db = new Database(databaseMeta);
            try
            {
                db.connect();
                
                if (!Const.isEmpty(tableName))
                {
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);

                    // Check if this table exists...
                    if (db.checkTableExists(schemaTable))
                    {
                        return db.getTableFields(schemaTable);
                    }
                    else
                    {
                        throw new KettleException(Messages.getString("InsertUpdateMeta.Exception.TableNotFound"));
                    }
                }
                else
                {
                    throw new KettleException(Messages.getString("InsertUpdateMeta.Exception.TableNotSpecified"));
                }
            }
            catch(Exception e)
            {
                throw new KettleException(Messages.getString("InsertUpdateMeta.Exception.ErrorGettingFields"), e);
            }
            finally
            {
                db.disconnect();
            }
        }
        else
        {
            throw new KettleException(Messages.getString("InsertUpdateMeta.Exception.ConnectionNotDefined"));
        }

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
}