/**
 * @(#)XMLWriter.java
 *
 *
 * @author 
 * @version 1.00 2009/6/22
 */

package avscience.ppc;

import java.util.*;
import java.io.*;
import avscience.wba.*;
import org.jdom.*;
import org.jdom.output.*;
///import avscience.pc.Sorter;

public class XMLWriter 
{
    private final static String pitdata = "¨Ì tEbld~155`tempProfile~37depths~4depths1~2¿Ä$depths2~2¿Ä$profile~4profile1~2¿Ä$profile2~2¿Ä$tempUnits~1C`depthUnits~1cm`|stability~1Good`serial~1nbarl1523922417828`aviPit~1false`iLayerNumber~17`measureFrom~1bottom`edited~1true`currentEditTest~1ECTN   120 4/16/2018.23:56:52`densityProfile~32depths~4depths1~2¿Ä$depths2~2¿Ä$profile~4profile1~2¿Ä$profile2~2¿Ä$depthUnits~1`densityUnits~1`|aviLoc~1crown`skiBoot~1Ski`user~3FtempUnits~1F`useSymbols~1true`state~1CO`name~1nbarlow`elvUnits~1ft`share~1true`phone~11`prof~1true`depthUnits~1cm`latType~1N`email~1nicholas.barlow@state.co.us`affil~1CAIC Forecaster`hardnessScaling~1linear`rhoUnits~1kg/cubic_m`first~1nick`last~1barlow`coordType~1Lat/Lon`measureFrom~1bottom`fractureCat~1Shear Quality`longType~1W`|windspeed~1Light Breeze`iDepth~120`surfacePen~110`heightOfSnowpack~1170`testPit~1false`crownObs~1false`airTemp~135`incline~133`skiAreaPit~1false`shearTests~2$3Esdepth~1150`depthUnits~1cm`code~1CT`quality~1Q2`dateString~14/16/2018`lengthOfColumn~10`character~1 `numberOfTaps~1`fractureCat~1Shear Quality`lengthOfCut~10`score~1CTM`s~1CTM Q2 150 4/16/2018.23:56:38`releaseType~1`ctScore~111`|3Esdepth~1120`depthUnits~1cm`code~1EC`quality~1 `dateString~14/16/2018`lengthOfColumn~10`character~1 `numberOfTaps~118`fractureCat~1Shear Quality`lengthOfCut~10`score~1ECTN`s~1ECTN   120 4/16/2018.23:56:52`releaseType~1`|3Ecomments~1 `sdepth~1120`depthUnits~1cm`code~1EC`quality~1 `lengthOfColumn~10`dateString~14/16/2018`character~1 `fractureCat~1Shear Quality`numberOfTaps~118`lengthOfCut~10`score~1ECTN`s~1ECTN   120 4/16/2018.23:57:1`releaseType~1`|loc~34zone~117T`ns~1N`state~1CO`range~1Front Range`north~10`elv~112139`name~1Butler Gulch`east~10`type~1LATLON`ew~1W`ID~1`lat~139.7567`longitude~1105.8729`|timestamp~11523922417831`version~1Version 10 - build 55 PC: Windows 10`dateString~116:23:46`windLoading~1no`precip~1None`winDir~1W`activities~2$1We skied slope.`aspect~190`pitNotes~1PST  65/100 Arr 150cm? 4-8mm (?)`sky~1sky 8/8 covered`layers~2$3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Precipitation particles`grainSize~1 `multipleHardness~1false`startDepth~1150`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1 `hardness1~14F`hsuffix2~1 `hardness2~1 `layerNumber~12`fromTop~1false`multipleGrainType~1false`multipleGrainSize~1false`comments~1dust in this layer`grainSize1~1 `waterContent~1 `endDepth~1165`|3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Wind-broken precipitation particles`grainSize~1 `multipleHardness~1false`startDepth~1120`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1 `hardness1~11F`hsuffix2~1 `hardness2~1 `layerNumber~13`fromTop~1false`multipleGrainType~1false`multipleGrainSize~1false`grainSize1~1 `waterContent~1 `endDepth~1150`|3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Rounded grains`grainSize~1 `multipleHardness~1false`startDepth~160`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1-`hardness1~1P`hsuffix2~1 `hardness2~1 `layerNumber~14`fromTop~1false`multipleGrainType~1true`multipleGrainSize~1false`grainType1~1Wind packed`grainSize1~1 `waterContent~1 `endDepth~1120`|3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Ice layer`grainSize~1 `multipleHardness~1false`startDepth~159`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1 `hardness1~1I`hsuffix2~1 `hardness2~1 `layerNumber~15`fromTop~1false`multipleGrainType~1false`multipleGrainSize~1false`grainSize1~1 `waterContent~1 `endDepth~160`|3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Wind packed`grainSize~1 `multipleHardness~1true`startDepth~120`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1-`hardness1~1K`hsuffix2~1 `hardness2~1P`layerNumber~16`fromTop~1false`multipleGrainType~1false`multipleGrainSize~1false`grainSize1~1 `waterContent~1 `endDepth~159`|3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Depth hoar`grainSize~16.0`multipleHardness~1false`startDepth~10`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1-`hardness1~1P`hsuffix2~1 `hardness2~1 `layerNumber~18`fromTop~1false`multipleGrainType~1true`multipleGrainSize~1false`grainType1~1Melt forms`grainSize1~1 `waterContent~1 `endDepth~15`|3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Large striated crystals`grainSize~14.0`multipleHardness~1false`startDepth~15`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1 `hardness1~14F`hsuffix2~1 `hardness2~1 `layerNumber~17`fromTop~1false`multipleGrainType~1false`multipleGrainSize~1true`comments~1layer was moist`grainSize1~18.0`waterContent~1Moist`endDepth~120`|3CgrainSizeUnits1~1mm`grainSizeUnits2~1mm`grainType~1Slush`grainSize~1 `multipleHardness~1false`startDepth~1165`grainSuffix~1 `multipleDensity~1false`grainSuffix1~1 `hsuffix1~1-`hardness1~1P`hsuffix2~1 `hardness2~1 `layerNumber~11`fromTop~1false`multipleGrainType~1false`multipleGrainSize~1false`grainSize1~1 `waterContent~1Very Wet`endDepth~1170`|bcPit~1false`";
    
    public static void main(String[] args)
    {
        PitObs mypit = new PitObs(pitdata);
        
    }
    
    GrainTypeConvertor gtc = GrainTypeConvertor.getInstance();
	File file = new File("/Users/mark/desktop/PITOBS.xml");
        Document doc;
	public XMLWriter(){}
    public XMLWriter(File file) 
    {
    	this.file = file;
    }
    
    public Document getDocumentFromPit(PitObs pit)
    {
        System.out.println("getDocumentFromPit()  "+pit.getSerial());
        avscience.ppc.PitObs tpit = new avscience.ppc.PitObs(pit.dataString());
  	//tpit = Sorter.sortPit(tpit);
        ///////////////
        
        System.out.println("Setting sky cover: "+pit.getSky());
        String cd = SkyCover.getInstance().getCode(pit.getSky());
        pit.setSky(cd);
        pit.attributes.put("sky", cd);
        System.out.println("sky cover code: "+cd);
        System.out.println("sky cover set to: "+pit.getSky());
                    
        String pcd = Precipitation.getInstance().getCode(pit.getPrecip());
        pit.setPrecip(pcd);
                
        String wcd = WindSpeed.getInstance().getCode(pit.getWindspeed());
        pit.setWindSpeed(wcd);
                    
        ////////////////
  		
	Element e = getElementFromObject(tpit);
	doc = new Document(e);
        return doc;
    }
    
    public void writePitToXML(avscience.ppc.PitObs pit)
  	{
  	//	avscience.ppc.PitObs pit = getPit(serial);
  		avscience.ppc.PitObs tpit = new avscience.ppc.PitObs(pit.dataString());
  		//tpit = Sorter.sortPit(tpit);
               
  		
  		System.out.println("writePitToXML");
		Element e = getElementFromObject(tpit);
		doc = new Document(e);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try
		{
		//	File f = new File(filename);
			outputter.output(doc, new FileOutputStream(file));
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
  	}
  	
  	Element addProfileFromTable(java.util.Hashtable table, Element el)
  	{
  		//int i=0;
  		java.util.Enumeration e = table.keys();
  		StringBuffer buffer = new StringBuffer();
  		while (e.hasMoreElements())
  		{
  			Object o = e.nextElement();
  			buffer.append(o);
  			buffer.append("_");
  			Object oo = table.get(o);
  			buffer.append(oo);
  			buffer.append(";");
  			
  		}
  		String s = buffer.toString();
  		String ss = s.substring(0, s.length()-1);
  		Attribute a = new Attribute("profile", ss);
  		el.setAttribute(a);
  		return el;
  	}
  	
  	
  	
  	public Element getElementFromObject(avscience.wba.AvScienceDataObject oo)
	{
		System.out.println("getElementFromObject: "+oo.toString());
		
		String name = avscience.ppc.AvScienceObjectTypes.getInstance().getName(oo.getKey());
		Element e = new Element(name);
		if ( oo instanceof PitObs )
		{
			PitObs pit = (PitObs) oo;
                        System.out.println("PitObs.");
                        System.out.println("PitObs:skyCover "+pit.getSky());
			Attribute a = new Attribute("activities", pit.getActivitiesString());
			e.setAttribute(a);
                        
		}
		
		if ( oo instanceof TempProfile )
		{
			TempProfile tp = (TempProfile) oo;
			if (tp.hasPoints())
			{
				Attribute a = new Attribute("tempUnits", tp.getTempUnits());
				e.setAttribute(a);
				Attribute aa = new Attribute("depthUnits", tp.getDepthUnits());
				e.setAttribute(aa);
				java.util.Hashtable table = (java.util.Hashtable) tp.attributes.get("profile");
				addProfileFromTable(table, e);
			}
			return e;
		}
		////////////
		if ( oo instanceof DensityProfile )
		{
			DensityProfile tp = (DensityProfile) oo;
			if (tp.hasPoints())
			{
				Attribute a = new Attribute("rhoUnits", tp.getDensityUnits());
				e.setAttribute(a);
				Attribute aa = new Attribute("depthUnits", tp.getDepthUnits());
				e.setAttribute(aa);
				java.util.Hashtable table = (java.util.Hashtable) tp.attributes.get("profile");
				addProfileFromTable(table, e);
			}
			return e;
		}
		/////////////
	/**/
                oo.setAttributes();
                java.util.Enumeration en = oo.attributes.keys();
		
		while ( en.hasMoreElements())
		{ 
			Object att = en.nextElement();
			System.out.println("att: "+att.toString());
			Object o = oo.attributes.get(att);
			
			if (o instanceof String)
			{
                            try
                            {
                                    
				Attribute a = new Attribute(att.toString(), o.toString());
				e.setAttribute(a);
                            }
                            catch(Exception ex)
                            {
                                System.out.println(ex.toString());
                            }
			}
			
                                
                       if  (o instanceof avscience.wba.Location)
			{
				Element ell = getElementFromObject((avscience.wba.AvScienceDataObject)o);
				e.addContent(ell);
			}
                       
                       if  (o instanceof avscience.wba.User)
			{
				Element ell = getElementFromObject((avscience.wba.AvScienceDataObject)o);
				e.addContent(ell);
			}
                       
			
			if ( o instanceof java.util.Vector )
			{
				java.util.Vector v = (java.util.Vector) o;
				Iterator it = v.iterator();
				while (it.hasNext())
				{
					Object ooo = it.next();
					if ( ooo instanceof avscience.wba.AvScienceDataObject)
					{
                                                if ( ooo instanceof avscience.ppc.Layer )
                                                {
                                                    avscience.ppc.Layer l = (avscience.ppc.Layer) ooo;
                                                    String gt1_desc = l.getGrainType1();
                                                    String gt2_desc = l.getGrainType2();
                                                    String gt1 = GrainTypeConvertor.getInstance().getSubTypeCode(gt1_desc);
                                                    String gt2 = GrainTypeConvertor.getInstance().getSubTypeCode(gt2_desc);
                                                    l.setGrainType1(gt1);
                                                    l.setGrainType2(gt2);
                                                    String cd = WaterContent.getInstance().getCode(l.getWaterContent());
                                                    l.setWaterContent(cd);
                                                   
                                                }
						Element elll = getElementFromObject((avscience.wba.AvScienceDataObject)ooo);
						e.addContent(elll);
					}
				}
				
			}
			
				
		}
		return e;
	}
	

  	
}