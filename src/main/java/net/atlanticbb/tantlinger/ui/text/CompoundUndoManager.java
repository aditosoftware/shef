/*
 * Created on Jun 7, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;
import org.bushe.swing.action.ActionManager;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;



//TODO add static method to unregister documents

/**
 * Manages compound undoable edits. 
 * 
 * Before an undoable edit happens on a particular document, you should call
 * the static method CompoundUndoManager.beginCompoundEdit(doc)
 * 
 * Conversely after an undoable edit happens on a particular document, 
 * you shoulc call the static method CompoundUndoManager.beginCompoundEdit(doc)
 * 
 * For either of these methods to work, you must add an instance of
 * CompoundUndoManager as a document listener... e.g
 * 
 * doc.addUndoableEditListener(new CompoundUndoManager(doc, new UndoManager());
 * 
 * Note that each CompoundUndoManager should have its own UndoManager.
 * 
 * 
 * @author Bob Tantlinger
 */
public class CompoundUndoManager implements UndoableEditListener
{   
    private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text");
    
    /**
     * Static undo action that works across all documents 
     * with a CompoundUndoManager registered as an UndoableEditListener
     */
    public static Action UNDO = new UndoAction();
    
    /**
     * Static undo action that works across all documents 
     * with a CompoundUndoManager registered as an UndoableEditListener
     */
    public static Action REDO = new RedoAction();
        
    private UndoManager undoer;
    private CompoundEdit compoundEdit = null;
    private WeakReference<Document> document = null;

    public static Map<Document, CompoundUndoManager> lsts = new WeakHashMap<>();
    public static Map<Document, UndoManager> undoers =  new WeakHashMap<>();
    
    protected static void registerDocument(Document doc, CompoundUndoManager lst, UndoManager um)
    {
        lsts.put(doc, lst);
        undoers.put(doc, um);
    }

    /**
     * Notifies the CompoundUndoManager for the specified Document that 
     * a compound edit is about to begin.
     * 
     * @param doc
     */
    public static void beginCompoundEdit(Document doc)
    {
        CompoundUndoManager c = lsts.get(doc);
        if (c != null)
            c.beginCompoundEdit();
    }
    
    /**
     * Notifies the CompoundUndoManager for the specified Document that 
     * a compound edit is complete.
     * 
     * @param doc
     */
    public static void endCompoundEdit(Document doc)
    {
        CompoundUndoManager c = lsts.get(doc);
        if (c != null)
            c.endCompoundEdit();
    }
    
    /**
     * Updates the enabled states of the UNDO and REDO actions
     * for the specified document
     * @param doc
     */
    public static void updateUndo(Document doc)
    {
        UndoManager um = undoers.get(doc);
        if(um != null)
        {
            UNDO.setEnabled(um.canUndo());
            REDO.setEnabled(um.canRedo());
        }
    }
    
    /**
     * Discards all edits for the specified Document
     * 
     * @param doc
     */
    public static void discardAllEdits(Document doc)
    {
        UndoManager um = undoers.get(doc);
        if(um != null)
        {
            um.discardAllEdits();
            UNDO.setEnabled(um.canUndo());
            REDO.setEnabled(um.canRedo());
        }
    }

    /**
     * Creates a new CompoundUndoManager
     * 
     * @param doc
     * @param um The UndoManager to use for this document
     */
    public CompoundUndoManager(Document doc, UndoManager um)
    {
       undoer = um;
       document = new WeakReference<>(doc);
       registerDocument(doc, this, undoer);
    }
    
    /**
     * Creates a new CompoundUndoManager
     * @param doc
     */
    public CompoundUndoManager(Document doc)
    {
        this(doc, new UndoManager());
    }
    
    public void undoableEditHappened(UndoableEditEvent evt)
    {       
        UndoableEdit edit = evt.getEdit();
        if(compoundEdit != null)
        {             
            //System.out.println("adding to compound");
            compoundEdit.addEdit(edit);               
        }
        else
        {            
            undoer.addEdit(edit);
            updateUndo(document.get());
        }        
    }
    
    protected void beginCompoundEdit()
    {
        //System.out.println("starting compound");
        compoundEdit = new CompoundEdit();
    }
    
    protected void endCompoundEdit()
    {
        //System.out.println("ending compound");
        if(compoundEdit != null)
        {
            compoundEdit.end();
            undoer.addEdit(compoundEdit);
            updateUndo(document.get());
        }
        compoundEdit = null;
    }    
    
    static class UndoAction extends TextAction 
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            super(i18n.str("undo"));                
            putValue(Action.SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "undo.png"));
            putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "undo.png"));
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("undo")));

            setEnabled(false);
            putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
            putValue(SHORT_DESCRIPTION, getValue(NAME));
        }

        public void actionPerformed(ActionEvent e) 
        {
            Document doc = getTextComponent(e).getDocument();
            UndoManager um = undoers.get(doc);
            if(um != null)
            {
                try 
                {               
                    um.undo();
                } 
                catch (CannotUndoException ex) 
                {
                    System.out.println("Unable to undo: " + ex);
                    ex.printStackTrace();
                }
                
                updateUndo(doc);
            }            
        }
    }

    static class RedoAction extends TextAction 
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public RedoAction() 
        {
            super(i18n.str("redo"));            
            putValue(Action.SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "redo.png"));
            putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "redo.png"));
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("redo")));
            
            setEnabled(false);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_Y, InputEvent.CTRL_MASK));
            putValue(SHORT_DESCRIPTION, getValue(NAME));
        }

        public void actionPerformed(ActionEvent e) 
        {
            Document doc = getTextComponent(e).getDocument();
            UndoManager um = undoers.get(doc);
            if(um != null)
            {
                try 
                {               
                    um.redo();
                } 
                catch (CannotUndoException ex) 
                {
                    System.out.println("Unable to redo: " + ex);
                    ex.printStackTrace();
                }
                
                updateUndo(doc);
            }
        }
    }
}