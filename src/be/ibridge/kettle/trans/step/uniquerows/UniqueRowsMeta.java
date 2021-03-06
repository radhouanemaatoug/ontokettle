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
package be.ibridge.kettle.trans.step.uniquerows;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/*
 * Created on 02-jun-2003
 *
 */

public class UniqueRowsMeta extends BaseStepMeta implements StepMetaInterface
{
    /**Indicate that we want to count the number of doubles*/
	private boolean countRows;
	
	/**The fieldname that will contain the number of doubles*/
	private String  countField;
	
	/**The fields to compare for double, null means all*/
	private String compareFields[];

    /**The fields to compare for double, null means all*/
    private boolean caseInsensitive[];

	public UniqueRowsMeta()
	{
		super(); // allocate BaseStepMeta
	}

	/**
     * @return Returns the countRows.
     */
    public boolean isCountRows()
    {
        return countRows;
    }
    
    /**
     * @param countRows The countRows to set.
     */
    public void setCountRows(boolean countRows)
    {
        this.countRows = countRows;
    }
    
    /**
     * @return Returns the countField.
     */
    public String getCountField()
    {
        return countField;
    }
    
    /**
     * @param countField The countField to set.
     */
    public void setCountField(String countField)
    {
        this.countField = countField;
    }
    
    /**
     * @param compareField The compareField to set.
     */
    public void setCompareFields(String[] compareField)
    {
        this.compareFields = compareField;
    }
    
    /**
     * @return Returns the compareField.
     */
    public String[] getCompareFields()
    {
        return compareFields;
    }
    
	public void allocate(int nrfields)
	{
		compareFields = new String[nrfields];
        caseInsensitive = new boolean[nrfields];
	}

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		UniqueRowsMeta retval = (UniqueRowsMeta) super.clone();
		
		int nrfields   = compareFields.length;
		
		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.getCompareFields()[i] = compareFields[i]; 
            retval.getCaseInsensitive()[i] = caseInsensitive[i];
		}

		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			countRows = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "count_rows")); //$NON-NLS-1$ //$NON-NLS-2$
			countField = XMLHandler.getTagValue(stepnode, "count_field"); //$NON-NLS-1$

			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int nrfields   = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				compareFields[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
                caseInsensitive[i] = !"N".equalsIgnoreCase( XMLHandler.getTagValue(fnode, "case_insensitive") ); //$NON-NLS-1$ //$NON-NLS-2$
			}

		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("UniqueRowsMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		countRows=false;
		countField=""; //$NON-NLS-1$
		
		int nrfields = 0;
		
		allocate(nrfields);		
		
		for (int i=0;i<nrfields;i++)
		{
			compareFields[i] = "field"+i; //$NON-NLS-1$
            caseInsensitive[i] = true;
		}
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		if (countRows)
		{
			Value v = new Value(countField, Value.VALUE_TYPE_INTEGER);
			v.setLength(9,0);
			v.setOrigin(name);
			r.addValue(v);
		}

		return row;
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();

		retval.append("      "+XMLHandler.addTagValue("count_rows",  countRows)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("count_field", countField)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    <fields>"); //$NON-NLS-1$
		for (int i=0;i<compareFields.length;i++)
		{
			retval.append("      <field>"); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name",  compareFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        "+XMLHandler.addTagValue("case_insensitive",  caseInsensitive[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </field>"); //$NON-NLS-1$
		}
		retval.append("      </fields>"); //$NON-NLS-1$

		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			countRows  = rep.getStepAttributeBoolean(id_step, "count_rows"); //$NON-NLS-1$
			countField = rep.getStepAttributeString (id_step, "count_fields"); //$NON-NLS-1$
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				compareFields[i] = rep.getStepAttributeString (id_step, i, "field_name"); //$NON-NLS-1$
                caseInsensitive[i] = rep.getStepAttributeBoolean(id_step, i, "case_insensitive", true); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("UniqueRowsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "count_rows",    countRows); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "count_fields",  countField); //$NON-NLS-1$

			for (int i=0;i<compareFields.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name", compareFields[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "case_insensitive", caseInsensitive[i]); //$NON-NLS-1$
			}
		}
		catch(KettleException e)
		{
			throw new KettleException(Messages.getString("UniqueRowsMeta.Exception.UnableToSaveStepInfo"), e); //$NON-NLS-1$
		}
	}

	
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;

		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("UniqueRowsMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("UniqueRowsMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new UniqueRowsDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new UniqueRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new UniqueRowsData();
	}

    /**
     * @return Returns the caseInsensitive.
     */
    public boolean[] getCaseInsensitive()
    {
        return caseInsensitive;
    }

    /**
     * @param caseInsensitive The caseInsensitive to set.
     */
    public void setCaseInsensitive(boolean[] caseInsensitive)
    {
        this.caseInsensitive = caseInsensitive;
    }

}
