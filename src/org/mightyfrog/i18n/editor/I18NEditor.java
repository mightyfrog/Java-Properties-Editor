package org.mightyfrog.i18n.editor;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicLabelUI;

/**
 *
 *
 * @author Shigehiro Soejima
 */
public class I18NEditor extends JFrame implements AdjustmentListener {
    //
    private static final String F_EXT = ".properties";

    /**
     * Contains display name & properties object pairs.
     *
     */
    private final Map<String, P> PROP_MAP = new HashMap<String, P>();

    //
    private P localizedProperties = null;

    //
    private final JSplitPane HORIZONTAL_SP =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JSplitPane VERTICAL_SP =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    //
    final JTabbedPane TABBED_PANE = new JTabbedPane();

    //
    final KeyTree KEY_TREE = new KeyTree();

    //
    final TextArea LOG_TA = new TextArea();
    final TextArea DEFAULT_TA = new TextArea(true);
    final JLabel DEFAULT_LABEL = new JLabel();
    final JScrollPane DEFAULT_SC = new JScrollPane();
    final PropertyTextArea I18N_TA = new PropertyTextArea(true);
    final JLabel I18N_LABEL = new JLabel();
    final JScrollPane I18N_SC = new JScrollPane();
    private static final VerticalLabelUI VERTICAL_LABEL_UI =
        new VerticalLabelUI();

    /**
     * This menu contains items with locale display stirngs. Items are
     * dynamically added/removed every time a new properites file is loaded.
     *
     */
    private final LocaleMenu LOCALE_MENU = new LocaleMenu();

    /**
     * File menus
     *
     */
    private JMenu fileMenu = null;
    private JMenuItem openMI = null;
    private JMenu openRecentMenu = null;
    private JMenuItem addNewLocaleMI = null;
    private JMenuItem createNewPropMI = null;
    private JMenuItem removeRecentMI = null;
    private JMenuItem saveMI = null;
    private JMenuItem saveAsMI = null;
    private JMenuItem saveAllMI = null;
    private JMenuItem saveReversedMI = null;
    private JMenuItem exitMI = null;

    /**
     * View menus
     *
     */
    private JMenu viewMenu = null;
    private JCheckBoxMenuItem visibleWhitespaceMI = null;
    private JCheckBoxMenuItem autoAdjustDividerMI = null;
    private JCheckBoxMenuItem highlightBackgroundMI = null;
    private JCheckBoxMenuItem lineWrapMI = null;

    /**
     * Edit menus
     *
     */
    private JMenu editMenu = null;
    private JMenuItem addNewKeyMI = null; // add new key
    private JMenu searchMenu = null;
    private JMenuItem searchKeyMI = null;
    private JMenuItem bulkTranslateMI = null;
    private JMenuItem resetTranslationSourceLanguageMI = null;
    private JMenuItem searchValueMI = null;
    private JMenuItem alphabetsMI = null;
    private JMenuItem digitsMI = null;
    private JMenuItem untranslatedMI = null;
    private JMenuItem translatedMI = null;
    private JMenuItem missingMI = null;
    private JMenuItem emptyValueMI = null;
    private JMenuItem sameValueMI = null;
    private JMenuItem multilineMI = null;
    private JMenuItem nextMatchMI = null;
    private JMenuItem previousMatchMI = null;
    private JMenuItem clearMI = null;
    private JMenu replaceMenu = null;
    private JMenuItem replaceKeyMI = null;
    private JMenuItem replaceValueMI = null;
    private JMenu eolMenu = null;
    private JRadioButtonMenuItem crlfMI = null;
    private JRadioButtonMenuItem lfMI = null;
    private JRadioButtonMenuItem crMI = null;
    private JMenu delimiterMenu = null;
    private JRadioButtonMenuItem noDelimiterChangeMI = null;
    private JRadioButtonMenuItem equalDelimiterMI = null;
    private JRadioButtonMenuItem colonDelimiterMI = null;
    private JRadioButtonMenuItem spaceDelimiterMI = null;
    private JMenu sortMenu = null;
    private JRadioButtonMenuItem sortNoneMI = null;
    private JRadioButtonMenuItem sortByDefaultMI = null;
    private JRadioButtonMenuItem sortByAlphabetMI = null;

    /**
     * Options menus
     *
     */
    private JMenu optionsMenu = null;
    private JMenu languageMenu = null;
    private JMenuItem defaultLocaleMI = null;
    private JMenuItem allowUnencodedDefaultMI = null;
    private JMenuItem regexSearchMI = null;
    private JMenuItem wildcardSearchMI = null;
    private JMenuItem caseSensitiveSearchMI = null;
    private JCheckBoxMenuItem mergeMissingMI = null;
    private JCheckBoxMenuItem escapeSpecialCharsMI = null;
    private JCheckBoxMenuItem useLowerCaseMI = null;
    private JCheckBoxMenuItem preserveBlanksMI = null;
    private JCheckBoxMenuItem openMostRecentMI = null;
    private JCheckBoxMenuItem makeBackupMI = null;
    private JCheckBoxMenuItem saveOnExitMI = null;
    private JCheckBoxMenuItem saveLogOnExitMI = null;
    private JCheckBoxMenuItem confirmOnExitMI = null;
    private JCheckBoxMenuItem warnUnsafeOnlyMI = null;
    private JMenuItem editCustomSearchMI = null;
    private JMenu editIgnoreListMenu = null;
    private JMenuItem ignoreListValueMI = null;
    private JMenuItem ignoreListKeyMI = null;

    /**
     * Tools menus
     *
     */
    private JMenu toolsMenu = null;
    private JMenuItem openInAppMI = null;
    private JMenu quickConvMenu = null;
    private JMenuItem quickConvN2AMI = null;
    private JMenuItem quickConvA2NMI = null;
    private JMenuItem scanMI = null;
    private JMenuItem scanConfigMI = null;
    private JMenu dialogPreviewMenu = null;
    private JMenuItem previewJavaScriptMI = null;
    private JMenuItem previewSwingMI = null;

    /**
     * Help menus
     *
     */
    private JMenu helpMenu = null;
    private JMenuItem acceleratorMI = null;
    private JMenuItem aboutMI = null;

    /**
     *
     */
    private final FileOpenListener FILE_OPEN_LISTENER =
        new FileOpenListener();

    //
    private JFileChooser fileChooser = null;

    //
    private final JLabel STATUS_LABEL = new JLabel();
    private final Label MISSING_LABEL = new Label();
    private final Label DUPLICATE_LABEL = new Label();
    private final Label UNKNOWN_LABEL = new Label();

    //
    private final KeyPanel RESULT_PANEL;
    private final KeyPanel UNKNOWN_KEY_PANEL;

    //
    private boolean allowUnencodedDefault = false;

    /**
     *
     *
     */
    public I18NEditor() {
        setTitle(I18N.get("Frame.title.init"));
        setIconImage(new ImageIcon(I18NEditor.class.getResource("icon.png")).
                     getImage());
        JOptionPane.setRootFrame(this);

        KEY_TREE.addPropertyChangeListener(new PropertyChangeListener() {
                /** */
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals("selectionChange")) {
                        selectionChanged(evt);
                    } else if (name.equals("nameChange")) {
                        nameChanged(evt);
                    } else if (name.equals("deleteProperty")) {
                        propertyDeleted(evt);
                    }
                }
            });

        HORIZONTAL_SP.setDividerSize(5);
        VERTICAL_SP.setDividerSize(5);

        HORIZONTAL_SP.setDividerLocation(calculateDividerLocation());
        HORIZONTAL_SP.setLeftComponent(new JScrollPane(KEY_TREE));
        HORIZONTAL_SP.setRightComponent(TABBED_PANE);

        RESULT_PANEL = new KeyPanel(false, true);
        UNKNOWN_KEY_PANEL = new KeyPanel(true, false);

        TABBED_PANE.addTab(I18N.get("TabbedPane.tab.property.value"), VERTICAL_SP);
        TABBED_PANE.addTab(I18N.get("TabbedPane.tab.search.result"), RESULT_PANEL);
        TABBED_PANE.addTab(I18N.get("TabbedPane.tab.unknown.keys"), UNKNOWN_KEY_PANEL);
        TABBED_PANE.addTab(I18N.get("TabbedPane.tab.log"), new JScrollPane(LOG_TA));

        TABBED_PANE.setEnabledAt(1, false);
        TABBED_PANE.setEnabledAt(2, false);

        RESULT_PANEL.addPropertyChangeListener(new TableChangeListener(1));
        UNKNOWN_KEY_PANEL.addPropertyChangeListener(new TableChangeListener(2));

        DEFAULT_SC.setViewportView(DEFAULT_TA);
        I18N_TA.setEditable(false);
        I18N_TA.addPropertyChangeListener(new PropertyChangeListener() {
                /** */
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals("modified")) {
                        P prop = (P) evt.getNewValue();
                        updateFrameTitle(prop);
                    }
                }
            });
        I18N_SC.setViewportView(I18N_TA);
        DEFAULT_SC.getVerticalScrollBar().addAdjustmentListener(this);
        I18N_SC.getVerticalScrollBar().addAdjustmentListener(this);
        DEFAULT_SC.getHorizontalScrollBar().addAdjustmentListener(this);
        I18N_SC.getHorizontalScrollBar().addAdjustmentListener(this);
        VERTICAL_SP.setTopComponent(createDefaultTextAreaPanel());
        VERTICAL_SP.setBottomComponent(createI18NTextAreaPanel());

        DEFAULT_TA.setEditable(false);
        DEFAULT_TA.setDragEnabled(true);

        LOCALE_MENU.setEnabled(false);

        addComponentListener(new ComponentAdapter() {
                /** */
                @Override
                public void componentResized(ComponentEvent evt) {
                    if (Options.getAutoAdjustDivider()) {
                        VERTICAL_SP.setDividerLocation(0.5);
                    }
                }
            });

        addWindowListener(new WindowAdapter() {
                /** */
                @Override
                public void windowClosing(WindowEvent evt) {
                    confirmExit();
                }

                /** */
                @Override
                public void windowActivated(WindowEvent evt) {
                    dirtyCheck(true);
                }
            });

        initActionMapAndInputMap();

        setJMenuBar(createMenuBar());

        add(HORIZONTAL_SP);
        add(createStatusBar(), BorderLayout.SOUTH);

        createOpenRecentMenu();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        if (Options.getOpenMostRecent()) {
            File file = Options.getMostRecentFile();
            if (file != null) {
                loadProperty(file);
            }
        }
    }

    /** */
    @Override
    public void adjustmentValueChanged(AdjustmentEvent evt) {
        if (evt.getAdjustable().getOrientation() == Adjustable.VERTICAL) {
            DEFAULT_SC.getVerticalScrollBar().setValue(evt.getValue());
            I18N_SC.getVerticalScrollBar().setValue(evt.getValue());
        } else {
            DEFAULT_SC.getHorizontalScrollBar().setValue(evt.getValue());
            I18N_SC.getHorizontalScrollBar().setValue(evt.getValue());
        }
    }

    /** */
    public static void main(String[] args) {
        JComponent.setDefaultLocale(Util.getLocale(Options.getLanguage()));
        String laf = Options.getProperty("laf");
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        //laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        if (laf == null) {
            laf = UIManager.getSystemLookAndFeelClassName();
        } else {
            if (laf.endsWith("MetalLookAndFeel")) {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
            }
        }
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            // ClassNotFoundException, InstantiationException,
            // IllegalAccessException,
            // javax.swing.UnsupportedLookAndFeelException
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        }

        EventQueue.invokeLater(new Runnable() {
                /** */
                @Override
                public void run() {
                    new I18NEditor();
                }
            });
    }

    /** */
    @Override
    public void addNotify() {
        super.addNotify();

        SwingUtilities.updateComponentTreeUI(this);
    }

    //
    //
    //

    /**
     * Returns the default Properties object.
     *
     */
    P getDefaultProperties() {
        File f = getDefaultFile();
        if (f == null) {
            return null;
        }

        return PROP_MAP.get(f.getName());
    }

    /**
     * Returns the current localized Properties object.
     *
     */
    P getLocalizedProperties() {
        return this.localizedProperties;
    }

    /**
     *
     * @param fileName
     */
    P getPropertiesByFileName(String fileName) {
        return PROP_MAP.get(fileName);
    }

    /**
     * Displays the specified text in the status bar.
     *
     * @param text
     */
    void setStatus(String text) {
        STATUS_LABEL.setText(text);
    }

    /**
     * Returns the default .properties File object.
     *
     */
    File getDefaultFile() {
        for (P p : PROP_MAP.values()) {
            if (p.isDefault()) {
                return p.getFile();
            }
        }

        assert false : "Default properties not found.";
        return null;
    }

    /**
     *
     */
    void searchUntranslatedKeys() {
        if (isBothLocalesSame(true)) {
            return;
        }
        this.untranslatedMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.untranslated"));
        SearchResult result = KEY_TREE.searchUntranslated();
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            logContent(node.getKey() + "=" +
                       Util.escapeString(node.getValue()));
        }
        showResult(result);
        this.untranslatedMI.setEnabled(true);
    }

    /**
     * Searches for translated/modified properties.
     *
     */
    void searchTranslatedKeys() {
        if (isBothLocalesSame(true)) {
            return;
        }
        this.translatedMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.translated"));
        SearchResult result = KEY_TREE.searchTranslated();
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            logContent(node.getKey() + "=" +
                       Util.escapeString(node.getValue()));
        }
        showResult(result);
        this.translatedMI.setEnabled(true);
    }

    /**
     *
     */
    void searchMissingKeys() {
        if (isBothLocalesSame(true)) {
            return;
        }
        this.missingMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.missing"));
        SearchResult result  = KEY_TREE.searchMissing();
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            logContent(node.getKey() + "=" +
                       Util.escapeString(node.getValue()));
        }
        showResult(result);
        this.missingMI.setEnabled(true);
    }

    /**
     *
     */
    void searchEmptyValueKeys() {
        this.emptyValueMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.empty.values"));
        SearchResult result = KEY_TREE.searchEmptyValueKeys();
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            logContent(node.getKey());
        }
        showResult(result);
        this.emptyValueMI.setEnabled(true);
    }

    /**
     *
     */
    void searchSameValueKeys() {
        this.sameValueMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.same.values"));
        List<String> list = KEY_TREE.searchSameValueKeys();
        RESULT_PANEL.addValues(list.toArray(new String[]{}));
        for (String value : list) {
            logContent(value);
        }
        int count = list.size();
        String message = null;
        if (count == 0) {
            logEnd(I18N.get("Log.End.no.same.property.found"));
            message = I18N.get("Dialog.no.same.value.found");
        } else {
            logEnd(I18N.get("Log.End.exists.in.multiple.properites", count));
            message = I18N.get("Dialog.property.values.found", count);
        }
        DialogUtil.self().showMessageDialog(message);
        this.sameValueMI.setEnabled(true);
    }

    /**
     *
     */
    void searchMultilineValues() {
        this.multilineMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.multiline"));
        SearchResult result = KEY_TREE.searchMultilineValues();
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            logContent(node.getKey());
        }
        showResult(result);
        this.multilineMI.setEnabled(true);
    }

    /**
     *
     */
    void searchAlphabets() {
        this.alphabetsMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.alphabets"));
        SearchResult result = KEY_TREE.searchAlphabets();
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            logContent(node.getKey() + "=" +
                       Util.escapeString(node.getValue()));
        }
        showResult(result);
        this.alphabetsMI.setEnabled(true);
    }

    /**
     *
     */
    void searchDigits() {
        this.digitsMI.setEnabled(false);
        logStart(I18N.get("Log.Start.searching.digits"));
        SearchResult result = KEY_TREE.searchDigits();
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            logContent(node.getKey() + "=" +
                       Util.escapeString(node.getValue()));
        }
        showResult(result);
        this.digitsMI.setEnabled(true);
    }

    /**
     *
     */
    void searchKeys() {
        String message = I18N.get("Dialog.enter.search.string");
        String title = I18N.get("Dialog.title.search.keys");
        String s = DialogUtil.self().showInputDialog(message, title);
        if (s != null && s.length() != 0) {
            logStart(I18N.get("Log.Start.searching", s));
            SearchResult result = KEY_TREE.searchKeys(s);
            List<KeyNode> list = result.getResult();
            RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
            for (KeyNode node : list) {
                logContent(node.getKey());
            }
            int count = list.size();
            logEnd(I18N.get("Log.End.properties.found", count));
            setStatus(I18N.get("Status.properties.found", count));
            showSearchResultDialog(result);
        }
    }

    /**
     *
     */
    void searchValues() {
        String message = I18N.get("Dialog.enter.search.string");
        String title = I18N.get("Dialog.title.search.values");
        String s = DialogUtil.self().showInputDialog(message, title);

        if (s != null && s.length() != 0) {
            logStart(I18N.get("Log.Start.searching", s));
            SearchResult result = KEY_TREE.searchValues(s);
            List<KeyNode> list = result.getResult();
            RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
            for (KeyNode node : list) {
                String value =
                    getLocalizedProperties().getProperty(node.getKey());
                logContent(node.getKey() + "=" +
                           Util.escapeString(value));
            }
            int count = list.size();
            logEnd(I18N.get("Log.End.properties.found", count));
            setStatus(I18N.get("Status.properties.found", count));
            showSearchResultDialog(result);
        }
    }

    /**
     *
     * @param regex
     */
    void doCustomSearch(String searchName, String regex) {
        logStart(I18N.get("Log.Start.custom.search", searchName));
        SearchResult result = KEY_TREE.doCustomSearch(regex);
        List<KeyNode> list = result.getResult();
        RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
        for (KeyNode node : list) {
            String value = getLocalizedProperties().getProperty(node.getKey());
            logContent(node.getKey() + "=" +
                       Util.escapeString(value));
        }
        showResult(result);
    }

    /**
     *
     */
    void replaceValues() {
        String title = I18N.get("Dialog.title.replace.values");
        String[] str = DialogUtil.self().showReplaceDialog(title);
        if (str != null && str[0].length() != 0) {
            String from = str[0];
            String to = str[1];
            if (from.equals(to)) {
                return;
            }
            logStart(I18N.get("Log.Start.replacing", from, to));
            Map<KeyNode, String> map = KEY_TREE.replaceValues(from, to);
            if (map.size() != 0) {
                I18N_TA.reload();
            }
            for (Map.Entry entry : map.entrySet()) {
                KeyNode node = (KeyNode) entry.getKey();
                String newValue = Util.getLocalizedValue(node.getKey());
                String oldValue = (String) entry.getValue();
                logContent(Util.escapeString(oldValue) +
                           " -----> " + Util.escapeString(newValue));
            }
            logEnd(I18N.get("Log.End.values.replaced", map.size()));
            showReplaceResultDialog(map.size());
        }
    }

    /**
     *
     */
    void replaceKeys() {
        String title = I18N.get("Dialog.title.replace.keys");
        String[] str = DialogUtil.self().showReplaceDialog(title);
        if (str != null && str[0].length() != 0) {
            String from = str[0];
            String to = str[1];
            if (from.equals(to)) {
                return;
            }
            logStart(I18N.get("Log.Start.replacing", from, to));
            Map<KeyNode, String> map = KEY_TREE.replaceKeys(from, to);
            List<String> duplicateList = new ArrayList<String>();
            for (Map.Entry entry : map.entrySet()) {
                KeyNode node = (KeyNode) entry.getKey();
                String oldKey = (String) entry.getValue();
                String newKey = node.getKey();
                if (oldKey.equals(newKey)) {
                    duplicateList.add("     " + I18N.get("Log.End.key.skipped", newKey));
                } else {
                    for (P p : PROP_MAP.values()) {
                        p.replaceKeys(oldKey, newKey);
                    }
                    logContent(oldKey + " -----> " + newKey);
                }
            }
            for (String s : duplicateList) {
                logEnd(s);
            }
            logEnd(I18N.get("Log.End.values.replaced",
                            map.size() - duplicateList.size()));
            showReplaceResultDialog(map.size() - duplicateList.size());
        }
    }

    /**
     * Returns the sort type.
     *
     */
    int getSortType() {
        if (this.sortNoneMI.isSelected()) {
            return Util.SORT_NONE;
        } else if (this.sortByDefaultMI.isSelected()) {
            return Util.SORT_DEFAULT;
        } else {
            return Util.SORT_ALPHABET;
        }
    }

    /**
     * Returns the delimiter type.
     *
     */
    int getDelimiterType() {
        if (this.equalDelimiterMI.isSelected()) {
            return Util.DELIMITER_EQUAL;
        } else if (this.colonDelimiterMI.isSelected()) {
            return Util.DELIMITER_COLON;
        } else if (this.spaceDelimiterMI.isSelected()) {
            return Util.DELIMITER_SPACE;
        } else {
            return Util.DELIMITER_NO_CHANGE;
        }
    }

    /**
     * Tests whether the specified file already exists or not.
     *
     * @param file
     */
    // TODO: move me
    boolean canOverwrite(File file) {
        boolean b = true;
        if (file.exists()) {
            String message = I18N.get("Dialog.do.u.want.to.overwrite",
                                      file.getName());
            int option = DialogUtil.self().showYesNoDialog(message);
            b = option == JOptionPane.YES_OPTION;
        }

        return b;
    }

    /**
     * Loads 'default' properties from the specified file.
     *
     * @param file
     */
    void loadProperty(File file) {
        file = lookForDefaultPropertiesFile(file);
        //if (file.length() == 0) {
        //    return;
        //}
        registerFilePath(file);

        P prop = new P(file);
        PROP_MAP.clear();
        PROP_MAP.put(file.getName(), prop);
        InputStream is = null;
        Reader reader = null;
        try {
            logStart(I18N.get("Log.Start.loading", file.getAbsolutePath()));
            is = getDefaultFile().toURI().toURL().openStream();
            if (getAllowUnencodedDefault()) {
                String encoding =
                    DialogUtil.self().showEncodingChooserDialog();
                if (encoding != null) {
                    reader =
                        new InputStreamReader(is, Charset.forName(encoding));
                } else {
                    reader = new InputStreamReader(is);
                }
            } else {
                reader = new InputStreamReader(is);
            }
            prop.load(reader);
            logDuplicates(prop);
            if (getAllowUnencodedDefault()) { // encode unencoded props
                prop.setModified(true);
                saveAll();
            }
            if (prop.getDuplicateKeys().size() == 0) {
                logEnd("OK");
                setStatus("OK");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        DEFAULT_LABEL.setText(file.getName());
        DEFAULT_LABEL.setToolTipText(file.getAbsolutePath());
        DEFAULT_LABEL.setUI(VERTICAL_LABEL_UI);

        setLocalizedProperties(prop, false);
        KEY_TREE.setProperties(prop);
        createLocaleMenu(prop);
        createOpenRecentMenu();
        toggleContextSensitiveMenus(true);
        I18N_TA.requestFocus();
        I18N_TA.setTranslationSourceLanguage(null);

        KEY_TREE.clearState();
        RESULT_PANEL.clear();
        //UNKNOWN_KEY_PANEL.clear();

        this.openInAppMI.setEnabled(true);
        this.scanMI.setEnabled(true);
        this.bulkTranslateMI.setEnabled(true);
        this.resetTranslationSourceLanguageMI.setEnabled(true);
        this.dialogPreviewMenu.setEnabled(true);
        this.eolMenu.setEnabled(true);

        //DEFAULT_LABEL.setText(file.getName());
        //DEFAULT_LABEL.setToolTipText(file.getAbsolutePath());
        //DEFAULT_LABEL.setUI(VERTICAL_LABEL_UI);
    }

    /**
     * Saves the specified properties file.
     *
     * @param prop
     * @param file
     * @param escape
     */
    boolean save(P prop, File file, boolean escape) {
        I18N_TA.flushLog();
        if (prop.hasUnsafeDuplicates()) {
            String message = I18N.get("Dialog.contains.duplicate.keys",
                                      prop.getFile().getName());
            int option = DialogUtil.self().showConfirmDialog(message);
            if (option != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        try {
            if (file == null) {
                prop.store(escape);
            } else {
                prop.store(file, escape);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        prop.updateLastModified();

        return true;
    }

    /**
     * Saves the currently opned properties file.
     *
     * @param escape
     */
    void saveAs(boolean escape) {
        int option = getFileChooser().showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = getFileChooser().getSelectedFile();
            if (file != null && canOverwrite(file)) {
                if (save(getLocalizedProperties(), file, escape)) {
                    refresh();
                }
            }
        }
    }

    /**
     * Saves all the properites files.
     *
     */
    void saveAll() {
        if (!PROP_MAP.isEmpty()) {
            new SaveAllTask().execute();
        }
    }

    /**
     * Toggles the state of the context sensitive menu items.
     *
     * @param enabled
     */
    void toggleContextSensitiveMenus(boolean enabled) {
        this.editMenu.setEnabled(enabled);
        this.addNewLocaleMI.setEnabled(enabled);
        this.saveMI.setEnabled(enabled);
        this.saveAsMI.setEnabled(enabled);
        this.saveAllMI.setEnabled(enabled);
        this.saveReversedMI.setEnabled(enabled);
        this.editIgnoreListMenu.setEnabled(enabled);
    }

    /**
     *
     *
     * @return the list of saved Properties objects. null indicates
     *         cancellation.
     */
    // TODO: rewrite me, change return type
    List<P> confirmSaveModified() {
        List<P> propList = new ArrayList<P>(PROP_MAP.values());
        List<P> saveList = new ArrayList<P>(); // TODO: rewrite me
        for (P p : propList) {
            if (p.getModified()) {
                saveList.add(p);
            }
        }
        if (saveList.size() != 0) {
            List<String> list =
                DialogUtil.self().showSelectiveSaveDialog(saveList);
            if (list == null) {
                return null;
            }
            for (String s : list) {
                save(Util.getPropertiesByFileName(s), null, true);
            }
            refresh();
        }

        return propList;
    }

    /**
     *
     */
    void refresh() {
        P p = getLocalizedProperties();
        try {
            p.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setLocalizedProperties(p, true);
        if (p == getDefaultProperties()) {
            KEY_TREE.setProperties(p);
        }
    }

    /**
     *
     * @param text
     */
    synchronized void logStart(String text) {
        // use system line separator for log
        String str = LOG_TA.getText();
        if (!str.endsWith(Util.EOL_STR_LF) && str.length() != 0) {
            LOG_TA.append(Util.LINE_SEPARATOR);
        }
        LOG_TA.append(text + Util.LINE_SEPARATOR);
    }

    /**
     *
     * @param text
     */
    synchronized void logContent(String text) {
        LOG_TA.append(text + Util.LINE_SEPARATOR);
    }

    /**
     *
     * @param text
     */
    synchronized void logEnd(String text) {
        LOG_TA.append(text + Util.LINE_SEPARATOR +
                      Util.LINE_SEPARATOR);
    }

    /**
     * Logs duplicate keys.
     *
     */
    void logDuplicates(P prop) {
        if (!prop.hasUnsafeDuplicates()) {
            return;
        }
        List<KeyNode> list = prop.getDuplicateKeys();
        Set<KeyNode> set = new HashSet<KeyNode>();
        set.addAll(list);
        for (KeyNode node : list) {
            String key = node.getKey();
            if (set.contains(node)) {
                logContent(I18N.get("Log.Content.duplicate.key",
                                    key, prop.getProperty(key)));
                set.remove(node);
            }
            logContent(I18N.get("Log.Content.duplicate.key.discarded",
                                key, node.getValue()));
        }
        logEnd(I18N.get("Log.End.duplicate.keys.found", list.size()));
        DialogUtil.self().
            showWarningDialog(I18N.get("Dialog.duplicate.keys.found"));
    }

    /**
     *
     * @param waiting
     */
    void toggleCursor(boolean waiting) {
        Component gp = getGlassPane();
        if (waiting) {
            gp.addMouseListener(new MouseAdapter() {
                    /** */
                    @Override
                    public void mousePressed(MouseEvent evt) {
                        evt.consume();
                    }
                });
            gp.setVisible(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            MouseListener[] ml = gp.getMouseListeners();
            for (MouseListener l : ml) {
                gp.removeMouseListener(l);
            }
            gp.setVisible(false);
            setCursor(null);
        }
    }

    /**
     * Tests if the locale properties is the default properties.
     *
     */
    boolean isBothLocalesSame(boolean doPopup) {
        if (getLocalizedProperties() == getDefaultProperties()) {
            if (doPopup) {
                String message = I18N.get("Dialog.editing.default.locale");
                DialogUtil.self().showWarningDialog(message);
            }

            return true;
        }

        return false;
    }

    /**
     *
     * @param missing the missing key count
     */
    void setMissingCount(int missing) {
        MISSING_LABEL.setText(I18N.get("Label.missing.keys", missing),
                              missing != 0);
    }

    /**
     *
     * @param duplicate the duplicate key count
     */
    void setDuplicateCount(int duplicate) {
        DUPLICATE_LABEL.setText(I18N.get("Label.duplicate.keys", duplicate),
                                duplicate != 0);
    }

    /**
     *
     * @param unknown the unknown key count
     */
    void setUnknownCount(int unknown) {
        UNKNOWN_LABEL.setText(I18N.get("Label.unknown.keys", unknown),
                              unknown != 0);
    }

    //
    //
    //

    /**
     *
     *
     */
    private void showResult(SearchResult result) {
        int count = result.getResult().size();
        int ignoredCount = result.getIgnoredCount();
        if (ignoredCount == 0) {
            logEnd(I18N.get("Log.End.properties.found", count));
        } else {
            logEnd(I18N.get("Log.End.properties.found2", count,
                            ignoredCount));
        }
        setStatus(I18N.get("Status.properties.found", count));
        showSearchResultDialog(result);
    }

    /**
     *
     * @param file
     */
    private File lookForDefaultPropertiesFile(File file) {
        String name = file.getName();
        int index = name.indexOf("_");
        if (index == -1) {
            return file;
        }
        name = name.substring(0, index);
        File[] files = file.getParentFile().listFiles();
        for (File f : files) {
            if (f.getName().startsWith(name + ".") &&
                f.getName().endsWith(".properties")) {
                String msg = I18N.get("Dialog.default.found",
                                      f.getName(), file.getName());
                int option = DialogUtil.self().showYesNoDialog(msg);
                if (option == JOptionPane.YES_OPTION) {
                    return f;
                }
            }
        }

        return file;
    }

    /**
     *
     */
    private JPanel createDefaultTextAreaPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(DEFAULT_SC);
        panel.add(DEFAULT_LABEL, BorderLayout.EAST);

        return panel;
    }

    /**
     *
     */
    private JPanel createI18NTextAreaPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(I18N_SC);
        panel.add(I18N_LABEL, BorderLayout.EAST);

        return panel;
    }

    /**
     * Initializes ActionMap and InputMap so the content pane can capture
     * key events.
     *
     */
    private void initActionMapAndInputMap() {
        JPanel p = (JPanel) getContentPane();
        p.getActionMap().put("toggleLocale", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    LOCALE_MENU.toggleSelection();
                }
            });
        p.getActionMap().put("focusPropTab", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    TABBED_PANE.requestFocusInWindow();
                    TABBED_PANE.setSelectedIndex(0);
                }
            });
        p.getActionMap().put("focusResultTab", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (TABBED_PANE.isEnabledAt(1)) {
                        TABBED_PANE.requestFocusInWindow();
                        TABBED_PANE.setSelectedIndex(1);
                    }
                }
            });
        p.getActionMap().put("focusUnknownTab", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (TABBED_PANE.isEnabledAt(2)) {
                        TABBED_PANE.requestFocusInWindow();
                        TABBED_PANE.setSelectedIndex(2);
                    }
                }
            });
        p.getActionMap().put("focusLogTab", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    TABBED_PANE.requestFocusInWindow();
                    TABBED_PANE.setSelectedIndex(3);
                }
            });
        InputMap im = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("ctrl alt pressed X"), "toggleLocale");
        im.put(KeyStroke.getKeyStroke("ctrl alt pressed P"), "focusPropTab");
        im.put(KeyStroke.getKeyStroke("ctrl alt pressed L"), "focusLogTab");
        im.put(KeyStroke.getKeyStroke("ctrl alt pressed R"), "focusResultTab");
        im.put(KeyStroke.getKeyStroke("ctrl alt pressed U"), "focusUnknownTab");
    }

    /**
     * Tests whether the current properties file has been modified by an
     * external application or not.
     *
     * @param reload
     */
    private void dirtyCheck(boolean reload) {
        P p = getLocalizedProperties();
        if (p != null && p.isDirty()) {
            String message =
                I18N.get("Dialog.modified.by.ext.app", p.getFile().getName());
            int option =
                DialogUtil.self().showYesNoDialog(message);
            if (option == JOptionPane.YES_OPTION) {
                if (reload) {
                    loadProperty(getDefaultProperties().getFile());
                }
            }
            p.updateLastModified();
        }
    }

    /**
     * Calculate the divider location. 250 < divLoc < 300.
     *
     */
    private int calculateDividerLocation() {
        int divLocation = getFontMetrics(KEY_TREE.getFont()).
            stringWidth(I18N.get("Tree.message.drop.file.here")) + 30;

        if (divLocation > 300) {
            divLocation = 300;
        } else if (divLocation < 250) {
            divLocation = 250;
        }

        return divLocation;
    }

    /**
     * Returns the EOL string.
     *
     */
    private String getEol() {
        if (this.crlfMI.isSelected()) {
            return Util.EOL_STR_CRLF;
        } else if (this.lfMI.isSelected()) {
            return Util.EOL_STR_LF;
        } else {
            return Util.EOL_STR_CR;
        }
    }

    /**
     *
     * @param evt
     */
    private void selectionChanged(PropertyChangeEvent evt) {
        KeyNode node = (KeyNode) evt.getNewValue();
        if (node == null) {
            DEFAULT_TA.setText(null);
            I18N_TA.setEditable(false);
            I18N_TA.setKey(null);
            I18N_TA.setText(null);
        } else {
            String key = node.getKey();
            DEFAULT_TA.setText(getDefaultProperties().getProperty(key));
            I18N_TA.setEditable(true);
            P prop = getLocalizedProperties();
            if (prop != null) {
                I18N_TA.setKey(key);
            }
        }
    }

    /**
     *
     * @param evt
     */
    private void nameChanged(PropertyChangeEvent evt) {
        String newName = (String) evt.getNewValue();
        String oldName = (String) evt.getOldValue();
        for (P p : PROP_MAP.values()) {
            String value = p.getProperty(oldName);
            p.setProperty(newName, value);
            p.remove(oldName);
        }
        logStart(I18N.get("Log.Start.property.renamed", oldName, newName));
        setStatus(I18N.get("Status.one.property.renamed"));
    }

    /**
     *
     * @param evt
     */
    private void propertyDeleted(PropertyChangeEvent evt) {
        String key = (String) evt.getNewValue();
        for (P p : PROP_MAP.values()) {
            p.remove(key);
        }
        logStart(I18N.get("Log.Start.property.deleted", key));
        setStatus(I18N.get("Status.one.property.deleted"));
    }

    /**
     *
     */
    private JFileChooser getFileChooser() {
        if (this.fileChooser == null) {
            this.fileChooser = new JFileChooser() {
                    /** */
                    @Override
                    public File getSelectedFile() {
                        File f = super.getSelectedFile();
                        if (f != null &&
                            getFileFilter().getDescription().indexOf(F_EXT) != -1 &&
                            !f.getName().toLowerCase().endsWith(F_EXT)) {
                            f = new File(f.getParentFile(), f.getName() + F_EXT);
                        }

                        return f;
                    }
                };
            this.fileChooser.setFileFilter(new FileFilter() {
                    /** */
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() ||
                            f.getName().endsWith(F_EXT);
                    }

                    /** */
                    @Override
                    public String getDescription() {
                        return I18N.get("FileFilter.java.properties.file");
                    }
                });
            String filePaths = Options.getRecentFiles();
            if (filePaths != null) {
                String path = filePaths;
                if (filePaths.indexOf(',') != -1) {
                    path = filePaths.substring(0, filePaths.indexOf(','));
                }
                this.fileChooser.setCurrentDirectory(new File(path).getParentFile());
            }
        }

        return this.fileChooser;
    }

    /**
     *
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createViewMenu());
        menuBar.add(LOCALE_MENU);
        menuBar.add(createOptionsMenu());
        menuBar.add(createToolsMenu());
        menuBar.add(createHelpMenu());

        return menuBar;
    }

    /**
     *
     */
    private JMenu createFileMenu() {
        this.fileMenu = MenuFactory.createMenu("Menu.file");

        this.createNewPropMI = MenuFactory.createMenuItem("MenuItem.create.new.prop");
        this.createNewPropMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    addNewPropFile();
                }
            });
        this.fileMenu.add(this.createNewPropMI);

        this.openMI = MenuFactory.createMenuItem("MenuItem.open.file");
        this.openMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    int option = getFileChooser().showOpenDialog(I18NEditor.this);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        loadProperty(getFileChooser().getSelectedFile());
                    }
                }
            });
        this.fileMenu.add(this.openMI);

        this.openRecentMenu = MenuFactory.createMenu("Menu.open.recently.used");
        this.removeRecentMI = MenuFactory.createMenuItem("MenuItem.remove");
        this.removeRecentMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    removeRecent();
                }
            });
        this.fileMenu.add(this.openRecentMenu);

        this.saveMI = MenuFactory.createMenuItem("MenuItem.save");
        this.saveMI.setEnabled(false);
        this.saveMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    toggleContextSensitiveMenus(false);
                    if (save(getLocalizedProperties(), null, true)) {
                        refresh();
                    }
                    toggleContextSensitiveMenus(true);
                }
            });
        this.fileMenu.add(this.saveMI);

        this.saveAsMI = MenuFactory.createMenuItem("MenuItem.save.as");
        this.saveAsMI.setEnabled(false);
        this.saveAsMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    saveAs(true);
                }
            });
        this.fileMenu.add(this.saveAsMI);

        this.saveAllMI = MenuFactory.createMenuItem("MenuItem.save.all");
        this.saveAllMI.setEnabled(false);
        this.saveAllMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    saveAll();
                }
            });
        this.fileMenu.add(this.saveAllMI);

        this.saveReversedMI = MenuFactory.createMenuItem("MenuItem.save.reversed");
        this.saveReversedMI.setEnabled(false);
        this.saveReversedMI.addActionListener(new ActionListener() {
                //
                private String[] charsets = null;

                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (this.charsets == null) {
                        List<String> list = new ArrayList<String>();
                        SortedMap<String, Charset> map =
                            Charset.availableCharsets();
                        for (Map.Entry entry : map.entrySet()) {
                            Charset c = (Charset) entry.getValue();
                            if (c.canEncode()) {
                                list.add(c.toString());
                            }
                        }
                        this.charsets = list.toArray(new String[]{});
                    }
                    String title = I18N.get("Dialog.title.select.charset");
                    String message = I18N.get("Dialog.select.file.encoding.charset");
                    String charset =
                        (String) JOptionPane.showInputDialog(I18NEditor.this,
                                                             message, title,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null, this.charsets,
                                                             Options.getFileEncodingCharset());
                    if (charset != null) {
                        Options.setFileEncodingCharset(charset);
                        saveAs(false);
                    }
                }
            });
        this.fileMenu.add(this.saveReversedMI);

        this.fileMenu.addSeparator();

        this.exitMI = MenuFactory.createMenuItem("MenuItem.exit");
        this.exitMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    confirmExit();
                }
            });
        this.fileMenu.add(this.exitMI);

        return this.fileMenu;
    }

    /**
     *
     */
    private void createOpenRecentMenu() {
        String regValue = Options.getRecentFiles();
        if (regValue.length() == 0) {
            this.openRecentMenu.setEnabled(false);
            return;
        }
        this.openRecentMenu.removeAll();
        final String[] filePaths = regValue.split(",");
        for (String filePath : filePaths) {
            if (new File(filePath).exists()) {
                JMenuItem mi = new JMenuItem(filePath);
                mi.addActionListener(FILE_OPEN_LISTENER);
                this.openRecentMenu.add(mi);
            } else {
                logContent(I18N.get("Log.Content.prop.not.found", filePath));
                unregisterFilePath(filePath);
            }
        }
        this.openRecentMenu.addSeparator();
        this.openRecentMenu.add(this.removeRecentMI);
        this.openRecentMenu.setEnabled(true);
    }

    /**
     *
     */
    private JMenu createViewMenu() {
        this.viewMenu = MenuFactory.createMenu("Menu.view");
        this.visibleWhitespaceMI = MenuFactory.createCheckBoxMenuItem("MenuItem.visible.whitespaces");
        this.autoAdjustDividerMI = MenuFactory.createCheckBoxMenuItem("MenuItem.auto.adjust.divider");
        this.highlightBackgroundMI = MenuFactory.createCheckBoxMenuItem("MenuItem.highlight.background");
        this.lineWrapMI = MenuFactory.createCheckBoxMenuItem("MenuItem.line.wrap");
        this.visibleWhitespaceMI.setSelected(Options.getVisibleWhitespace());
        this.autoAdjustDividerMI.setSelected(Options.getAutoAdjustDivider());
        this.highlightBackgroundMI.setSelected(Options.getHighlightOnDefault());
        this.lineWrapMI.setSelected(Options.getLineWrap());

        this.viewMenu.add(this.visibleWhitespaceMI);
        this.viewMenu.add(this.autoAdjustDividerMI);
        this.viewMenu.add(this.highlightBackgroundMI);
        this.viewMenu.add(this.lineWrapMI);

        this.visibleWhitespaceMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.visibleWhitespaceMI.isSelected();
                    Options.setVisibleWhitespace(b);
                    EventQueue.invokeLater(new Runnable() {
                            /** */
                            @Override
                            public void run() {
                                DEFAULT_TA.repaint();
                                I18N_TA.repaint();
                            }
                        });
                }
            });

        this.autoAdjustDividerMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.autoAdjustDividerMI.isSelected();
                    Options.setAutoAdjustDivider(b);
                }
            });

        this.highlightBackgroundMI.addItemListener(new ItemListener() {
                /** */
                @Override
                public void itemStateChanged(ItemEvent evt) {
                    boolean b = evt.getStateChange() == ItemEvent.SELECTED;
                    Options.setHighlightOnDefault(b); // set me first
                    toggleI18NTAColor();
                }
            });

        this.lineWrapMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.lineWrapMI.isSelected();
                    Options.setLineWrap(b);
                    DEFAULT_TA.setLineWrap(b);
                    I18N_TA.setLineWrap(b);
                }
            });

        return this.viewMenu;
    }

    /**
     *
     */
    private JMenu createEditMenu() {
        // this method is called again when a new custom search is added too.
        // TODO: reconstruct custom search mi only, do not reconstruct all
        if (this.editMenu == null) {
            this.editMenu = MenuFactory.createMenu("Menu.edit");
            this.editMenu.setEnabled(false);
        }
        this.editMenu.removeAll();
        this.addNewKeyMI = MenuFactory.createMenuItem("MenuItem.add.new.property");
        this.searchMenu = MenuFactory.createMenu("Menu.search");
        this.searchKeyMI = MenuFactory.createMenuItem("MenuItem.search.keys");
        this.searchValueMI = MenuFactory.createMenuItem("MenuItem.search.values");
        this.bulkTranslateMI = MenuFactory.createMenuItem("MenuItem.bulk.translate");
        this.resetTranslationSourceLanguageMI = MenuFactory.createMenuItem("MenuItem.reset.trans.from");
        this.alphabetsMI = MenuFactory.createMenuItem("MenuItem.alphabets");
        this.digitsMI = MenuFactory.createMenuItem("MenuItem.digits");
        this.untranslatedMI = MenuFactory.createMenuItem("MenuItem.untranslated");
        this.translatedMI = MenuFactory.createMenuItem("MenuItem.translated");
        this.missingMI = MenuFactory.createMenuItem("MenuItem.missing");
        this.emptyValueMI = MenuFactory.createMenuItem("MenuItem.empty.values");
        this.sameValueMI = MenuFactory.createMenuItem("MenuItem.same.values");
        this.multilineMI = MenuFactory.createMenuItem("MenuItem.multiline.values");
        this.nextMatchMI = MenuFactory.createMenuItem("MenuItem.next.match");
        this.previousMatchMI = MenuFactory.createMenuItem("MenuItem.previous.match");
        this.clearMI = MenuFactory.createMenuItem("MenuItem.clear.results");

        this.replaceMenu = MenuFactory.createMenu("Menu.replace");
        this.replaceKeyMI = MenuFactory.createMenuItem("MenuItem.replace.keys");
        this.replaceValueMI = MenuFactory.createMenuItem("MenuItem.replace.values");

        this.eolMenu = MenuFactory.createMenu("MenuItem.change.eol");
        this.eolMenu.setEnabled(false);
        this.lfMI = MenuFactory.createRadioButtonMenuItem("MenuItem.eol.lf");
        this.crlfMI = MenuFactory.createRadioButtonMenuItem("MenuItem.eol.crlf");
        this.crMI = MenuFactory.createRadioButtonMenuItem("MenuItem.eol.cr");
        ActionListener eolChangeListener = new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    P p = getLocalizedProperties();
                    if (p != null) {
                        p.setEol(getEol());
                    }
                }
            };
        ItemListener itemListener = new ItemListener() {
                /** */
                @Override
                public void itemStateChanged(ItemEvent evt) {
                    JMenuItem mi = (JMenuItem) evt.getSource();
                    if (evt.getStateChange() != ItemEvent.SELECTED ||
                        !mi.isArmed()) {
                        // mi.isArmed() filters out programtical select
                        // performed in the background
                        return;
                    }

                    // doesn't allow them w/o making a backup
                    if (mi == I18NEditor.this.sortByDefaultMI ||
                        mi == I18NEditor.this.sortByAlphabetMI) {
                        if (!Options.getMakeBackup()) {
                            String msg =
                                I18N.get("Dialog.enable.make.backup.first");
                            DialogUtil.self().showMessageDialog(msg);
                            I18NEditor.this.sortNoneMI.setSelected(true);
                            return;
                        }
                    }

                    String msg = I18N.get("Dialog.save.to.apply.change");
                    int option = DialogUtil.self().showYesNoDialog(msg);
                    if (option == JOptionPane.OK_OPTION) {
                        saveAll();
                    }
                }
            };
        this.lfMI.addActionListener(eolChangeListener);
        this.lfMI.addItemListener(itemListener);
        this.crlfMI.addActionListener(eolChangeListener);
        this.crlfMI.addItemListener(itemListener);
        this.crMI.addActionListener(eolChangeListener);
        this.crMI.addItemListener(itemListener);
        final ButtonGroup bg1 = new ButtonGroup();
        bg1.add(this.lfMI);
        bg1.add(this.crlfMI);
        bg1.add(this.crMI);
        this.eolMenu.add(this.lfMI);
        this.eolMenu.add(this.crlfMI);
        this.eolMenu.add(this.crMI);

        this.delimiterMenu = MenuFactory.createMenu("Menu.unify.delimiters.to");
        this.noDelimiterChangeMI = MenuFactory.createRadioButtonMenuItem("MenuItem.no.delimiter.change");
        this.equalDelimiterMI = MenuFactory.createRadioButtonMenuItem("MenuItem.equal");
        this.colonDelimiterMI = MenuFactory.createRadioButtonMenuItem("MenuItem.colon");
        this.spaceDelimiterMI = MenuFactory.createRadioButtonMenuItem("MenuItem.white.space");
        this.equalDelimiterMI.addItemListener(itemListener);
        this.colonDelimiterMI.addItemListener(itemListener);
        this.spaceDelimiterMI.addItemListener(itemListener);
        final ButtonGroup bg2 = new ButtonGroup();
        bg2.add(this.noDelimiterChangeMI);
        bg2.add(this.equalDelimiterMI);
        bg2.add(this.colonDelimiterMI);
        bg2.add(this.spaceDelimiterMI);
        this.noDelimiterChangeMI.setSelected(true);
        this.delimiterMenu.add(this.noDelimiterChangeMI);
        this.delimiterMenu.add(this.equalDelimiterMI);
        this.delimiterMenu.add(this.colonDelimiterMI);
        this.delimiterMenu.add(this.spaceDelimiterMI);

        this.sortMenu = MenuFactory.createMenu("Menu.sort.property.keys.on.save");
        this.sortNoneMI = MenuFactory.createRadioButtonMenuItem("MenuItem.no.sorting");
        // default order: comments will be replaced with those in the default.
        // missing key will be merged. unknown keys will be lost.
        // alphabet order: comments will be lost.
        this.sortByDefaultMI = MenuFactory.createRadioButtonMenuItem("MenuItem.by.default.order");
        this.sortByAlphabetMI = MenuFactory.createRadioButtonMenuItem("MenuItem.by.alphabet");
        this.sortNoneMI.setSelected(true);
        this.sortByAlphabetMI.addItemListener(itemListener);
        this.sortByDefaultMI.addItemListener(itemListener);
        //this.sortNoneMI.addItemListener(itemListener);
        ButtonGroup bg3 = new ButtonGroup();
        bg3.add(this.sortByAlphabetMI);
        bg3.add(this.sortByDefaultMI);
        bg3.add(this.sortNoneMI);
        this.sortMenu.add(this.sortNoneMI);
        this.sortMenu.add(this.sortByDefaultMI);
        this.sortMenu.add(this.sortByAlphabetMI);

        this.editMenu.add(this.searchMenu);
        this.editMenu.add(this.replaceMenu);
        this.editMenu.addSeparator();
        this.editMenu.add(this.addNewKeyMI);
        this.editMenu.addSeparator();
        this.editMenu.add(this.eolMenu);
        this.editMenu.add(this.delimiterMenu);
        this.editMenu.add(this.sortMenu);

        // TODO: memory memory memory memory memory memory
        this.replaceKeyMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    replaceKeys();
                }
            });
        this.replaceValueMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    replaceValues();
                }
            });

        this.addNewKeyMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    addNewKey();
                }
            });

        this.searchKeyMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchKeys();
                }
            });
        this.searchValueMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchValues();
                }
            });
        this.untranslatedMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchUntranslatedKeys();
                }
            });
        this.translatedMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchTranslatedKeys();
                }
            });
        this.missingMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchMissingKeys();
                }
            });
        this.emptyValueMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchEmptyValueKeys();
                }
            });
        this.sameValueMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchSameValueKeys();
                }
            });
        this.multilineMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchMultilineValues();
                }
            });
        this.alphabetsMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchAlphabets();
                }
            });
        this.digitsMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    searchDigits();
                }
            });
        this.nextMatchMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    KEY_TREE.scrollToNextMatch();
                }
            });
        this.previousMatchMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    KEY_TREE.scrollToPreviousMatch();
                }
            });
        this.clearMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    KEY_TREE.clearState();
                }
            });

        this.searchMenu.add(this.searchKeyMI);
        this.searchMenu.add(this.searchValueMI);
        this.searchMenu.addSeparator();
        this.searchMenu.add(this.untranslatedMI);
        this.searchMenu.add(this.translatedMI);
        this.searchMenu.add(this.missingMI);
        this.searchMenu.addSeparator();
        this.searchMenu.add(this.emptyValueMI);
        this.searchMenu.add(this.sameValueMI);
        this.searchMenu.add(this.multilineMI);
        this.searchMenu.add(this.alphabetsMI);
        this.searchMenu.add(this.digitsMI);
        createCustomEditMIs();
        this.searchMenu.addSeparator();
        this.searchMenu.add(this.nextMatchMI);
        this.searchMenu.add(this.previousMatchMI);
        this.searchMenu.addSeparator();
        this.searchMenu.add(this.clearMI);

        this.replaceMenu.add(this.replaceKeyMI);
        this.replaceMenu.add(this.replaceValueMI);

        return this.editMenu;
    }

    /**
     *
     */
    private void createCustomEditMIs() {
        this.searchMenu.addSeparator();
        Properties p = new Properties();
        String str = Options.getCustomSearch();
        try {
            p.load(new StringReader(str));
            if (p.size() == 0) {
                JMenuItem item =
                    new JMenuItem(I18N.get("MenuItem.no.custom.search.found"));
                item.setEnabled(false);
                this.searchMenu.add(item);
            }
        } catch (IOException e) {
            //
        }

        Set<String> set = p.stringPropertyNames();
        if (set.size() != 0) {
            for (Iterator<String> itr = set.iterator(); itr.hasNext(); ) {
                final String key = itr.next();
                final String actionCommand = p.getProperty(key);
                JMenuItem mi = new JMenuItem(key);
                mi.setActionCommand(actionCommand);
                mi.addActionListener(new ActionListener() {
                        /** */
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            doCustomSearch(key, actionCommand);
                        }
                    });
                this.searchMenu.add(mi);
            }
        }
    }

    /**
     *
     */
    private JMenu createOptionsMenu() {
        this.optionsMenu = MenuFactory.createMenu("Menu.options");
        this.languageMenu = MenuFactory.createMenu("MenuItem.ui.language");
        this.defaultLocaleMI = MenuFactory.createMenuItem("MenuItem.auto.load.locale");
        createUILanguageMenu();
        this.allowUnencodedDefaultMI = MenuFactory.createCheckBoxMenuItem("MenuItem.allow.unencoded.default");
        this.regexSearchMI = MenuFactory.createCheckBoxMenuItem("MenuItem.regex.search");
        this.wildcardSearchMI = MenuFactory.createCheckBoxMenuItem("MenuItem.wildcard.search");
        this.caseSensitiveSearchMI = MenuFactory.createCheckBoxMenuItem("MenuItem.case.sensitive.search");
        this.mergeMissingMI = MenuFactory.createCheckBoxMenuItem("MenuItem.merge.missing.keys");
        this.escapeSpecialCharsMI = MenuFactory.createCheckBoxMenuItem("MenuItem.escape");
        this.useLowerCaseMI = MenuFactory.createCheckBoxMenuItem("MenuItem.use.lower.case");
        this.preserveBlanksMI = MenuFactory.createCheckBoxMenuItem("MenuItem.preserve.whitespaces");
        this.openMostRecentMI = MenuFactory.createCheckBoxMenuItem("MenuItem.open.most.recent");
        this.makeBackupMI = MenuFactory.createCheckBoxMenuItem("MenuItem.make.backup.on.save");
        this.saveOnExitMI = MenuFactory.createCheckBoxMenuItem("MenuItem.save.on.exit");
        this.saveLogOnExitMI = MenuFactory.createCheckBoxMenuItem("MenuItem.save.log.on.exit");
        this.confirmOnExitMI = MenuFactory.createCheckBoxMenuItem("MenuItem.show.exit.confirmation");

        this.warnUnsafeOnlyMI =
            MenuFactory.createCheckBoxMenuItem("MenuItem.warn.unsafe.duplicate.keys.only");

        this.editCustomSearchMI = MenuFactory.createMenuItem("MenuItem.edit.custom.search");

        this.editIgnoreListMenu = MenuFactory.createMenu("MenuItem.edit.ignore.list");
        this.editIgnoreListMenu.setEnabled(false);
        this.ignoreListValueMI = MenuFactory.createMenuItem("MenuItem.ignore.list.value");
        this.ignoreListKeyMI = MenuFactory.createMenuItem("MenuItem.ignore.list.key");
        this.editIgnoreListMenu.add(this.ignoreListKeyMI);
        this.editIgnoreListMenu.add(this.ignoreListValueMI);

        this.optionsMenu.add(this.languageMenu);
        this.optionsMenu.add(this.defaultLocaleMI);
        this.optionsMenu.addSeparator();
        this.optionsMenu.add(this.allowUnencodedDefaultMI);
        this.optionsMenu.addSeparator();
        this.optionsMenu.add(this.regexSearchMI);
        this.optionsMenu.add(this.wildcardSearchMI);
        this.optionsMenu.add(this.caseSensitiveSearchMI);
        this.optionsMenu.addSeparator();
        this.optionsMenu.add(this.mergeMissingMI);
        this.optionsMenu.add(this.escapeSpecialCharsMI);
        this.optionsMenu.add(this.useLowerCaseMI);
        this.optionsMenu.add(this.preserveBlanksMI);
        this.optionsMenu.addSeparator();
        this.optionsMenu.add(this.openMostRecentMI);
        this.optionsMenu.add(this.makeBackupMI);
        this.optionsMenu.add(this.saveOnExitMI);
        this.optionsMenu.add(this.saveLogOnExitMI);
        this.optionsMenu.add(this.confirmOnExitMI);
        this.optionsMenu.addSeparator();
        this.optionsMenu.add(this.warnUnsafeOnlyMI);
        this.optionsMenu.addSeparator();
        this.optionsMenu.add(this.editCustomSearchMI);
        this.optionsMenu.add(this.editIgnoreListMenu);

        this.allowUnencodedDefaultMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b =
                        I18NEditor.this.allowUnencodedDefaultMI.isSelected();
                    setAllowUnencodedDefault(b);
                    if (b) {
                        String msg =
                            I18N.get("Dialog.unencoded.prop.will.be.encoded");
                        DialogUtil.self().showWarningDialog(msg);
                    }
                }
            });

        this.regexSearchMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.regexSearchMI.isSelected();
                    Options.setRegexSearch(b);

                    // mutually exclusive opitons
                    I18NEditor.this.wildcardSearchMI.setEnabled(!b);
                    I18NEditor.this.caseSensitiveSearchMI.setEnabled(!b);
                    if (b) {
                        Options.setWildcardSearch(!b);
                        Options.setCaseSensitiveSearch(!b);
                    } else {
                        b = I18NEditor.this.wildcardSearchMI.isSelected();
                        Options.setWildcardSearch(b);
                        b = I18NEditor.this.caseSensitiveSearchMI.isSelected();
                        Options.setCaseSensitiveSearch(b);
                    }
                }
            });

        this.wildcardSearchMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.wildcardSearchMI.isSelected();
                    Options.setWildcardSearch(b);
                }
            });

        this.caseSensitiveSearchMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.caseSensitiveSearchMI.isSelected();
                    Options.setCaseSensitiveSearch(b);
                }
            });

        this.mergeMissingMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.mergeMissingMI.isSelected();
                    Options.setMergeMissingKeys(b);
                }
            });

        this.escapeSpecialCharsMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.escapeSpecialCharsMI.isSelected();
                    Options.setEscapeSpecialChars(b);
                }
            });

        this.useLowerCaseMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.useLowerCaseMI.isSelected();
                    Options.setUseLowerCase(b);
                }
            });

        this.preserveBlanksMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.preserveBlanksMI.isSelected();
                    Options.setPreserveBlanks(b);
                }
            });

        this.openMostRecentMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.openMostRecentMI.isSelected();
                    Options.setOpenMostRecent(b);
                }
            });

        this.makeBackupMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.makeBackupMI.isSelected();
                    Options.setMakeBackup(b);
                }
            });

        this.saveOnExitMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.saveOnExitMI.isSelected();
                    Options.setSaveOnExit(b);
                }
            });

        this.saveLogOnExitMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.saveLogOnExitMI.isSelected();
                    Options.setSaveLogOnExit(b);
                }
            });

        this.confirmOnExitMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.confirmOnExitMI.isSelected();
                    Options.setConfirmOnExit(b);
                }
            });

        this.editCustomSearchMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showEditCustomSearchDialog();
                    createEditMenu();
                }
            });

        this.ignoreListValueMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showIgnoreValueListDialog();
                }
            });

        this.defaultLocaleMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showDefaultLangToEditDialog();
                }
            });

        this.ignoreListKeyMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showIgnoreKeyListDialog();
                }
            });

        this.warnUnsafeOnlyMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    boolean b = I18NEditor.this.warnUnsafeOnlyMI.isSelected();
                    Options.setIgnoreDuplicates(b);
                }
            });

        this.regexSearchMI.setSelected(Options.getRegexSearch());
        this.wildcardSearchMI.setSelected(Options.getWildcardSearch());
        this.wildcardSearchMI.setEnabled(!this.regexSearchMI.isSelected());
        this.caseSensitiveSearchMI.setSelected(Options.getCaseSensitiveSearch());
        this.caseSensitiveSearchMI.setEnabled(!this.regexSearchMI.isSelected());

        this.mergeMissingMI.setSelected(Options.getMergeMissingKeys());
        this.escapeSpecialCharsMI.setSelected(Options.getEscapeSpecialChars());
        this.useLowerCaseMI.setSelected(Options.getUseLowerCase());
        this.preserveBlanksMI.setSelected(Options.getPreserveBlanks());
        this.openMostRecentMI.setSelected(Options.getOpenMostRecent());
        this.makeBackupMI.setSelected(Options.getMakeBackup());
        this.saveOnExitMI.setSelected(Options.getSaveOnExit());
        this.saveLogOnExitMI.setSelected(Options.getSaveLogOnExit());
        this.confirmOnExitMI.setSelected(Options.getConfirmOnExit());
        this.warnUnsafeOnlyMI.setSelected(Options.getIgnoreDuplicates());

        return this.optionsMenu;
    }

    /**
     *
     */
    private JMenu createToolsMenu() {
        this.toolsMenu = MenuFactory.createMenu("Menu.tools");
        this.toolsMenu.addMenuListener(new MenuListener() {
                /** */
                @Override
                public void menuCanceled(MenuEvent evt) {
                    // no-op
                }

                /** */
                @Override
                public void menuDeselected(MenuEvent evt) {
                    // no-op
                }

                /** */
                @Override
                public void menuSelected(MenuEvent evt) {
                    boolean b = !isBothLocalesSame(false);
                    I18NEditor.this.bulkTranslateMI.setEnabled(b);
                    I18NEditor.this.resetTranslationSourceLanguageMI.setEnabled(b);
                }
            });
        this.openInAppMI = MenuFactory.createMenuItem("MenuItem.open.in.app");
        this.quickConvMenu = MenuFactory.createMenu("Menu.quick.conv");
        this.quickConvN2AMI =
            MenuFactory.createMenuItem("MenuItem.conv.native2ascii");
        this.quickConvA2NMI =
            MenuFactory.createMenuItem("MenuItem.conv.ascii2native");
        this.quickConvMenu.addMenuListener(new MenuListener() {
                /** */
                @Override
                public void menuCanceled(MenuEvent evt) {
                    // no-op
                }

                /** */
                @Override
                public void menuDeselected(MenuEvent evt) {
                    // no-op
                }

                /** */
                @Override
                public void menuSelected(MenuEvent evt) {
                    String s = Util.getClipboardData();
                    s = s == null ? "" : s;
                    I18NEditor.this.quickConvA2NMI.
                        setEnabled(s.trim().length() != 0);
                }
            });
        this.scanMI = MenuFactory.createMenuItem("MenuItem.scan.for.key");
        this.scanConfigMI = MenuFactory.createMenuItem("MenuItem.scan.config");
        this.dialogPreviewMenu = MenuFactory.createMenu("Menu.dialog.preview");
        this.previewJavaScriptMI = MenuFactory.createMenuItem("MenuItem.preview.javascript");
        boolean browseEnabled = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        this.previewJavaScriptMI.setEnabled(browseEnabled);
        this.previewSwingMI = MenuFactory.createMenuItem("MenuItem.preview.swing");

        this.bulkTranslateMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    bulkTranslate();
                }
            });
        this.resetTranslationSourceLanguageMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    resetTranslationSourceLanguage();
                }
            });

        this.openInAppMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    P p = getLocalizedProperties();
                    if (p != null) {
                        try {
                            Desktop.getDesktop().open(p.getFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        this.quickConvN2AMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showQuickConverterDialog();
                }
            });

        this.scanMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    scan();
                }
            });

        this.scanConfigMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showScanConfigDialog();
                }
            });

        this.quickConvA2NMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showQuickConverterDialog2();
                }
            });

        this.previewSwingMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String key = KEY_TREE.getCurrentKey();
                    P p = Util.getLocalizedProperties();
                    DialogUtil.self().showMessageDialog(p.getProperty(key));
                }
            });

        this.previewJavaScriptMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String key = KEY_TREE.getCurrentKey();
                    String value =
                        Util.getLocalizedProperties().getProperty(key);
                    FileWriter writer = null;
                    try {
                        value = value.replaceAll("\n", "\\\\n");
                        String s = "<html><body onload=\"javascript: " +
                            "alert('0')\"></body></html>".replace("0", value);
                        StringReader reader = new StringReader(s);
                        final File f = File.createTempFile("tmp", ".html");
                        f.deleteOnExit();
                        writer = new FileWriter(f);
                        int n = 0;
                        char[] c = new char[s.length()];
                        while ((n = reader.read(c)) != -1) {
                            writer.write(c, 0, n);
                        }
                        new Thread(new Runnable() {
                                /** */
                                @Override
                                public void run() {
                                    try {
                                        Desktop.getDesktop().browse(f.toURI());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                            }
                        }
                    }

                }
            });

        this.openInAppMI.setEnabled(false);
        this.scanMI.setEnabled(false);
        this.dialogPreviewMenu.setEnabled(false);
        this.bulkTranslateMI.setEnabled(false);
        this.resetTranslationSourceLanguageMI.setEnabled(false);

        this.toolsMenu.add(this.openInAppMI);
        this.quickConvMenu.add(this.quickConvN2AMI);
        this.quickConvMenu.add(this.quickConvA2NMI);
        this.toolsMenu.add(this.quickConvMenu);
        this.dialogPreviewMenu.add(this.previewJavaScriptMI);
        this.dialogPreviewMenu.add(this.previewSwingMI);
        this.toolsMenu.add(this.dialogPreviewMenu);
        this.toolsMenu.addSeparator();
        this.toolsMenu.add(this.scanMI);
        this.toolsMenu.add(this.scanConfigMI);
        this.toolsMenu.addSeparator();
        this.toolsMenu.add(this.bulkTranslateMI);
        this.toolsMenu.add(this.resetTranslationSourceLanguageMI);

        return this.toolsMenu;
    }

    /**
     *
     */
    private JMenu createHelpMenu() {
        this.helpMenu = MenuFactory.createMenu("Menu.help");

        this.acceleratorMI = MenuFactory.createMenuItem("MenuItem.accelerator.help");
        this.aboutMI = MenuFactory.createMenuItem("MenuItem.about");

        this.acceleratorMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    DialogUtil.self().showAcceleratorHelpDialog();
                }
            });

        this.aboutMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String version = I18N.get("Dialog.about.message",
                                              "Shigehiro Soejima",
                                              "mightyfrog.gc@gmail.com",
                                              "1122200912");
                    DialogUtil.self().showMessageDialog(version);
                }
            });

        this.helpMenu.add(this.acceleratorMI);
        this.helpMenu.addSeparator();
        this.helpMenu.add(this.aboutMI);

        return this.helpMenu;
    }

    /**
     * Sets if unencoded defautl properties are allowed or not.
     *
     * @param allowUnencodedDefault
     */
    private void setAllowUnencodedDefault(boolean allowUnencodedDefault) {
        this.allowUnencodedDefault = allowUnencodedDefault;
    }

    /**
     * Returns true if unencoded default properties are allowed to be loaded.
     *
     */
    private boolean getAllowUnencodedDefault() {
        return this.allowUnencodedDefault;
    }

    /**
     * Toggles I18N_TA color. Color.YELLOW is set while the default prop is
     * being edited when the highlight option is set.
     *
     */
    private void toggleI18NTAColor() {
        if (LOCALE_MENU.isDefaultSelected() &&
            Options.getHighlightOnDefault()) {
            I18N_TA.setBackground(Color.YELLOW);
        } else {
            I18N_TA.setBackground(UIManager.getColor("TextArea.background"));
        }
    }

    /**
     *
     */
    private void addNewKey() {
        String[] keyValue =
            DialogUtil.self().showAddNewPropertyDialog();
        if (keyValue == null) {
            return;
        }
        String key = keyValue[0];
        String value = keyValue[1];
        if (KEY_TREE.addNewProperty(key, value)) {
            for (P p : PROP_MAP.values()) {
                //p.setProperty(key, value);
                p.put(key, value);
                p.setModified(true); // don't remove this line
            }
            KEY_TREE.fireSelectionChange();
            logStart(I18N.get("Log.Start.property.added") +
                     key + "=" + Util.escapeString(value));
        } else {
            String message = I18N.get("Dialog.key.already.exists", key);
            String title = I18N.get("Dialog.title.duplicate.key");
            DialogUtil.self().showErrorDialog(message, title);
        }
    }

    /**
     *
     */
    private void resetTranslationSourceLanguage() {
        String from = DialogUtil.self().
            showSetDefaultTranslationLanguageDialog();
        if (from == null) {
            return;
        }
        I18N_TA.setTranslationSourceLanguage(from);
    }

    /**
     * Bulk-translate untranslated properties.
     *
     */
    private void bulkTranslate() { // TODO: compact me too messy
        SearchResult result = KEY_TREE.searchUntranslated();
        List<KeyNode> keyNodeList = result.getResult();
        if (keyNodeList.isEmpty()) {
            String msg = I18N.get("Dialog.nothing.to.translate");
            DialogUtil.self().showMessageDialog(msg);
            return;
        }

        String msg = I18N.get("Dialog.proceed.bulk.translation");
        int option = DialogUtil.self().showOKCancelDialog(msg);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        String from = I18N_TA.getTranslationSourceLanguage();
        if (from == null) {
            resetTranslationSourceLanguage();
            from = I18N_TA.getTranslationSourceLanguage();
            if (from == null) {
                return;
            }
        }
        String to = I18N_TA.getTranslationDestLanguage();

        String[] names = new String[keyNodeList.size()];
        String[] values = new String[keyNodeList.size()];
        for (int i = 0; i < keyNodeList.size(); i++) {
            names[i] = keyNodeList.get(i).getKey();
            values[i] = keyNodeList.get(i).getValue();
        }
        P p = getLocalizedProperties();
        ArrayList<String> list = // prop name list
            new ArrayList<String>(Arrays.asList(names));
        for (int i = 0; i < names.length; i++) {
            if (values[i].trim().length() == 0) {
                list.remove(names[i]);
                continue;
            }
            if (values[i].length() == 1) {
                if (Character.isLetterOrDigit(values[i].toCharArray()[0])) {
                    list.remove(names[i]);
                    continue; // single letter or digt
                }
            } else {
                char[] array = values[i].toCharArray();
                boolean b = false;
                for (char c : array) {
                    if (!Character.isDigit(c)) {
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    list.remove(names[i]);
                    continue; // digits won't get translated
                }
            }
            if (values[i].indexOf("\n") != -1) {
                list.remove(names[i]);
            }
        }
        names = list.toArray(new String[]{});
        values = new String[names.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = p.getProperty(names[i]);
        }
        try {
            toggleCursor(true);
            String[] translated = Util.translate(from, to, values);
            if (translated == null) {
                KEY_TREE.clearState();
                msg = I18N.get("Dialog.translation.failed");
                String title = I18N.get("Dialog.title.error");
                DialogUtil.self().showErrorDialog(msg, title);
                return; // TODO: say something here
            }
            for (int i = 0; i < names.length; i++) {
                // "translated" could have one more index than "names"
                // so make sure to use names.length to loop through
                p.setProperty(names[i], translated[i]);
            }
            I18N_TA.reload();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            toggleCursor(false);
        }
   }

    // used by scan methods
    private int fileCount = 0;

    /**
     * Recursively scans files for unused properties.
     *
     */
    private void scan() {
        logStart(I18N.get("Log.Start.scan.for.unsed.properties"));
        boolean includeIgnoreKeys = Options.getScanIgnoreKeys();
        final Set<String> keys =
            new HashSet<String>(KEY_TREE.getAllKeys(includeIgnoreKeys));
        if (keys.size() == 0) {
            return;
        }

        String absPath = Util.getDefaultFile().getAbsolutePath();
        String regKey = Options.SCAN_PATH + absPath.hashCode();
        String scanPath = Options.getProperty(regKey);
        JFileChooser fc = new JFileChooser();
        if (scanPath != null) {
            fc.setCurrentDirectory(new File(scanPath));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fc.showOpenDialog(I18NEditor.this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File f = fc.getSelectedFile();
        Options.setProperty(regKey, f.getAbsolutePath());

        int maxFileCount = 0;
        try {
            toggleCursor(true);
            maxFileCount = Util.getFileCount(0, f) - 1;
        } finally {
            toggleCursor(false);
        }
        String message = I18N.get("Dialog.scanning", maxFileCount);
        final ProgressMonitor pm =
            new ProgressMonitor(this, message, " ", 0, maxFileCount);
        pm.setMillisToDecideToPopup(0); // 0 sec

        //
        new SwingWorker<Void, Void>() {
            /** */
            @Override
            public Void doInBackground() {
                I18NEditor.this.fileCount = 0;
                keys.removeAll(scan(f, keys, new HashSet<String>(), pm));
                toggleCursor(true);

                return null;
            }

            /** */
            @Override
            public void done() {
                toggleCursor(false);
                if (!pm.isCanceled()) {
                    pm.close();
                    updateKeyTree(keys);
                    int size = keys.size();
                    logEnd(I18N.get("Log.End.properties.found", size));
                    setStatus(I18N.get("Status.properties.found", size));

                } else {
                    logEnd(I18N.get("Log.End.scan.canceled"));
                }
            }
        }.execute(); // start
    }

    /**
     * Recursively scans files for unused properties.
     *
     * @param root the scan root file
     * @param keys the property name keys
     * @param result keys found in the specified source code
     * @param pm
     */
    private Set<String> scan(File root, Set<String> keys,
                             Set<String> result, ProgressMonitor pm) {
        // TODO: rewrite all scan methods
        keys.removeAll(result);
        if (keys.size() == 0) {
            pm.setProgress(pm.getMaximum());
        } else {
            String name = root.getName();
            String ext = name.substring(name.lastIndexOf(".") + 1);
            if (Options.getIgnoreScanExtensions().indexOf(ext) == -1 &&
                !pm.isCanceled() && !root.isDirectory()) {
                pm.setProgress(this.fileCount++);
                try {
                    Scanner scanner = new Scanner(root, "UTF-8");
                    scanner.useDelimiter(System.getProperty("line.separator"));
                    ArrayList<String> keyList = new ArrayList<String>(keys);
                    while (scanner.hasNext()) { // optimize me
                        String s = scanner.next();
                        for (int i = 0; i < keyList.size(); i++) {
                            String key = keyList.get(i);
                            if (s.indexOf(key) != -1) {
                                result.add(key);
                                keys.remove(key);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pm.setNote(root.getName());
            }
            File[] files = root.listFiles();
            if (files == null) {
                return result;
            }
            for (File file : files) {
                result = scan(file, keys, result, pm);
            }
        }

        return result;
    }

    /**
     * Used by scanner only.
     *
     * @param keys keys found
     */
    void updateKeyTree(Set<String> keys) {
        int count = 0;
        List<KeyNode> list = Collections.<KeyNode>emptyList();
        if (keys != null && keys.size() != 0) {
            list = KEY_TREE.setMatchedNode(keys);
            RESULT_PANEL.addKeys(list.toArray(new KeyNode[]{}));
            for (KeyNode node : list) {
                logContent(node.getKey());
            }
            count = list.size();
        } else {
            KEY_TREE.clearState();
        }
        String message = I18N.get("Dialog.properties.found", list.size());
        //logEnd(I18N.get("Log.End.properties.found", count));
        //setStatus(I18N.get("Status.properties.found", count));
        DialogUtil.self().showMessageDialog(message);
        setStatus(message);
    }

    /**
     * Puts the path of the specified file in the registry.
     *
     * @param file
     */
    private void registerFilePath(File file) {
        String regValue = Options.getRecentFiles();
        String filePath = file.getPath();
        if (regValue.length() == 0) {
            regValue = filePath;
        } else {
            if (regValue.indexOf(filePath) == -1) {
                regValue = filePath + "," + regValue;
            } else {
                if (!regValue.startsWith(filePath)) {
                    String[] paths = regValue.split(",");
                    if (paths.length == 1) {
                        regValue = filePath + "," + regValue;
                    } else {
                        regValue = regValue.replace(filePath, "");
                        regValue = regValue.replace(",,", ",");
                        regValue = filePath + "," + regValue;
                    }
                }
            }
        }
        Options.setRecentFiles(regValue);
        createOpenRecentMenu();
    }

    /**
     * Removes the specified file path from the recently used file list.
     *
     * @param path
     */
    private void unregisterFilePath(String path) {
        String regValue = Options.getRecentFiles();
        if (regValue.indexOf("," + path) != -1) {
            regValue = regValue.replace("," + path, "");
        } else if (regValue.indexOf(path + ",") != -1) {
            regValue = regValue.replace(path + ",", "");
        } else {
            regValue = null;
        }
        Options.setRecentFiles(regValue);
        Options.cleanup(path.hashCode());
    }

    /**
     *
     * @param list
     */
    private void showSearchResultDialog(SearchResult result) {
        List<KeyNode> list = result.getResult();
        if (list != null) {
            String message = I18N.get("Dialog.properties.found", list.size());
            setStatus(message);
            if (result.getIgnoredCount() != 0) {
                message = I18N.get("Dialog.properties.found2", list.size(),
                                   result.getIgnoredCount());
            }
            DialogUtil.self().showMessageDialog(message);
        }
    }

    /**
     *
     * @param count
     */
    private void showReplaceResultDialog(int count) {
        String message = I18N.get("Dialog.properties.replaced", count);
        DialogUtil.self().showMessageDialog(message);
        setStatus(message);
    }

    /**
     * Returns all the localized properties files for the current
     * default properties excluding the default properties file.
     *
     */
    private File[] getPropertiesFiles() {
        File propDir = getDefaultFile().getParentFile();

        return propDir.listFiles(new FilenameFilter() {
                /** */
                @Override
                public boolean accept(File file, String name) {
                    // append "_" to exclude the default properties
                    return name.startsWith(Util.getDefaultName() + "_") &&
                        name.endsWith(F_EXT) &&
                        (name.indexOf("backup") == -1);
                }
            });
    }

    /**
     *
     * @param defaultProperties
     */
    private void createLocaleMenu(P defaultProperties) {
        File[] files = getPropertiesFiles();
        LOCALE_MENU.setEnabled(true);
        LOCALE_MENU.removeAll();
        JRadioButtonMenuItem mi =
            MenuFactory.createRadioButtonMenuItem("MenuItem.default.locale");
        LOCALE_MENU.add(mi);
        mi.setActionCommand(defaultProperties.getFile().getName());
        mi.setSelected(true);
        LOCALE_MENU.addSeparator();
        String autoloadLocale = Options.getAutoloadLocale();
        boolean autoloadLocaleFound = // "" means no default set
            autoloadLocale.equals("") ? true : false;
        for (File f : files) {
            P p = new P(defaultProperties, f);
            PROP_MAP.put(f.getName(), p);
            Locale l = p.getLocale();
            String text =
                l.getDisplayName(I18N.getLocale()) + " [" + l + "]";
            mi = new JRadioButtonMenuItem(text);
            mi.setActionCommand(f.getName());
            LOCALE_MENU.add(mi);
            if (l.toString().equals(autoloadLocale)) {
                autoloadLocaleFound = true;
                mi.setSelected(true);
                I18N_TA.reload();
            }
        }
        if (!autoloadLocaleFound && files.length != 0) {
            String fileName = // this prop file does not exist
                Util.getDefaultName() + "_" + autoloadLocale + F_EXT;
            String locale = Options.getAutoloadLocale();
            String message =
                I18N.get("Dialog.autoload.locale.not.found",
                         fileName, locale,
                         new Locale(locale).
                         getDisplayName(new Locale(Options.getLanguage())));
            String title = I18N.get("Dialog.title.autoloading.locale");
            DialogUtil.self().showWarningDialog(message, title);
        }

        this.addNewLocaleMI =
            MenuFactory.createMenuItem("MenuItem.add.new.locale");
        this.addNewLocaleMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    addNewLocale();
                }
            });

        if (files.length != 0) {
            LOCALE_MENU.addSeparator();
        }
        LOCALE_MENU.add(this.addNewLocaleMI);
    }

    /**
     *
     */
    private void createUILanguageMenu() {
        Locale[] locales = I18N.getAvailableLocales();
        if (locales.length == 0) {
            return;
        }
        LanguageChangeListener listener = new LanguageChangeListener();
        ButtonGroup bg = new ButtonGroup();
        JRadioButtonMenuItem defaultMI =
            new JRadioButtonMenuItem(I18N.get("MenuItem.ui.default.lang"));
        defaultMI.addActionListener(listener);
        defaultMI.setActionCommand("");
        bg.add(defaultMI);
        this.languageMenu.add(defaultMI);
        this.languageMenu.addSeparator();
        String lang = Options.getLanguage();
        Set<String> set = new HashSet<String>();
        for (Locale l : locales) {
            if (l != Locale.ROOT) {
                String text =
                    l.getDisplayName(I18N.getLocale()) + " [" + l + "]";
                if (set.contains(text)) {
                    continue;
                }
                set.add(text);
                JRadioButtonMenuItem mi = new JRadioButtonMenuItem(text);
                mi.setActionCommand(l.toString());
                if (lang.length() == 0) {
                    defaultMI.setSelected(true);
                } else {
                    if (lang.equals(l.toString())) {
                        mi.setSelected(true);
                    }
                }
                bg.add(mi);
                mi.addActionListener(listener);
                this.languageMenu.add(mi);
            }
        }
        if (!I18N.isAvailable(Util.getLocale(lang))) {
            defaultMI.setSelected(true);
            Options.setLanguage("");
        }
    }

    /**
     *
     * @param p
     * @param fresh
     */
    private void setLocalizedProperties(P p, boolean refresh) {
        // TODO: rewrite me, too messy
        // TODO: optimize refresh == true case
        if (!refresh && this.localizedProperties == p) {
            return;
        }
        this.localizedProperties = p;
        updateFrameTitle(p);
        updateEolMIState(p.getEol());

        UNKNOWN_KEY_PANEL.addKeys(new String[]{});
        if (refresh || p != getDefaultProperties()) {
            logStart(I18N.get("Log.Start.loading",
                              p.getFile().getAbsolutePath()));
            List<KeyNode> duplicateList = p.getDuplicateKeys();
            logDuplicates(p); // logs duplicate keys
            String[] missingKeys = p.getMissingKeys();
            if (missingKeys.length != 0) {
                for (String key : missingKeys) {
                    logContent(I18N.get("Log.Content.missing.key", key));
                }
                logEnd(I18N.get("Log.End.missing.keys.found",
                                missingKeys.length));
            }

            String[] unknownKeys = p.getUnknownKeys();
            if (unknownKeys.length != 0) {
                for (String key : unknownKeys) {
                    logContent(I18N.get("Log.Content.unknown.key", key));
                }
                logEnd(I18N.get("Log.End.unknown.keys.found",
                                unknownKeys.length));
                UNKNOWN_KEY_PANEL.addKeys(unknownKeys);
            }
            if (duplicateList.size() == 0 && missingKeys.length == 0 &&
                unknownKeys.length == 0) {
                logEnd("OK");
                setStatus("OK");
            }
            setKeyCounts(missingKeys.length, duplicateList.size(),
                         unknownKeys.length);
            KEY_TREE.updateMissingKeys();
        } else {
            setKeyCounts(0, p.getDuplicateKeys().size(), 0);
        }

        String lang = Options.getLanguage();
        String displayName = p.getLocale().getDisplayName(new Locale(lang));
        String label = p.getFile().getName();
        if (displayName.length() != 0) {
            label = displayName + " - " + label;
        }
        I18N_LABEL.setText(label);
        I18N_LABEL.setUI(VERTICAL_LABEL_UI);
    }

    /**
     *
     * @param p
     */
    private void updateFrameTitle(P p) {
        // sets the frame title
        if (p == null) {
            return;
        }
        String title =
            I18N.get("Frame.title", p.getModified() ? "*" : "",
                     p.getLocale().getDisplayName(I18N.getLocale()),
                     p.getFile().getPath(), p.stringPropertyNames().size());
        setTitle(title);
    }

    /**
     *
     * @param eol
     */
    private void updateEolMIState(String eol) {
        if (eol.equals(Util.EOL_STR_CRLF)) {
            this.crlfMI.setSelected(true);
        } else if (eol.equals(Util.EOL_STR_LF)) {
            this.lfMI.setSelected(true);
        } else {
            this.crMI.setSelected(true);
        }
    }

    /**
     *
     * @param missing the missing key count
     * @param duplicate the duplicat key count
     * @param unknown the unknown key count
     */
    private void setKeyCounts(int missing, int duplicate, int unknown) {
        setMissingCount(missing);
        setDuplicateCount(duplicate);
        setUnknownCount(unknown);
    }

    /**
     * Creates the status bar.
     *
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;

        panel.add(STATUS_LABEL, gbc);
        gbc.weightx = 0.1;
        MISSING_LABEL.addMouseListener(new MouseAdapter() {
                /** */
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (MISSING_LABEL.getIcon() != null &&
                        evt.getClickCount() == 2) {
                        searchMissingKeys();
                    }
                }
            });
        panel.add(MISSING_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        panel.add(DUPLICATE_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(UNKNOWN_LABEL, gbc);

        STATUS_LABEL.setBorder(BorderFactory.createEtchedBorder());
        MISSING_LABEL.setBorder(BorderFactory.createEtchedBorder());
        DUPLICATE_LABEL.setBorder(BorderFactory.createEtchedBorder());
        UNKNOWN_LABEL.setBorder(BorderFactory.createEtchedBorder());

        STATUS_LABEL.setText("OK");
        setKeyCounts(0, 0, 0);

        return panel;
    }

    /**
     *
     */
    private void removeRecent() {
        List<String> list = new ArrayList<String>();
        for (int i = 0;
             i < this.openRecentMenu.getItemCount() - 2;
             i++) {
            JMenuItem mi = this.openRecentMenu.getItem(i);
            list.add(mi.getActionCommand());
        }
        list = DialogUtil.self().showRemoveFileHistoryDialog(list);
        if (list.size() != 0) {
            for (String path : list) {
                unregisterFilePath(path);
            }
            createOpenRecentMenu();
        }
    }

    /**
     *
     */
    private void addNewPropFile() {
        JFileChooser fc = getFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                /** */
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".properties");
                }

                /** */
                @Override
                public String getDescription() {
                    return "*.propeties";
                }
            });
        fc.showOpenDialog(this);
        File file = fc.getSelectedFile();
        if (file != null) {
            if (!canOverwrite(file)) {
                return;
            }
            String name = file.getName();
            if (!name.toLowerCase().endsWith(".properties")) {
                try {
                    File newFile =
                        new File(file.getParent(), name + ".properties");
                    if (newFile.createNewFile()) {
                        file = newFile;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Util.saveStringToFile("", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            loadProperty(file);
        }
    }

    /**
     *
     */
    private void addNewLocale() {
        String localeStr = DialogUtil.self().showAddNewLocaleDialog();
        if (localeStr == null) {
            return;
        }
        int startIndex = localeStr.indexOf('[');
        int lastIndex = localeStr.lastIndexOf(']');
        localeStr = localeStr.substring(startIndex + 1, lastIndex);
        String name = Util.getDefaultName() + "_" + localeStr + F_EXT;
        File file = new File(getDefaultFile().getParent(), name);
        if (file.exists()) {
            String msg = I18N.get("Dialog.already.exists", file.getName());
            DialogUtil.self().showMessageDialog(msg);
        } else {
            try {
                Util.copyFile(getDefaultFile(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        createLocaleMenu(getDefaultProperties());

        // TODO: move me
        // need to call I18N_TA.reload()?
        for (int i = 0; i < LOCALE_MENU.getItemCount(); i++) {
            JMenuItem mi = LOCALE_MENU.getItem(i);
            if (mi != null) {
                if (mi.getText().matches(".*\\[" + localeStr + "\\]$")) {
                    mi.setSelected(true);
                    break;
                }
            }
        }
    }

    /**
     *
     */
    private void confirmExit() {
        I18N_TA.flushLog();
        Options.store();
        if (getDefaultFile() != null) {
            int option = JOptionPane.YES_OPTION;
            if (Options.getConfirmOnExit()) {
                String message = I18N.get("Dialog.do.u.want.to.exit");
                option = DialogUtil.self().showYesNoDialog(message);
            }
            if (option == JOptionPane.YES_OPTION) {
                if (Options.getSaveOnExit()) {
                    for (P p : new ArrayList<P>(PROP_MAP.values())) {
                        if (p.getModified()) {
                            save(p, null, true);
                        }
                    }
                } else {
                    if (confirmSaveModified() == null) {
                        return;
                    }
                }

                if (Options.getSaveLogOnExit()) { // TODO: rewrite me
                    File logDir = new File(getDefaultFile().getParent(),
                                           Util.getDefaultName() + "_log");
                    if (!logDir.exists()) {
                        if (!logDir.mkdirs()) {
                            // shouldn't happen
                        }
                    }
                    String name = "i18n_" + System.currentTimeMillis() + ".log";
                    try {
                        Util.saveStringToFile(LOG_TA.getText(),
                                              new File(logDir, name));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    //
    //
    //

    /**
     *
     */
    private class FileOpenListener implements ActionListener {
        /** */
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (confirmSaveModified() == null) {
                return;
            }
            JMenuItem mi = (JMenuItem) evt.getSource();
            String fileName = mi.getActionCommand();
            File file = new File(fileName);
            if (file.exists()) {
                loadProperty(file);
            }
        }
    }

    /**
     *
     */
    private class TableChangeListener implements PropertyChangeListener {
        private int index = -1;

        /**
         *
         * @param index
         */
        public TableChangeListener(int index) {
            this.index = index;
        }

        /** */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            // rowFilled property is also fired when the table gets cleared.
            if (name.equals("rowFilled")) {
                boolean filled = (Boolean) evt.getNewValue();
                TABBED_PANE.setEnabledAt(this.index, filled);
                if (!filled){
                    TABBED_PANE.setSelectedIndex(0);
                }
            } else if (name.equals("keyChange")) {
                KEY_TREE.scrollToKey((String) evt.getNewValue());
            } else if (name.equals("propertyDeleted")) {
                String[] keys = (String[]) evt.getNewValue();
                P prop = getLocalizedProperties();
                logStart(I18N.get("Log.Start.removing.unknown",
                                  prop.getFile().getName()));
                for (String key : keys) {
                    prop.remove(key);
                    logContent(key);
                }
                logEnd(I18N.get("Log.End.properties.removed", keys.length));
                setStatus(I18N.get("Status.unknown.properties.removed",
                                   keys.length));
            }
        }
    }

    /**
     *
     */
    private static class LanguageChangeListener implements ActionListener {
        /** */
        @Override
        public void actionPerformed(ActionEvent evt) {
            String lang = evt.getActionCommand();
            if (!Options.getLanguage().equals(lang)) {
                Options.setLanguage(lang);
                String msg = I18N.get("Dialog.restart.to.apply.lang.change");
                DialogUtil.self().showMessageDialog(msg);
            }
        }
    }

    /**
     *
     */
    static class Label extends JLabel {
        //
        private static Icon _icon = null;

        {
            Object obj = UIManager.getIcon("OptionPane.warningIcon");
            int iconSize = getFontMetrics(getFont()).getHeight();
            if (obj instanceof ImageIcon) {
                Image img = ((ImageIcon) obj).getImage();
                _icon =
                    new ImageIcon(img.getScaledInstance(iconSize,
                                                        iconSize,
                                                        Image.SCALE_SMOOTH));
            }
        }

        /**
         *
         */
        public Label() {
            this(null);
        }

        /**
         *
         * @param text
         */
        public Label(String text) {
            super(text);
            setIconTextGap(3);
            setOpaque(false);
        }

        /** */
        @Override
        public void setText(String text) {
            this.setText(text, false);
        }

        //
        //
        //

        /**
         *
         * @param text
         * @param showIcon
         */
        void setText(String text, boolean showIcon) {
            super.setText(text);
            if (showIcon) {
                setIcon(_icon);
                setForeground(SystemColor.textText);
            } else {
                setIcon(null);
                setForeground(SystemColor.textInactiveText);
            }
        }
    }

    /**
     *
     */
    private class SaveAllTask extends SwingWorker<Set<String>, Void> {
        /** */
        @Override
        public Set<String> doInBackground() {
            ProgressMonitor pm =
                new ProgressMonitor(I18NEditor.this,
                                    I18N.get("ProgressMonitor.saving"),
                                    " ", 0, PROP_MAP.size() - 1);
            pm.setMillisToDecideToPopup(0); // popup delay
            int i = 0;
            Set<String> savedSet = new HashSet<String>(); // contains saved file names
            for (P p : PROP_MAP.values()) {
                if (pm.isCanceled()) {
                    break;
                }
                pm.setProgress(i++);
                pm.setNote(p.getFile().getName());
                if (save(p, null, true)) {
                    savedSet.add(p.getFile().getName());
                }
            }

            return savedSet;
        }

        /** */
        @Override
        public void done() {
            String fileName =
                getLocalizedProperties().getFile().getName();

            Set<String> savedSet = null;
            try {
                savedSet = get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return;
            }

            if (savedSet.contains(fileName)) {
                refresh();
            }

            Set<String> tmpSet = new HashSet<String>(PROP_MAP.keySet());
            tmpSet.removeAll(savedSet); // set contains unsaved file names
            if (tmpSet.size() != 0) {
                String message = I18N.get("Dialog.properties.not.saved");
                StringBuilder sb = new StringBuilder(message);
                for (String s : tmpSet) {
                    sb.append(Util.LINE_SEPARATOR + s);
                }
                DialogUtil.self().showMessageDialog(sb.toString());
            }
        }
    }

    /**
     * The first menu item must be the default properties menu item.
     *
     */
    private class LocaleMenu extends JMenu {
        //
        private ButtonGroup bg = new ButtonGroup();

        //
        private JMenuItem defaultMI = null;
        private JMenuItem selectedMI = null;

        //
        private transient ItemListener listener = new ItemListener() {
                /** */
                @Override
                public void itemStateChanged(ItemEvent evt) {
                    if (evt.getStateChange() == ItemEvent.SELECTED) {
                        JMenuItem mi = (JMenuItem) evt.getSource();
                        setSelectedMI(mi);
                        P p = PROP_MAP.get(mi.getActionCommand());
                        setLocalizedProperties(p, false);
                        dirtyCheck(false);
                        I18N_TA.reload();
                    }
                }
            };

        {
            setText(I18N.get("Menu.locale"));
            setMnemonic(I18N.get("Menu.locale.mnemonic").charAt(0));
        }

        /** */
        @Override
        public JMenuItem add(JMenuItem mi) {
            if (this.defaultMI == null) {
                this.defaultMI = mi;
                this.defaultMI.addItemListener(new ItemListener() {
                    /** */
                    @Override
                    public void itemStateChanged(ItemEvent evt) {
                        toggleI18NTAColor();
                    }
                });

            }
            bg.add(mi);
            mi.addItemListener(this.listener);

            return super.add(mi);
        }

        /** */
        @Override
        public void removeAll() {
            super.removeAll();
            this.defaultMI = null;
            this.selectedMI = null;
            this.bg = new ButtonGroup();
        }

        //
        //
        //


        /**
         *
         */
        boolean isDefaultSelected() {
            return this.defaultMI.isSelected();
        }

        /**
         * Toggles the selected menu item between the default menu item and the
         * last selected non-default menu item.
         *
         */
        void toggleSelection() {
            if (this.defaultMI == null | this.selectedMI == null) {
                return;
            }
            if (!this.defaultMI.isSelected() &&
                this.selectedMI != this.defaultMI) {
                this.defaultMI.setSelected(true);
            } else {
                this.selectedMI.setSelected(true);
            }
        }

        /**
         * Sets the selected menu item.
         *
         * @param selectedMI
         */
        private void setSelectedMI(JMenuItem selectedMI) {
            if (selectedMI != this.defaultMI) {
                this.selectedMI = selectedMI;
            }
        }
    }

    /**
     *
     */
    private static class VerticalLabelUI extends BasicLabelUI {
        /** */
        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension dim = super.getPreferredSize(c);

            return new Dimension(dim.height, dim.width);
        }

        /** */
        @Override
        public void paint(Graphics g, JComponent c) {
            JLabel label = (JLabel) c;
            String text = label.getText();
            if (text == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform tr = g2.getTransform();
            g2.rotate(Math.PI / 2);
            g2.translate(0, -label.getWidth());
            int offset = g.getFontMetrics().getAscent();
            paintEnabledText(label, g, text, offset, offset);
            //if (c.isEnabled()) {
            //    paintEnabledText(label, g, text, offset, offset);
            //} else {
            //    paintDisabledText(label, g, text, offset, offset);
            //}
            g2.setTransform(tr);
        }
    }
}
