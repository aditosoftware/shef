package net.atlanticbb.tantlinger.ui.text.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import java.awt.event.ActionEvent;

/**
 * Testet das Ändern des Block Typs.
 * Mithilfe dieses Tests sollen Differenzen zum gegenwärtigen Zustand ausgemacht werden.
 *
 * Dieser Test prüft also nicht unbedingt auf Fehlerfreiheit.
 *
 * @author k.mifka, 30.03.2017
 */
public class Test_HTMLBlockAction
{
    private ShefTestEnvironment shef;
    private HTMLBlockAction[] allActions = new HTMLBlockAction[12];
    private static final String EDITOR = "editor";

    @Before
    public void setup()
    {
        shef = new ShefTestEnvironment();

        for (int i = 0; i < allActions.length; i++)
        {
            allActions[i] = new HTMLBlockAction(allActions.length - i - 1);
            allActions[i].putValue(EDITOR, shef.getEditor());
            allActions[i].putContextValue(EDITOR, shef.getEditor());
        }
    }

    @Test
    public void testChangeBlockType()
    {
        shef.setHTML("<p>Zeile 1</p><div>Zeile 2</div>");
        shef.selectAll();

        _testAllBlockActions("\n" + shef.getText() + "\n");

        shef.setHTML("<p>Zeile 1</p><h1></h1><h2></h2><div>Zeile 2</div>");
        shef.selectAll();

        _testAllBlockActions("\n\nZeile 1\n\nZeile 2\n");

        shef.setHTML("<p>Zeile 1</p>");
        shef.setCaretPosition(8);
        shef.pressEnter();
        shef.pressEnter();

        _testAllBlockActions(shef.getText() + "\n");
        
        shef.setHTML("<ul>\n  <li>\nZeile 1\n  </li>\n  <li>\nZeile 2\n  </li>\n  <li>\nZeile 3\n  </li>\n</ul>");
        shef.selectAll();
        shef.pressEnter();
        shef.pressEnter();

        _testAllBlockActions(shef.getText());
    }

    /**
     * Testet alle Blocktypen
     *
     * @param pExpectedText Der erwartete Text
     */
    private void _testAllBlockActions(String pExpectedText)
    {
        shef.saveCaretSelection();

        ActionEvent event = new ActionEvent(shef.getEditor(), ActionEvent.ACTION_PERFORMED, null);

        for (HTMLBlockAction action : allActions)
        {
            try
            {
                action.actionPerformed(event);
            }
            catch (Exception pE)
            {
                Assert.fail();
            }

            shef.restoreCaretSelection();

            _testBlocks(action);

            String newText = shef.getText();
            Assert.assertEquals(newText, pExpectedText);
        }
    }

    /**
     * Überprüft, ob der Blocktyp in allen Selektierten Blöcken richtig umgesetzt wurde
     *
     * @param action verwendete Action
     */
    private void _testBlocks(HTMLBlockAction action)
    {
        HTML.Tag tag = action.getTag();

        if(tag == HTML.Tag.UL || tag == HTML.Tag.OL) //Wenn es sich um eine Liste handelt
            tag = HTML.Tag.LI;

        for (Element element : shef.getSelectedElements())
        {
            String[] split = shef.getHTML(element).split("<");

            if(split.length == 1)
                split = shef.getHTML(element.getParentElement()).split("<");

            for (int i = 1; i < split.length; i++)
                if(!split[i].startsWith("/" + tag.toString()) && !split[i].startsWith(tag.toString()))
                    Assert.fail();
        }
    }
}
