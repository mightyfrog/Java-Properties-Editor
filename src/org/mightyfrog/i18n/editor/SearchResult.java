package org.mightyfrog.i18n.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 *
 */
class SearchResult {
    //
    static final  SearchResult SEARCH_RESULT = new SearchResult();

    //
    private List<KeyNode> list = new ArrayList<KeyNode>();

    //
    private int ignoredCount;

    /**
     *
     */
    private SearchResult() {
    }

    /**
     *
     *
     */
    static SearchResult sharedInstance() {
        SEARCH_RESULT.list.clear();
        SEARCH_RESULT.setIgnoredCount(0);

        return SEARCH_RESULT;
    }

    /**
     * Returns all the keys in the search result.
     *
     */
    Set<String> keySet() {
        Set<String> set = new HashSet<String>();
        for (KeyNode node : this.list) {
            set.add(node.getKey());
        }

        return set;
    }

    /**
     * Returns all the values in the search result.
     *
     */
    Set<String> valueSet() {
        Set<String> set = new HashSet<String>();
        for (KeyNode node : this.list) {
            set.add(node.getValue());
        }

        return set;
    }

    /**
     *
     * @param list
     */
    void setResult(List<KeyNode> list) {
        this.list = list;
    }

    /**
     *
     * @param ignoredCount
     */
    void setIgnoredCount(int ignoredCount) {
        this.ignoredCount = ignoredCount;
    }

    /**
     *
     */
    List<KeyNode> getResult() {
        return this.list;
    }

    /**
     *
     */
    int getIgnoredCount() {
        return this.ignoredCount;
    }
}
