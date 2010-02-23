package org.mightyfrog.i18n.editor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Shigehiro Soejima
 */
class P extends Properties {
    /**
     * The properties file.
     *
     */
    private File file = null;

    //
    private List<KeyNode> DUPLICATE_KEY_LIST = new ArrayList<KeyNode>();
    private List<String> MISSING_KEY_LIST = new ArrayList<String>();

    //
    private final Map<String, String> REPLACED_KEY_MAP =
        new HashMap<String, String>();

    //
    private boolean modified = false;
    private boolean loaded = false;
    private boolean isDefault = false;

    //
    private String eol = Util.EOL_STR_LF;

    //
    private long lastModified = 0;

    /**
     *
     * @param file
     */
    public P(File file) {
        super();

        this.file = file;
        this.isDefault = true;
        updateLastModified();
    }

    /**
     *
     * @param defaultProperties the default Properties object
     * @param file the properties file
     */
    public P(Properties defaultProperteis, File file) {
        super(defaultProperteis);

        this.file = file;
        updateLastModified();
        InputStream is = null;
        Reader reader = null;
        try {
            is = file.toURI().toURL().openStream();
            reader = new InputStreamReader(is);
            load(reader);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // nothing can be done here
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /** */
    @Override
    public Object setProperty(String key, String value) {
        if (key == null || value == null) {
            return null;
        }

        if (get(key) == null && value.equals(getProperty(key))) {
            return null;
        }

        String s = (String) super.setProperty(key, value);
        // TODO: this condition checking fails when a new prop is added
        // call setModified(true) after this method as workaround
        if (s != null && !s.equals(value)) {
            setModified(true);
        }

        MISSING_KEY_LIST.remove(key); // watch this line, might be problematic

        return super.setProperty(key, value);
    }

    /** */
    @Override
    public Object put(Object key, Object value) {
        if (!this.loaded && containsKey(key)) {
            String loadedValue = getProperty((String) key);
            if (loadedValue.equals(value)) {
                if (Options.getIgnoreDuplicates()) {
                    return null;
                }
            }

            DUPLICATE_KEY_LIST.add(new KeyNode((String) key, loadedValue));
        }

        MISSING_KEY_LIST.remove(key); // watch this line, might be problematic

        return super.put(key, value);
    }

    /** */
    @Override
    public Object remove(Object key) {
        Object obj = super.remove(key);
        if (obj != null) {
            setModified(true);
            MISSING_KEY_LIST.remove(key); // watch this line, might be problematic
        }

        return obj;
    }

    /** */
    @Override
    public void load(Reader reader) throws IOException {
        super.load(detectEol(reader));

        this.loaded = true;
    }

    /** */
    @Override
    public void load(InputStream is) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        return ((P) obj).getFile() == getFile();
    }

    /** */
    @Override
    public int hashCode() {
        return getFile().hashCode();
    }

    //
    //
    //

    /**
     * Tests whether the prop file is modified or not.
     *
     */
    boolean isDirty() {
        // getFile().lastModified() must be the left side operand
        return getFile().lastModified() != this.lastModified;
    }

    /**
     *
     */
    void updateLastModified() {
        this.lastModified = getFile().lastModified();
    }

    /**
     *
     */
    String getEol() {
        return this.eol;
    }

    /**
     *
     * @param eol
     */
    void setEol(String eol) {
        this.eol = eol;
    }

    /**
     *
     * @param reader
     * @param throws java.io.IOException
     */
    BufferedReader detectEol(Reader reader) throws IOException {
        int c = -1; // char
        BufferedReader br = new BufferedReader(reader);
        try {
            br.mark(126);
            while ((c = br.read()) != -1) {
                if (c == Util.EOL_CHAR_LF) {
                    setEol(Util.EOL_STR_LF);
                    break;
                } else if (c == Util.EOL_CHAR_CR) {
                    if (br.read() == Util.EOL_CHAR_LF) {
                        setEol(Util.EOL_STR_CRLF);
                    } else {
                        setEol(Util.EOL_STR_CR);
                    }
                    break;
                }
            }
            br.reset();
        } catch (IOException e) {
            throw e;
        }

        return br;
    }

    /**
     * Returns true if this properites file contains duplicate keys with
     * different values.
     *
     */
    boolean hasUnsafeDuplicates() {
        return DUPLICATE_KEY_LIST.size() != 0;
    }

    /**
     * Returns true if this is the default properites.
     *
     */
    boolean isDefault() {
        return this.isDefault;
    }

    /**
     * Returns the locale of this property.
     *
     */
    Locale getLocale() {
        return Util.getLocale(getFile());
    }

    /**
     *
     * @param oldKey
     * @param newKey
     */
    void replaceKeys(String oldKey, String newKey) {
        // TODO: performance check
        String value = getProperty(oldKey);
        remove(oldKey);
        setProperty(newKey, value);

        if (REPLACED_KEY_MAP.containsValue(oldKey)) {
            // s = original key
            for (String s : REPLACED_KEY_MAP.keySet()) {
                if (REPLACED_KEY_MAP.get(s).equals(oldKey)) {
                    REPLACED_KEY_MAP.put(s, newKey);
                }
            }
        } else {
            REPLACED_KEY_MAP.put(oldKey, newKey);
        }
    }

    /**
     * Returns keys that do not exist in the default properties.
     *
     */
    String[] getUnknownKeys() {
        Set<String> s1 = stringPropertyNames();
        Set<String> s2 = Util.getRootFrame().
            getDefaultProperties().stringPropertyNames();
        s1.removeAll(s2);

        String[] keys = s1.toArray(new String[]{});
        Arrays.sort(keys);

        return keys;
    }

    /**
     *
     */
    List<KeyNode> getDuplicateKeys() {
        Collections.sort(DUPLICATE_KEY_LIST, new KeyTree.KeyNodeComparator());

        return DUPLICATE_KEY_LIST;
    }

    /**
     * Returns all the missing keys. Keys are sorted in natural order.
     *
     */
    String[] getMissingKeys() {
        if (MISSING_KEY_LIST.size() == 0) {
            Set<String> set = stringPropertyNames();
            for (String key : set) {
                if (get(key) == null) {
                    MISSING_KEY_LIST.add(key);
                }
            }
            Collections.sort(MISSING_KEY_LIST);
        }

        return (String[]) MISSING_KEY_LIST.toArray(new String[]{});
    }

    /**
     * Sets the modification state of this Properties object.
     *
     * @param modified
     */
    void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Tests whether this Properties object has been modified or not.
     *
     */
    boolean getModified() {
        return this.modified;
    }

    /**
     * Tests whether any missing property has been modified or not.
     *
     */
    boolean missingKeyModified() {
        for (String s : getMissingKeys()) {
            if (get(s) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the properties file.
     *
     */
    File getFile() {
        if (!this.file.exists()) {
            // file gets deleted manually while the editor is open
            // re-create file using this prop
            try {
                this.file.createNewFile();
                store(this.file, false);
                updateLastModified();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return this.file;
    }

    /**
     * Reloads from the .properties file.
     *
     * @throws java.io.IOException
     */
    synchronized void reload() throws IOException {
        Reader reader = null;
        try {
            reader = new FileReader(getFile());
            load(reader);
        } catch (IOException e) {
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     *
     * @param file
     * @param escape
     * @throws java.io.IOException
     */
    @SuppressWarnings("unchecked")
    synchronized void store(File file, boolean escape) throws IOException {
        // TODO: code clean up
        if (Options.getMakeBackup()) {
            makeBackup(file);
        }

        int sortType = Util.getRootFrame().getSortType();
        Set<String> writtenKeySet = new HashSet<String>();
        LineNumberReader lnr = null;
        StringBuilder sb = new StringBuilder();
        try {
            FileReader reader = null;
            if (sortType == Util.SORT_ALPHABET) {
                sortByAlphabet(sb, escape);
            } else {
                if (sortType == Util.SORT_NONE) {
                    reader = new FileReader(getFile());
                } else {
                    reader = new FileReader(Util.getDefaultFile());
                }
                lnr = new LineNumberReader(reader);
                String str = null;
                String multilineStr = null;
                while ((str = lnr.readLine()) != null) {
                    char[] chars = str.trim().toCharArray();
                    if (chars.length == 0 || chars[0] == '#' ||
                        chars[0] == '!') {
                        sb.append(str);
                    } else {
                        boolean backslash = chars[0] == '\\';
                        int index = 0;
                        char delimiter = ' ';
                        String startSpace = "";
                        for (int i = 0; i < chars.length; i++) {
                            char c = chars[i];
                            if ((c == '=' || c == ':' || c == ' ' ||
                                 c == '\t' || c == '\f') && !backslash) {
                                delimiter = c;
                                if (c == ' ') {
                                    c = chars[i + 1];
                                    if (c == ' ' || c == '=' || c == ':' ||
                                        c == '\t' || c == '\f') {
                                        startSpace += " ";
                                        continue;
                                    }
                                }
                                break;
                            } else {
                                index++;
                            }
                            backslash = c == '\\';
                        }
                        String key = str.substring(0, index);
                        String val = getProperty(key);
                        if (val != null) {
                            multilineStr = val;
                        }
                        if (writtenKeySet.contains(key)) {
                            // skip duplicate keys
                            continue;
                        }
                        writtenKeySet.add(key);
                        index += startSpace.length();
                        StringBuilder sb2 = new StringBuilder();
                        // TODO: check index has the correct num
                        // chars[i] == ' ' is throwing ArrayIndexOutOfBoundsException
                        // on one of the machines I use
                        for (int i = index + 1;
                             i < chars.length && i < str.length(); i++) {
                            if (chars[i] == ' ') {
                                sb2.append(" ");
                            } else {
                                break;
                            }
                        }
                        String endSpace = sb2.toString();
                        if (key.trim().length() != 0) {
                            // replace the original key w/ the replaced one
                            if (REPLACED_KEY_MAP.get(key) != null) {
                                key = REPLACED_KEY_MAP.get(key);
                            }
                            String value = getProperty(key);
                            if (value == null) { // multiline case
                                continue;
                            }
                            if (!Options.getPreserveBlanks()) {
                                startSpace = "";
                                endSpace = "";
                                value = value.trim();
                            }
                            switch (Util.getRootFrame().getDelimiterType()) {
                            case Util.DELIMITER_NO_CHANGE:
                                break;
                            case Util.DELIMITER_EQUAL:
                                delimiter = '=';
                                break;
                            case Util.DELIMITER_COLON:
                                delimiter = ':';
                                break;
                            case Util.DELIMITER_SPACE:
                                delimiter = ' ';
                                break;
                            }
                            sb.append(key + startSpace + delimiter + endSpace);
                            if (escape) {
                                sb.append(Util.nativeToAscii(value));
                            } else {
                                sb.append(Util.escapeString(value));
                            }
                        }
                    }
                    sb.append(getEol());
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (lnr != null) {
                try {
                    lnr.close();
                } catch (IOException e) {
                }
            }
        }

        // see if there are new keys
        Set<String> tmpSet = new HashSet(keySet());
        tmpSet.removeAll(writtenKeySet);
        String[] newKeys = tmpSet.toArray(new String[]{});
        Arrays.sort(newKeys);
        if (newKeys.length != 0 && sortType != Util.SORT_ALPHABET) {
            for (String key : newKeys) {
                char delimiter = '=';
                switch (Util.getRootFrame().getDelimiterType()) {
                case Util.DELIMITER_NO_CHANGE:
                    break; // ignore, use =
                case Util.DELIMITER_EQUAL:
                    delimiter = '=';
                    break;
                case Util.DELIMITER_COLON:
                    delimiter = ':';
                    break;
                case Util.DELIMITER_SPACE:
                    delimiter = ' ';
                    break;
                }
                sb.append(key + delimiter);
                String value = getProperty(key);
                if (escape) {
                    sb.append(Util.nativeToAscii(value));
                } else {
                    sb.append(Util.escapeString(value));
                }
                sb.append(getEol());
            }
        }

        if (sortType != Util.SORT_DEFAULT && Options.getMergeMissingKeys()) {
            String[] keys = getMissingKeys();
            for (String key : keys) {
                if (tmpSet.contains(key)) {
                    continue;
                }
                sb.append(key + "=" + Util.nativeToAscii(getProperty(key)));
                sb.append(getEol());
            }
        }

        Reader in = null;
        Writer out = null;
        try {
            Charset cs = Charset.forName("UTF-8");
            if (!escape) {
                cs = Charset.forName(Options.getFileEncodingCharset());
            }
            OutputStream os = new FileOutputStream(file);
            out = new BufferedWriter(new OutputStreamWriter(os, cs));
            in = new BufferedReader(new StringReader(sb.toString()));
            int n = 0;
            char[] c = new char[1024];
            while ((n = in.read(c)) != -1) {
                out.write(c, 0, n);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        setModified(false);
        MISSING_KEY_LIST.clear(); // watch this line, could be problematic
        DUPLICATE_KEY_LIST.clear();
        Util.getRootFrame().logEnd(I18N.get("Log.Start.saved",
                                            getFile().getName()));
    }

    /**
     *
     * @param escape
     * @throws java.io.IOException
     */
    void store(boolean escape) throws IOException {
        store(getFile(), escape);
    }

    /**
     *
     * @param file
     */
    void makeBackup(File file) { // TODO: rewrite me
        if (!file.exists()) {
            return;
        }
        File backupDir = new File(file.getParentFile(),
                                  Util.getDefaultName() + "_backup");
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                // shouldn't happen
                return;
            }
        }
        String name = file.getName();
        String nameNoExt = name.substring(0, name.indexOf("."));
        name = nameNoExt + " - backup.properties";
        File backup = new File(backupDir, name);
        int count = 2;
        while (backup.exists()) {
            name = nameNoExt + " - backup (" + count++ + ").properties";
            backup = new File(backupDir, name);
        }

        try {
            Util.copyFile(file, backup);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    //
    //
    //

    /**
     *
     * @param sb
     * @param escape
     */
    private void sortByAlphabet(StringBuilder sb, boolean escape) {
        Set<String> set = stringPropertyNames();
        List<String> keyList = new ArrayList<String>();
        for (String key : set) {
            keyList.add(key);
        }
        Collections.sort(keyList);
        for (String key : keyList) {
            String value = getProperty(key);
            if (value == null) {
                continue;
            }
            if (escape) {
                sb.append(key + "=" + Util.nativeToAscii(value));
            } else {
                sb.append(key + "=" + value);
            }
            sb.append(getEol());
        }
    }
}
