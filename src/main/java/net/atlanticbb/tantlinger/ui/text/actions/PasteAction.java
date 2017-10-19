/*
 * Created on Jun 19, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import org.bushe.swing.action.ActionManager;
import org.bushe.swing.action.ShouldBeEnabledDelegate;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;


public class PasteAction extends HTMLTextEditAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

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
        HTMLEditorKit ekit = (HTMLEditorKit)editor.getEditorKit();
        HTMLDocument document = (HTMLDocument)editor.getDocument();

        int selectStart = editor.getSelectionStart();
        int selectEnd = editor.getSelectionEnd();

        String clipText = _getClipboardText();

        if(clipText == null)
            return;

        String unmodHTML = HTMLUtils.getElementHTML(document.getDefaultRootElement(), true);

        try
        {

            CompoundUndoManager.beginCompoundEdit(document);
            String lineSeparator = "\n";

            Element startElem = document.getParagraphElement(selectStart);
            Element endElem = document.getParagraphElement(selectEnd);

            HTML.Tag t = HTMLUtils.getTag(startElem);

            /* Bei Div-Tags werden beim Einfügen standardmäßig alle Zeilen in ein einziges Tag eingefügt
             * Weil dieses Verhalten vom Shef-Editor nicht erwartet wird und zu Problemen führt (z.B. #15367),
             * muss das Einfügen selber erledigt werden.
             * Die Vorgehensweise ist etwas komplex und wird deshalb im Rahmen eines AsciiDoc Dokuments erläutert (story15367.adoc)*/

            if (clipText.contains(lineSeparator) && t == HTML.Tag.DIV)
            {
                // Informationen werden gesammelt...

                String[] lines = clipText.split(lineSeparator);

                int endLength = endElem.getEndOffset() - selectEnd; // Anzahl von Zeichen zwischen Zeilenanfang und Selektionsanfang
                int startLength = endElem.getEndOffset() - endElem.getStartOffset() - endLength; // Anzahl von Zeichen zwischen Selektionsende und Zeilenende

                Element selectStartElement = startElem.getElement(startElem.getElementIndex(selectStart));
                AttributeSet attr = selectStartElement.getAttributes();

                String endHTML = HTMLUtils.createTag(t, HTMLUtils.getElementHTML(endElem, true));
                String lastLine = lines[lines.length - 1];

                // Schritt 1: Erste Zeile in vorhandenes Tag einfügen

                int replLength = startElem.getEndOffset() - selectStart - 1;
                document.replace(selectStart, replLength, lines[0], attr); // <- Hier geschieht das Einfügen

                // Schritt 2: Ursprünglich vorhandenen Tag (endHTML) unter den eben manipulierten Tag einfügen

                Element parentEndElement = endElem.getParentElement();
                editor.setCaretPosition(parentEndElement.getEndOffset() - 1);
                HTMLUtils.insertHTML(endHTML, t, editor);
                Element newEndElem = document.getParagraphElement(parentEndElement.getEndOffset() + 1);// +1 damit das Element nach dem parentEndElement zurückgegeben wird

                int caret = startElem.getEndOffset();

                // Schritt 3: Letzte Zeile in eingefügten Tag einfügen

                document.replace(caret, (newEndElem.getStartOffset() - caret) + startLength, lastLine, attr); // <- Hier geschieht das zweite Einfügen

                // Schritt 4: Fehlende Zeilen zwischen der Ersten und der Letzten einfügen
                // Caret wird positioniert, um bei HTMLUtils.insertHTML an der richtigen Stelle einzufügen

                editor.setCaretPosition(caret);

                for (int i = 1; i < lines.length - 1; i++) //Erste und Letzte werden ignoriert
                {
                    String tag = HTMLUtils.createTag(t, attr, "");
                    HTMLUtils.insertHTML(tag, t, editor);
                    Element element = document.getParagraphElement(caret);
                    document.replace(element.getStartOffset(), 0, lines[i], attr);
                    caret = element.getEndOffset();
                }

                // Schritt 5: Caret an die vom Benutzer erwartete Stelle setzten (Hinter dem eingefügten Text)

                int newCaret = editor.getCaretPosition() + lastLine.length();

                if (newCaret >= 0 && newCaret < document.getLength())
                    editor.setCaretPosition(newCaret);

                return;
            }
        }
        catch(Exception ex)
        {
            try
            {
                document.remove(0, document.getLength());
                editor.setCaretPosition(0);
                document.setInnerHTML(document.getDefaultRootElement(), unmodHTML);
            }
            catch (Exception e1)
            {
                //Wenn das auch nicht mehr geht ist "Hopfen und Malz verloren!"
            }
        }
        finally
        {
            CompoundUndoManager.endCompoundEdit(document);
        }

        // Standardverfahren
        try
        {
            document.replace(selectStart, selectEnd - selectStart, clipText, ekit.getInputAttributes());
        }
        catch (Exception pE)
        {
            // Wenn die Position nicht legal ist, kann nicht eingefügt werden. -> Nix tun
        }
    }

    /**
     * Holt den Text aus der Zwischenablage
     *
     * @return Clipboard Text
     */
    private String _getClipboardText()
    {
        String txt = null;

        try
        {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable content = clip.getContents(this);
            txt = content.getTransferData(new DataFlavor(String.class, "String")).toString();
        }
        catch (IOException | UnsupportedFlavorException pE)
        {
            // Null wird zurückgeben
        }

        return txt;
    }
}
