package avscience.server;

import java.sql.*;
import java.util.*;
import avscience.pda.Integer;
import avscience.wba.*;
import javax.naming.*;
import javax.sql.*;
//import org.gjt.mm.mysql.jdbc2.Blobber;
import java.net.*;
import avscience.web.*;
import java.io.*;
import avscience.ppc.*;
import avscience.pc.Location;

public class DAO {

    private final static String DBUrl = "jdbc:mysql:///avscience";

    java.util.Date startDate = new java.util.Date(114, 4, 19);

    public DAO() {
        System.out.println("DAO() StartDate: " + startDate.toString());
        loadDriver();
    }

    public static void setNewsProps(String newNews) {

        Properties props = getNewsProps();
        props.setProperty("current_news", newNews);
        try {
            File file = new File("news.properties");
            FileOutputStream fout = new FileOutputStream(file);
            props.save(fout, "new_change");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    static Properties getNewsProps() {
        Properties props = new Properties();
        try {
            File file = new File("news.properties");
            FileInputStream fin = new FileInputStream(file);
            props.load(fin);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return props;
    }

    public String getNews() {
        System.out.println("getNews()");
        Properties props = getNewsProps();
        String news = props.getProperty("current_news");
        return news;
    }

    /////////////////
    public void addPitXML(long serial, String xml) {
        System.out.println("addPitXML");
        String query = "UPDATE PIT_TABLE SET PIT_XML = ? WHERE SERIAL =?";
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, xml);
            ps.setLong(2, serial);
            int i = ps.executeUpdate();
            if (i > 0) {
                System.out.println("PIT XML added.");
            } else {
                System.out.println("PIT XML NOT added!!");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    ///////////////////
    public void writeAllPitsToXML() {
        System.out.println("writeAllPitsToXML()");
        String query = "SELECT serial FROM PIT_TABLE";
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                long serial = rs.getLong("serial");
                String s = serial + "";
                System.out.println("Getting pit: " + serial);
                String spit = getPPCPit(s);
                System.out.println("Got pit: " + serial);
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(spit);
                System.out.println("writing pit xml..");
                try {
                    XMLWriter writer = new XMLWriter();
                    //String xml = writer.getXML(pit);
                    System.out.println("adding pit xml.....");
                    //addPitXML(serial, xml);
                } catch (Exception exx) {
                    System.out.println(exx.toString());
                }

            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
	/////////////////////
    ///////////////////

    public void updateLayers() {
        String query = "SELECT serial FROM PIT_TABLE";
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                long serial = rs.getLong("serial");
                String s = serial + "";
                System.out.println("Getting pit: " + serial);
                String spit = getPPCPit(s);
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(spit);
                updatePitLayers(pit);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /////
    public void updatePitLayers(avscience.ppc.PitObs pit) {
        System.out.println("updatePitLayers()");
        if (pit == null) {
            System.out.println("PIT IS NULL");
            return;
        }
        System.out.println("updatePitLayers()");
        avscience.ppc.User u = pit.getUser();
        if (u == null) {
            System.out.println("USER IS NULL.");
        }
        boolean fromTop = false;
        if (u.getMeasureFrom() == null) {
            System.out.println("measure from null.");
            return;
        }
        if (u.getMeasureFrom().equals("top")) {
            fromTop = true;
        } else {
            fromTop = false;
        }
		//java.util.Enumeration layers = pit.getLayers();
        //	while ( layers.hasMoreElements() )

        String[] lnames = pit.getLayerStrings();
        for (int i = 0; i < lnames.length; i++) {
            System.out.println("Layer String: " + lnames[i]);
            String lstring = lnames[i];
            avscience.ppc.Layer l = pit.getLayerByString(lstring);
            if (l != null) {
                l.setFromTop(fromTop);
                //l.swapHardness();
                pit.updateCurrentEditLayer(l);
            }

        }
    }
	///

    public boolean checkBuild(avscience.ppc.PitObs pit) {
        int bld = pit.getBuild();
        /// build # on whick pit changed to top/bottom instead of start/end
        if (bld >= 563) {
            return true;
        } else {
            return false;
        }
    }

    public void writeSLFLayersToFile() {
        System.out.println("writeSLFLayersToFile()");
        File pitfile = new File("/Users/mark/SLFLayers.csv");
        StringBuffer buffer = new StringBuffer();
        buffer.append("OBS_DATE;ASPECT;LAT;LONG;PROFILE_ID;ELEVATION;SNOW_DEPTH;GRAIN_TYPE1;GRAIN_TYPE2;HARD1;HARD2;SZ_SNOW1;SZ_SNOW2;DENSITY;DEPTH_TOP;DEPTH_BOTTOM;WET1 \n");
        Hashtable pits = getAllPits();
        Enumeration e = pits.keys();

        while (e.hasMoreElements()) {
            String serial = (String) e.nextElement();
            String dat = (String) pits.get(serial);
            avscience.ppc.PitObs pit = new avscience.ppc.PitObs(dat);
            avscience.wba.Location loc = pit.getLocation();
            avscience.wba.User u = pit.getUser();
            if (u == null) {
                u = new avscience.ppc.User();
            }
            java.util.Enumeration layers = pit.getLayers();
            while (layers.hasMoreElements()) {
                String ds = new java.util.Date(pit.getTimestamp()).toString();
                buffer.append(ds + ";");
                buffer.append(pit.getAspect() + ";");
                buffer.append(loc.getLat() + " " + loc.getLatType() + ";");
                buffer.append(loc.getLongitude() + " " + loc.getLongType() + ";");
                buffer.append(serial + ";");
                String selv = loc.getElv();
                int elv = 0;
                if (selv != null) {
                    elv = stringToInt(selv);
                }
                if (u.getElvUnits() != null) {
                    if (u.getElvUnits().equals("ft")) {
                        elv = (int) (elv / 3.27);
                    }
                }
                buffer.append(elv + ";");
                String s = pit.getHeightOfSnowpack();
                int ht = stringToInt(s);
                if (u.getDepthUnits().equals("inches")) {
                    ht = (int) (ht * 2.54);
                }
                buffer.append(ht + ";");

                Object o = layers.nextElement();
                avscience.ppc.Layer layer = new avscience.ppc.Layer();

                if (o != null) {
                    StringSerializable slayer = (StringSerializable) o;
                    if (slayer != null) {
                        try {
                            layer = new avscience.ppc.Layer(slayer.dataString());
						///	buffer.append(layer.getGrainType1()+";"+layer.getGrainType2()+";");
                            ////if (layer!=null) layerBuffer.append(serial+", "+layer.getStartDepth()+", "+layer.getEndDepth()+", "+layer.getHardness1()+", "+layer.getHSuffix1()+", "+layer.getHardness2()+", "+layer.getHSuffix2()+", "+layer.getGrainType1()+", "+layer.getGrainType2()+", "+layer.getGrainSize1()+", "+layer.getGrainSize2()+", "+layer.getGrainSizeUnits1()+", "+layer.getGrainSizeUnits2()+", "+layer.getDensity1()+", "+layer.getDensity2()+", "+layer.getWaterContent()+"\n");
                        } catch (Throwable t) {
                        }
                    }
                }

                buffer.append(layer.getGrainType1() + ";" + layer.getGrainType2() + ";");
                buffer.append(layer.getHardness1() + layer.getHSuffix1() + ";");
                buffer.append(layer.getHardness2() + layer.getHSuffix2() + ";");
                buffer.append(layer.getGrainSize1() + layer.getGrainSuffix() + ";");
                buffer.append(layer.getGrainSize2() + layer.getGrainSuffix1() + ";");
                String rho = layer.getDensity();
                float rh = stringToFloat(rho);
                String runits = u.getRhoUnits();
                if (runits.equals("lbs/cubic_ft")) {
                    rh = (float) (rh / 16.0184634);
                }
                buffer.append(rho + ";");

                double tdpth = layer.getTopDepth();
                if (u.getDepthUnits().equals("inches")) {
                    tdpth = (int) (tdpth * 2.54);
                }
                buffer.append(tdpth + ";");

                double bdpth = layer.getBottomDepth();
                if (u.getDepthUnits().equals("inches")) {
                    bdpth = (int) (bdpth * 2.54);
                }
                buffer.append(bdpth + ";");

                buffer.append(layer.getWaterContent() + "\n");
            }
        }

        FileOutputStream out = null;
        PrintWriter writer = null;

        try {
            out = new FileOutputStream(pitfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void writeECPTTestPits() {
        File pitfile = new File("/Users/markkahrl/ISSW2014_PitData.csv");
        File joinfile = new File("/Users/markkahrl/JoinData.csv");
        File testfile = new File("/Users/markkahrl/CT_TestData.csv");
        StringBuffer pitBuffer = new StringBuffer();
        StringBuffer joinBuffer = new StringBuffer();
        StringBuffer testBuffer = new StringBuffer();
        pitBuffer.append("SERIAL ; OBSERVER ; LOCATION ; MTN_RANGE ; STATE ; ELV ; ELV_UOM ; LAT ; LONG ; ASPECT ; INCLINE ; PRECIP ; SKY ; WINDSPEED ; WIND_DIR ; WIND_LOADING ; AIR_TEMP ; AIR_TEMP_UOM ; STABILITY ; MEASURE_FROM ; DATE ; TIME ; DEPTH_UNITS ; DENSITY_UNITS ; PI_DEPTH \n");
        joinBuffer.append("pit_serial,ecScore , CTScore,ECT ShearQuality , CT ShearQuality, Total depth,depth to problematic layer , number of taps, Release type,length of cut ,length of column , slope angle, depth units \n");
        testBuffer.append("pit_serial, Score, CTScore \n");

        Hashtable pits = getAllPits();
        Enumeration e = pits.keys();
        while (e.hasMoreElements()) {
            String serial = (String) e.nextElement();
            String dat = (String) pits.get(serial);
            avscience.ppc.PitObs pit = new avscience.ppc.PitObs(dat);
            avscience.wba.User u = pit.getUser();
            if (u == null) {
                u = new avscience.wba.User();
            }
            avscience.wba.Location loc = pit.getLocation();
            if (loc == null) {
                loc = new avscience.wba.Location();
            }

            System.out.println("writing tests for pit: " + serial);
            java.util.Enumeration tests = pit.getShearTests();
            boolean hasCTTest = false;
            boolean hasECPTTest = false;
            while (tests.hasMoreElements()) {

                StringSerializable stest = (StringSerializable) tests.nextElement();
                avscience.ppc.ShearTestResult result = new avscience.ppc.ShearTestResult(stest.dataString());
                String code = result.getCode().trim();
                String score = result.getScore().trim();
                System.out.println("Pit:: " + serial + " code " + code + " Score: " + score);
                if (code.equals("CT")) {
                    hasCTTest = true;
                }
                if (score.equals("ECTP")) {
                    hasECPTTest = true;
                }

                //	testBuffer.append(serial+", "+result.getCode()+", "+result.getScore()+", "+result.getCTScore()+", "+result.getQuality()+", "+result.getDepth()+", "+result.getECScore()+", "+result.numberOfTaps+", "+result.releaseType+", "+result.lengthOfCut+", " +result.lengthOfColumn+"\n");
            }
            if (true) /// if (hasCTTest & hasECPTTest)  
            {
                avscience.ppc.Layer l = pit.getPILayer();

                System.out.println("writing data for pit: " + serial);
                pitBuffer.append(serial + " ; ");
                pitBuffer.append(u.getFirst() + " " + u.getLast() + " ; ");
                pitBuffer.append(loc.getName() + " ; ");
                pitBuffer.append(loc.getRange() + " ; ");
                pitBuffer.append(loc.getState() + " ; ");
                pitBuffer.append(loc.getElv() + " ; ");
                pitBuffer.append(u.getElvUnits() + " ; ");
                pitBuffer.append(loc.getLat() + " ; ");
                pitBuffer.append(loc.getLongitude() + " ; ");
                pitBuffer.append(pit.getAspect() + " ; ");
                pitBuffer.append(pit.getIncline() + " ; ");
                pitBuffer.append(pit.getPrecip() + " ; ");
                pitBuffer.append(pit.getSky() + " ; ");
                pitBuffer.append(pit.getWindspeed() + " ; ");
                pitBuffer.append(pit.getWinDir() + " ; ");
                pitBuffer.append(pit.getWindLoading() + " ; ");
                pitBuffer.append(pit.getAirTemp() + " ; ");
                pitBuffer.append(u.getTempUnits() + " ; ");
                pitBuffer.append(pit.getStability() + " ; ");
                pitBuffer.append(pit.getMeasureFrom() + " ; ");
                pitBuffer.append(pit.getDate() + " ; ");
                pitBuffer.append(pit.getTime() + " ; ");
                ///	pitBuffer.append(pit.getPitNotes()+", ");
                pitBuffer.append(u.getDepthUnits() + " ; ");

                pitBuffer.append(u.getRhoUnits() + " ;");
                pitBuffer.append(pit.iDepth + "\n");
                ////////
                System.out.println("writing tests for pit: " + serial);
                tests = pit.getShearTests();
                avscience.ppc.ShearTestResult ectTest = null;
                avscience.ppc.ShearTestResult ctTest = null;
                String numberOfTaps = null;
                String releaseType = null;
                String lengthOfCut = null;
                String lengthOfColumn = null;

                while (tests.hasMoreElements()) {
                    StringSerializable stest = (StringSerializable) tests.nextElement();
                    avscience.ppc.ShearTestResult result = new avscience.ppc.ShearTestResult(stest.dataString());
                    String code = result.getCode();
                    String score = result.getScore();
                    String rt = result.getReleaseType();
                    rt = rt.trim();
                    if (rt.length() > 0) {
                        releaseType = rt;
                    }
                    /////
                    String nt = result.numberOfTaps;
                    nt = nt.trim();
                    if (nt.length() > 0) {
                        numberOfTaps = nt;
                    }

                    String lc = result.lengthOfCut;
                    lc = lc.trim();
                    if (lc.length() > 0) {
                        lengthOfCut = lc;
                    }

                    String lcc = result.lengthOfColumn;
                    lcc = lcc.trim();
                    if (lcc.length() > 0) {
                        lengthOfColumn = lcc;
                    }

                    // int scr = new java.lang.Integer(score).intValue();
                    int ecScore = result.getECScoreAsInt();
                    int ctScore = result.getCTScoreAsInt();
                    if (ectTest != null) {
                        if (ecScore != 0) {
                            if (ecScore < ectTest.getECScoreAsInt()) {
                                ectTest = result;
                            }
                        }

                    }

                    if (score.equals("ECTP") & ectTest == null) {
                        ectTest = result;
                    }

                    if (ctTest != null) {
                        if (ctScore != 0) {
                            if (ctScore > ctTest.getCTScoreAsInt()) {
                                ctTest = result;
                            }
                        }

                    }

                    if (score.equals("ECTP") & ectTest == null) {
                        ectTest = result;
                    }
                    if (code.equals("CT") & ctTest == null) {
                        ctTest = result;
                    }

                    ////
                   if (code.equals("CT"))    testBuffer.append(serial + ", " + result.getScore() + ", " + result.getCTScore() + "\n");
                }
               // joinBuffer.append(serial+", "+ectTest.getECScore()+", "+ctTest.getCTScore()+", "+ectTest.getQuality()+", "+ctTest.getQuality()+", "+getMaxDepth(pit)+", "+pit.iDepth+", "+numberOfTaps+", "+releaseType+", "+lengthOfCut+", "+lengthOfColumn+", "+pit.getIncline()+", " +pit.getUser().getDepthUnits()+"\n");

            }
        }

        FileOutputStream out = null;
        PrintWriter writer = null;

      /*  try {
            out = new FileOutputStream(pitfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(pitBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }*/

        try {
            out = new FileOutputStream(testfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(testBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

     /*   try {
            out = new FileOutputStream(joinfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        try {
            writer.print(joinBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }*/
    }

    /////////////
    public int getMaxDepth(avscience.ppc.PitObs pit) {
        System.out.println("getMaxDepth");
        int max = 0;
        if (pit == null) {
            System.out.println("PIT IS NULL.");
            return 0;
        }

        System.out.println("max depth layers.");
        java.util.Enumeration e = null;
        if (pit.hasLayers()) {
            e = pit.getLayers();
            if (e != null) {
                while (e.hasMoreElements()) {
                    avscience.ppc.Layer l = (avscience.ppc.Layer) e.nextElement();
                    int end = l.getEndDepthInt();
                    if (end > max) {
                        max = end;
                    }
                    int start = l.getStartDepthInt();
                    if (start > max) {
                        max = start;
                    }
                }
            }
        }
        System.out.println("max depth tests.");
        if (max == 0) {
            e = pit.getShearTests();
            if (e != null) {
                while (e.hasMoreElements()) {
                    avscience.ppc.ShearTestResult result = (avscience.ppc.ShearTestResult) e.nextElement();
                    int depth = result.getDepthValueInt();
                    if (depth > max) {
                        max = depth;
                    }
                }
            }
            //	max+=6;
        }
        /* System.out.println("max depth tempprofile.");
         if ( (pit.getTempProfile()!=null) && (pit.getTempProfile().getDepths()!=null))
         {
         avscience.util.Enumeration ee = pit.getTempProfile().getDepths().elements();
		   
         while ( ee.hasMoreElements() )
         {
         avscience.pda.Integer I = (avscience.pda.Integer)ee.nextElement();
         int depth = I.intValue();
         // need to scale for temp depth??
         depth=depth*10;
         if ( depth > max ) max=depth;
         }
         }
         boolean mor=false;
         System.out.println("max depth rho profile.");
         if (( pit.getDensityProfile()!=null) && (pit.getDensityProfile().getDepths()!=null))
         {
         avscience.util.Enumeration ee = pit.getDensityProfile().getDepths().elements();
		    
         while ( ee.hasMoreElements() )
         {
         avscience.pda.Integer I = (avscience.pda.Integer)ee.nextElement();
         int depth = I.intValue();
         // need to scale for rho depth??
         depth=depth*10;
         if ( depth > max )
         {
         max=depth;
         mor=true;
         }
         }
         }*/
	    //if (mor) max+=4;

        //if ( max == 0 ) max = 60;
        return max;
    }
    //////////////

    public void writePitsToFiles() {
        System.out.println("writePitsToFiles()");
        File pitfile = new File("/Users/mark/PitData.csv");
        File layerfile = new File("/Users/mark/LayerData.csv");
        File testfile = new File("/Users/mark/TestData.csv");
        File rhofile = new File("/Users/mark/DensityData.csv");
        File tempfile = new File("/Users/mark/TempData.csv");
        File actsfile = new File("/Users/mark/ActsData.csv");

        StringBuffer pitBuffer = new StringBuffer();
        StringBuffer layerBuffer = new StringBuffer();
        StringBuffer testBuffer = new StringBuffer();
        StringBuffer rhoBuffer = new StringBuffer();
        StringBuffer tempBuffer = new StringBuffer();
        StringBuffer actBuffer = new StringBuffer();

        pitBuffer.append("SERIAL ; OBSERVER ; LOCATION ; MTN_RANGE ; STATE ; ELV ; ELV_UOM ; LAT ; LONG ; ASPECT ; INCLINE ; PRECIP ; SKY ; WINDSPEED ; WIND_DIR ; WIND_LOADING ; AIR_TEMP ; AIR_TEMP_UOM ; STABILITY ; MEASURE_FROM ; DATE ; TIME ; DEPTH_UNITS ; DENSITY_UNITS \n");
        layerBuffer.append("pit_serial, Layer start, Layer end, Hardness 1, hardness suffix1, Hardness 2, hardness suffix2, Crystal Form 1, Crystal Form 2, Crystal Size 1, Crystal Size 2, Size Units 1, Size Units 2, Density 1, Density 2, Water Content \n");
        tempBuffer.append("pit_serial, Depth, Temperature \n");
        testBuffer.append("pit_serial, Test, Score, CTScore, Shear quality, Depth, ecScore, numberOfTaps, releaseType, lengthOfCut, lengthOfColumn \n");
        rhoBuffer.append("pit_serial, Depth, Density \n");
        actBuffer.append("pit_serial ; activity \n");
        Hashtable pits = getAllPits();
        Enumeration e = pits.keys();
        while (e.hasMoreElements()) {
            String serial = (String) e.nextElement();
            String dat = (String) pits.get(serial);
            avscience.ppc.PitObs pit = new avscience.ppc.PitObs(dat);
            avscience.wba.User u = pit.getUser();
            if (u == null) {
                u = new avscience.wba.User();
            }
            avscience.wba.Location loc = pit.getLocation();
            if (loc == null) {
                loc = new avscience.wba.Location();
            }

            System.out.println("writing data for pit: " + serial);
            pitBuffer.append(serial + " ; ");
            pitBuffer.append(u.getFirst() + " " + u.getLast() + " ; ");
            pitBuffer.append(loc.getName() + " ; ");
            pitBuffer.append(loc.getRange() + " ; ");
            pitBuffer.append(loc.getState() + " ; ");
            pitBuffer.append(loc.getElv() + " ; ");
            pitBuffer.append(u.getElvUnits() + " ; ");
            pitBuffer.append(loc.getLat() + " ; ");
            pitBuffer.append(loc.getLongitude() + " ; ");
            pitBuffer.append(pit.getAspect() + " ; ");
            pitBuffer.append(pit.getIncline() + " ; ");
            pitBuffer.append(pit.getPrecip() + " ; ");
            pitBuffer.append(pit.getSky() + " ; ");
            pitBuffer.append(pit.getWindspeed() + " ; ");
            pitBuffer.append(pit.getWinDir() + " ; ");
            pitBuffer.append(pit.getWindLoading() + " ; ");
            pitBuffer.append(pit.getAirTemp() + " ; ");
            pitBuffer.append(u.getTempUnits() + " ; ");
            pitBuffer.append(pit.getStability() + " ; ");
            pitBuffer.append(pit.getMeasureFrom() + " ; ");
            pitBuffer.append(pit.getDate() + " ; ");
            pitBuffer.append(pit.getTime() + " ; ");
            ///	pitBuffer.append(pit.getPitNotes()+", ");
            pitBuffer.append(u.getDepthUnits() + " ; ");
            pitBuffer.append(u.getRhoUnits() + "\n ");

            System.out.println("writing layers for pit: " + serial);
            if (pit.hasLayers()) {
                java.util.Enumeration l = pit.getLayers();

                if (l != null) {
                    while (l.hasMoreElements()) {
                        Object o = l.nextElement();
                        if (o != null) {
                            StringSerializable slayer = (StringSerializable) o;
                            if (slayer != null) {
                                try {
                                    avscience.ppc.Layer layer = new avscience.ppc.Layer(slayer.dataString());
                                    if (layer != null) {
                                        layerBuffer.append(serial + ", " + layer.getStartDepth() + ", " + layer.getEndDepth() + ", " + layer.getHardness1() + ", " + layer.getHSuffix1() + ", " + layer.getHardness2() + ", " + layer.getHSuffix2() + ", " + layer.getGrainType1() + ", " + layer.getGrainType2() + ", " + layer.getGrainSize1() + ", " + layer.getGrainSize2() + ", " + layer.getGrainSizeUnits1() + ", " + layer.getGrainSizeUnits2() + ", " + layer.getDensity1() + ", " + layer.getDensity2() + ", " + layer.getWaterContent() + "\n");
                                    }
                                } catch (Throwable t) {
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("writing tests for pit: " + serial);
            java.util.Enumeration tests = pit.getShearTests();
            while (tests.hasMoreElements()) {
                StringSerializable stest = (StringSerializable) tests.nextElement();
                avscience.ppc.ShearTestResult result = new avscience.ppc.ShearTestResult(stest.dataString());
                testBuffer.append(serial + ", " + result.getCode() + ", " + result.getScore() + ", " + result.getCTScore() + ", " + result.getQuality() + ", " + result.getDepth() + ", " + result.getECScore() + ", " + result.numberOfTaps + ", " + result.releaseType + ", " + result.lengthOfCut + ", " + result.lengthOfColumn + "\n");
            }

            System.out.println("writing temp data for pit: " + serial);
            try {

                if (pit.hasTempProfile()) {
                    TempProfile tp = pit.getTempProfile();

                    if (tp != null) {
                        if (tp.getDepths() != null) {
                            avscience.util.Enumeration dpths = tp.getDepths().elements();
                            if (dpths != null) {
                                while (dpths.hasMoreElements()) {
                                    avscience.pda.Integer depth = (avscience.pda.Integer) dpths.nextElement();
                                    if (depth != null) {
                                        int t = tp.getTemp(depth);
                                        t = t / 10;
                                        tempBuffer.append(serial + ", " + depth.toString() + ", " + t + "\n");
                                    }

                                }
                            }

                        }
                    }

                }
            } catch (Exception ee) {
                System.out.println(ee.toString());
            }

            System.out.println("writing density for pit: " + serial);
            if (pit.hasDensityProfile()) {
                DensityProfile dp = null;
                try {
                    dp = pit.getDensityProfile();
                } catch (Throwable t) {
                }
                System.out.println("Profile gotten.");
                if (dp != null) {
                    avscience.util.Vector dprofile = null;
                    System.out.println("getting depths.");
                    try {
                        dprofile = dp.getDepths();
                    } catch (Throwable t) {
                    }

                    if ((dprofile != null) && (dprofile.size() > 0)) {
                        avscience.util.Enumeration dpths = dprofile.elements();

                        if (dpths != null) {
                            while (dpths.hasMoreElements()) {
                                Object o = dpths.nextElement();
                                if (o != null) {
                                    avscience.pda.Integer depth = (avscience.pda.Integer) o;
                                    {
                                        if (depth != null) {
                                            //System.out.println("getDensity");
                                            String rho = dp.getDensity(depth);

                                            if (rho != null) {
                                                rhoBuffer.append(serial + ", " + depth.toString() + ", " + rho + "\n");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("writing activities for pit: " + serial);
            Enumeration acts = pit.getActivities().elements();
            while (acts.hasMoreElements()) {
                actBuffer.append(serial + " ; " + acts.nextElement().toString() + "\n");
            }

        }
        FileOutputStream out = null;
        PrintWriter writer = null;

        try {
            out = new FileOutputStream(pitfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(pitBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        try {
            out = new FileOutputStream(layerfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(layerBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        try {
            out = new FileOutputStream(testfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(testBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        try {
            out = new FileOutputStream(tempfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(tempBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        try {
            out = new FileOutputStream(rhofile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(rhoBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        try {
            out = new FileOutputStream(actsfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(actBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /* public void writeUserInfoToFile()
     {
     File file = new File("UserInfo.txt");
     String query = "SELECT USERNAME, EMAIL, REAL_NAME FROM WEBUSER_TABLE";
     Statement stmt = null;
     try
     {
     stmt = getConnection();
     ResultSet rs = stmt.executeQuery(query);
     while ( rs.next())
     {
     String user = rs.get
     }
     }
     catch(Exception e){System.out.println(e.toString());}
     }*/
    public void logDownload(String target) {
        String query = "INSERT INTO DOWNLOAD_TABLE (TARGET, LOCAL_TIME) VALUES (?, ?)";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, target);
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(2, ts);
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void writePitFiles(String user) {
        String query = "SELECT PIT_NAME, USERNAME FROM PIT_TABLE WHERE USERNAME = '" + user + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                //String name = rs.getString("PIT_NAME");
                String serial = rs.getString("SERIAL");
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(getPPCPit(serial));
                writePitToFile(pit);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void fixStrings() {
        System.out.println("Fix strings:");
        String query = "SELECT PIT_DATA FROM PIT_TABLE";

        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {

                String data = rs.getString("PIT_DATA");
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                if ((pit != null) && (data != null) && (data.trim().length() > 0)) {
                    if ((pit.getName() != null) && (pit.getName().trim().length() > 0)) {
                        System.out.println("Fixing pit: " + pit.getName());
                        if (deletePit(pit)) {
                            writePitToDB(data);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    void writePitToFile(avscience.wba.PitObs pit) {
        avscience.wba.User u = pit.getUser();
        FileOutputStream out = null;
        PrintWriter writer = null;
        File file = new File("TestPitFile.txt");
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        StringBuffer buffer = new StringBuffer();
        avscience.wba.Location loc = pit.getLocation();
        buffer.append(pit.getDateString() + "\n");
        buffer.append("Observer ," + u.getFirst() + " " + u.getLast() + "\n");
        buffer.append("Location ," + loc.getName() + "\n");
        buffer.append("Mtn Range ," + loc.getRange() + "\n");
        buffer.append("State/Prov ," + loc.getState() + "\n");
        buffer.append("Elevation " + u.getElvUnits() + " ," + loc.getElv() + "\n");
        buffer.append("Lat. ," + loc.getLat() + "\n");
        buffer.append("Long. ," + loc.getLongitude() + "\n");

        Hashtable labels = getPitLabels();
        avscience.util.Hashtable atts = pit.attributes;
        Enumeration e = labels.keys();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            String v = (String) atts.get(s);
            String l = (String) labels.get(s);
            s = l + " ," + v + "\n";
            if (!(s.trim().equals("null"))) {
                buffer.append(s);
            }
        }
        buffer.append("Activities: \n");
        avscience.util.Enumeration ee = pit.getActivities().elements();
        while (ee.hasMoreElements()) {
            String s = (String) ee.nextElement();
            buffer.append(s + "\n");
        }

        if (file == null) {
            return;
        }
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    void writePitToFile(avscience.ppc.PitObs pit) {
        avscience.ppc.User u = pit.getUser();
        FileOutputStream out = null;
        PrintWriter writer = null;
        File file = new File("TestPitFile.txt");
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        StringBuffer buffer = new StringBuffer();
        avscience.wba.Location loc = pit.getLocation();
        buffer.append(pit.getDateString() + "\n");
        buffer.append("Observer ," + u.getFirst() + " " + u.getLast() + "\n");
        buffer.append("Location ," + loc.getName() + "\n");
        buffer.append("Mtn Range ," + loc.getRange() + "\n");
        buffer.append("State/Prov ," + loc.getState() + "\n");
        buffer.append("Elevation " + u.getElvUnits() + " ," + loc.getElv() + "\n");
        buffer.append("Lat. ," + loc.getLat() + "\n");
        buffer.append("Long. ," + loc.getLongitude() + "\n");

        Hashtable labels = getPitLabels();
        avscience.util.Hashtable atts = pit.attributes;
        Enumeration e = labels.keys();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            String v = (String) atts.get(s);
            String l = (String) labels.get(s);
            s = l + " ," + v + "\n";
            if (!(s.trim().equals("null"))) {
                buffer.append(s);
            }
        }
        buffer.append("Activities: \n");
        java.util.Enumeration ee = pit.getActivities().elements();
        while (ee.hasMoreElements()) {
            String s = (String) ee.nextElement();
            buffer.append(s + "\n");
        }

        if (file == null) {
            return;
        }
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    public LinkedHashMap getPitsFromQuery(String whereclause) {
        System.out.println("getPitsFromQuery(): " + whereclause);
        LinkedHashMap v = new LinkedHashMap();
        String[][] pits = getPitListArrayFromQuery(whereclause, false);

        for (int i = 0; i < pits[1].length; i++) {
            String serial = pits[1][i];
            String data = getPPCPit(serial);

            v.put(serial, data);
        }
        return v;
    }

   /// public LinkedHashMap getPitsFromQuery(String whereclause) 
    public String[][] getPitStringArrayFromQuery(String whereclause) 
    {
        System.out.println("getPitsFromQuery(): " + whereclause);
        ///LinkedHashMap v = new LinkedHashMap();
        String[][] pits = getPitListArrayFromQuery(whereclause, false);
        return pits;

        /*for (int i = 0; i < pits[1].length; i++) {
            String serial = pits[1][i];
            String data = getPPCPit(serial);

            v.put(serial, data);
        }*/
       /// return v;
    }

    Hashtable getPitLabels() {
        Hashtable attributes = new Hashtable();

        attributes.put("aspect", "Aspect");
        attributes.put("incline", "Slope Angle");
        attributes.put("precip", "Precipitation");
        attributes.put("sky", "Sky Cover");
        attributes.put("windspeed", "Wind Speed");
        attributes.put("winDir", "Wind Direction");
        attributes.put("windLoading", "Wind Loading");

        attributes.put("airTemp", "Air Temperature");
        attributes.put("stability", "Stability on simular slopes");

        attributes.put("measureFrom", "Measure from: ");

        attributes.put("date", "Date");
        attributes.put("time", "Time");
        attributes.put("pitNotes", "Notes");
        return attributes;
    }

    public String[] getUserAddreses() {
        FileOutputStream out = null;
        PrintWriter writer = null;
        File file = new File("UserInfo.txt");
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        Vector adds = new Vector();
        StringBuffer buffer = new StringBuffer();
        buffer.append("Username, Full Name, Email   \n\n");
        String query = "SELECT * FROM WEBUSER_TABLE";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String email = rs.getString("EMAIL");
                String user = rs.getString("USERNAME");
                String name = rs.getString("REAL_NAME");
                if (email == null) {
                    email = "";
                }
                if (user == null) {
                    user = "";
                }
                if (name == null) {
                    name = "";
                }
                if (email.trim().length() > 5) {
                    adds.add(email.trim());
                    buffer.append(user + ";");
                    buffer.append(name + ";");
                    buffer.append(email + " \n");
                }
                // System.out.println("email: "+email);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        String[] ads = new String[adds.size()];
        Enumeration e = adds.elements();
        int i = 0;
        while (e.hasMoreElements()) {
            ads[i] = (String) e.nextElement();
            i++;
        }
        return ads;
    }

    public void getEmailsAsCSVFile() {
        FileOutputStream out = null;
        PrintWriter writer = null;
        File file = new File("UserEmails.txt");
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        StringBuffer buffer = new StringBuffer();
        String query = "SELECT EMAIL FROM WEBUSER_TABLE";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String email = rs.getString("EMAIL");
                if (email == null) {
                    email = "";
                }
                if (email.trim().length() > 5) {
                    buffer.append(email + ", \n ");
                }

            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

    }

    private void loadDriver() {
        System.out.println("Load Driver..");
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.out.println("Unable to Load Driver: " + e.toString());
        }
    }

    private Connection getConnection() throws SQLException {
        System.out.println("get Connection..");
        return DriverManager.getConnection(DBUrl, "root", "port");
    }

    boolean hasUser(WebUser u) {
        String userName = u.getName();

        boolean has = false;
        String query = "SELECT * FROM WEBUSER_TABLE WHERE USERNAME ='" + userName + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                has = true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return has;
    }

    public boolean addUser(WebUser user) {
        boolean add = false;
        if (!hasUser(user)) {
            add = true;
            System.out.println("dao: Adding user: ");
            String query = "INSERT INTO WEBUSER_TABLE (USERNAME, EMAIL, PROF, AFFILIATION, REAL_NAME, SHARE_DATA) VALUES (?,?,?,?,?,?)";

            try {
                Connection conn = getConnection();
                if (conn == null) {
                    System.out.println("Connection null::");
                } else {
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, user.getName());
                    stmt.setString(2, user.getEmail());
                    stmt.setBoolean(3, user.getProf());
                    stmt.setString(4, user.getAffil());
                    stmt.setString(5, user.getRealName());
                    stmt.setBoolean(6, user.getShare());
                    int r = stmt.executeUpdate();
                    if (r > 0) {
                        System.out.println("User added.");
                    } else {
                        System.out.println("User not added.");
                    }
                }

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return add;
    }

    public void updateUser(WebUser user) {
        deleteUser(user);
        addUser(user);
    }

    public void deleteUser(WebUser user) {
        String query = "DELETE FROM WEBUSER_TABLE WHERE USERNAME = '" + user.getName() + "'";

        try {
            Statement stmt = getConnection().createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean webUserExist(String userName) {
        boolean exist = false;
        String query = "SELECT * FROM WEBUSER_TABLE WHERE USERNAME ='" + userName + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                exist = true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return exist;
    }

    public boolean authWebUser(String userName, String email) {
        System.out.println("authWebUser");
        System.out.println("user: " + userName);
        System.out.println("email: " + email);
        boolean auth = false;
        String query = "SELECT * FROM WEBUSER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '" + email + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                auth = true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Auth: " + auth);
        return auth;
    }

    public boolean authDataUser(String userName, String email) {
        System.out.println("authWebUser");
        System.out.println("user: " + userName);
        System.out.println("email: " + email);
        boolean auth = false;
        String query = "SELECT * FROM WEBUSER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '" + email + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                boolean duser = rs.getBoolean("DATAUSER");
                auth = duser;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Auth: " + auth);
        return auth;
    }

    public boolean authSuperUser(String userName, String email) {
        System.out.println("authSuperUser");
        System.out.println("user: " + userName);
        System.out.println("email: " + email);
        boolean auth = false;
        String query = "SELECT * FROM WEBUSER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '" + email + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                System.out.println("SU: " + rs.getBoolean("SUPERUSER"));
                if (rs.getBoolean("SUPERUSER")) {
                    auth = true;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Auth: " + auth);
        return auth;
    }

    public WebUser getUser(String userName, String email) {
        WebUser user = null;
        String query = "SELECT * FROM WEBUSER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '" + email + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString("USERNAME");
                String mail = rs.getString("EMAIL");
                String aff = rs.getString("AFFILIATION");
                String rname = rs.getString("REAL_NAME");
                boolean prof = rs.getBoolean("PROF");
                boolean share = rs.getBoolean("SHARE_DATA");
                user = new WebUser(name, mail, rname, aff, prof, share);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return user;
    }

    /*private boolean pitUnAltered(PitObs pit)
     {
     System.out.println("pitUnAltered()");
     boolean un=false;
     ///PitObs pit_current = null;
     String name = pit.getName();
     String old = pit.dataString().trim();
     String ss=null;
     System.out.println("pit: "+	name);
     String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE PIT_NAME = '" + name +"'";
     Statement stmt = null;
     try
     {
     stmt = getConnection().createStatement();
     ResultSet rs = stmt.executeQuery(query);
     System.out.println("Query executed:");
     if ( rs.next() )
     {
     System.out.println("Result::");
     String s = rs.getString("PIT_DATA");
     ss = URLDecoder.decode(s, "UTF-8");
     ss=ss.trim();
     PitObs oldpit = new PitObs(ss);
     ss = oldpit.dataString();
     }
     if (( ss!=null ) && ( old !=null ))
     {
     System.out.println("comparing data strings:");
     System.out.println("Old length: "+old.length());
     System.out.println("ss length: "+ss.length());
     System.out.println("old:: "+old);
     System.out.println("ss :: "+ss);
     un = old.equals(ss);
     }
     }
     catch(Throwable e){System.out.println(e.toString());}
     System.out.println("pit unaltered?: "+un);
     return un;
     }*/
    private boolean pitUnAltered(String data) {
        System.out.println("pitUnAltered()");
        boolean un = false;
        ///PitObs pit_current = null;
        avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
        String name = pit.getName();
        String ser = pit.getSerial();
        System.out.println("pit: " + name);

        if (ser.trim().length() > 0) {
            String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE PIT_NAME = ? AND LOCAL_SERIAL = ?";
            PreparedStatement stmt = null;
            String s = "";
            if ((name != null) && (name.trim().length() > 0)) {
                try {
                    stmt = getConnection().prepareStatement(query);
                    stmt.setString(1, name);
                    stmt.setString(2, ser);
                    ResultSet rs = stmt.executeQuery();
                    System.out.println("Query executed:");
                    if (rs.next()) {
                        System.out.println("Result::");
                        s = rs.getString("PIT_DATA");
                    }
                    un = s.equals(data);
                } catch (Throwable e) {
                    System.out.println(e.toString());
                }
            }
        } else {
            System.out.println("no local serial for pit: " + name);
            String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE PIT_NAME = ?";
            PreparedStatement stmt = null;
            String s = "";
            if ((name != null) && (name.trim().length() > 0)) {
                try {
                    stmt = getConnection().prepareStatement(query);
                    stmt.setString(1, name);

                    ResultSet rs = stmt.executeQuery();
                    System.out.println("Query executed:");
                    if (rs.next()) {
                        System.out.println("Result::");
                        s = rs.getString("PIT_DATA");
                    }
                    un = s.equals(data);
                } catch (Throwable e) {
                    System.out.println(e.toString());
                }
            }
        }

        System.out.println("pit unaltered?: " + un);
        return un;
    }

    private boolean occUnAltered(String data) {
        System.out.println("occUnAltered");
        boolean unaltered = false;

        String query = "SELECT SERIAL FROM OCC_TABLE WHERE OCC_DATA = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, data);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                unaltered = true;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return unaltered;
    }

    /*  private boolean checkSerial(String serial)
     {
     boolean avail = true;
     String query = "SELECT SERIAL FROM PIT_TABLE";
     Statement stmt = null;
     Connection conn=null;
     try
     {
     conn=getConnection();
     stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(query);
            
     while ( rs.next() )
     {
     String s = rs.getString("SERIAL");
     if (s==null) s = "";
     if ( s.equals(serial))
     {
     avail=false;
     break;
     }
     }
     conn.close();
     }
     catch(Exception e){System.out.println(e.toString());}
     return avail;
     }*/
    /*private String getNewSerial()
     {
     long time = System.currentTimeMillis();
     String serial = "SNOWPILOT"+time;
    	
     if (checkSerial(serial)) ;
     else serial = getNewSerial();
     return serial;
     }
    
     private String getNewDBSerial()
     {
     long time = System.currentTimeMillis();
     String serial = "SP"+time;
     if ( checkSerial(serial)) return serial;
     return getNewSerial();
     }*/
    public String removeDelims(String s) {
        System.out.println("s: " + s);
        String d = "'";
        char delim = d.charAt(0);
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == delim) {
                chars[i] = ' ';
            }
        }
        String result = new String(chars);
        System.out.println("result: " + result);
        return result;
    }

    boolean dbPitEdited(avscience.ppc.PitObs pit) {
        avscience.ppc.PitObs dbPit = new avscience.ppc.PitObs(getPPCPit(pit.getSerial()));
        if (dbPit.getEdited()) {
            return true;
        } else {
            return false;
        }
    }

    public avscience.ppc.PitObs convertPit(avscience.ppc.PitObs pit) {
        System.out.println("covertPit");
        java.util.Vector nt = new java.util.Vector();
        java.util.Vector np = new java.util.Vector();
        java.util.Enumeration e = pit.getShearTests();
        if (e != null) {
            while (e.hasMoreElements()) {
                StringSerializable gtest = (StringSerializable) e.nextElement();
                avscience.ppc.ShearTestResult test = new avscience.ppc.ShearTestResult(gtest.dataString());
                nt.add(test);

            }
            pit.shearTests = nt;
        }
        java.util.Enumeration ee = pit.getLayers();

        while (ee.hasMoreElements()) {
            StringSerializable glayer = (StringSerializable) ee.nextElement();
            avscience.ppc.Layer l = new avscience.ppc.Layer(glayer.dataString());
            np.add(l);
        }
        pit.layers = np;
        StringSerializable genuser = (StringSerializable) pit.getUser();
        avscience.ppc.User u = new avscience.ppc.User(genuser.dataString());
        pit.setUser(u);

        StringSerializable genloc = (StringSerializable) pit.getLocation();
        avscience.wba.Location l = new avscience.wba.Location(genloc.dataString());
        pit.setLocation(l);
        return pit;
    }

    public void writePitToDB(avscience.ppc.PitObs pit) {
        System.out.println("writePitToDB::PitObs");
        if (pit != null) {

            System.out.println("writing Pit: " + pit.getName());
            System.out.println("Pit:: " + pit.getName());
            if (pit.getName().trim().length() < 2) {
                return;
            }
    	/// convert old style pits to top/bottom format.

            avscience.wba.Location lc = pit.getLocation();
            String pn = lc.getName().trim();
            pn = removeDelims(pn);
            pn = cleanString(pn);
            lc.setName(pn);
            pit.setLocation(lc);
            System.out.println("Names set.");
            avscience.ppc.User user = pit.getUser();
            if (user == null) {
                user = new avscience.ppc.User();
            }

            String name = user.getName();
            System.out.println("User: " + name);
            String email = user.getEmail();
            WebUser wu = new WebUser(name, email, user.getFirst() + " " + user.getLast(), user.getAffil(), user.getProf(), user.getShare());
            addUser(wu);
            //
            System.out.println("user added.");
            String data = pit.dataString();
            if (pitPresent(pit)) {
                System.out.println("Pit already in DB");
                if (pitUnAltered(data)) {
                    return;
                } else {
                    System.out.println("deleting pit:");
                    deletePit(pit);

                }
            }

            System.out.println("writing pit to DB : " + pit.getName());
            String query = "INSERT INTO PIT_TABLE (PIT_DATA, AIR_TEMP, ASPECT, CROWN_OBS, OBS_DATE, TIMESTAMP, INCLINE, LOC_NAME, LOC_ID, STATE, MTN_RANGE, LAT, LONGITUDE, NORTH, WEST, ELEVATION, USERNAME, WINDLOADING, PIT_NAME, HASLAYERS, LOCAL_SERIAL, WINDLOAD, PRECIP, SKY_COVER, WIND_SPEED, WIND_DIR, STABILITY, SHARE, OBS_DATETIME, ACTIVITIES, TEST_PIT, PLATFORM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try {
                Connection conn = getConnection();
                if (conn == null) {
                    System.out.println("Connection null::");
                } else {
                    if (!checkBuild(pit)) {
                        updatePitLayers(pit);
                    }
                    PreparedStatement stmt = conn.prepareStatement(query);
                    java.sql.Date pdate = null;
                    long ptime = 0;
                    ptime = pit.getTimestamp();
                    if (ptime > 1) {
                        pdate = new java.sql.Date(ptime);
                    } else {
                        String s = pit.getDateString();
                        System.out.println("datestring: " + s);
                        String dt = pit.getDate();
                        String tt = pit.getTime();

                        if (dt.trim().length() > 5) {
                            pdate = getDateTime(dt, tt);
                        } else {
                            pdate = getDate(s);
                        }
                        if (pdate == null) {
                            pdate = new java.sql.Date(System.currentTimeMillis());
                        }
                        pit.setTimestamp(pdate.getTime());
                    }
                    if (checkBuild(pit)) {
                        stmt.setString(1, data);
                    } else {
                        stmt.setString(1, pit.dataString());
                    }
                    float temp = -999.9f;
                    System.out.println("setting air temp");
                    try {
                        if ((pit.getAirTemp() != null) && (pit.getAirTemp().trim().length() > 0)) {
                            temp = stringToFloat(pit.getAirTemp());
                            if (pit.getUser().getTempUnits().equals("F")) {
                                temp = FtoC(temp);
                            }
                        }
                        // air temp C
                        stmt.setFloat(2, temp);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        stmt.setFloat(2, -999.9f);
                    }
                    System.out.println("setting aspect");
                    try {
                        stmt.setInt(3, stringToInt(pit.getAspect()));
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        stmt.setInt(3, -999);
                    }
                    // is Crown obs?
                    System.out.println("setting crown obs");
                    boolean co = pit.getCrownObs();
                    stmt.setBoolean(4, co);
                    /// date of pit
                    System.out.println("setting datestring");

                    stmt.setDate(5, pdate);
                    
                    // date entered here
                    System.out.println("setting timestamp");
                    stmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                    // incline
                    System.out.println("setting incline");
                    try {
                        String incl = pit.getIncline();
                        System.out.println("incline: " + incl);
                        stmt.setInt(7, stringToInt(incl));
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        stmt.setInt(7, -999);
                    }
                    // location
                    System.out.println("getting loc");
                    avscience.wba.Location loc = pit.getLocation();
                    System.out.println("Locname : " + loc.getName().trim());
                    String ln = loc.getName().trim();
                    //System.out.println("ln: "+ln);
                    //char c = (char) ''';
                    ln.replaceAll("'", "");
                    System.out.println("setting locname");
                    stmt.setString(8, ln);
                    System.out.println("setting locID");
                    stmt.setString(9, loc.getID().trim());
                    System.out.println("setting state");
                    stmt.setString(10, loc.getState().trim());
                    System.out.println("setting range");
                    stmt.setString(11, loc.getRange().trim());
                    System.out.println("setting lat");
                    float lat = -999.9f;
                    try {
                        lat = stringToFloat(loc.getLat());
                        stmt.setFloat(12, lat);
                    } catch (Exception e) {
                        stmt.setFloat(12, -999.9f);
                        System.out.println(e.toString());
                    }
                    System.out.println("setting long");
                    float longitude = -999.9f;
                    try {
                        longitude = stringToFloat(loc.getLongitude());
                        stmt.setFloat(13, longitude);
                    } catch (Exception e) {
                        stmt.setFloat(13, -999.9f);
                        System.out.println(e.toString());
                    }
                    System.out.println("setting lat type");
                    stmt.setBoolean(14, loc.getLatType().equals("N"));
                    System.out.println("setting long type");
                    stmt.setBoolean(15, loc.getLongType().equals("W"));

                    System.out.println("setting elv");
                    try {
                        System.out.println("elv: " + loc.getElv());
                        int e = stringToInt(loc.getElv());
                        //	int e = new java.lang.Integer(loc.getElv()).intValue();
                        System.out.println("elevation: " + e);
                        if (pit.getUser().getElvUnits().equals("ft")) {
                            e = ft_to_m(e);
                        }
                        stmt.setInt(16, e);
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                        stmt.setInt(16, -999);
                    }
                    // user name
                    System.out.println("setting username");
                    stmt.setString(17, pit.getUser().getName().trim());
                    /// wind loading
                    System.out.println("wind loading");
                    if (pit.getWindLoading() != null) {
                        stmt.setBoolean(18, pit.getWindLoading().equals("yes"));
                    } else {
                        stmt.setBoolean(18, false);
                    }
                    // name
                    pn = pit.getName().trim();
                    pn.replaceAll("'", "");
                    System.out.println("setting name");
                    stmt.setString(19, pn);
                    System.out.println("setting has layers");
                    stmt.setBoolean(20, pit.hasLayers());
                    System.out.println("setting serial.");
                    String sser = pit.getSerial();

                    if (sser == null) {
                        sser = "";
                    }
                    stmt.setString(21, sser);

                    stmt.setString(22, pit.getWindLoading());
                    stmt.setString(23, pit.getPrecip());
                    stmt.setString(24, pit.getSky());
                    stmt.setString(25, pit.getWindspeed());
                    stmt.setString(26, pit.getWinDir());
                    stmt.setString(27, pit.getStability());
                    stmt.setBoolean(28, pit.getUser().getShare());
                    Timestamp ots = new Timestamp(pdate.getTime());
                    stmt.setTimestamp(29, ots);
                    StringBuffer buffer = new StringBuffer(" ");
                    try {
                        System.out.println("setting activities: ");
                        if (pit.getActivities() != null) {
                            System.out.println("# activities: " + pit.getActivities().size());
                            java.util.Enumeration e = pit.getActivities().elements();
                            if (e != null) {
                                while (e.hasMoreElements()) {
                                    String s = (String) e.nextElement();
                                    System.out.println("acts: " + s);
                                    buffer.append(" : " + s + " : ");
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

                    stmt.setString(30, buffer.toString());
                    boolean testPit = false;
                    if (pit.testPit != null) {
                        if (pit.testPit.trim().equals("true")) {
                            testPit = true;
                        }
                        stmt.setBoolean(31, testPit);
                    } else {
                        stmt.setBoolean(31, false);
                    }
                    if (pit.version != null) {
                        stmt.setString(32, pit.version);
                    } else {
                        stmt.setString(32, "");
                    }

                    System.out.println("ex query");
                    stmt.executeUpdate();
                    conn.close();
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    public void writePitToDB(String data) {
        System.out.println("writePitToDB");
        //System.out.println("data: "+data);
        if ((data == null) | (data.trim().length() < 9)) {
            System.out.println("data not valid.");
            return;
        }
        avscience.ppc.PitObs pit = null;
        try {
            pit = new avscience.ppc.PitObs(data);
        } catch (Exception e) {
            try {
                avscience.wba.PitObs wpit = new avscience.wba.PitObs(data);
                String ndata = wpit.dataString();
                pit = new avscience.ppc.PitObs(ndata);
            } catch (Exception ee) {
                System.out.println("PIT FAILED TO POPULATE " + ee.toString());
                return;
            }

        }
        try {
            if (!checkBuild(pit)) {
                pit = convertPit(pit);
            }
        } catch (Exception e) {
            System.out.println("PIT FAILED TO CONVERT");
            return;
        }
        if (pit == null) {
            System.out.println("PIT IS NULL..");
            return;
        }
        if (pit.getName() == null) {
            System.out.println("PIT NAME IS NULL..");
            return;
        }

        System.out.println("writing Pit: " + pit.getName());
        System.out.println("Pit:: " + pit.getName());
        if (pit.getName().trim().length() < 2) {
            return;
        }
    	/// convert old style pits to top/bottom format.

        avscience.wba.Location lc = pit.getLocation();
        String pn = lc.getName().trim();
        pn = removeDelims(pn);
        pn = cleanString(pn);
        lc.setName(pn);
        pit.setLocation(lc);
        System.out.println("Names set.");
        avscience.ppc.User user = pit.getUser();
        if (user == null) {
            user = new avscience.ppc.User();
        }

        String name = user.getName();
        System.out.println("User: " + name);
        String email = user.getEmail();
        WebUser wu = new WebUser(name, email, user.getFirst() + " " + user.getLast(), user.getAffil(), user.getProf(), user.getShare());
        addUser(wu);
        //
        System.out.println("user added.");

        if (pitPresent(pit)) {
            System.out.println("Pit already in DB");
            if (pitUnAltered(data)) {
                return;
            } else {
                System.out.println("deleting pit:");
                deletePit(pit);

            }
        }

        System.out.println("writing pit to DB : " + pit.getName());
        String query = "INSERT INTO PIT_TABLE (PIT_DATA, AIR_TEMP, ASPECT, CROWN_OBS, OBS_DATE, TIMESTAMP, INCLINE, LOC_NAME, LOC_ID, STATE, MTN_RANGE, LAT, LONGITUDE, NORTH, WEST, ELEVATION, USERNAME, WINDLOADING, PIT_NAME, HASLAYERS, LOCAL_SERIAL, WINDLOAD, PRECIP, SKY_COVER, WIND_SPEED, WIND_DIR, STABILITY, SHARE, ACTIVITIES, TEST_PIT, PLATFORM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.out.println("Connection null::");
            } else {
                if (!checkBuild(pit)) {
                    updatePitLayers(pit);
                }
                PreparedStatement stmt = conn.prepareStatement(query);
                java.sql.Date pdate = null;
                long ptime = 0;
                ptime = pit.getTimestamp();
                if (ptime > 1) {
                    pdate = new java.sql.Date(ptime);
                } else {
                    String s = pit.getDateString();
                    System.out.println("datestring: " + s);
                    String dt = pit.getDate();
                    String tt = pit.getTime();

                    if (dt.trim().length() > 5) {
                        pdate = getDateTime(dt, tt);
                    } else {
                        pdate = getDate(s);
                    }
                    if (pdate == null) {
                        pdate = new java.sql.Date(System.currentTimeMillis());
                    }
                    pit.setTimestamp(pdate.getTime());
                }
                if (checkBuild(pit)) {
                    stmt.setString(1, data);
                } else {
                    stmt.setString(1, pit.dataString());
                }
                float temp = -999.9f;
                System.out.println("setting air temp");
                try {
                    if ((pit.getAirTemp() != null) && (pit.getAirTemp().trim().length() > 0)) {
                        temp = stringToFloat(pit.getAirTemp());
                        if (pit.getUser().getTempUnits().equals("F")) {
                            temp = FtoC(temp);
                        }
                    }
                    // air temp C
                    stmt.setFloat(2, temp);
                } catch (Exception e) {
                    System.out.println(e.toString());
                    stmt.setFloat(2, -999.9f);
                }
                System.out.println("setting aspect");
                try {
                    stmt.setInt(3, stringToInt(pit.getAspect()));
                } catch (Exception e) {
                    System.out.println(e.toString());
                    stmt.setInt(3, -999);
                }
                // is Crown obs?
                System.out.println("setting crown obs");
                boolean co = pit.getCrownObs();
                stmt.setBoolean(4, co);
                /// date of pit
                System.out.println("setting datestring");

                stmt.setDate(5, pdate);
                Timestamp ots = new Timestamp(pdate.getTime());
                stmt.setTimestamp(29, ots);
                // date entered here
                System.out.println("setting timestamp");
                stmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                // incline
                System.out.println("setting incline");
                try {
                    String incl = pit.getIncline();
                    System.out.println("incline: " + incl);
                    stmt.setInt(7, stringToInt(incl));
                } catch (Exception e) {
                    System.out.println(e.toString());
                    stmt.setInt(7, -999);
                }
                // location
                System.out.println("getting loc");
                avscience.wba.Location loc = pit.getLocation();
                System.out.println("Locname : " + loc.getName().trim());
                String ln = loc.getName().trim();
                    //System.out.println("ln: "+ln);
                //char c = (char) ''';
                ln.replaceAll("'", "");
                System.out.println("setting locname");
                stmt.setString(8, ln);
                System.out.println("setting locID");
                String lid = " ";
                if (loc.getID() != null) {
                    lid = loc.getID();
                }
                stmt.setString(9, lid.trim());
                System.out.println("setting state");
                String st = " ";
                if (loc.getState() != null) {
                    st = loc.getState();
                }
                stmt.setString(10, st.trim());
                System.out.println("setting range");
                String rng = " ";
                if (loc.getRange() != null) {
                    rng = loc.getRange();
                }
                stmt.setString(11, rng.trim());
                
                System.out.println("setting lat");
                float lat = -999.9f;
                try {
                    if (loc.getLat() != null) {
                        lat = stringToFloat(loc.getLat());
                    }
                    stmt.setFloat(12, lat);
                } catch (Exception e) {
                    stmt.setFloat(12, -999.9f);
                    System.out.println(e.toString());
                }
                System.out.println("setting long");
                float longitude = -999.9f;
                try {
                    if (loc.getLongitude() != null) {
                        longitude = stringToFloat(loc.getLongitude());
                    }
                    stmt.setFloat(13, longitude);
                } catch (Exception e) {
                    stmt.setFloat(13, -999.9f);
                    System.out.println(e.toString());
                }
                System.out.println("setting lat type");
                stmt.setBoolean(14, loc.getLatType().equals("N"));
                System.out.println("setting long type");
                stmt.setBoolean(15, loc.getLongType().equals("W"));

                System.out.println("setting elv");
                try {
                    System.out.println("elv: " + loc.getElv());
                    int e = 0;
                    if (loc.getElv() != null) {
                        e = stringToInt(loc.getElv());
                    }
                    //	int e = new java.lang.Integer(loc.getElv()).intValue();
                    System.out.println("elevation: " + e);
                    if (pit.getUser().getElvUnits().equals("ft")) {
                        e = ft_to_m(e);
                    }
                    stmt.setInt(16, e);
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                    stmt.setInt(16, -999);
                }
                // user name
                System.out.println("setting username");
                stmt.setString(17, pit.getUser().getName().trim());
                /// wind loading
                System.out.println("wind loading");
                if (pit.getWindLoading() != null) {
                    stmt.setBoolean(18, pit.getWindLoading().equals("yes"));
                } else {
                    stmt.setBoolean(18, false);
                }
                // name
                pn = pit.getName().trim();
                pn.replaceAll("'", "");
                System.out.println("setting name");
                stmt.setString(19, pn);
                System.out.println("setting has layers");
                stmt.setBoolean(20, pit.hasLayers());
                System.out.println("setting serial.");
                String sser = pit.getSerial();

                if (sser == null) {
                    sser = "";
                }
                stmt.setString(21, sser);

                stmt.setString(22, pit.getWindLoading());
                stmt.setString(23, pit.getPrecip());
                stmt.setString(24, pit.getSky());
                stmt.setString(25, pit.getWindspeed());
                stmt.setString(26, pit.getWinDir());
                stmt.setString(27, pit.getStability());
                stmt.setBoolean(28, pit.getUser().getShare());
                StringBuffer buffer = new StringBuffer(" ");
                try {
                    System.out.println("setting activities: ");
                    if (pit.getActivities() != null) {
                        System.out.println("# activities: " + pit.getActivities().size());
                        java.util.Enumeration e = pit.getActivities().elements();
                        if (e != null) {
                            while (e.hasMoreElements()) {
                                String s = (String) e.nextElement();
                                System.out.println("acts: " + s);
                                buffer.append(" : " + s + " : ");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                stmt.setString(29, buffer.toString());
                boolean testPit = false;
                if (pit.testPit != null) {
                    if (pit.testPit.trim().equals("true")) {
                        testPit = true;
                    }
                    stmt.setBoolean(30, testPit);
                } else {
                    stmt.setBoolean(30, false);
                }
                if (pit.version != null) {
                    stmt.setString(31, pit.version);
                } else {
                    stmt.setString(31, "");
                }

                System.out.println("ex query: Fields: "+32);
                stmt.executeUpdate();
                conn.close();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        //  }
    }

    public void writeOccToDB(String data) {
        System.out.println("writeOccToDB");
        avscience.server.CharacterCleaner cleaner = new avscience.server.CharacterCleaner();
        //String ndata = cleaner.cleanString(data);
        avscience.ppc.AvOccurence occ = new avscience.ppc.AvOccurence(data);
        occ = cleaner.cleanStrings(occ);
        String pn = occ.getPitName();
        pn = removeDelims(pn);
        occ.setPitName(pn);

        if (occPresent(occ)) {
            System.out.println("Occ already in DB");
            if (occUnAltered(data)) {
                return;
            } else {
                System.out.println("Deleting occ..");
                deleteOcc(occ);
            }
        }

        System.out.println("writing occ to DB : " + occ.getPitName());
        String query = "INSERT INTO OCC_TABLE (OCC_DATA, OBS_DATE, TIMESTAMP, ELV_START, ELV_DEPOSIT, ASPECT, TYPE, TRIGGER_TYPE, TRIGGER_CODE, US_SIZE, CDN_SIZE, AVG_FRACTURE_DEPTH, MAX_FRACTURE_DEPTH, WEAK_LAYER_TYPE, WEAK_LAYER_HARDNESS, SNOW_PACK_TYPE, FRACTURE_WIDTH, FRACTURE_LENGTH, AV_LENGTH, AVG_START_ANGLE, MAX_START_ANGLE, MIN_START_ANGLE, ALPHA_ANGLE, DEPTH_DEPOSIT, LOC_NAME, LOC_ID, STATE, ,MTN_RANGE, LAT, LONGITUDE, NORTH, WEST, USERNAME, NAME, LOCAL_SERIAL, SHARE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.out.println("Connection null::");
            } else {
                PreparedStatement stmt = conn.prepareStatement(query);
               // String data = occ.dataString();
                //  data = URLEncoder.encode(data, "UTF-8");
                stmt.setString(1, data);
                String ser = occ.getSerial();
                System.out.println("Occ Serial: " + ser);
                String pdata = "";
                java.sql.Date odate = null;
                long otime = 0;
                avscience.ppc.PitObs pit = null;
                if ((ser != null) && (ser.trim().length() > 0)) {
                    System.out.println("getPitBySerial: " + ser);
                    pdata = getPitByLocalSerial(ser);
                    if ((pdata != null) && (pdata.trim().length() > 1)) {
                        System.out.println("getting pit by local serial.");
                        pit = new avscience.ppc.PitObs(pdata);
                        if (pit != null) {
                            otime = pit.getTimestamp();
                        }
                    } else {
                        System.out.println("Can't get Pit: " + ser + " by serial.");
                    }
                } else {
                    String wdata = getPit(pn);
                    //	= wpit.dataString();
                    if ((wdata != null) && (wdata.trim().length() > 1)) {
                        pit = new avscience.ppc.PitObs(wdata);
                    } else {
                        System.out.println("Can't get Pit: " + pn + " by Name.");
                    }
                }
                if (pit == null) {
                    System.out.println("Pit is null..");
                } else if (pit.getUser() == null) {
                    System.out.println("Pit user is null.");
                }

                System.out.println("setting dates.");

                if (otime > 1) {
                    odate = new java.sql.Date(otime);
                } else {
                    String dt = pit.getDateString();
                    if (dt != null) {
                        odate = getDate(dt);
                    }
                    if (odate == null) {
                        odate = new java.sql.Date(System.currentTimeMillis());
                    }
                }
                System.out.println("Date: " + odate.toString());
                stmt.setDate(2, odate);
                ///stmt.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                stmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                //
                System.out.println("setting elevation.");
                int elvStart = 0;
                if (occ.getElvStart().trim().length() > 0) {
                    elvStart = stringToInt(occ.getElvStart());
                }
                if (pit.getUser().getElvUnits().equals("ft")) {
                    elvStart = ft_to_m(elvStart);
                }
                stmt.setInt(4, elvStart);
                System.out.println("setting elevation.");
                int elvDep = 0;
                if (occ.getElvDeposit().trim().length() > 0) {
                    elvDep = stringToInt(occ.getElvDeposit());
                }
                if (pit.getUser().getElvUnits().equals("ft")) {
                    elvDep = ft_to_m(elvDep);
                }
                stmt.setInt(5, elvDep);
                System.out.println("setting aspect");
                int aspect = 0;
                if (occ.getAspect().trim().length() > 0) {
                    aspect = stringToInt(occ.getAspect());
                }
                stmt.setInt(6, aspect);
                stmt.setString(7, occ.getType().trim());
                stmt.setString(8, occ.getTriggerType().trim());
                stmt.setString(9, occ.getTriggerCode().trim());
                stmt.setString(10, occ.getUSSize());
                String cs = occ.getCASize().trim();
                System.out.println("CASize: " + cs);
                if (cs.lastIndexOf('D') > -1) {
                    cs = cs.substring(1, cs.length());
                }
                System.out.println("cs: " + cs);
                float csize = 0;
                if (cs.trim().length() > 0) {
                    csize = (new Float(cs)).floatValue();
                }
                stmt.setFloat(11, csize);

                int aDepth = stringToInt(occ.getAvgFractureDepth());
                if (pit.getUser().getDepthUnits().equals("in")) {
                    aDepth = in_to_cm(aDepth);
                }
                stmt.setInt(12, aDepth);

                int mDepth = stringToInt(occ.getMaxFractureDepth());
                if (pit.getUser().getDepthUnits().equals("in")) {
                    mDepth = in_to_cm(mDepth);
                }
                stmt.setInt(13, mDepth);
                System.out.println("setting layer types");
                stmt.setString(14, occ.getWeakLayerType().trim());
                stmt.setString(15, occ.getWeakLayerHardness().trim());
                stmt.setString(16, occ.getSnowPackType().trim());

                int fw = stringToInt(occ.getFractureWidth());
                if (pit.getUser().getElvUnits().equals("ft")) {
                    fw = ft_to_m(fw);
                }
                stmt.setInt(17, fw);

                int fl = stringToInt(occ.getFractureLength());
                if (pit.getUser().getElvUnits().equals("ft")) {
                    fl = ft_to_m(fl);
                }
                stmt.setInt(18, fl);

                int al = stringToInt(occ.getLengthOfAvalanche());
                if (pit.getUser().getElvUnits().equals("ft")) {
                    al = ft_to_m(al);
                }
                stmt.setInt(19, al);
                System.out.println("setting angles");
                stmt.setInt(20, stringToInt(occ.getAvgStartAngle()));
                stmt.setInt(21, stringToInt(occ.getMaxStartAngle()));
                stmt.setInt(22, stringToInt(occ.getMinStartAngle()));
                stmt.setInt(23, stringToInt(occ.getAlphaAngle()));

                int d = stringToInt(occ.getDepthOfDeposit());
                if (pit.getUser().getElvUnits().equals("ft")) {
                    d = ft_to_m(d);
                }
                stmt.setInt(24, d);
                System.out.println("setting location");
                avscience.wba.Location loc = pit.getLocation();
                String ln = loc.getName().trim();
                ln.replaceAll("'", "");
                stmt.setString(25, ln);
                stmt.setString(26, loc.getID().trim());
                stmt.setString(27, loc.getState().trim());
                stmt.setString(28, loc.getRange().trim());

                float lat = -999.9f;
                lat = stringToFloat(loc.getLat());
                stmt.setFloat(29, lat);
                float longitude = -999.9f;
                System.out.println("setting lat/lon.");
                longitude = stringToFloat(loc.getLongitude());
                stmt.setFloat(30, longitude);
                stmt.setBoolean(31, loc.getLatType().equals("N"));
                stmt.setBoolean(32, loc.getLongType().equals("W"));
                stmt.setString(33, pit.getUser().getName().trim());
                pn = pit.getName().trim();
                pn.replaceAll("'", "");
                stmt.setString(34, pn);
                stmt.setString(35, occ.getSerial());
                stmt.setBoolean(36, pit.getUser().getShare());
                System.out.println("executing occ update: ");
                stmt.executeUpdate();
                conn.close();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        //  }
    }

    private int stringToInt(String s) {
        s = s.trim();
        System.out.println("stringToInt() " + s);
        int i = -999;
        if ((s != null) && (s.trim().length() > 0)) {
            try {
                // s = makeNumeric(s);
                if (s.trim().length() > 0) {
                    i = (new java.lang.Integer(s)).intValue();
                    i = java.lang.Math.abs(i);
                }
            } catch (Exception e) {
                System.out.println("stringToInt: " + e.toString());
            }
        }
        return i;
    }

    private float stringToFloat(String s) {
        s = s.trim();
        float f = -999.9f;
        try {
            if (s.trim().length() > 0) {
                f = (new java.lang.Float(s)).floatValue();
            }
        } catch (Exception e) {
            System.out.println("stringTofloat: " + e.toString());
        }
        return f;
    }

    private String makeNumeric(String s) {
        System.out.println("makeNumeric: " + s);
        int length = s.length();
        char[] chars = new char[length];
        Vector digs = new Vector();
        int j = 0;
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            Character C = new Character(c);
            if (Character.isDigit(C.charValue())) {
                digs.add(j, C);
                j++;
            }
        }
        length = digs.size();
        char[] newChars = new char[length];
        for (int i = 0; i < length; i++) {
            newChars[i] = ((Character) digs.elementAt(i)).charValue();
        }
        return new String(newChars);
    }

    private int in_to_cm(int in) {
        return (int) java.lang.Math.rint(in * 2.54);
    }

    private boolean pitPresent(avscience.wba.PitObs pit) {
        System.out.println("pitPresent");
        String name = pit.getName();
        String user = pit.getUser().getName();
        if (name == null) {
            name = "";
        }
        if (user == null) {
            user = "";
        }
        String query = "SELECT * FROM PIT_TABLE WHERE PIT_NAME = ? AND USERNAME = ?";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, user);
            ResultSet rs = stmt.executeQuery();
            conn.close();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    /*public void updateDB()
     {
     String query = "SELECT PIT_DATA, SERIAL FROM PIT_TABLE";
     Statement stmt = null;
        
     String data="";
     int ser=-1;
     Connection conn=null;
     Connection conn1=null;
     Statement stmt1=null;
     String qry=null;
     int count=0;
     try
     {
     conn = getConnection();
     conn1 = getConnection();
     stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(query);
     while ( rs.next())
     {
     ser = rs.getInt("SERIAL");
     System.out.println("updating pit: "+ser);
     String s = ser+"";
     data = rs.getString("PIT_DATA");
     qry = "DELETE FROM PIT_TABLE WHERE SERIAL = "+ser;
     try
     {
     stmt1 = conn1.createStatement();
     int c = stmt1.executeUpdate(qry);
     if ( c > 0 )System.out.println("deleted pit: "+ser);
	            	
     System.out.println("writing pit: "+ser);
     if (( data != null ) && ( data.trim().length()> 0)) 
     {
     writePitToDB(data);
     count++;
     }
     else System.out.println("INVALID DATA FOR PIT: "+ser);
     }
     catch(Exception e)
     {
     System.out.println("ERROR UPDATING PIT: "+ser);
     System.out.println(e.toString());
     }
     }
     conn.close();
     conn1.close();
     }
     catch(Exception e)
     {
     System.out.println("ERROR UPDATING PITS");
     System.out.println(e.toString());
     }
     System.out.println(count+" Pits Updated.");
     }*/
    
    public void updateRanges()
    {
        String query = "SELECT SERIAL, PIT_DATA FROM PIT_TABLE";
        Statement stmt = null;
        String serial = "";
      //  String name = "";
        String data = "";
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
             //   name = rs.getString("PIT_NAME");
                serial = rs.getString("SERIAL");
                data = rs.getString("PIT_DATA");
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                String rng = pit.getLocation().getRange();
                System.out.println("Setting range for pit: "+serial+" to "+rng);
                try {
                    String q2 = "UPDATE PIT_TABLE SET MTN_RANGE = ? WHERE SERIAL = ?";
                    PreparedStatement stmt1 = conn.prepareStatement(q2);
                    stmt1.setString(1, rng);
                    stmt1.setString(2, serial);
                    stmt1.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
               
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        
    }
    
    public void checkPits() {
        String query = "SELECT PIT_NAME, SERIAL, PIT_DATA FROM PIT_TABLE";
        Statement stmt = null;
        String serial = "";
        String name = "";
        String data = "";
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                name = rs.getString("PIT_NAME");
                serial = rs.getString("SERIAL");
                data = rs.getString("PIT_DATA");
                try {
                    Statement stmt1 = conn.createStatement();
                    String q2 = "DELETE FROM PIT_TABLE WHERE SERIAL = " + serial;
                    stmt1.executeQuery(q2);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                writePitToDB(data);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private boolean pitPresent(avscience.ppc.PitObs pit) {
        System.out.println("pitPresent");
        String name = pit.getName();
        String user = pit.getUser().getName();
        String ser = pit.getSerial();
        String query = "SELECT * FROM PIT_TABLE WHERE PIT_NAME = ? AND USERNAME = ? AND LOCAL_SERIAL = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, user);
            stmt.setString(3, ser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    private boolean occPresent(avscience.ppc.AvOccurence occ) {
        System.out.println("occPresent()");
        String name = occ.getPitName();
        String serial = occ.getSerial();
        if (name == null) {
            name = "";
        }
        if (serial == null) {
            serial = "";
        }

        String query = "SELECT * FROM OCC_TABLE WHERE NAME = ? AND LOCAL_SERIAL = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, serial);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    private float FtoC(float t) {
        t = t - 32;
        float c = t * (5 / 9);
        return c;
    }

    private int ft_to_m(int ft) {
        return (int) java.lang.Math.rint(ft / 3.29f);
    }

    public java.sql.Date getDateTime(String dt, String time) {
        String yr = "0";
        String mnth = "0";
        String dy = "0";
        String hr = "0";
        String min = "0";
        if (!(dt.trim().length() < 8)) {
            yr = dt.substring(0, 4);
            mnth = dt.substring(4, 6);
            dy = dt.substring(6, 8);
        }

        if (!(time.trim().length() < 4)) {
            hr = time.substring(0, 2);
            min = time.substring(2, 4);
        }

        int y = new java.lang.Integer(yr).intValue();
        int m = new java.lang.Integer(mnth).intValue() - 1;
        int d = new java.lang.Integer(dy).intValue();
        int h = new java.lang.Integer(hr).intValue();
        int mn = new java.lang.Integer(min).intValue();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(y, m, d, h, mn);
        long ts = cal.getTimeInMillis();
        System.out.println("date/time: " + new java.sql.Date(ts).toString());
        return new java.sql.Date(ts);
    }

    private java.sql.Date getDate(String date) {
        System.out.println("getDate(): " + date);
        //	if ( date == null ) return;
        String tm = "";
        date = date.trim();
        if (date.length() > 12) {
            tm = date.substring(11, date.length());
        }
        System.out.println("time: " + tm);
        if (date.length() > 10) {
            date = date.substring(0, 10);
        }
        long time = 0;
        int month = 0;
        int day = 0;
        int year = 0;
        Calendar cal = Calendar.getInstance();
        int start = 0;
        int end = 0;
        if (date.length() > 6) {
            end = date.indexOf("/");
            String m = date.substring(0, end);
            if ((m != null) && (m.trim().length() > 0)) {
                month = (new java.lang.Integer(m)).intValue();
            }
            start = end + 1;
            end = date.indexOf(" ", start);
            if (end > 1) {
                day = (new java.lang.Integer(date.substring(start, end))).intValue();
                year = (new java.lang.Integer(date.substring(date.length() - 4, date.length()))).intValue();
            } else {
                year = new java.lang.Integer(date.substring(date.length() - 4, date.length())).intValue();
                // month =  new java.lang.Integer(date.substring(4, 6)).intValue();
                day = new java.lang.Integer(date.substring(3, 5)).intValue();
            }
        }
        int hr = 0;
        int mn = 0;
        int sc = 0;

        if (tm.trim().length() > 6) {
            try {
                String h = "";
                String min = "";
                String s = "";
                start = 0;
                end = tm.indexOf(":", start);
                if (end > 0) {
                    h = tm.substring(start, end);
                }
                start = end + 1;
                end = tm.indexOf(":", start);
                if (end > 0) {
                    min = tm.substring(start, end);
                }
                start = end + 1;
                s = tm.substring(start, start + 2);

                String ap = tm.substring(tm.length() - 2, tm.length());

                hr = new java.lang.Integer(h).intValue();
                if (ap.equals("PM")) {
                    hr += 12;
                }
                mn = new java.lang.Integer(min).intValue();
                sc = new java.lang.Integer(s).intValue();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        month -= month;
        if (month > 11) {
            month = 11;
        }
        cal.set(year, month, day, hr, mn, sc);
        time = cal.getTimeInMillis();

        System.out.println("date/time: " + new java.sql.Date(time).toString());
        return new java.sql.Date(time);
    }

    public String cleanString(String s) {
        if (s.trim().length() < 2) {
            return s;
        }

        try {
            char[] chars = s.toCharArray();
            int l = chars.length;

            for (int jj = 0; jj < l; jj++) {
                int idx = jj;
                //logger.println("idx: "+idx);
                if ((idx < l) && (idx > 0)) {
                    char test = chars[idx];
                    if (test <= 0) {
                        chars[idx] = ' ';
                    }
                }

            }
            String tmp = "";
            tmp = new String(chars);
            if ((tmp != null) && (tmp.trim().length() > 5)) {
                s = tmp;
            }
        } catch (Throwable e) {
            System.out.println("cleanString failed: " + e.toString());
        }
        return s;

    }

    public String[][] getPitListArray(boolean datefilter) {
        Vector serials = new Vector();
        Vector names = new Vector();
        String query = "SELECT CROWN_OBS, OBS_DATE, OBS_DATETIME, PIT_NAME, SHARE, SERIAL FROM PIT_TABLE WHERE SHARE > 0 ORDER BY OBS_DATE DESC";
        Statement stmt = null;
        Connection conn;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATE");
                if ((datefilter) && (pitDate.after(startDate))) {
                    if (!rs.getBoolean("CROWN_OBS")) {
                        String serial = "" + rs.getInt("SERIAL");
                        String name = rs.getString("PIT_NAME");
                        serials.insertElementAt(serial, i);
                        names.insertElementAt(name, i);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        String[][] list = new String[2][serials.size()];

        int i = 0;
        Iterator e = serials.iterator();
        Iterator ee = names.iterator();
        while (e.hasNext()) {
            String ser = (String) e.next();
            String nm = (String) ee.next();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }
    
    
    public String[][] getPitListArrayFromQuery(String whereclause, boolean datefilter) {
        System.out.println("getPitListArrayFromQuery()   " + whereclause);
        Vector serials = new Vector();
        Vector names = new Vector();
        String query = "SELECT CROWN_OBS, OBS_DATE, OBS_DATETIME, PIT_NAME, SERIAL ,SHARE FROM PIT_TABLE " + whereclause + " AND SHARE > 0 ORDER BY OBS_DATETIME DESC";
        System.out.println("Query:  " + query);
        Statement stmt = null;
        Connection conn;
        int i = 0;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATE");
                if (datefilter && (pitDate.after(startDate))) {
                    if (!rs.getBoolean("CROWN_OBS")) {
                        String serial = "" + rs.getInt("SERIAL");
                        String name = rs.getString("PIT_NAME");
                        serials.insertElementAt(serial, i);
                        names.insertElementAt(name, i);
                        i++;
                    }
                } else if (!datefilter) {
                    if (!rs.getBoolean("CROWN_OBS")) {
                        String serial = "" + rs.getInt("SERIAL");
                        String name = rs.getString("PIT_NAME");
                        serials.insertElementAt(serial, i);
                        names.insertElementAt(name, i);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        String[][] list = new String[2][serials.size()];

        i = 0;
        Enumeration e = serials.elements();
        Enumeration ee = names.elements();
        while (e.hasMoreElements()) {
            String ser = (String) e.nextElement();
            String nm = (String) ee.nextElement();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }

    public Hashtable getAllPits() {
        System.out.println("getAllPits()");
        Hashtable v = new Hashtable();
        String query = "SELECT SERIAL, PIT_DATA FROM PIT_TABLE";
        try {
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String dat = rs.getString("PIT_DATA");
                String serial = rs.getString("SERIAL");
                if ((dat != null) && (dat.trim().length() > 5)) {
                    v.put(serial, dat);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("No of PITS: " + v.size());
        return v;

    }

    /*  public Vector getPitList()
     {
    	
     Vector v = new Vector();
     String query = "SELECT PIT_NAME, OBS_DATE, OBS_DATETIME FROM PIT_TABLE ORDER BY OBS_DATETIME DESC";
     Statement stmt = null;
     try
     {
     stmt = getConnection().createStatement();
     ResultSet rs = stmt.executeQuery(query);
            
     while ( rs.next() )
     {
     java.util.Date pitDate = rs.getDate("OBS_DATE");
     if ( pitDate.after(startDate))
     {
     String s = rs.getString(1);
     v.add(s);
     }
     }
     }
     catch(Exception e){System.out.println(e.toString());}
     return v;
     }*/
    public Vector getPitListFromQuery(String whereclause) throws Exception {

        System.out.println("DAO pitlist query");
        Vector v = new Vector();
        String query = "SELECT PIT_NAME, TIMESTAMP, OBS_DATE, SHARE FROM PIT_TABLE " + whereclause + " AND SHARE > 0 ORDER BY OBS_DATE DESC";
        System.out.println("QUERY:: " + query);
        Statement stmt = null;
        whereclause = "";
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATE");
               // if (pitDate.after(startDate)) {
                    String s = rs.getString(1);
                    v.add(s);
               // }
            }
        } catch (Exception e) {
            whereclause = e.toString();
            System.out.println(e.toString());
            throw e;

        }

        return v;
    }

    public Vector getOccListFromQuery(String whereclause) throws Exception {
        System.out.println("DAO occlist query");
        Vector v = new Vector();
        String query = "SELECT NAME, TIMESTAMP, SHARE FROM OCC_TABLE " + whereclause + " AND SHARE > 0 ORDER BY TIMESTAMP";
        System.out.println("QUERY: " + query);
        Statement stmt = null;
        whereclause = "";
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            whereclause = e.toString();
            System.out.println(e.toString());
            throw e;

        }
        return v;
    }

    public String[][] getOccListArrayFromQuery(String whereclause, boolean datefilter) throws Exception {
        System.out.println("DaO occlist query " + whereclause);
        Vector names = new Vector();
        Vector serials = new Vector();
        String query = "SELECT NAME, TIMESTAMP, OBS_DATE, SERIAL, SHARE FROM OCC_TABLE " + whereclause + "  AND SHARE > 0 ORDER BY TIMESTAMP";
        System.out.println("QUERY: " + query);
        Statement stmt = null;
        whereclause = "";
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                java.util.Date date = rs.getDate("OBS_DATE");
                if ((datefilter) && (date.after(startDate))) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                } else if (!datefilter) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                }
            }
        } catch (Exception e) {
            whereclause = e.toString();
            System.out.println(e.toString());
            throw e;

        }
        System.out.println(serials.size() + " OCCs retieved.");
        String[][] list = new String[2][serials.size()];

        int i = 0;
        Enumeration e = serials.elements();
        Enumeration ee = names.elements();
        while (e.hasMoreElements()) {
            String ser = (String) e.nextElement();
            String nm = (String) ee.nextElement();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }

    public String[][] getOccListArray(boolean datefilter) {
        Vector names = new Vector();
        Vector serials = new Vector();
        String query = "SELECT NAME, TIMESTAMP, SERIAL, OBS_DATE, SHARE FROM OCC_TABLE WHERE SHARE > 0 ORDER BY TIMESTAMP";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                java.util.Date date = rs.getDate("OBS_DATE");
                if ((datefilter) && (date.after(startDate))) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                } else if (!datefilter) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        String[][] list = new String[2][serials.size()];

        int i = 0;
        Enumeration e = serials.elements();
        Enumeration ee = names.elements();
        while (e.hasMoreElements()) {
            String ser = (String) e.nextElement();
            String nm = (String) ee.nextElement();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }

    public Vector getOccList() {
        Vector v = new Vector();
        String query = "SELECT NAME, TIMESTAMP, SHARE FROM OCC_TABLE WHERE SHARE > 0 ORDER BY TIMESTAMP";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public Vector getLocationList() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT LOC_NAME FROM PIT_TABLE ORDER BY LOC_NAME";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public Vector getRangeList() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT MTN_RANGE, OBS_DATE FROM PIT_TABLE ORDER BY MTN_RANGE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATE");
                if (pitDate.after(startDate)) {
                    String s = rs.getString(1);
                    s = s.trim();
                    if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                        v.add(s);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    /////////////////
    public Vector getRangeListAll() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT MTN_RANGE, OBS_DATE FROM PIT_TABLE ORDER BY MTN_RANGE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATE");
                String s = rs.getString(1);
                s = s.trim();
                if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                    v.add(s);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }
    ///////////////

    public Vector getStateList() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT STATE, OBS_DATE FROM PIT_TABLE ORDER BY STATE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATE");
                if (pitDate.after(startDate)) {
                    String s = rs.getString(1);
                    s = s.trim();
                    if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                        v.add(s);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    ////////////////
    public Vector getStateListAll() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT STATE, OBS_DATE FROM PIT_TABLE ORDER BY STATE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATE");
                String s = rs.getString(1);
                s = s.trim();
                if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                    v.add(s);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }
    ////////////////////

    public Vector getPitList(String user) {
        Vector v = new Vector();
        String query = "SELECT PIT_NAME, OBS_DATE FROM PIT_TABLE WHERE USERNAME ='" + user + "' AND SHARE > 0 ORDER BY OBS_DATE DESC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public Vector getOccList(String user) {
        Vector v = new Vector();
        String query = "SELECT NAME, TIMESTAMP FROM OCC_TABLE WHERE USERNAME ='" + user + "' ORDER BY TIMESTAMP";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public boolean deletePit(avscience.ppc.PitObs pit) {
        System.out.println("deletePit. PPC");
        boolean del = false;
        if ((pit != null) && (pit.getUser() != null)) {
            String name = pit.getDBName();
            String oname = pit.getName();
            String aname = pit.getArchName();
            String user = pit.getUser().getName();
            String serial = pit.getSerial();
            String query = null;
            System.out.println("name: " + name + " oname: " + oname + " serial: " + serial + " user: " + user);

            if ((serial != null) && (serial.trim().length() > 0)) {
                query = "DELETE FROM PIT_TABLE WHERE LOCAL_SERIAL = ? AND USERNAME = ?";
                try {
                    PreparedStatement stmt = getConnection().prepareStatement(query);
                    stmt.setString(1, serial);
                    stmt.setString(2, user);
                    stmt.executeUpdate();

                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

            query = "DELETE FROM PIT_TABLE WHERE PIT_NAME = ? AND USERNAME = ?";
            try {
	           // PreparedStatement stmt = getConnection().prepareStatement(query);
                // stmt.setString(2, user);

                if ((name != null) && (name.trim().length() > 0)) {
                    PreparedStatement stmt = getConnection().prepareStatement(query);
                    stmt.setString(2, user);
                    stmt.setString(1, name);
                    stmt.executeUpdate();
                }

                if ((aname != null) && (aname.trim().length() > 0)) {
                    PreparedStatement stmt = getConnection().prepareStatement(query);
                    stmt.setString(2, user);
                    stmt.setString(1, aname);
                    stmt.executeUpdate();
                }

                if ((oname != null) && (oname.trim().length() > 0)) {
                    PreparedStatement stmt = getConnection().prepareStatement(query);
                    stmt.setString(2, user);
                    stmt.setString(1, oname);
                    stmt.executeUpdate();
                }

            } catch (Exception e) {
                System.out.println(e.toString());
            }

        }
        return del;
    }

    public boolean deletePit(String user, String serial, String name) {
        System.out.println("deletePit. " + serial + " " + name);
        boolean del = false;
        String query = null;
        int n = 0;
        try {

            if ((serial != null) && (serial.trim().length() > 1)) {
                query = "DELETE FROM PIT_TABLE WHERE LOCAL_SERIAL = ? AND USERNAME = ?";
                PreparedStatement stmt = getConnection().prepareStatement(query);
                stmt.setString(1, serial);
                stmt.setString(2, user);
                n = stmt.executeUpdate();
            } else {
                query = "DELETE FROM PIT_TABLE WHERE PIT_NAME = ? AND USERNAME = ?";
                PreparedStatement stmt = getConnection().prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, user);
                n = stmt.executeUpdate();
            }
            if (n > 0) {
                System.out.println("pit deleted: " + serial);
                del = true;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return del;
    }

    public void deletePit(String dbserial) {
        String query = "DELETE FROM PIT_TABLE WHERE SERIAL = " + dbserial;
        try {
            Statement stmt = getConnection().createStatement();
            stmt.executeUpdate(query);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean deleteOcc(String user, String serial, String name) {
        System.out.println("deleteOcc. " + serial + " " + name);
        boolean del = false;
        String query = null;
        if ((serial != null) && (serial.trim().length() > 1)) {
            query = "DELETE FROM OCC_TABLE WHERE LOCAL_SERIAL = '" + serial + "' AND USERNAME = '" + user + "'";

        } else {
            query = "DELETE FROM OCC_TABLE WHERE PIT_NAME = '" + name + "' AND USERNAME = '" + user + "'";
        }

        try {
            Statement stmt = getConnection().createStatement();
            int n = stmt.executeUpdate(query);
            if (n > 0) {
                System.out.println("OCC deleted: " + serial);
                del = true;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        //  deletePit(user, serial, name);
        return del;
    }

    private void deleteOcc(avscience.ppc.AvOccurence occ) {
        System.out.println("DELETE OCC. ");
        String name = occ.getPitName();
        String serial = occ.getSerial();
        System.out.println("OCC SERIAL: " + serial);
        avscience.ppc.PitObs pit = null;

        if ((serial == null) || (serial.trim().length() < 2)) {
            System.out.println("getting pit by name: " + name);
            String data = getPit(name);
            pit = new avscience.ppc.PitObs(data);
            name = pit.getDBName();
        } else {
            System.out.println("getting pit by serial: " + serial);
            String data = getPitByLocalSerial(serial);
            pit = new avscience.ppc.PitObs(data);
        }
        String user = pit.getUser().getName();
        deleteOcc(user, serial, name);
    }

    public String getPit(String name) {
        System.out.println("DAO: getting WBA pit: " + name);
        //  avscience.wba.PitObs pit = null;
        String s = "";
        String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE PIT_NAME = ?";
        PreparedStatement stmt = null;
        if ((name != null) && (name.trim().length() > 0)) {
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    System.out.println("pit in DB");

                    s = rs.getString("PIT_DATA");
                    if (s != null) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return s;
    }

    public String getPitByLocalSerial(String ser) {
        System.out.println("getPitByLocalSerial()");
        String s = "";
        String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE LOCAL_SERIAL = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, ser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("PIT in DB");

                s = rs.getString("PIT_DATA");
                // System.out.println("Data: "+s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return s;
    }

    public void cleanOccData() {
        /*String query = "SELECT * FROM OCC_TABLE";
         Statement stmt = null;
         try
         {
         stmt = getConnection().createStatement();
         ResultSet rs = stmt.executeQuery(query);
         while (rs.next())
         {
         String data = rs.getString("OCC_DATA");
         int ser = rs.getInt("SERIAL");
         data = URLDecoder.decode(data, "UTF-8");
         String q = "UPDATE OCC_TABLE SET OCC_DATA = ? WHERE SERIAL = ?";
         PreparedStatement stat = getConnection().prepareStatement(q);
         stat.setString(1, data);
         stat.setInt(2, ser);
         int f = stat.executeUpdate();
         }
         }
         catch(Exception e){System.out.println(e.toString());}*/
    }

    public String getPPCOcc(String serial) {
        System.out.println("DAO: getting PPC occ: # " + serial);
        // avscience.ppc.PitObs pit = null;
        String query = "SELECT OCC_DATA FROM OCC_TABLE WHERE SERIAL = ?";
        PreparedStatement stmt = null;
        String s = "";
        if ((serial != null) && (serial.trim().length() > 0)) {
            int ser = 0;
            ser = new java.lang.Integer(serial).intValue();
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setInt(1, ser);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.println("OCC in DB");
                    //	System.out.println("Data: "+s);
                    s = rs.getString("OCC_DATA");
                } else {
                    System.out.println("PPC OCC query failed:.");
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return s;
    }

    public String getPPCPit(String serial) {
        System.out.println("DAO: getting PPC pit: # " + serial);

        // avscience.ppc.PitObs pit = null;
        String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE SERIAL = ?";
        PreparedStatement stmt = null;
        String s = "";
        if ((serial != null) && (serial.trim().length() > 0)) {
            int ser = 0;
            ser = new java.lang.Integer(serial).intValue();
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setInt(1, ser);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.println("pit in DB");
                    //	System.out.println("Data: "+s);
                    s = rs.getString("PIT_DATA");
                    System.out.println("PIT Data: " + s);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        //System.out.println("Data: " + s);
        return s;
    }

    public avscience.wba.AvOccurence getOcc(String name) {
        System.out.println("DAO: getting occ: " + name);
        avscience.wba.AvOccurence occ = null;
        String query = "SELECT OCC_DATA FROM OCC_TABLE WHERE NAME = ?";
        PreparedStatement stmt = null;
        if ((name != null) && (name.trim().length() > 0)) {
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("occ in DB.");
                    String data = rs.getString("OCC_DATA");
                    if ((data != null) && (data.trim().length() > 0)) {
                        occ = new avscience.wba.AvOccurence(data);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return occ;
    }

    public void tallyTests() {
        System.out.println("TallyTests()");
        String[] queries = new String[10];
        String[] years = {"2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012"};
        String[] types = ShearTests.getInstance().getShearTestDescriptions();
        int[] total = new int[years.length];
        int[][] testTotal = new int[years.length][types.length];
        int[] prof = new int[years.length];
        int[] testPits = new int[years.length];
        int[] ectNotes = new int[years.length];
        int[] pstNotes = new int[years.length];

        queries[0] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2003-01-01' AND OBS_DATE < '2004-01-01' ORDER BY OBS_DATE DESC";
        queries[1] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2004-01-01' AND OBS_DATE < '2005-01-01' ORDER BY OBS_DATE DESC";
        queries[2] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2005-01-01' AND OBS_DATE < '2006-01-01' ORDER BY OBS_DATE DESC";
        queries[3] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2006-01-01' AND OBS_DATE < '2007-01-01' ORDER BY OBS_DATE DESC";
        queries[4] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2007-01-01' AND OBS_DATE < '2008-01-01' ORDER BY OBS_DATE DESC";
        queries[5] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2008-01-01' AND OBS_DATE < '2009-01-01' ORDER BY OBS_DATE DESC";
        queries[6] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2009-01-01' AND OBS_DATE < '2010-01-01' ORDER BY OBS_DATE DESC";
        queries[7] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2010-01-01' AND OBS_DATE < '2011-01-01' ORDER BY OBS_DATE DESC";
        queries[8] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2011-01-01' AND OBS_DATE < '2012-01-01' ORDER BY OBS_DATE DESC";
        queries[9] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2012-01-01' ORDER BY OBS_DATE DESC";

        for (int i = 0; i < queries.length; i++) {
            System.out.println("Getting tests for query: " + queries[i]);
            Statement stmt = null;
            try {
                stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(queries[i]);

                while (rs.next()) {
                    String s = rs.getString("SERIAL");
                    String data = getPPCPit(s);
                    avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                    if (pit.getPitNotes().contains("ECT")) {
                        ectNotes[i]++;
                    }
                    if (pit.getPitNotes().contains("PST")) {
                        pstNotes[i]++;
                    }
                    Enumeration tests = pit.getShearTests();
                    if (tests != null) {

                        if (tests.hasMoreElements()) {
                            testPits[i]++;
                            try {
                                avscience.ppc.User u = pit.getUser();
                                boolean prf = u.getProf();
                                if (prf) {
                                    prof[i]++;
                                }
                            } catch (Exception e) {
                                System.out.println(e.toString());
                            }

                        }
                    }

                    for (int j = 0; j < types.length; j++) {
                        tests = pit.getShearTests();
                        if (tests != null) {
                            while (tests.hasMoreElements()) {
                                avscience.ppc.ShearTestResult result = (avscience.ppc.ShearTestResult) tests.nextElement();
                                String cd = result.getCode();
                                String type = ShearTests.getInstance().getShearTestByCode(cd).getType();
                                if (types[j].equals(type)) {
                                    testTotal[i][j]++;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        System.out.println("Writing test tally results to file:");
        FileOutputStream out = null;
        PrintWriter writer = null;
        File file = new File("TestSummaryByYear.txt");
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(" ,");

        for (int i = 0; i < years.length; i++) {
            buffer.append(years[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("Pits with tests: ,");

        for (int i = 0; i < testPits.length; i++) {
            buffer.append(testPits[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("Pits by professional: ,");

        for (int i = 0; i < prof.length; i++) {
            buffer.append(prof[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        for (int i = 0; i < types.length; i++) {
            buffer.append(types[i]);
            buffer.append(",");
            for (int j = 0; j < years.length; j++) {
                buffer.append(testTotal[j][i]);
                buffer.append(",");
            }
            buffer.append("\n");
        }
        buffer.append(",");
        for (int i = 0; i < total.length; i++) {
            buffer.append(total[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("ECT in notes,");
        for (int i = 0; i < ectNotes.length; i++) {
            buffer.append(ectNotes[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("PST in notes,");
        for (int i = 0; i < pstNotes.length; i++) {
            buffer.append(pstNotes[i]);
            buffer.append(",");
        }
        buffer.append("\n");
        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

}
