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

package be.ibridge.kettle.trans.step.addxml;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
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


/**
 * This class knows how to handle the MetaData for the XML output step
 * 
 * @since 14-jan-2006
 *
 */

public class AddXMLMeta extends BaseStepMeta  implements StepMetaInterface
{
    /** The base name of the output file */

    /** Flag: ommit the XML Header*/
    private  boolean omitXMLheader;

    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;

    /** The name value containing the resulting XML fragment */
    private String valueName;

    /** The name of the repeating row XML element */
    private String rootNode;

    /* THE FIELD SPECIFICATIONS ... */
    
    /** The output fields */
    private  XMLField outputFields[];


    public AddXMLMeta()
    {
        super(); // allocate BaseStepMeta
    }
    
    
    
    /**
     * @return Returns the zipped.
     */
    public boolean isOmitXMLheader()
    {
        return omitXMLheader;
    }



    /**
     * @param zipped The zipped to set.
     */
    public void setOmitXMLheader(boolean omitXMLheader)
    {
        this.omitXMLheader = omitXMLheader;
    }



    /**
     * @return Returns the outputFields.
     */
    public XMLField[] getOutputFields()
    {
        return outputFields;
    }
    
    /**
     * @param outputFields The outputFields to set.
     */
    public void setOutputFields(XMLField[] outputFields)
    {
        this.outputFields = outputFields;
    }
    
    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
        throws KettleXMLException
    {
        readData(stepnode);
    }

    public void allocate(int nrfields)
    {
        outputFields = new XMLField[nrfields];
    }
    
    public Object clone()
    {
        AddXMLMeta retval = (AddXMLMeta)super.clone();
        int nrfields=outputFields.length;
        
        retval.allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            retval.outputFields[i] = (XMLField) outputFields[i].clone();
        }
        
        return retval;
    }
    
    private void readData(Node stepnode)
        throws KettleXMLException
    {
        try
        {
            encoding         = XMLHandler.getTagValue(stepnode, "encoding");
            valueName      = XMLHandler.getTagValue(stepnode, "valueName");
            rootNode    = XMLHandler.getTagValue(stepnode, "xml_repeat_element");

            omitXMLheader    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "omitXMLheader"));
            
            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            int nrfields= XMLHandler.countNodes(fields, "field");
    
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
            
                outputFields[i] = new XMLField();
                outputFields[i].setFieldName( XMLHandler.getTagValue(fnode, "name") );
                outputFields[i].setElementName( XMLHandler.getTagValue(fnode, "element") );
                outputFields[i].setType( XMLHandler.getTagValue(fnode, "type") );
                outputFields[i].setFormat( XMLHandler.getTagValue(fnode, "format") );
                outputFields[i].setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") );
                outputFields[i].setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") );
                outputFields[i].setGroupingSymbol( XMLHandler.getTagValue(fnode, "group") );
                outputFields[i].setNullString( XMLHandler.getTagValue(fnode, "nullif") );
                outputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
                outputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
                outputFields[i].setAttribute( "Y".equalsIgnoreCase( XMLHandler.getTagValue(fnode, "attribute") ) );
            }
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }

    public void setDefault()
    {
        omitXMLheader    = true;
        encoding         = Const.XML_ENCODING;
        
        valueName        = "xmlvaluename";
        rootNode         = "Row";


        int nrfields=0;
        
        allocate(nrfields);
                    
        for (int i=0;i<nrfields;i++)
        {
            outputFields[i] = new XMLField();

            outputFields[i].setFieldName( "field"+i );              
            outputFields[i].setElementName( "field"+i );              
            outputFields[i].setType( "Number" );
            outputFields[i].setFormat( " 0,000,000.00;-0,000,000.00" );
            outputFields[i].setCurrencySymbol( "" );
            outputFields[i].setDecimalSymbol( "," );
            outputFields[i].setGroupingSymbol(  "." );
            outputFields[i].setNullString( "" );
            outputFields[i].setLength( -1 );
            outputFields[i].setPrecision( -1 );
            outputFields[i].setAttribute( false );
        }
    }
    
    /**
     * The only field added by this transform is the XML output value column
     */
    public Row getFields(Row r, String name, Row info) throws KettleStepException
    {
        Row row = super.getFields(r, name, info);
        
        Value v=new Value(this.getValueName(), Value.VALUE_TYPE_STRING);
        v.setOrigin(name);
        row.addValue( v );
        
        return row;
    }

    public String getXML()
    {
        StringBuffer retval=new StringBuffer();
        
        retval.append("    "+XMLHandler.addTagValue("encoding",  encoding));
        retval.append("    "+XMLHandler.addTagValue("valueName",  valueName));
        retval.append("    "+XMLHandler.addTagValue("xml_repeat_element",  rootNode));

        retval.append("    <file>"+Const.CR);
        retval.append("      "+XMLHandler.addTagValue("omitXMLheader", omitXMLheader));
        retval.append("      </file>"+Const.CR);
        retval.append("    <fields>"+Const.CR);
        for (int i=0;i<outputFields.length;i++)
        {
            XMLField field = outputFields[i];
            
            if (field.getFieldName()!=null && field.getFieldName().length()!=0)
            {
                retval.append("      <field>"+Const.CR);
                retval.append("        "+XMLHandler.addTagValue("name",      field.getFieldName()));
                retval.append("        "+XMLHandler.addTagValue("element",   field.getElementName()));
                retval.append("        "+XMLHandler.addTagValue("type",      field.getTypeDesc()));
                retval.append("        "+XMLHandler.addTagValue("format",    field.getFormat()));
                retval.append("        "+XMLHandler.addTagValue("currency",  field.getCurrencySymbol()));
                retval.append("        "+XMLHandler.addTagValue("decimal",   field.getDecimalSymbol()));
                retval.append("        "+XMLHandler.addTagValue("group",     field.getGroupingSymbol()));
                retval.append("        "+XMLHandler.addTagValue("nullif",    field.getNullString()));
                retval.append("        "+XMLHandler.addTagValue("length",    field.getLength()));
                retval.append("        "+XMLHandler.addTagValue("precision", field.getPrecision()));
                retval.append("        "+XMLHandler.addTagValue("attribute", field.isAttribute()));
                retval.append("        </field>"+Const.CR);
            }
        }
        retval.append("      </fields>"+Const.CR);

        return retval.toString();
    }
    
    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
        throws KettleException
    {
        try
        {
            encoding        =      rep.getStepAttributeString (id_step, "encoding");
            valueName     =      rep.getStepAttributeString (id_step, "valueName");
            rootNode   =      rep.getStepAttributeString (id_step, "xml_repeat_element");
            
            omitXMLheader          =      rep.getStepAttributeBoolean(id_step, "omitXMLheader");
    
            int nrfields = rep.countNrStepAttributes(id_step, "field_name");
            
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                outputFields[i] = new XMLField();

                outputFields[i].setFieldName(       rep.getStepAttributeString (id_step, i, "field_name") );
                outputFields[i].setElementName(     rep.getStepAttributeString (id_step, i, "field_element") );
                outputFields[i].setType(            rep.getStepAttributeString (id_step, i, "field_type") );
                outputFields[i].setFormat(          rep.getStepAttributeString (id_step, i, "field_format") );
                outputFields[i].setCurrencySymbol(  rep.getStepAttributeString (id_step, i, "field_currency") );
                outputFields[i].setDecimalSymbol(   rep.getStepAttributeString (id_step, i, "field_decimal") );
                outputFields[i].setGroupingSymbol(  rep.getStepAttributeString (id_step, i, "field_group") );
                outputFields[i].setNullString(      rep.getStepAttributeString (id_step, i, "field_nullif") );
                outputFields[i].setLength(     (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
                outputFields[i].setPrecision(  (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
                outputFields[i].setAttribute(       rep.getStepAttributeBoolean(id_step, i, "field_attribute") );
            }       
        }
        catch(Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step)
        throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "encoding",           encoding);
            rep.saveStepAttribute(id_transformation, id_step, "valueName",          valueName);
            rep.saveStepAttribute(id_transformation, id_step, "xml_repeat_element", rootNode);
            rep.saveStepAttribute(id_transformation, id_step, "omitXMLheader",        omitXMLheader);
            
            for (int i=0;i<outputFields.length;i++)
            {
                XMLField field = outputFields[i];
                
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field.getFieldName());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_element",   field.getElementName());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      field.getTypeDesc());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    field.getFormat());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  field.getCurrencySymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   field.getDecimalSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     field.getGroupingSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    field.getNullString());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    field.getLength());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_attribute", field.isAttribute());
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
        }
    }


    public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
    {
        CheckResult cr;
        // TODO - add checks for empty fieldnames 
        
        // Check output fields
        if (prev!=null && prev.size()>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddXMLMeta.CheckResult.FieldsReceived", ""+prev.size()), stepinfo);
            remarks.add(cr);
            
            String  error_message="";
            boolean error_found=false;
            
            // Starting from selected fields in ...
            for (int i=0;i<outputFields.length;i++)
            {
                int idx = prev.searchValueIndex(outputFields[i].getFieldName());
                if (idx<0)
                {
                    error_message+="\t\t"+outputFields[i].getFieldName()+Const.CR;
                    error_found=true;
                } 
            }
            if (error_found) 
            {
                error_message=Messages.getString("AddXMLMeta.CheckResult.FieldsNotFound", error_message);
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddXMLMeta.CheckResult.AllFieldsFound"), stepinfo);
                remarks.add(cr);
            }
        }
        
        // See if we have input streams leading to this step!
        if (input.length>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddXMLMeta.CheckResult.ExpectedInputOk"), stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AddXMLMeta.CheckResult.ExpectedInputError"), stepinfo);
            remarks.add(cr);
        }
        
        cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, Messages.getString("AddXMLMeta.CheckResult.FilesNotChecked"), stepinfo);
        remarks.add(cr);
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new AddXMLDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new AddXML(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new AddXMLData();
    }



    public String getEncoding()
    {
        return encoding;
    }


    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }


    /**
     * @return Returns the rootNode.
     */
    public String getRootNode()
    {
        return rootNode;
    }

    /**
     * @param rootNode The rootNode to set.
     */
    public void setRootNode(String repeatElement)
    {
        this.rootNode = repeatElement;
    }

    
    public String getValueName() {
        return valueName;
    }


    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
