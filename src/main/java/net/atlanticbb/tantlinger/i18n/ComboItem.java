package net.atlanticbb.tantlinger.i18n;

/**
 * @author jones3 on 16.10.15.
 */
public class ComboItem {

    private String key;
    private String displayValue;
    I18n i18n;

    public ComboItem(String pKey, I18n pI18n)
    {
        i18n = pI18n;
        key = pKey;
        displayValue = i18n.str(key);
    }

    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return displayValue;
    }

}
