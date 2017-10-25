package net.atlanticbb.tantlinger.ui.text;

import javax.swing.text.*;

/**
 * @author j.boesl, 25.10.17
 */
class WysiwygHTMLEditorDocumentFilter extends DocumentFilter
{
  @Override
  public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
  {
    super.insertString(fb, offset, _fix(string), attr);
  }

  @Override
  public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
  {

    super.replace(fb, offset, length, _fix(text), attrs);
  }

  private String _fix(String pText)
  {
    return pText
        .replace("\t", "\u00A0\u00A0\u00A0\u00A0")
        .replace(" ", "\u00A0");
  }
}
