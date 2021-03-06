package avscience.wba;

public final class WaterContentSnow
    implements DataTable
{

    public static WaterContentSnow getInstance()
    {
        return instance;
    }

    private WaterContentSnow()
    {
        size = 5;
        init();
    }

    private void init()
    {
        codes = new String[size];
        descriptions = new String[size];
        codes[0] = " ";
        codes[1] = "D";
        codes[2] = "M";
        codes[3] = "W";
        codes[4] = "U";
        descriptions[0] = " ";
        descriptions[1] = "Dry";
        descriptions[2] = "Moist";
        descriptions[3] = "Wet";
        descriptions[4] = "Unknown";
    }

    public String[] getCodes()
    {
        return codes;
    }
    
    public String getCode(int i)
    {
        return codes[i];
    }

    public String getCode(String desc)
    {
        desc.trim();
        if(desc.equals("Dry"))
            return "D";
        if(desc.equals("Moist"))
            return "M";
        if(desc.equals("Wet"))
            return "W";
        if(desc.equals("Unknown"))
            return "U";
        else
            return "";
    }

    public String[] getDescriptions()
    {
        return descriptions;
    }

    private static final WaterContentSnow instance = new WaterContentSnow();
    private String codes[];
    private String descriptions[];
    private int size;

}
