package be.ibridge.kettle.trans.step.XMLInputSax;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import be.ibridge.kettle.core.exception.KettleValueException;
/**
 * Parse XML document using SAX and retreive fields
 * @author Youssef
 * @since 22-may-2006
 */
public class XMLInputSaxFieldRetreiver extends DefaultHandler{
    
    ArrayList fields;
    
    int [] position={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
    
    //list of elements to the root element
    private ArrayList pathToRootElement=new ArrayList();
    
    //list of elements to the root element
    private ArrayList _pathToRootElement=new ArrayList(); 
    
    //count the deep to the current element in pathToStartElement
    private int counter=0;
    
    //count the deep to the current element in xml file
    private int _counter=-1;
    
    //true when the root element is reached
    private boolean rootFound = false;
    
    //source xml file name
    private String sourceFile;
    
    private XMLInputSaxMeta meta;
    
    //private String tempVal;
    
    
    public XMLInputSaxFieldRetreiver(String sourceFile, XMLInputSaxMeta meta)
    {
    	for(int i=0;i<meta.getInputPosition().length;i++)
    	{
    		this.pathToRootElement.add(meta.getInputPosition()[i]);
    	}
    	this.meta=meta;
        this.sourceFile=sourceFile;
        fields = new ArrayList();
    }
    
    public ArrayList getFields() 
    {
        parseDocument();
        return this.fields;
    }
    
    private void parseDocument() 
    {
        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {            
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            //parse the file and also register this class for call backs
            sp.parse(sourceFile, this);
            
        }catch(SAXException se) {
            se.printStackTrace();
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    
    private void counterUp()
    {
        if (counter==pathToRootElement.size()-1){
            rootFound=true;
            counter++;
        } else{
            counter++;
        }
    }
    
    private boolean comparePaths(int count)
    {
    	for (int i=0; i<=count; i++)
    	{
    		if(!((XMLInputSaxFieldPosition)pathToRootElement.get(i)).equals((XMLInputSaxFieldPosition)pathToRootElement.get(i)))
    		{
    			return false;
    		}
    	}
    	return true;
    }
    
    private void counterDown()
    {
        if((counter-1==_counter)&& comparePaths(_counter))
        {
            _pathToRootElement.remove(_counter);
            counter--;
            _counter--;
            rootFound=false;
        } 
        else
        {
            _pathToRootElement.remove(_counter);
            _counter--;
        }
    }
    
    private String naming(XMLInputSaxFieldPosition[] path){
    	String ret="";
    	for(int i=pathToRootElement.size();i<path.length;i++){
    		String name;
    		if(path[i].getType()==XMLInputSaxFieldPosition.XML_ELEMENT_ATT)
    		{
    			name = path[i].getAttributeValue();
    		}
    		else
    		{
    			name = path[i].getName()+path[i].getElementNr();
    		}
    		if(i>pathToRootElement.size())
    		{
    			ret+="_"+name;
    		}
    		else
    		{
    			ret+=name;
    		}
    	}
    	return ret;
    }
    
    
        
    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
    {
        //set the _counter level
        position[_counter+1]+=1;
        _counter++;
        try {
			if(!rootFound)
			{
			    XMLInputSaxFieldPosition el=(XMLInputSaxFieldPosition)pathToRootElement.get(counter);
			    if((counter==_counter) && qName.equalsIgnoreCase(el.getName()))
			    {
			        if (el.getType()==XMLInputSaxFieldPosition.XML_ELEMENT_ATT)
			        {
			            String att1=attributes.getValue(el.getAttribute()); // must throw exception
			            String att2=el.getAttributeValue();
			            if(att1.equals(att2))
			            {                        
			                _pathToRootElement.add(new XMLInputSaxFieldPosition(qName,el.getAttribute(),el.getAttributeValue()));  // to test with clone
			                if(counter==pathToRootElement.size()-1)
			                {
			                    int i=0;
			                    while(i<attributes.getLength())
			                    {
			                    	XMLInputSaxFieldPosition tempP=new XMLInputSaxFieldPosition(attributes.getQName(i),XMLInputSaxFieldPosition.XML_ATTRIBUTE,i+1);
			                    	_pathToRootElement.add(tempP);
			                    	XMLInputSaxFieldPosition[] path=new XMLInputSaxFieldPosition[_pathToRootElement.size()];
			                    	_pathToRootElement.toArray(path);
			                    	_pathToRootElement.remove(_pathToRootElement.size()-1);
			                    	XMLInputSaxField tempF=new XMLInputSaxField(tempP.getName(), path);
			                        if(!fields.contains(tempF))
			                        {
			                            fields.add(tempF);
			                        }
			                        i++;
			                    }
			                }
			                counterUp();
			            }
			            else
			            {
			                _pathToRootElement.add(new XMLInputSaxFieldPosition(qName,XMLInputSaxFieldPosition.XML_ELEMENT_POS,position[_counter]+1));
			            }
			        } 
			        else
			        {
			        	_pathToRootElement.add(new XMLInputSaxFieldPosition(qName,XMLInputSaxFieldPosition.XML_ELEMENT_POS,position[_counter]+1));
			            counterUp();
			        }
			    }
			    else
			    {
			    	_pathToRootElement.add(new XMLInputSaxFieldPosition(qName,XMLInputSaxFieldPosition.XML_ELEMENT_POS,position[_counter]+1));
			    }
			} 
			else
			{
				XMLInputSaxField temp=null;
			    if(attributes.getValue(meta.getDefiningAttribute(qName))==null)
			    {
			    	_pathToRootElement.add(new XMLInputSaxFieldPosition(qName,XMLInputSaxFieldPosition.XML_ELEMENT_POS,position[_counter]+1));
			    	XMLInputSaxFieldPosition[] path=new XMLInputSaxFieldPosition[_pathToRootElement.size()];
			    	_pathToRootElement.toArray(path);
			    	temp = new XMLInputSaxField(naming(path),path);
			    }
			    else
			    {
			    	String attribute = meta.getDefiningAttribute(qName);
			    	_pathToRootElement.add(new XMLInputSaxFieldPosition(qName, attribute, attributes.getValue(attribute)));
			    	XMLInputSaxFieldPosition[] path=new XMLInputSaxFieldPosition[_pathToRootElement.size()];
			    	_pathToRootElement.toArray(path);
			    	temp = new XMLInputSaxField(naming(path),path);
			    }
			    
			    if(!fields.contains(temp))
			    {
			        fields.add(temp);
			    }
			}
		} catch (KettleValueException e) {
			e.printStackTrace();
			throw new SAXException(_counter+","+counter+((XMLInputSaxFieldPosition)_pathToRootElement.get(_pathToRootElement.size()-1)).toString(),e);
			
		}
    }
    
    
    public void characters(char[] ch, int start, int length) throws SAXException {
        //tempVal = new String(ch,start,length);
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        position[_counter+1]=-1;
        counterDown();        
    }
    
    /*public static void main(String[] args){
    XMLvInputFieldPosition[] path=new XMLvInputFieldPosition[3];
    try {
		path[0]=new XMLvInputFieldPosition("Ep=raml");
		path[1]=new XMLvInputFieldPosition("Ep=cmData");
		path[2]=new XMLvInputFieldPosition("Ea=managedObject/class:BTS");
	} catch (KettleValueException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    //System.out.println(new xmlElement("hello","hello","hello").equals(new xmlElement("hello","hello","hello")));
    XMLvSaxFieldRetreiver spe = new XMLvSaxFieldRetreiver("D:\\NOKIA\\Project\\Ressources\\CASA-1.XML",path,"name");
    ArrayList l=spe.getFields();
    System.out.println(l.size());
    for(int i=0;i<l.size();i++){
    	System.out.println(((XMLvInputField)l.get(i)).getFieldPositionsCode(3));
    }
}*/
}