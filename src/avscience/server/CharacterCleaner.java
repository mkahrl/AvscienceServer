package avscience.server;
import avscience.util.*;
import java.util.*;
import avscience.pc.PitObs;
import avscience.wba.*;
import avscience.pc.Layer;
import avscience.pc.Location;

public class CharacterCleaner
{
	public avscience.wba.PitObs cleanStrings(avscience.wba.PitObs pit)
    {
    	avscience.util.Enumeration lays = pit.getLayers();
    	while ( lays.hasMoreElements() )
    	{
    		avscience.wba.Layer l = (avscience.wba.Layer) lays.nextElement();
    		String coms = l.getComments();
    		coms = cleanString(coms);
    		l.setComments(coms);
    		pit.updateCurrentEditLayer(l);
    	}
    	avscience.util.Enumeration tests = pit.getShearTests();
    	while ( tests.hasMoreElements() )
    	{
    		ShearTestResult test = (ShearTestResult) tests.nextElement();
    		String coms = test.getComments();
    		coms = cleanString(coms);
    		test.setComments(coms);
    		pit.updateCurrentTestResult(test);
    	}
    	avscience.wba.Location l = pit.getLocation();
    	String nm = cleanString(l.getName());
    	l.setName(nm);
    	String st = cleanString(l.getState());
    	l.setState(st);
    	String rng = cleanString(l.getRange());
    	l.setRange(rng);
    	String lat = cleanString(l.getLat());
    	l.setLat(lat);
    	String lon = cleanString(l.getLongitude());
    	l.setLongitude(lon);
    	pit.setLocation(l);
    	String nts = cleanString(pit.getPitNotes());
    	pit.setPitNotes(nts);
    	
    	return pit;
    }
    
    public avscience.ppc.PitObs cleanStrings(avscience.ppc.PitObs pit)
    {
    	java.util.Enumeration lays = pit.getLayers();
    	while ( lays.hasMoreElements() )
    	{
    		avscience.ppc.Layer l = (avscience.ppc.Layer) lays.nextElement();
    		String coms = l.getComments();
    		coms = cleanString(coms);
    		l.setComments(coms);
    		pit.updateCurrentEditLayer(l);
    	}
    	java.util.Enumeration tests = pit.getShearTests();
    	while ( tests.hasMoreElements() )
    	{
    		avscience.ppc.ShearTestResult test = (avscience.ppc.ShearTestResult) tests.nextElement();
    		String coms = test.getComments();
    		coms = cleanString(coms);
    		test.setComments(coms);
    		pit.updateCurrentTestResult(test);
    	}
    	avscience.wba.Location l = pit.getLocation();
    	String nm = cleanString(l.getName());
    	l.setName(nm);
    	String st = cleanString(l.getState());
    	l.setState(st);
    	String rng = cleanString(l.getRange());
    	l.setRange(rng);
    	String lat = cleanString(l.getLat());
    	l.setLat(lat);
    	String lon = cleanString(l.getLongitude());
    	l.setLongitude(lon);
    	pit.setLocation(l);
    	String nts = cleanString(pit.getPitNotes());
    	pit.setPitNotes(nts);
    	
    	return pit;
    }
    
    public avscience.pc.PitObs cleanStrings(avscience.pc.PitObs pit)
    {
    	PitObs p = new PitObs();
    	if (pit!=null) p = new PitObs(pit.dataString());
    	String data = p.dataString();
    	return new avscience.pc.PitObs(data);
    }
    
   /* public avscience.wba.AvOccurence cleanStrings(avscience.wba.AvOccurence occ)
    {
    	avscience.util.Hashtable atts = occ.attributes;
    	avscience.util.Enumeration e = atts.elements();
    	avscience.util.Enumeration keys = atts.keys();
    	while ( e.hasMoreElements())
    	{
    		String key = (String) keys.nextElement();
    		Object o = e.nextElement();
    		if ( o instanceof String )
    		{
    			String s = (String) o;
    			s = cleanString(s);
    			occ.attributes.put(key, s);
    		}
    	}
    	occ.getAttributes();
    	return occ;
    }*/
    
    /*public avscience.ppc.AvOccurence cleanStrings(avscience.ppc.AvOccurence occ)
    {
    	avscience.util.Hashtable atts = occ.attributes;
    	avscience.util.Enumeration e = atts.elements();
    	avscience.util.Enumeration keys = atts.keys();
    	while ( e.hasMoreElements())
    	{
    		String key = (String) keys.nextElement();
    		Object o = e.nextElement();
    		if ( o instanceof String )
    		{
    			String s = (String) o;
    			s = cleanString(s);
    			occ.attributes.put(key, s);
    		}
    	}
    	occ.getAttributes();
    	return occ;
    }*/
    
    public  String cleanString(String s)
    {
    	//System.out.println("cleanString");
    	java.util.Vector goodChars = new java.util.Vector();
    	char[] chars = s.toCharArray();
    	int els=0;
    	for ( int i = 0; i < chars.length; i++ )
    	{
    		int cc = (int) chars[i];
    	//	if (cc==0) System.out.println("removing char.");
    		if ( cc > 0 )
    		{
    			goodChars.add(els, new Character(chars[i]));
    			els++;
    		}
    	}
    	
    	char[] res = new char[els];
    	java.util.Enumeration e = goodChars.elements();
    	int idx=0;
    	while ( e.hasMoreElements() )
    	{
    		Character C = (Character) e.nextElement();
    		res[idx] = C.charValue();
    		idx++;
    	}
    	String result = new String(res);
    //	System.out.println("Clean String: "+result);
    	return result;
    }
}