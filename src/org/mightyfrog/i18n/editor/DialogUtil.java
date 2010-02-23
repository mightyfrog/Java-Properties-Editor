package org.mightyfrog.i18n.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Shigehiro Soejima
 */
class DialogUtil { // TODO: optimize memory usage
    private static DialogUtil _self = null;

    private final JLabel NAME_LABEL = new JLabel(I18N.get("Label.name"));
    private final JLabel VALUE_LABEL = new JLabel(I18N.get("Label.value"));
    private final JTextField NAME_FIELD = new JTextField();
    private final JTextField VALUE_FIELD = new JTextField();

    private final JLabel REPLACE_FROM_LABEL = new JLabel(I18N.get("Label.string"));
    private final JLabel REPLACE_TO_LABEL = new JLabel(I18N.get("Label.replace.with"));
    private final JTextField REPLACE_FROM_FIELD = new JTextField();
    private final JTextField REPLACE_TO_FIELD = new JTextField();

    // holds last entered string
    private String cache = null;

    // gets instanciated lazily
    private JComboBox localeComboBox = null;
    private JComboBox encodingComboBox = null;

    /**
     *
     */
    private DialogUtil() {
        // this is a singleton class.
    }

    /**
     *
     */
    public static DialogUtil self() {
        if (_self == null) { // KISS
            _self = new DialogUtil();
        }

        return _self;
    }

    /**
     * Shows the Yes/No option dialog.
     *
     * @param text
     */
    int showYesNoDialog(String text) {
        String title = UIManager.getString("OptionPane.titleText",
                                           new Locale(Options.getLanguage()));
        return showYesNoDialog(text, title);
    }

    /**
     * Shows the Yes/No option dialog.
     *
     * @param text
     * @param title
     */
    int showYesNoDialog(String text, String title) {
        return JOptionPane.
            showConfirmDialog(JOptionPane.getRootFrame(),
                              text, title,
                              JOptionPane.YES_NO_OPTION);
    }

    /**
     * Shows the OK/Cancel option dialog.
     *
     * @param text
     */
    int showOKCancelDialog(String text) {
        String title = UIManager.getString("OptionPane.titleText",
                                           new Locale(Options.getLanguage()));
        return showOKCancelDialog(text, title);
    }

    /**
     * Shows the OK/Cancel option dialog.
     *
     * @param text
     * @param title
     */
    int showOKCancelDialog(String text, String title) {
        return JOptionPane.
            showConfirmDialog(JOptionPane.getRootFrame(),
                              text, title,
                              JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Shows the confirmation dialog.
     *
     * @param text
     */
    int showConfirmDialog(String text) {
        return JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), text);
    }

    /**
     *
     * @param text
     */
    void showWarningDialog(String text) {
        String title = UIManager.getString("OptionPane.messageDialogTitle",
                                           new Locale(Options.getLanguage()));
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), text,
                                      title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     *
     * @param text
     * @param title
     */
    void showWarningDialog(String text, String title) {
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), text, title,
                                      JOptionPane.WARNING_MESSAGE);
    }

    /**
     *
     * @param text
     */
    void showErrorDialog(String text, String title) {
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), text, title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    /**
     *
     * @param text
     */
    void showMessageDialog(String text) {
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), text);
    }

    /**
     *
     * @param text
     * @param title
     */
    String showInputDialog(String text, String title) {
        String input =
            (String) JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                                                 text, title,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null, null, this.cache);
        if (input != null) {
            // if input is null, don't cache it and return null
            this.cache = input;
        }

        return input;
    }

    /**
     *
     * @param text
     * @param title
     * @param initValue
     */
    String showInputDialog(String text, String title, String initValue) {
        String input =
            (String) JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                                                 text, title,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null, null, initValue);

        return input;
    }

    /**
     *
     */
    String[] showAddNewPropertyDialog() { // TODO: rename me
        String title = I18N.get("Dialog.title.add.new.property");
        int option =
            JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                         createKeyValuePanel(),
                                         title,
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            if (NAME_FIELD.getText() != null &&
                !NAME_FIELD.getText().trim().isEmpty()) {
                return new String[]{NAME_FIELD.getText().trim(),
                                    VALUE_FIELD.getText().trim()};
            }
        }

        return null;
    }

    /**
     *
     */
    String showAddNewLocaleDialog() {
        String msg = I18N.get("Label.select.locale");
        String title = I18N.get("Dialog.title.add.new.locale");
        return showLocaleChooserDialog(msg, title, null);
    }

    /**
     *
     * @param message the label string
     * @param title the dialog title
     * @param locale the selected locale
     */
    String showLocaleChooserDialog(String message, String title,
                                   Locale locale) {
        if (locale != null) {
            JComboBox cmb = getLocaleComboBox();
            for (int i = 0; i < cmb.getItemCount(); i++) {
                String s = (String) cmb.getItemAt(i);
                if (s.matches(".*\\[" + locale.getLanguage() + "\\]")) {
                    cmb.setSelectedIndex(i);
                    break;
                }
            }
        }
        int option =
            JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                         createLocalePanel(message),
                                         title,
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            return (String) getLocaleComboBox().getSelectedItem();
        }

        return null;
    }

    /**
     *
     */
    String showEncodingChooserDialog() {
        String message = I18N.get("Dialog.select.encoding");
        String title = I18N.get("Dialog.title.select.encoding");
        int option =
            JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                         createEncodingPanel(message),
                                         title,
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            return (String) getEncodingComboBox().getSelectedItem();
        }

        return null;
    }

    /**
     *
     */
    String showSetDefaultTranslationLanguageDialog() {
        String title = I18N.get("Dialog.title.set.default.trans.lang");
        String selectedItem = "English [en]";
        if (Options.getLanguage().length() != 0) {
            Locale uiLocale = Util.getLocale(Options.getLanguage());
            selectedItem =
                Locale.ENGLISH.getDisplayName(uiLocale) + " [en]";
        }
        getLocaleComboBox().setSelectedItem(selectedItem);
        String label = I18N.get("Label.trans.source");
        int option =
            JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                         createLocalePanel(label),
                                         title,
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            String lang = (String) getLocaleComboBox().getSelectedItem();
            return lang.substring(lang.indexOf("[") + 1, lang.length() - 1);
        }

        return null;
    }

    /**
     *
     * @param title
     */
    String[] showReplaceDialog(String title) {
        int option =
            JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                         createReplacePanel(),
                                         title,
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            if (NAME_FIELD.getText() != null) {
                return new String[]{REPLACE_FROM_FIELD.getText(),
                                    REPLACE_TO_FIELD.getText()};
            }
        }

        return null;
    }

    /**
     *
     * @param list
     */
    List<String> showRemoveFileHistoryDialog(List<String> list) {
        JPanel panel = createRemoveFileHistoryPanel(list);
        String title = I18N.get("Dialog.title.remove");
        int option = JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                                  panel, title,
                                                  JOptionPane.OK_CANCEL_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  null, null, null);
        List<String> newList = new ArrayList<String>();
        if (option == JOptionPane.OK_OPTION) {
            Component[] comps = panel.getComponents();
            for (Component comp : comps) {
                if (comp.getClass() == JCheckBox.class) {
                    JCheckBox cb = (JCheckBox) comp;
                    if (cb.isSelected()) {
                        newList.add(cb.getText());
                    }
                }
            }
        }

        return newList;
    }

    /**
     *
     * @param list
     * @return properties names. returns null for cancelation.
     */
    List<String> showSelectiveSaveDialog(List<P> list) {
        JPanel panel = createSelectiveSavePanel(list);
        int option =
            JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                         panel,
                                         I18N.get("Dialog.title.save"),
                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null);
        List<String> newList = new ArrayList<String>();
        if (option == JOptionPane.OK_OPTION) {
            Component[] comps = panel.getComponents();
            for (Component comp : comps) {
                if (comp.getClass() == JCheckBox.class) {
                    JCheckBox cb = (JCheckBox) comp;
                    if (cb.isSelected()) {
                        newList.add(cb.getText());
                    }
                }
            }
        } else if (option == JOptionPane.CANCEL_OPTION) {
            return null;
        }

        return newList;
    }

    /**
     *
     */
    void showIgnoreValueListDialog() {
        String regKey = Options.IGNORE_VALUE +
            Util.getDefaultFile().getAbsolutePath().hashCode();

        showIgnoreListDialog(regKey, false);
    }

    /**
     *
     */
    void showIgnoreKeyListDialog() {
        String regKey = Options.IGNORE_KEY +
            Util.getDefaultFile().getAbsolutePath().hashCode();

        showIgnoreListDialog(regKey, true);
    }

    /**
     *
     * @param key
     * @param isIgnoreKey
     */
    void showIgnoreListDialog(String key, boolean isIgnoreKey) {
        // TODO: rewrite me
        final TextArea TA = new TextArea();
        String str = Options.getProperty(key);
        TA.setText(str);
        JPanel panel = new JPanel() {
                /** */
                @Override
                public void addNotify() {
                    super.addNotify();

                    new Thread(new Runnable() { // hack
                            /** */
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                                TA.requestFocusInWindow();
                                if (!TA.getText().isEmpty()) {
                                    TA.setCaretPosition(TA.getText().length());
                                }
                            }
                        }).start();
                }
            };
        panel.setLayout(new BorderLayout(0, 6));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(TA);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        String message = I18N.get("Dialog.edit.ignore.list");
        panel.add(new JLabel(message), BorderLayout.NORTH);
        panel.add(scrollPane);

        String title = I18N.get("Dialog.title.edit.ignore.list.keys");
        if (!isIgnoreKey) {
            title = I18N.get("Dialog.title.edit.ignore.list.values");
        }
        int option = JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                                  panel, title,
                                                  JOptionPane.OK_CANCEL_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            str = TA.getText();
            if (str != null && !str.trim().isEmpty()) {
                String[] split = str.split("\n");
                StringBuilder sb = new StringBuilder();
                for (String regex : split) {
                    if (validateRegex(regex)) {
                        sb.append(regex + "\n");
                    }
                }
                Options.setProperty(key, sb.toString());
            } else {
                Options.remove(key);
            }
        }
    }


    /**
     *
     */
    void showScanConfigDialog() {
        // TODO: rewrite me
        final TextArea TA = new TextArea();
        TA.setLineWrap(true);
        TA.setText(Options.getIgnoreScanExtensions());
        JPanel panel = new JPanel() {
                /** */
                @Override
                public void addNotify() {
                    super.addNotify();

                    new Thread(new Runnable() { // hack
                            /** */
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                                TA.requestFocusInWindow();
                                if (!TA.getText().isEmpty()) {
                                    TA.setCaretPosition(TA.getText().length());
                                }
                            }
                        }).start();
                }
            };
        panel.setLayout(new BorderLayout(0, 6));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(TA);
        scrollPane.setPreferredSize(new Dimension(250, 100));
        String message = I18N.get("Dialog.enter.csv.file.ext");
        panel.add(new JLabel(message), BorderLayout.NORTH);
        panel.add(scrollPane);
        final JCheckBox chkBox =
            new JCheckBox(I18N.get("CheckBox.do.not.scan.ignored"));
        chkBox.setSelected(!Options.getScanIgnoreKeys());
        chkBox.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    Options.setScanIgnoreKeys(!chkBox.isSelected());
                }
            });
        panel.add(chkBox, BorderLayout.SOUTH);

        String title = I18N.get("Dialog.title.configure.scanner");
        int option = JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                                  panel, title,
                                                  JOptionPane.OK_CANCEL_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            String str = TA.getText().trim();
            if (str.length() != 0) {
                Options.setIgnoreScanExtensions(str);
            } else {
                Options.remove(Options.IGNORE_SCAN_EXTENSIONS);
            }
        }
    }

    /**
     *
     */
    private boolean validateRegex(String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            String msg = I18N.get("Dialog.invalid.regex2", regex,
                                  e.getLocalizedMessage());
            DialogUtil.self().showWarningDialog(msg);
            return false;
        }

        return true;
    }

    /**
     *
     */
    void showEditCustomSearchDialog() { // TODO: rewrite me, so poorly written
        final TextArea TA = new TextArea();
        JPanel panel = new JPanel() {
                /** */
                @Override
                public void addNotify() {
                    super.addNotify();

                    new Thread(new Runnable() { // hack
                            /** */
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                                TA.requestFocusInWindow();
                                if (!TA.getText().isEmpty()) {
                                    TA.setCaretPosition(TA.getText().length());
                                }
                            }
                        }).start();
                }
            };
        panel.setLayout(new BorderLayout(0, 6));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(TA);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        panel.add(new JLabel(I18N.get("Dialog.edit.custom.search")),
                  BorderLayout.NORTH);
        panel.add(scrollPane);

        String str = Options.getCustomSearch();
        String title = I18N.get("Dialog.title.edit.custom.search");
        TA.setText(str);
        int option = JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                                  panel,
                                                  title,
                                                  JOptionPane.OK_CANCEL_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            str = TA.getText();
            if (str != null && !str.trim().isEmpty()) {
                Options.setCustomSearch(str);
            } else {
                Options.remove(Options.CUSTOM_SEARCH);
            }
        }
    }

    /**
     * Opens a dialog to set the default language to edit.
     *
     */
    void showDefaultLangToEditDialog() {
        String str = Options.getAutoloadLocale();
        String message = I18N.get("Dialog.select.default.lang.to.edit");
        String title = I18N.get("Dialog.title.default.lang.to.edit");
        getLocaleComboBox().insertItemAt(I18N.get("Text.none"), 0);
        getLocaleComboBox().setSelectedIndex(0);
        str = showLocaleChooserDialog(message, title, Util.getLocale(str));
        getLocaleComboBox().removeItemAt(0);
        if (str == null) {
            return;
        }
        if (getLocaleComboBox().getSelectedIndex() == 0) {
            Options.removeAutoloadLocale();
        } else { // locale's lang entred
            str = str.substring(str.indexOf("[") + 1, str.length() - 1);
            Options.setAutoloadLocale(str);
        }
    }

    /**
     *
     */
    void showQuickConverterDialog() { // TODO: rewrite me
        String initValue = Util.getClipboardData();
        String message = I18N.get("Dialog.sends.to.clipboad");
        String title = I18N.get("Dialog.title.quick.converter.n2a");
        String str =
            (String) JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                                                 message, title,
                                                 JOptionPane.PLAIN_MESSAGE,
                                                 null, null, initValue);
        if (str != null && !str.trim().isEmpty()) {
            Util.exportToClipboard(Util.nativeToAscii(str));
        }
    }

    /**
     *
     */
    void showQuickConverterDialog2() { // TODO: rewrite me
        String str = "";
        try {
            Properties p = new Properties();
            str = Util.getClipboardData();
            str = str == null ? "" : str;
            p.load(new StringReader("s=" + str));
            str = p.getProperty("s");
        } catch (IllegalArgumentException e) {
            showErrorDialog(e.getLocalizedMessage(),
                            I18N.get("Dialog.title.error"));
            return;
        } catch (IOException e) {
            // no-op
        }
        String message = I18N.get("Dialog.converted.clipboard.data");
        String title = I18N.get("Dialog.title.quick.converter.a2n");
        JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                                    message, title,
                                    JOptionPane.PLAIN_MESSAGE,
                                    null, null, str);
    }

    /**
     *
     */
    void showAcceleratorHelpDialog() {
        TextArea TA = new TextArea();
        TA.setText(I18N.get("Dialog.accelerator.help"));
        TA.setEditable(false);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(TA);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        String title = I18N.get("Dialog.title.accelerator.help");
        JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                     scrollPane, title,
                                     JOptionPane.OK_CANCEL_OPTION,
                                     JOptionPane.PLAIN_MESSAGE,
                                     null, null, null);
    }

    /**
     *
     * @param list
     */
    private JPanel createRemoveFileHistoryPanel(List<String> list) {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String title = I18N.get("Dialog.title.select.files.to.remove");
        panel.setBorder(BorderFactory.createTitledBorder(title));
        for (String path : list) {
            gbc.weightx = 0.0;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            panel.add(new JCheckBox(path), gbc);
            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(Box.createHorizontalBox(), gbc);
        }

        Dimension dim = panel.getPreferredSize();
        FontMetrics fm = panel.getFontMetrics(panel.getFont());
        int titleWidth = SwingUtilities.computeStringWidth(fm, title);
        if (dim.width < titleWidth) {
            dim.width = titleWidth + 18; // offset = 18
            panel.setPreferredSize(dim);
        }

        panel.getActionMap().put("selectAll", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    Component[] comps = panel.getComponents();
                    boolean allSelected = true;
                    for (Component c : comps) {
                        if (c instanceof JCheckBox) {
                            if (!((JCheckBox) c).isSelected()) {
                                allSelected = false;
                            }
                        }
                    }
                    for (Component c : comps) {
                        if (c instanceof JCheckBox) {
                            ((JCheckBox) c).setSelected(true && !allSelected);
                        }
                    }
                }
            });
        InputMap im = panel.getInputMap();
        im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("ctrl pressed A"), "selectAll");

        return panel;
    }

    /**
     *
     * @param list
     */
    private JPanel createSelectiveSavePanel(List<P> list) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.0;

        String label = I18N.get("Dialog.properties.have.been.modified");
        panel.add(new JLabel(label), gbc);
        if (list.size() > 15) {
            // workaround for Bug ID: 6465146 JOptionPane buttons don't shown
            // when many lines in JOptionPane
            for (int i = 0; i < list.size(); i++) {
                P p = list.get(i);
                JCheckBox cb = new JCheckBox(p.getFile().getName());
                cb.setSelected(true);
                if (i % 2 == 0) {
                    gbc.gridwidth = GridBagConstraints.RELATIVE;
                } else {
                    gbc.gridwidth = GridBagConstraints.REMAINDER;
                }
                panel.add(cb, gbc);
            }
        } else {
            for (P p : list) {
                JCheckBox cb = new JCheckBox(p.getFile().getName());
                cb.setSelected(true);
                panel.add(cb, gbc);
            }
        }

        return panel;
    }

    /**
     *
     */
    private JPanel createKeyValuePanel() {
        JPanel panel = new JPanel() {
                /** */
                @Override
                public void addNotify() {
                    super.addNotify();

                    new Thread(new Runnable() { // hack
                            /** */
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                                NAME_FIELD.requestFocusInWindow();
                                NAME_FIELD.selectAll();
                            }
                        }).start();
                }
            };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        panel.add(NAME_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        panel.add(NAME_FIELD, gbc);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        panel.add(VALUE_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        panel.add(VALUE_FIELD, gbc);

        return panel;
    }

    /**
     *
     */
    private JPanel createReplacePanel() {
        JPanel panel = new JPanel() {
                /** */
                @Override
                public void addNotify() {
                    super.addNotify();

                    new Thread(new Runnable() { // hack
                            /** */
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                                REPLACE_FROM_FIELD.requestFocusInWindow();
                                REPLACE_FROM_FIELD.selectAll();
                            }
                        }).start();
                }
            };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        panel.add(REPLACE_FROM_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        panel.add(REPLACE_FROM_FIELD, gbc);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        panel.add(REPLACE_TO_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        panel.add(REPLACE_TO_FIELD, gbc);

        return panel;
    }

    /**
     *
     */
    private JPanel createLocalePanel(String message) {
        JPanel panel = new JPanel() {
                /** */
                @Override
                public void addNotify() {
                    super.addNotify();

                    new Thread(new Runnable() { // hack
                            /** */
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                                getLocaleComboBox().requestFocusInWindow();
                            }
                        }).start();
                }
            };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        panel.add(new JLabel(message), gbc);
        panel.add(getLocaleComboBox(), gbc);

        return panel;
    }

    /**
     *
     */
    private JComboBox getLocaleComboBox() {
        if (this.localeComboBox == null) {
            this.localeComboBox = new JComboBox();
            this.localeComboBox.setMaximumRowCount(20);
            Locale[] l = Locale.getAvailableLocales();
            String[] displayNames = new String[l.length];
            Locale lang = new Locale(Options.getLanguage());
            for (int i = 0; i < l.length; i++) {
                displayNames[i] =
                    l[i].getDisplayName(lang) + " [" + l[i] + "]";
            }
            Arrays.sort(displayNames);
            for (String s : displayNames) {
                this.localeComboBox.addItem(s);
            }
        }

        return this.localeComboBox;
    }

    /**
     *
     */
    private JPanel createEncodingPanel(String message) {
        JPanel panel = new JPanel() {
                /** */
                @Override
                public void addNotify() {
                    super.addNotify();

                    new Thread(new Runnable() { // hack
                            /** */
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                                getEncodingComboBox().requestFocusInWindow();
                            }
                        }).start();
                }
            };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        panel.add(new JLabel(message), gbc);
        panel.add(getEncodingComboBox(), gbc);

        return panel;
    }

    /**
     *
     */
    private JComboBox getEncodingComboBox() {
        if (this.encodingComboBox == null) {
            this.encodingComboBox = new JComboBox();
            this.encodingComboBox.setMaximumRowCount(20);
            SortedMap<String, Charset> map = Charset.availableCharsets();
            for (Map.Entry entry : map.entrySet()) {
                this.encodingComboBox.addItem(entry.getKey());
            }
            this.encodingComboBox.setSelectedItem("UTF-8");
        }

        return this.encodingComboBox;
    }
}
