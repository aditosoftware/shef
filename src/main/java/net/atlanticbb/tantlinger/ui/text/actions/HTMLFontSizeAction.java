/*
 * Created on Feb 27, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.*;

import javax.swing.text.*;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;

import net.atlanticbb.tantlinger.ui.text.HTMLUtils;

import org.bushe.swing.action.ActionManager;


/**
 * Action which edits HTML font size
 * 
 * @author Bob Tantlinger
 *
 */
public class HTMLFontSizeAction extends HTMLTextEditAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final int XXSMALL = 0;
    public static final int XSMALL = 1;
    public static final int SMALL = 2;
    public static final int MEDIUM = 3;
    public static final int LARGE = 4;
    public static final int XLARGE = 5;
    public static final int XXLARGE = 6;
    
    private static final String SML = i18n.str("small");
    private static final String MED = i18n.str("medium");
    private static final String LRG = i18n.str("large");
    
    public static final int FONT_SIZES[] = {8, 10, 12, 14, 18, 24, 36/*28*/};
        
    public static final String SIZES[] =
    {
        "xx-" + SML, "x-" + SML, SML, MED,
        LRG, "x-" + LRG, "xx-" + LRG
    };
    
    private int size;
    
    /**
     * Creates a new HTMLFontSizeAction
     * 
     * @param size one of the FONT_SIZES 
     * (XXSMALL, xSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE)
     * 
     * @throws IllegalArgumentException
     */
    public HTMLFontSizeAction(int size) throws IllegalArgumentException
    {
        super("");
        if(size < 0 || size > 6)
            throw new IllegalArgumentException("Invalid size");
        this.size = size;
        putValue(NAME, SIZES[size]);
        putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_RADIO);
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }
    
    protected void updateWysiwygContextState(JEditorPane ed)
    {
        /*HTMLDocument document = (HTMLDocument)ed.getDocument();        
        int caret = (ed.getCaretPosition() > 0) 
            ? (ed.getCaretPosition() - 1) : ed.getCaretPosition();
               
        AttributeSet at = document.getCharacterElement(caret).getAttributes();*/
        AttributeSet at = HTMLUtils.getCharacterAttributes(ed);
        if(at.isDefined(StyleConstants.FontSize))
        {
            int realFontSize = -1;
            try
            {
                realFontSize = Integer.parseInt(((HTMLDocument.RunElement) at).getAttribute(CSS.Attribute.FONT_SIZE).toString());
            }
            catch (Exception e)
            {
                //nix
            }
            if (realFontSize != -1)
                setSelected(realFontSize - 1 == size);
                /*setSelected(at.containsAttribute(
                    StyleConstants.FontSize, new Integer(FONT_SIZES[size])));*/
        }
        else
        {
           setSelected(size == MEDIUM);
        }        
    }
    
    protected void updateSourceContextState(JEditorPane ed)
    {
        setSelected(false);
    }

    
    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        String prefix = "<font size=" + (size + 1) + ">";
        String postfix = "</font>";
        String sel = editor.getSelectedText();
        if(sel == null)
        {
            editor.replaceSelection(prefix + postfix);
            
            int pos = editor.getCaretPosition() - postfix.length();
            if(pos >= 0)
            	editor.setCaretPosition(pos);                    		  
        }
        else
        {
            sel = prefix + sel + postfix;
            editor.replaceSelection(sel);                
        }
    }

    /* (non-Javadoc)
     * @see com.bob.blah.HTMLTextEditAction#wysiwygEditPerformed(java.awt.event.ActionEvent, javax.swing.JTextPane)
     */
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {        
        Action a = new StyledEditorKit.FontSizeAction(SIZES[size], FONT_SIZES[size]);
        a.actionPerformed(e);        
    }
}
