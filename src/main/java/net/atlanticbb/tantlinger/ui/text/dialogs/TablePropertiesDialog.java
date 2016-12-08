/*
 * Created on Dec 21, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;


import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.OptionDialog;
import net.atlanticbb.tantlinger.ui.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class TablePropertiesDialog extends OptionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    
    private static Icon icon = UIUtils.getIcon(UIUtils.X48, "table.png"); //$NON-NLS-1$
    private static String title = i18n.str("table_properties"); //$NON-NLS-1$
    private static String desc = i18n.str("table_properties_desc"); //$NON-NLS-1$
    
    private TableAttributesPanel tableProps = new TableAttributesPanel();
    private RowAttributesPanel rowProps = new RowAttributesPanel();
    private CellAttributesPanel cellProps = new CellAttributesPanel();    
    
    public TablePropertiesDialog(Frame parent)
    {
        super(parent, title, desc, icon);
        init();       
    }
    
    public TablePropertiesDialog(Dialog parent)
    {
        super(parent, title, desc, icon);
        init();   
    }
    
    private void init()
    {
        Border emptyBorder = new EmptyBorder(5, 5, 5, 5);
        Border titleBorder = BorderFactory.createTitledBorder(i18n.str("table_properties")); //$NON-NLS-1$
        
        tableProps.setBorder(BorderFactory.createCompoundBorder(emptyBorder, titleBorder));
        rowProps.setBorder(emptyBorder);
        cellProps.setBorder(emptyBorder);
        
        JTabbedPane tabs = new JTabbedPane();        
        tabs.add(tableProps, i18n.str("table"));         //$NON-NLS-1$
        tabs.add(rowProps, i18n.str("row")); //$NON-NLS-1$
        tabs.add(cellProps, i18n.str("cell")); //$NON-NLS-1$
        
        setContentPane(tabs);
        setMinimumSize(new Dimension(440, 375));
        setResizable(false);
    }
    
    public void setTableAttributes(Map at)
    {        
        tableProps.setAttributes(at);
    }
    
    public void setRowAttributes(Map at)
    {        
        rowProps.setAttributes(at);
    }
    
    public void setCellAttributes(Map at)
    {        
        cellProps.setAttributes(at);
    }
    
    public Map getTableAttributes()
    {
        return tableProps.getAttributes();
    }
    
    public Map getRowAttribures()
    {
        return rowProps.getAttributes();
    }
    
    public Map getCellAttributes()
    {
        return cellProps.getAttributes();
    }    
}
