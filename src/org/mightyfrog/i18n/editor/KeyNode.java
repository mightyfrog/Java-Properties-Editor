package org.mightyfrog.i18n.editor;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Creates a KeyNode.
 *
 * @author Shigehiro Soejima
 */
class KeyNode extends DefaultMutableTreeNode {
    public static final int UNDEF = 0;
    public static final int MATCH = 1;

    private String key = null;
    private String value = null;
    private boolean keyMissing = false;
    private int state = UNDEF;

    // TODO: remove keyMissing and add int MISSING = 2

    /**
     * Creates a new KeyNode.
     *
     * @param key
     */
    public KeyNode(String key) {
        setKey(key);
    }

    /**
     * Creates a new KeyNode.
     *
     * @param key
     * @param value
     */
    public KeyNode(String key, String value) {
        this(key);

        this.value = value;
    }

    /**
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     *
     * @param state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     *
     * @param keyMissing
     */
    public void setKeyMissing(boolean keyMissing) {
        this.keyMissing = keyMissing;
    }

    /**
     *
     */
    public String getKey() {
        return this.key;
    }

    /**
     *
     */
    public String getValue() {
        return this.value;
    }

    /**
     *
     */
    public int getState() {
        return this.state;
    }

    /**
     *
     */
    public boolean getKeyMissing() {
        return this.keyMissing;
    }

    /** */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        return ((KeyNode) obj).getKey().equals(getKey());
    }

    /** */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /** */
    @Override
    public String toString() {
        // modifying this breaks getNextMatch(...)
        return getKey();
    }
}
