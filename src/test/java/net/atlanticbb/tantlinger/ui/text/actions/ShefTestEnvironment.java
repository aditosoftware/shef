package net.atlanticbb.tantlinger.ui.text.actions;

import net.atlanticbb.tantlinger.ui.text.ElementWriter;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.WysiwygHTMLEditorKit;
import org.junit.Assert;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Testumgebung zum Testen der EditorPane
 *
 * @author k.mifka, 10.04.2017
 */
class ShefTestEnvironment
{
    private JEditorPane pane;
    private int selectionStart;
    private int selectionEnd;
    private int caretPosition;

    ShefTestEnvironment()
    {
        pane = new JEditorPane();
        WysiwygHTMLEditorKit editorKit = new WysiwygHTMLEditorKit();
        pane.setEditorKitForContentType("text/html", editorKit);
        pane.setContentType("text/html");
    }

    /**
     * @return Gibt den Editor zurück
     */
    JEditorPane getEditor()
    {
        return pane;
    }

    /**
     * @return Gibt das Document zurück
     */
    HTMLDocument getDocument()
    {
        Document document = pane.getDocument();

        if(document instanceof HTMLDocument)
        {
            return (HTMLDocument) document;
        }
        else
        {
            Assert.fail();
            return new HTMLDocument();
        }
    }

    /**
     * Gibt den HTML Code für das übergebene Element zurück
     *
     * @param pElement das gewünschte Element
     * @return HTML
     */
    String getHTML(Element pElement)
    {
        StringWriter out = new StringWriter();
        ElementWriter w = new ElementWriter(out, pElement);
        try
        {
            w.write();
        }
        catch (Exception pE)
        {
            //Egal
        }

        return out.toString();
    }

    /**
     * Setzt den HTML Code
     *
     * @param html den zu setzenden Code
     */
    void setHTML(String html)
    {
        try
        {
            HTMLEditorKit kit = (HTMLEditorKit) pane.getEditorKit();
            Document doc = pane.getDocument();
            doc.remove(0, doc.getLength());
            StringReader reader = new StringReader(HTMLUtils.jEditorPaneizeHTML(html));
            kit.read(reader, doc, 0);
        }
        catch (Exception ex)
        {
            Assert.fail();
        }
    }

    /**
     * @return Gibt den Text zurück
     */
    String getText()
    {
        Document doc = pane.getDocument();
        String text = null;

        try
        {
            text = doc.getText(0, doc.getLength());
        }
        catch (BadLocationException pE)
        {
            //Kann nicht passieren, da immer nur die Länge des Documents übergeben wird
        }

        return text;
    }

    /**
     * Speichert die aktuelle Caret Position und Selektion
     */
    void saveCaretSelection()
    {
        selectionStart = pane.getSelectionStart();
        selectionEnd = pane.getSelectionEnd();
        caretPosition = pane.getCaretPosition();
    }

    /**
     * Stellt die gespeicherte Caret Position und Selektion wieder her
     */
    void restoreCaretSelection()
    {
        select(selectionStart, selectionEnd);
        setCaretPosition(caretPosition);
    }

    /**
     * Setzt die Position des Cursors
     *
     * @param pPosition Position
     */
    void setCaretPosition(int pPosition)
    {
        pane.setCaretPosition(pPosition);
    }

    void selectAll()
    {
        pane.selectAll();
    }

    /**
     * Selektiert den gewünschten Bereich
     *
     * @param pStart Startindex
     * @param pEnd Endindex
     */
    void select(int pStart, int pEnd)
    {
        pane.select(pStart, pEnd);
    }

    /**
     * Gibt alle selektierten Elemente zurück
     *
     * @return selektierte Elemente
     */
    Element[] getSelectedElements()
    {
        List<Element> result = new ArrayList<>();

        HTMLDocument doc = getDocument();

        Element curE = doc.getParagraphElement(pane.getSelectionStart());
        Element endE = doc.getParagraphElement(pane.getSelectionEnd());

        while (curE.getEndOffset() <= endE.getEndOffset() && curE.getEndOffset() <= doc.getLength())
        {
            result.add(curE);
            curE = doc.getParagraphElement(curE.getEndOffset() + 1);
        }

        return result.toArray(new Element[0]);
    }

    /**
     * Simuliert das Drücken der Entertaste
     */
    void pressEnter()
    {
        ActionEvent event = new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, null);
        EnterKeyAction action = new EnterKeyAction(pane.getActionMap().get("insert-break"));
        action.actionPerformed(event);
    }
}
