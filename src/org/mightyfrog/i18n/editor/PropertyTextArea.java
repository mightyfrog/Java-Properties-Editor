package org.mightyfrog.i18n.editor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author Shigehiro Soejima
 */
class PropertyTextArea extends TextArea implements DocumentListener,
                                                   FocusListener {
    //
    private String key = null;
    private String textOnFocus = null;

    /**
     *
     */
    public PropertyTextArea() {
        this(false);
    }

    /**
     *
     * @param visibleEol
     */
    public PropertyTextArea(boolean visibleEol) {
        super(visibleEol);

        addFocusListener(this);
        getDocument().addDocumentListener(this);
    }

    /** */
    @Override
    public void changedUpdate(DocumentEvent evt) {
        // won't happen
    }

    /** */
    @Override
    public void insertUpdate(DocumentEvent evt) {
        updateDocument();
    }

    /** */
    @Override
    public void removeUpdate(DocumentEvent evt) {
        updateDocument();
    }

    /** */
    @Override
    public void focusGained(FocusEvent evt) {
        this.textOnFocus = getText();
    }

    /** */
    @Override
    public void focusLost(FocusEvent evt) {
        flushLog();
    }

    /** */
    @Override
    public void setText(String text) {
        flushLog();

        super.setText(text);
    }

    /** */
    @Override
    public String getText() {
        Document doc = getDocument();
        String text = null;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return super.getText(); // TODO: remove me
        }
        if (text.isEmpty()) {
            P prop = Util.getLocalizedProperties();
            // key can be null if .properties contains no key
            if (prop != null && this.key != null) {
                String value = prop.getProperty(this.key);
                if (!text.equals(this.key)) {
                    text = value;
                }
            }
        }

        return text;
    }

    //
    //
    //

    /**
     *
     */
    void flushLog() {
        if (this.textOnFocus != null && this.key != null &&
            !this.textOnFocus.equals(getText())) {
            Util.getRootFrame().
                logStart(I18N.get("Log.Start.property.modified",
                                  this.key, this.textOnFocus,
                                  Util.escapeString(getText())));
        }
        this.textOnFocus = null;
    }

    /**
     *
     * @param key
     */
    void setKey(String key) {
        if (key == null) {
            return;
        }
        this.key = key;
        setText(Util.getLocalizedProperties().getProperty(key));
    }

    /**
     * Reloads with a new property value.
     *
     */
    void reload() {
        if (this.key != null) {
            setText(Util.getLocalizedProperties().getProperty(this.key));
        }
    }

    //
    //
    //

    /**
     *
     */
    private void updateDocument() {
        P prop = Util.getLocalizedProperties();
        if (prop != null) {
            prop.setProperty(this.key, getText());
        }
        firePropertyChange("modified", null, prop);
    }

//    String searchString = null;
//
//    /**
//     *
//     *
//     */
//    void setSearchString(String searchString) {
//        this.searchString = searchString;
//    }
//
//    /**
//     *
//     *
//     */
//    public void highlight() {
//        if (this.searchString == null) {
//            return;
//        }
//
//        java.util.regex.Pattern pattern = null;
//        try {
//            pattern = java.util.regex.Pattern.compile(this.searchString);
//
//            P prop = Util.getLocalizedProperties();
//            String text = "";
//            if (prop != null) {
//                text = prop.getProperty(this.key);
//            }
//            java.util.regex.Matcher m = pattern.matcher(getText());
//            while (m.find()) {
//                int start = m.start();
//                int end = m.end();;
//                if (start != end) {
//                    highlight(start, end);
//                }
//            }
//        } catch (java.util.regex.PatternSyntaxException e) {
//            removeHighlight();
//        } catch (Throwable t) {
//            System.exit(-1);
//        } finally {
//            //toggleCursor(false);
//        }
//    }
//
//    /**
//     *
//     * @param start
//     * @param end
//     */
//    void highlight(int start, int end) {
//        try {
//            javax.swing.text.Highlighter highlighter = getHighlighter();
//            javax.swing.text.DefaultHighlighter.DefaultHighlightPainter hp =
//                new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.YELLOW);
//            highlighter.addHighlight(start, end, hp);
//        } catch(javax.swing.text.BadLocationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     *
//     *
//     */
//    void removeHighlight() {
//        javax.swing.text.Highlighter highlighter = getHighlighter();
//        javax.swing.text.Highlighter.Highlight[] highlighters = highlighter.getHighlights();
//        for (javax.swing.text.Highlighter.Highlight h : highlighters) {
//            if (h.getPainter() instanceof
//                javax.swing.text.DefaultHighlighter.DefaultHighlightPainter) {
//                highlighter.removeHighlight(h);
//            }
//        }
//    }
}
