package org.mightyfrog.i18n.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author Shigehiro Soejima
 */
class TextArea extends JTextArea implements MouseListener,
                                            PopupMenuListener,
                                            UndoableEditListener {
    //
    private final JPopupMenu POPUP = new JPopupMenu();
    private final JMenuItem COPY_MI =
        new JMenuItem(getActionMap().get("copy-to-clipboard"));
    private final JMenuItem CUT_MI =
        new JMenuItem(getActionMap().get("cut-to-clipboard"));
    private final JMenuItem PASTE_MI =
        new JMenuItem(getActionMap().get("paste-from-clipboard"));
    private final JMenuItem TRANSLATE_MI =
        new JMenuItem(I18N.get("Popup.MenuItem.translate"));

    //
    private final UndoManager UNDO_MANAGER = new UndoManager();

    //
    private boolean visibleWhitespace = false;

    //
    private String translateFrom = null;

    /**
     *
     */
    public TextArea() {
        this(false);
    }

    /**
     *
     */
    public TextArea(boolean visibleWhitespace) {
        this.visibleWhitespace = visibleWhitespace;

        addMouseListener(this);
        getDocument().addUndoableEditListener(this);

        COPY_MI.setText(I18N.get("Popup.MenuItem.copy"));
        CUT_MI.setText(I18N.get("Popup.MenuItem.cut"));
        PASTE_MI.setText(I18N.get("Popup.MenuItem.paste"));

        TRANSLATE_MI.addActionListener(new java.awt.event.ActionListener() {
                /** */
                public void actionPerformed(ActionEvent evt) {
                    translate();
                }
            });

        POPUP.add(COPY_MI);
        POPUP.add(CUT_MI);
        POPUP.add(PASTE_MI);
        POPUP.addSeparator();
        POPUP.add(TRANSLATE_MI);

        POPUP.addPopupMenuListener(this);

        getActionMap().put("undo", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        UNDO_MANAGER.undo();
                    } catch (CannotUndoException e) {
                        //
                    }
                }
            });
        getActionMap().put("redo", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        UNDO_MANAGER.redo();
                    } catch (CannotRedoException e) {
                        //
                    }
                }
            });
        InputMap im = getInputMap();
        im.put(KeyStroke.getKeyStroke("ctrl pressed Z"), "undo");
        im.put(KeyStroke.getKeyStroke("shift ctrl pressed Z"), "redo");

        // init font
        Map<TextAttribute, Object> map =
            new HashMap<TextAttribute, Object>(getFont().getAttributes());
        map.put(TextAttribute.FAMILY, Font.MONOSPACED);
        setFont(new Font(map));

        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /** */
    @Override
    public String getToolTipText() {
        String str = getSelectedText();
        if (!hasFocus() || str == null) {
            return null;
        }

        return I18N.get("ToolTip.character.count", str.length());
    }

    /** */
    @Override
    public void setText(String text) {
        super.setText(text);
        setCaretPosition(0);
        UNDO_MANAGER.discardAllEdits();
    }

    /** */
    @Override
    public void mouseEntered(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mousePressed(MouseEvent evt) {
        handlePopup(evt);
    }

    /** */
    @Override
    public void mouseClicked(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseReleased(MouseEvent evt) {
        handlePopup(evt);
    }

    /** */
    @Override
    public void mouseExited(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void popupMenuCanceled(PopupMenuEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
        boolean b = getCaret().getMark() != getCaret().getDot();
        CUT_MI.setEnabled(b && isEditable());
        COPY_MI.setEnabled(b);
        PASTE_MI.setEnabled(isEditable());

        TRANSLATE_MI.setEnabled(CUT_MI.isEnabled() &&
                                !Util.getRootFrame().isBothLocalesSame(false));
    }

    /** */
    @Override
    public void undoableEditHappened(UndoableEditEvent evt) {
        UNDO_MANAGER.addEdit(evt.getEdit());
    }

    /** */
    @Override
    public void append(String text) {
        if (getText() == null || getText().isEmpty()) {
            text = text.trim() + Util.LINE_SEPARATOR;
        }
        super.append(text);
    }

    // some systems have SystemColor.textHighlight=SystemColor.textText
    private final static Color TH =
        SystemColor.textHighlight.getRGB() == SystemColor.textText.getRGB() ?
        new Color(51, 153, 255) : SystemColor.textHighlight; // TH = text highlight

    /** */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.visibleWhitespace && Options.getVisibleWhitespace()) {
            int lineStart = 0;
            int lineEnd = 0;
            try {
                Rectangle rect = getVisibleRect();
                Point pStart = rect.getLocation();
                Point pEnd = new Point(rect.x, rect.y + rect.height);
                lineStart = getLineOfOffset(viewToModel(pStart));
                lineEnd = getLineOfOffset(viewToModel(pEnd));
                lineEnd = // add some buffer for painting
                    lineEnd + 2 < getLineCount() ? lineEnd + 2 : getLineCount();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            int esw = g.getFontMetrics().stringWidth(" "); // empty space width
            for (int i = lineStart; i < lineEnd; i++) {
                try {
                    int so = getLineStartOffset(i);
                    int eo = getLineEndOffset(i);
                    if (so != eo) {
                        String str = getText().substring(so, eo);
                        for (int j = 0; j < str.length(); j++) {
                            char c = str.charAt(j);
                            // whitespace support handles ideographic space only
                            if (c == ' ' || c == '\t' || c == '\u3000') {
                                if (!hasFocus()) {
                                    g.setColor(TH);
                                } else {
                                    int ss = getSelectionStart();
                                    int se = getSelectionEnd();
                                    if (ss <= so + j && so + j < se) {
                                        g.setColor(SystemColor.textInactiveText);
                                    } else {
                                        g.setColor(TH);
                                    }
                                }
                                Rectangle r = modelToView(so + j);
                                if (str.charAt(j) == ' ') {
                                    int w = esw / 2;
                                    int h = w;
                                    int x = r.x + w / 2;
                                    int y = r.y + (r.height - h) / 2;
                                    g.drawOval(x, y, w, h);
                                } else {
                                    Rectangle r2 = modelToView(so + j + 1);
                                    int x = r.x + 1;
                                    int w = r2.x - r.x - esw / 2;
                                    if (str.charAt(j) == '\t') {
                                        int h = esw / 2;
                                        int y = r.y + (r.height - h) / 2;
                                        g.drawRect(x, y, w, h);
                                    } else { // \u3000
                                        int h = r.height - esw;
                                        int y = r.y + esw / 2;
                                        g.drawRect(x, y, w, h);
                                    }
                                }
                            }
                        }
                    }
                    // visible EOL
                    eo -= (lineEnd >= 2 && i != lineEnd -1) ? 1 : 0;
                    Rectangle r = modelToView(eo);
                    g.setColor(TH);
                    g.drawLine(r.x + 6, r.y + 6, r.x + 6, r.y + 9);
                    g.drawLine(r.x + 5, r.y + 10, r.x, r.y + 10);
                    g.drawLine(r.x, r.y + 10, r.x + 2, r.y + 8);
                    g.drawLine(r.x, r.y + 10, r.x + 2, r.y + 12);
                    g.drawLine(r.x + 2, r.y + 8, r.x + 2, r.y + 11);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //
    //
    //

    /**
     * Sets the translation source language.
     *
     * @param translateFrom
     */
    void setTranslationSourceLanguage(String translateFrom) {
        this.translateFrom = translateFrom;
    }

    /**
     * Returns the translation source language.
     *
     */
    String getTranslationSourceLanguage() {
        return this.translateFrom;
    }

    /**
     *
     */
    String getTranslationDestLanguage() {
        return Util.getRootFrame().
            getLocalizedProperties().getLocale().getLanguage();
    }

    //
    //
    //

    /**
     *
     * @param evt
     */
    private void handlePopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            POPUP.show(this, evt.getX(), evt.getY());
        }
    }

    /**
     *
     *
     */
    private void translate() {
        String to = getTranslationDestLanguage();
        String from = getTranslationSourceLanguage();
        if (from == null) {
            from =
                DialogUtil.self().showSetDefaultTranslationLanguageDialog();
            if (from == null) {
                return;
            }
            setTranslationSourceLanguage(from);
        }
        try {
            String translated =
                Util.translate(from, to, getSelectedText());
            if (translated != null) {
                int mark = getCaret().getMark();
                int dot = getCaret().getDot();
                if (mark < dot) {
                    replaceRange(translated, mark, dot);
                } else {
                    replaceRange(translated, dot, mark);
                }
            } else {
                DialogUtil.self().showWarningDialog("Unable to translate");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
