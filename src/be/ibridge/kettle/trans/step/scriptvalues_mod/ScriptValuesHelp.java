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

import java.io.InputStream;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class ScriptValuesHelp {

	private static Document dom;
	private static Hashtable hatFunctionsList;
	
	
	public ScriptValuesHelp(String strFileName) throws KettleXMLException {
		super();
		xparseXmlFile(strFileName);
		buildFunctionList();
		
	}
	public Hashtable getFunctionList(){
		return hatFunctionsList;
	}
	
	private static void buildFunctionList(){
		hatFunctionsList = new Hashtable();
		NodeList nlFunctions = dom.getElementsByTagName("jsFunction");
		for(int i=0;i<nlFunctions.getLength();i++){
			String strFunctionName = nlFunctions.item(i).getAttributes().getNamedItem("name").getNodeValue();
			Node elType = ((Element)nlFunctions.item(i)).getElementsByTagName("type").item(0);
			String strType = "";
			if(elType.hasChildNodes()) strType = elType.getFirstChild().getNodeValue();
			NodeList nlFunctionArgs=((Element)nlFunctions.item(i)).getElementsByTagName("argument");
			for(int j=0;j<nlFunctionArgs.getLength();j++){
				String strFunctionArgs=nlFunctionArgs.item(j).getFirstChild().getNodeValue();
				hatFunctionsList.put(strFunctionName +"("+strFunctionArgs+")", strType);
			}
			if(nlFunctionArgs.getLength()==0) hatFunctionsList.put(strFunctionName +"()", strType);
		}
	}
	
	public String getSample(String strFunctionName, String strFunctionNameWithArgs){
		String sRC="// Sorry, no Script available for "+ strFunctionNameWithArgs;
		
		NodeList nl = dom.getElementsByTagName("jsFunction");
		for(int i=0;i<nl.getLength();i++){
			if(nl.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(strFunctionName)){
				Node elSample = ((Element)nl.item(i)).getElementsByTagName("sample").item(0);
				if(elSample.hasChildNodes()) return(elSample.getFirstChild().getNodeValue());
			}
		}
		return sRC;
	}
	
	private static void xparseXmlFile(String strFileName) throws KettleXMLException{
        try
        {
            InputStream is = ScriptValuesHelp.class.getResourceAsStream(strFileName);
            int c;
            StringBuffer buffer = new StringBuffer();
            while ( (c=is.read())!=-1 ) buffer.append((char)c);
            is.close();
            dom = XMLHandler.loadXMLString(buffer.toString());
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to read script values help file from file ["+strFileName+"]", e);
        }
	}	
}