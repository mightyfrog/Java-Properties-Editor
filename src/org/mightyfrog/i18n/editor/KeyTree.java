package org.mightyfrog.i18n.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Shigehiro Soejima
 */
class KeyTree extends JTree implements TreeSelectionListener,
                                       TreeWillExpandListener,
                                       MouseListener {
    /**
     * The root node.
     *
     */
    private final DefaultMutableTreeNode ROOT_NODE =
        new DefaultMutableTreeNode();

    //
    private final JPopupMenu POPUP = new JPopupMenu() {
            /** */
            @Override
            public void addNotify() {
                super.addNotify();

                SwingUtilities.updateComponentTreeUI(POPUP);
            }
        };
    private JMenuItem renameMI = null;
    private JMenuItem deleteMI = null;
    private JMenuItem copyKeyMI = null;
    private JMenuItem mergeKeyMI = null;

    private List<KeyNode> matchList = new ArrayList<KeyNode>();

    {
        getActionMap().put("deleteNode", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    deleteNode();
                }
            });
        getActionMap().put("editNode", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    changeNodeName();
                }
            });
        getActionMap().put("copyNode", new AbstractAction() {
                /** */
                public void actionPerformed(ActionEvent evt) {
                    exportNodeNameToClipboard();
                }
            });
        InputMap im = getInputMap();
        im.put(KeyStroke.getKeyStroke("pressed DELETE"), "deleteNode");
        im.put(KeyStroke.getKeyStroke("pressed F2"), "editNode");
        im.put(KeyStroke.getKeyStroke("ctrl pressed C"), "copyNode");
        im.put(KeyStroke.getKeyStroke("pressed LEFT"), "selectPrevious");
        im.put(KeyStroke.getKeyStroke("pressed RIGHT"), "selectNext");
        im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("ctrl alt pressed F"), "selectNext");
        im.put(KeyStroke.getKeyStroke("ctrl alt pressed B"), "selectPrevious");
    }

    /**
     * Creates a new KeyTree.
     *
     */
    public KeyTree() {
        ((DefaultTreeModel) getModel()).setRoot(ROOT_NODE);

        addTreeSelectionListener(this);
        addTreeWillExpandListener(this);
        addMouseListener(this);

        setCellRenderer(new KeyTree.CellRenderer());

        TreeSelectionModel tsm = getSelectionModel();
        tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setSelectionModel(tsm);

        initializePopupMenu();

        setTransferHandler(new KeyTree.PropertiesTransferHandler());
    }

    /** */
    @Override
    public void valueChanged(TreeSelectionEvent evt) {
        fireSelectionChange();
    }

    /** */
    @Override
    public void treeWillExpand(TreeExpansionEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void treeWillCollapse(TreeExpansionEvent evt)
        throws ExpandVetoException {
        // doesn't allow the root node to collapse
        if (evt.getPath().getPathCount() == 1) {
            throw(new ExpandVetoException(evt));
        }
    }

    /** */
    @Override
    public void mouseEntered(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseClicked(MouseEvent evt) {
        // double-click on the null TreePath scrolls the tree
        // to the selection path
        if (evt.getClickCount() == 2) {
            TreePath path = getPathForLocation(evt.getX(), evt.getY());
            if (path == null && !isSelectionEmpty()) {
                scrollRowToVisible(getSelectionRows()[0]);
            }
        }
    }

    /** */
    @Override
    public void mousePressed(MouseEvent evt) {
        handlePopup(evt);
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
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (ROOT_NODE.getUserObject() == null) {
            g.setColor(SystemColor.textInactiveText);
            g.drawString(I18N.get("Tree.message.drop.file.here"), 10, 20);

        }
    }

    //
    //
    //

    /**
     *
     * @param key
     * @param value
     */
    boolean addNewProperty(String key, String value) {
        if (duplicateKeyExists(key)) {
            return false;
        }
        KeyNode node = new KeyNode(key, value);
        ROOT_NODE.insert(node, 0);

        sort(getNodeList());
        scrollToKey(key);

        reload();

        return true;
    }

    /**
     * Sorts the tree.
     *
     */
    void sort(List<KeyNode> list) {
        Collections.sort(list, new KeyTree.KeyNodeComparator());

        ROOT_NODE.removeAllChildren();
        for (KeyNode node : list) {
            ROOT_NODE.add(node);
        }

        reload();
    }

    /**
     * Fires the node selection change notice. The new value is a KeyNode
     * object.
     *
     */
    void fireSelectionChange() {
        KeyNode node = null;
        TreePath path = getSelectionPath();
        if (path == null || path.getPathCount() == 1) {
            node = null;
        } else {
            node = (KeyNode) path.getLastPathComponent();
        }

        firePropertyChange("selectionChange", null, node);
    }

    /**
     * Deletes the selected node.
     *
     */
    void deleteNode() {
        TreePath path = getSelectionPath();
        if (path.getPathCount() == 1) {
            return;
        }
        KeyNode node = (KeyNode) path.getLastPathComponent();
        String key = node.getKey();
        int option =
            DialogUtil.self().showYesNoDialog(I18N.get("Dialog.delete", key));
        if (option == JOptionPane.YES_OPTION) {
            firePropertyChange("deleteProperty", null, key);
            ROOT_NODE.remove(node);
            this.matchList.remove(node);
            reload();
        }
    }

    /**
     * Merges a missing key.
     *
     */
    void mergeKey() {
        TreePath path = getSelectionPath();
        KeyNode node = (KeyNode) path.getLastPathComponent();
        node.setKeyMissing(false);
        reload(node);

        P p = Util.getRootFrame().getLocalizedProperties();
        String value = p.getProperty(node.getKey());
        if (value == null) {
            value = node.getValue();
        }
        p.put(node.getKey(), value);
        p.setModified(true);
        Util.getRootFrame().logStart(I18N.get("Log.Start.property.merged",
                                              p.getLocale(), value));
        Util.getRootFrame().setMissingCount(p.getMissingKeys().length);
    }

    /**
     * Renames the selected node.
     *
     */
    void changeNodeName() {
        TreePath path = getSelectionPath();
        KeyNode node = (KeyNode) path.getLastPathComponent();

        String message = I18N.get("Dialog.enter.new.property.key");
        String title = I18N.get("Dialog.title.rename");
        String newKey =
            DialogUtil.self().showInputDialog(message, title, node.getKey());
        if (newKey == null || newKey.trim().length() == 0) {
            return;
        }
        newKey = Util.escapeKey(newKey);
        I18NEditor rootFrame = Util.getRootFrame();
        P p = rootFrame.getDefaultProperties();
        if (!p.containsKey(newKey)) {
            String key = node.getKey();
            node.setKey(newKey);
            firePropertyChange("nameChange", key, newKey);
        } else {
            message = I18N.get("Dialog.same.name.exists");
            DialogUtil.self().showMessageDialog(message);
        }

        reload();
    }

    /**
     * Send the selected node name to the clipboard.
     *
     */
    void exportNodeNameToClipboard() {
        Util.exportToClipboard(getCurrentKey());
    }

    /**
     * Returns the current key string.
     *
     */
    String getCurrentKey() {
        TreePath path = getSelectionPath();
        KeyNode node = (KeyNode) path.getLastPathComponent();

        return node.getKey();
    }

    /**
     *
     *
     * @see #getAllKeys(boolean)
     */
    List<String> getAllKeys() {
        return getAllKeys(true);
    }

    /**
     *
     * @param includeIgnoreKeys
     * @see #getAllKeys()
     */
    List<String> getAllKeys(boolean includeIgnoreKeys) {
        List<KeyNode> nodeList = getNodeList();
        ArrayList<String> list = new ArrayList<String>();
        for (KeyNode node : nodeList) {
            String key = node.getKey();
            if (includeIgnoreKeys) {
                list.add(key);
            } else {
                if (!shouldIgnoreKey(key)) {
                    list.add(key);
                }
            }
        }

        return list;
    }

    /**
     *
     * @param value
     */
    boolean shouldIgnore(String value) {
        String regKey = Options.IGNORE_VALUE +
            Util.getDefaultFile().getAbsolutePath().hashCode();
        String str = Options.getProperty(regKey);
        if (str == null) {
            return false;
        }
        for (String regex : str.split("\n")) {
            if (value.matches(regex)) {
                String log =
                    I18N.get("Log.Content.matched.but.ignored", value);
                Util.getRootFrame().logContent(log);
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param key
     */
    boolean shouldIgnoreKey(String key) {
        String regKey = Options.IGNORE_KEY +
            Util.getDefaultFile().getAbsolutePath().hashCode();
        String str = Options.getProperty(regKey);
        if (str == null) {
            return false;
        }
        for (String regex : str.split("\n")) {
            if (key.matches(regex)) {
                String log =
                    I18N.get("Log.Content.matched.but.ignored", key);
                Util.getRootFrame().logContent(log);
                return true;
            }
        }

        return false;
    }

    /**
     *
     */
    void scrollToNextMatch() {
        scrollToMatch(Position.Bias.Forward);
    }

    /**
     *
     */
    void scrollToPreviousMatch() {
        scrollToMatch(Position.Bias.Backward);
    }

    /**
     *
     * @param bias
     */
    void scrollToMatch(Position.Bias bias) {
        // TODO: clean up
        if (this.matchList.size() == 0) {
            return;
        }

        TreePath selectionPath = getSelectionPath();
        if (selectionPath.getPathCount() == 1) {
            setSelectionRow(1);
            selectionPath = getSelectionPath();
        }
        KeyNode selectionNode =
            (KeyNode) selectionPath.getLastPathComponent();
        String selectionKey = selectionNode.getKey();
        int row = getSelectionRows()[0];
        TreePath path = null;
        if (bias == Position.Bias.Forward) {
            for (KeyNode node : this.matchList) {
                String key = node.getKey();
                if (selectionKey.compareTo(key) < 0) {
                    path = getNextMatch(key, row + 1, bias);
                    break;
                }
            }
        } else {
            for (int i = this.matchList.size() - 1; i >= 0; i--) {
                KeyNode node = this.matchList.get(i);
                String key = node.getKey();
                if (selectionKey.compareTo(key) > 0) {
                    path = getNextMatch(key, row - 1, bias);
                    break;
                }
            }
        }

        if (path != null) {
            setSelectionPath(path);
            scrollPathToVisible(path);
        } else {
            if (bias == Position.Bias.Forward) {
                scrollToFirstMatch();
            } else {
                scrollToLastMatch();
            }
        }
    }

    /**
     *
     * @param key
     */
    void scrollToKey(String key) {
        TreePath path = getNextMatch(key, 1, Position.Bias.Forward);
        setSelectionPath(path);
        scrollPathToVisible(path);
    }

    /**
     *
     */
    void scrollToFirstMatch() {
        if (this.matchList.size() == 0) {
            return;
        }

        KeyNode node = this.matchList.get(0);
        TreePath path = getNextMatch(node.getKey(), 1, Position.Bias.Forward);
        setSelectionPath(path);
        scrollPathToVisible(path);
    }


    /**
     *
     */
    void scrollToLastMatch() {
        if (this.matchList.size() == 0) {
            return;
        }

        KeyNode node = this.matchList.get(this.matchList.size() - 1);
        TreePath path = getNextMatch(node.getKey(), getChildCount(),
                                     Position.Bias.Backward);
        setSelectionPath(path);
        scrollPathToVisible(path);
    }

    /**
     *
     * @param keys key name set
     */
    List<KeyNode> setMatchedNode(Set<String> keys) {
        clearMatchList();
        List<KeyNode> list = getNodeList();
        for (KeyNode node : list) {
            if (keys.contains(node.getKey())) {
                node.setState(KeyNode.MATCH);
                this.matchList.add(node);
            } else {
                node.setState(KeyNode.UNDEF);
            }
        }
        reload();
        scrollToFirstMatch();

        return this.matchList;
    }

    /**
     *
     * @param text
     */
    SearchResult searchKeys(String text) {
        clearMatchList();
        text = formatQuery(text);
        List<KeyNode> list = getNodeList();
        SearchResult result = SearchResult.sharedInstance();
        Pattern pattern = compileRegex(text);
        if (pattern != null) {
            int ignoredCount = 0;
            for (KeyNode node : list) {
                String key = node.getKey();
                if (pattern.matcher(key).find()) {
                    if (shouldIgnoreKey(key)) {
                        node.setState(KeyNode.UNDEF);
                        ignoredCount++;
                        continue;
                    }
                    node.setState(KeyNode.MATCH);
                    this.matchList.add(node);
                } else {
                    node.setState(KeyNode.UNDEF);
                }
            }
            result.setResult(this.matchList);
            result.setIgnoredCount(ignoredCount);
            reload();
            scrollToFirstMatch();
        }

        return result;
    }

    /**
     *
     * @param text
     */
    SearchResult searchValues(String text) {
        clearMatchList();
        text = formatQuery(text);
        List<KeyNode> list = getNodeList();
        SearchResult result = SearchResult.sharedInstance();
        Pattern pattern = compileRegex(text);
        if (pattern != null) {
            int ignoredCount = 0;
            for (KeyNode node : list) {
                String key = node.getKey();
                String value = Util.getLocalizedValue(key);
                Matcher m = pattern.matcher(value);
                if (m != null && m.find()) {
                    if (shouldIgnore(value)) {
                        node.setState(KeyNode.UNDEF);
                        ignoredCount++;
                        continue;
                    }
                    node.setState(KeyNode.MATCH);
                    this.matchList.add(node);
                } else {
                    node.setState(KeyNode.UNDEF);
                }
            }
            result.setResult(this.matchList);
            result.setIgnoredCount(ignoredCount);
            reload();
            scrollToFirstMatch();
        }

        return result;
    }

    /**
     * Format a query string according to the search options.
     *
     * @param text
     */
    private String formatQuery(String text) {
        if (!Options.getRegexSearch()) {
            if (Options.getWildcardSearch()) {
                StringBuilder sb = new StringBuilder();
                if (!text.startsWith("*")) {
                    if (!text.startsWith("^")) {
                        sb.append("^");
                    }
                } else {
                    sb.append(".*");
                }
                String[] split = text.split("\\*");
                int i = 0;
                for ( ; i < split.length - 1; i++) {
                    sb.append(Pattern.quote(split[i]) + ".*");
                }
                sb.append(split[i]);
                if (text.endsWith("*")) {
                    sb.append(".*");
                } else {
                    sb.append("$");
                }
                text = sb.toString();
            } else {
                text = Pattern.quote(text);
            }

            if (!Options.getCaseSensitiveSearch()) {
                text = "(?i)" + text;
            }
        }

        return text;
    }

    /**
     * Searches untranslated values. The method assumes a localized value is
     * not translated if the value is indentical to the default value.
     *
     */
    SearchResult searchUntranslated() {
        clearMatchList();
        P prop = Util.getLocalizedProperties();
        SearchResult result = SearchResult.sharedInstance();
        if (prop != null && !prop.isDefault()) {
            List<KeyNode> list = getNodeList();
            int ignoredCount = 0;
            for (KeyNode node : list) {
                String key = node.getKey();
                if (shouldIgnoreKey(key)) {
                    node.setState(KeyNode.UNDEF);
                    ignoredCount++;
                    continue;
                }
                String value = prop.getProperty(key);
                if (!shouldIgnore(value) && value.equals(node.getValue())) {
                    node.setState(KeyNode.MATCH);
                    this.matchList.add(node);
                } else {
                    node.setState(KeyNode.UNDEF);
                }
            }
            result.setResult(this.matchList);
            result.setIgnoredCount(ignoredCount);
            reload();
            scrollToFirstMatch();
        }

        return result;
    }

    /**
     * Searches translated values. The method assumes a localized value is
     * translated if the value is not indentical to the default value.
     *
     */
    SearchResult searchTranslated() {
        clearMatchList();
        P prop = Util.getLocalizedProperties();
        SearchResult result = SearchResult.sharedInstance();
        if (prop != null && !prop.isDefault()) {
            List<KeyNode> list = getNodeList();
            int ignoredCount = 0;
            for (KeyNode node : list) {
                String key = node.getKey();
                if (shouldIgnoreKey(key)) {
                    node.setState(KeyNode.UNDEF);
                    ignoredCount++;
                    continue;
                }
                String value = prop.getProperty(key);
                if (!shouldIgnore(value) && !value.equals(node.getValue())) {
                    node.setState(KeyNode.MATCH);
                    this.matchList.add(node);
                } else {
                    node.setState(KeyNode.UNDEF);
                }
            }
            result.setResult(this.matchList);
            result.setIgnoredCount(ignoredCount);
            reload();
            scrollToFirstMatch();
        }

        return result;
    }

    /**
     * Searches missing values.
     *
     */
    SearchResult searchMissing() {
        clearMatchList();
        List<KeyNode> list = getNodeList();
        for (KeyNode node : list) {
            if (node.getKeyMissing()) {
                node.setState(KeyNode.MATCH);
                this.matchList.add(node);
            } else {
                node.setState(KeyNode.UNDEF);
            }
        }
        SearchResult result = SearchResult.sharedInstance();
        result.setResult(this.matchList);
        reload();
        scrollToFirstMatch();

        return result;
    }

    /**
     *
     */
    List<String> searchSameValueKeys() {
        clearMatchList();
        List<String> valueList = new ArrayList<String>();
        P prop = Util.getLocalizedProperties();
        if (prop != null) {
            List<KeyNode> list = getNodeList();
            List<String> tmpList = new ArrayList<String>();
            for (KeyNode node : list) {
                String key = node.getKey();
                String value = prop.getProperty(key);
                if (tmpList.contains(value)) {
                    if (!valueList.contains(value)) {
                        valueList.add(value);
                    }
                } else {
                    tmpList.add(value);
                }
            }
        }
        Collections.sort(valueList);

        return valueList;
    }

    /**
     * Searches alphabets in the current localized value.
     *
     */
    SearchResult searchAlphabets() {
        return searchRegex("[a-zA-Z]");
    }

    /**
     * Searchs numeric characters in the current localized value.
     *
     */
    SearchResult searchDigits() {
        return searchRegex("[0-9]"); // \d
    }

    /**
     *
     */
    SearchResult searchEmptyValueKeys() {
        return searchRegex("^$");
    }

    /**
     *
     */
    SearchResult searchMultilineValues() {
        return searchRegex("\n");
    }

    /**
     *
     * @param regex
     */
    SearchResult doCustomSearch(String regex) {
        return searchRegex(regex);
    }

    /**
     *
     * @param from
     * @param to
     */
    Map<KeyNode, String> replaceKeys(String from, String to) {
        List<KeyNode> list = getNodeList();
        Pattern pattern = compileRegex(formatQuery(from));
        Map<KeyNode, String> map = new HashMap<KeyNode, String>();
        if (pattern == null) {
            return map;
        }
        for (KeyNode node : list) {
            String key = node.getKey();
            Matcher m = pattern.matcher(key);
            if (m.find()) {
                String newKey = m.replaceAll(to);
                if (duplicateKeyExists(newKey)) {
                    map.put(new KeyNode(key), key);
                } else {
                    node.setKey(newKey);
                    map.put(node, key);
                }
            }
        }
        reload();
        return map;
    }

    /**
     *
     * @param from
     * @param to
     */
    Map<KeyNode, String> replaceValues(String from, String to) {
        P prop = Util.getLocalizedProperties();
        Map<KeyNode, String> map = new HashMap<KeyNode, String>();
        if (prop != null) {
            List<KeyNode> list = getNodeList();
            Pattern pattern = compileRegex(formatQuery(from));
            if (pattern == null) {
                return map;
            }
            for (KeyNode node : list) {
                String key = node.getKey();
                String value = prop.getProperty(key);
                Matcher m = pattern.matcher(value);
                if (m.find()) {
                    map.put(node, value);
                    String newValue = m.replaceAll(to);
                    prop.setProperty(key, newValue);
                }
            }
        }

        return map;
    }

    /**
     * Tests whether the specified key already exists in the tree.
     *
     * @param key
     */
    boolean duplicateKeyExists(String key) {
        List<KeyNode> list = getNodeList();
        for (KeyNode node : list) {
            if (node.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Clears search results.
     *
     */
    void clearState() {
        clearMatchList();

        List<KeyNode> list = getNodeList();
        for (KeyNode node :list) {
            node.setState(KeyNode.UNDEF);
        }
        setSelectionRow(1);
        scrollRowToVisible(1);

        reload();
    }

    /**
     * Sets the default properties.
     *
     * @param p the properties
     */
    void setProperties(P p) {
        ROOT_NODE.removeAllChildren();
        List<String> list = new ArrayList<String>();
        for (String key : p.stringPropertyNames()) {
            list.add(key);
        }
        Collections.sort(list);

        for (String name : list) {
            KeyNode node = new KeyNode(name);
            node.setValue(p.getProperty(name));
            ROOT_NODE.add(node);
        }

        String fileName = p.getFile().getName();
        ROOT_NODE.setUserObject("<html><body><b>" + fileName + "</b></body></html>");
        reload();
        expandRow(0);
    }

    /**
     * Reloads the tree without losing the selected node.
     *
     */
    void reload() {
        int[] rows = getSelectionRows();
        ((DefaultTreeModel) getModel()).reload();
        if (rows == null) {
            setSelectionRows(new int[]{1});
        } else {
            setSelectionRows(rows);
        }
    }

    /**
     * Reloads the specified node.
     *
     * @param node the node to reload
     */
    void reload(KeyNode node) {
        ((DefaultTreeModel) getModel()).reload(node);
    }

    /**
     *
     */
    void updateMissingKeys() {
        List<KeyNode> list = getNodeList();
        P prop = Util.getLocalizedProperties();
        for (KeyNode node :list) {
            node.setKeyMissing(prop.get(node.getKey()) == null);
        }

        reload();
    }

    /**
     *
     */
    List<KeyNode> getNodeList() {
        Enumeration enm = ROOT_NODE.breadthFirstEnumeration();
        enm.nextElement(); // skip the root node
        ArrayList<KeyNode> list = new ArrayList<KeyNode>();
        while (enm.hasMoreElements()) {
            list.add((KeyNode) enm.nextElement());
        }

        return list;
    }

    /**
     *
     */
    int getChildCount() {
        return ROOT_NODE.getChildCount();
    }

    /**
     *
     */
    void clearMatchList() {
        this.matchList.clear();
    }

    //
    //
    //

    /**
     * Use this method only when the given regular expression is
     * NOT an invalid expression.
     *
     */
    private SearchResult searchRegex(String exp) {
        clearMatchList();
        P prop = Util.getLocalizedProperties();
        SearchResult result = SearchResult.sharedInstance();
        if (prop == null) {
            return result;
        }
        Pattern pattern = Pattern.compile(exp);
        List<KeyNode> list = getNodeList();
        for (KeyNode node : list) {
            String value = prop.getProperty(node.getKey());
            Matcher m = pattern.matcher(value);
            if (m.find()) {
                node.setState(KeyNode.MATCH);
                this.matchList.add(node);
            } else {
                node.setState(KeyNode.UNDEF);
            }
        }
        reload();
        scrollToFirstMatch();

        result.setResult(this.matchList);

        return result;
    }

    /**
     * Initializes the popup menu.
     *
     */
    private void initializePopupMenu() {
        this.renameMI = MenuFactory.createMenuItem("Popup.MenuItem.rename");
        this.renameMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    changeNodeName();
                }
            });

        this.deleteMI = MenuFactory.createMenuItem("Popup.MenuItem.delete");
        this.deleteMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    deleteNode();
                }
            });

        this.copyKeyMI = MenuFactory.createMenuItem("Popup.MenuItem.copy.key.string");
        this.copyKeyMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    TreePath path = getSelectionPath();
                    KeyNode node = (KeyNode) path.getLastPathComponent();
                    Util.exportToClipboard(node.getKey());
                }
            });

        this.mergeKeyMI = MenuFactory.createMenuItem("Popup.MenuItem.merge.missing.key");
        this.mergeKeyMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    mergeKey();
                }
            });

        POPUP.add(this.copyKeyMI);
        POPUP.add(this.renameMI);
        POPUP.add(this.mergeKeyMI);
        POPUP.addSeparator();
        POPUP.add(this.deleteMI);
    }

    /**
     * Handles the popup event.
     *
     * @param evt
     */
    private void handlePopup(MouseEvent evt) {
        if (!evt.isPopupTrigger()) {
            return;
        }
        TreePath path = getPathForLocation(evt.getX(), evt.getY());
        if (path == null) {
            //popupAddNewPropertyDialog();
            return;
        }
        setSelectionPath(path);

        Object obj = path.getLastPathComponent();
        if (obj instanceof KeyNode) {
            KeyNode node = (KeyNode) obj;
            this.mergeKeyMI.setEnabled(node.getKeyMissing());

            boolean b = path.getPathCount() != 1;
            for (Component c : POPUP.getComponents()) {
                c.setVisible(b);
            }

            POPUP.show(this, evt.getX(), evt.getY());
        }
    }

    /**
     * Popups up the Add New Property dialog.
     *
     */
    //private void popupAddNewPropertyDialog() {
    //    String[] str = DialogUtil.self().showAddNewPropertyDialog();
    //    if (str != null) {
    //        addNewProperty(str[0], str[1]);
    //    }
    //}

    /**
     * Compiles the specified regular expression.
     *
     * @param regex
     */
    private Pattern compileRegex(String regex) {
        Pattern pattern = null;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            Util.getRootFrame().logStart(I18N.get("Log.Start.regex.syntax.error",
                                                  e.getLocalizedMessage()));
            DialogUtil.self().showWarningDialog(I18N.get("Dialog.invalid.regex"));
        }

        return pattern;
    }

    //
    //
    //

    /**
     *
     */
    private static class CellRenderer extends DefaultTreeCellRenderer {
        //
        private final Icon ROOT_ICON = new KeyTree.Icon(null);
        private final Icon GRAY_ICON = new KeyTree.Icon(Color.LIGHT_GRAY);
        private final Icon GREEN_ICON = new KeyTree.Icon(Color.GREEN);

        /** */
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected,
                                               expanded, leaf, row, hasFocus);
            if (row == 0) {
                setIcon(ROOT_ICON);
            } else {
                KeyNode node = (KeyNode) value;
                if (node.getKeyMissing()) {
                    setForeground(SystemColor.textInactiveText);
                } else {
                    setForeground(SystemColor.textText);
                }

                setText(node.getKey());

                if (node.getState() == KeyNode.UNDEF) {
                    setIcon(GRAY_ICON);
                } else {
                    setIcon(GREEN_ICON);
                }
            }

            return this;
        }
    }

    /**
     *
     * @param COLOR the icon color
     */
    static class Icon implements javax.swing.Icon { // same name -> fully qualified
        //
        private Color color = null;

        /**
         *
         * @param color null to make a transparent icon
         */
        public Icon(Color color) {
            this.color = color;
        }

        /** */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (this.color == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.color);
            g2.fillOval(3, 3, 10, 10);
            g2.setColor(this.color.darker());
            g2.drawOval(3, 3, 10, 10);
        }

        /** */
        @Override
        public int getIconHeight() {
            return this.color == null ? 3 : 16;
        }

        /** */
        @Override
        public int getIconWidth() {
            return this.color == null ? 3 : 16;
        }
    }

    /**
     *
     */
    static class PropertiesTransferHandler extends TransferHandler {
        /** */
        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            support.setShowDropLocation(false);
            Transferable t = support.getTransferable();
            try {
                List<?> list = null;
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    list = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } else {
                    String data =
                        (String) t.getTransferData(createURIListFlavor());
                    list = textURIListToFileList(data);
                }
                for (Object obj : list) {
                    File file = (File) obj;
                    if (file.isFile() && file.getName().endsWith(".properties")) {
                        return true;
                    }
                }
                return false;
            } catch (InvalidDnDOperationException e) {
                // ignore
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

            return super.canImport(support);
        }

        /** */
        @Override
        public boolean canImport(JComponent comp, DataFlavor[] flavors) {
            return true;
        }

        /** */
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                List<?> list = null;
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    list = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } else {
                    String data =
                        (String) t.getTransferData(createURIListFlavor());
                    list = textURIListToFileList(data);
                }
                for (int i = 0; i < list.size(); i++) {
                    File file = (File) list.get(i);
                    if (file.getName().endsWith(".properties")) {
                        Util.getRootFrame().loadProperty(file);
                    }
                }
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }

    /**
     *
     */
    static class KeyNodeComparator implements Comparator<KeyNode>, Serializable {
        /** */
        @Override
        public int compare(KeyNode n1, KeyNode n2) {
            return n1.getKey().compareTo(n2.getKey());
        }

        /** */
        @Override
        public boolean equals(Object n1) {
            return false; // tree never has the same key

            // for paranoia: only possibility of 'true' is comparing with itself
            //return n1 == this;
        }
    }

    //
    //
    //

    /**
     *
     */
    private static DataFlavor createURIListFlavor() {
        DataFlavor df = null;
        try {
            df = new DataFlavor("text/uri-list;class=java.lang.String");
        } catch (ClassNotFoundException e) {
            // shouldn't happen
        }

        return df;
    }

    /**
     *
     * @param uriList
     */
    private static List<File> textURIListToFileList(String uriList) {
        List<File> list = new ArrayList<File>(1);
        StringTokenizer st = new StringTokenizer(uriList, "\r\n");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.startsWith("#")) { // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                URI uri = new URI(s);
                File file = new File(uri);
                if (file.length() != 0) {
                    list.add(file);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        return list;
    }
}
