/*
 * Created on Jun 19, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;

import org.bushe.swing.action.ActionManager;
import org.bushe.swing.action.ShouldBeEnabledDelegate;


public class PasteAction extends HTMLTextEditAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JEditorPane wys, src;

    public PasteAction()
    {
        super(i18n.str("paste"));
        putValue(MNEMONIC_KEY, new Integer(i18n.mnem("paste")));
        putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "paste.png"));
        putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "paste.png"));
		putValue(ACCELERATOR_KEY,
			KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        addShouldBeEnabledDelegate(new ShouldBeEnabledDelegate()
        {
            public boolean shouldBeEnabled(Action a)
            {                          
                //return getCurrentEditor() != null &&
                //    Toolkit.getDefaultToolkit().getSystemClipboard().getContents(PasteAction.this) != null;
                return true;
            }
        });
        
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    public void setEditors(JEditorPane pSrc, JEditorPane pWys)
    {
        src = pSrc;
        wys = pWys;
    }
    
    protected void updateWysiwygContextState(JEditorPane wysEditor)
    {
        this.updateEnabledState();
    }
    
    protected void updateSourceContextState(JEditorPane srcEditor)
    {
        this.updateEnabledState();
    }

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#sourceEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        editor.paste();
    }

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#wysiwygEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HTMLEditorKit ekit = (HTMLEditorKit)wys.getEditorKit();
        HTMLDocument document = (HTMLDocument)wys.getDocument();
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        
        try 
        {
            CompoundUndoManager.beginCompoundEdit(document);
            Transferable content = clip.getContents(this);                           
            String txt = content.getTransferData(
                new DataFlavor(String.class, "String")).toString();

            if (txt.contains("\n"))
            {
                // Haesslicher Hack, damit gepastete Zeilenumbrueche in der WYS-Ansicht erhalten bleiben:
                // 1. \n durch <!--br--> ersetzen
                txt = txt.trim().replaceAll("\n", "<!--br-->\n");

                // 2. veraenderten Text im Wys-Editor einfuegen
                document.replace(wys.getSelectionStart(),
                        wys.getSelectionEnd() - wys.getSelectionStart(),
                        txt, ekit.getInputAttributes());

                // 3. aktuellen wys-Text mit escapetem <!--br--> in Src-Ansicht uebernehmen => richtige <br> werden eingefuegt
                src.setText(wys.getText());

                // 4. wys-Ansicht aus Src wiederherstellen => <br>s sind jetzt richtige Zeilenumbrueche
                wys.setText(src.getText());
            }
            else
            {
                document.replace(wys.getSelectionStart(),
                        wys.getSelectionEnd() - wys.getSelectionStart(),
                        txt, ekit.getInputAttributes());
            }

        }
        catch(Exception ex)
        {
            //ex.printStackTrace();
        }
        finally
        {
            CompoundUndoManager.endCompoundEdit(document);
        }
    }    
}
