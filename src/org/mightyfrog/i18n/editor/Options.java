package org.mightyfrog.i18n.editor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 *
 * @author Shigehiro Soejima
 */
class Options {
    private static final String FILE_NAME = "pref.xml";

    //
    static final String AUTOLOAD_LOCALE = "autoloadLocale";
    static final String AUTO_ADJUST_DIVIDER = "autoAdjustDivider";
    static final String CONFIRM_ON_EXIT = "confirmOnExit";
    static final String CUSTOM_SEARCH = "customSearch";
    static final String ESCAPE_SPECIAL_CHARS = "escapeSpecialChars";
    static final String ESCAPE_TO_LOWER_CASE = "useLowerCase";
    static final String FILE_ENCODING_CHARSET = "fileEncodingCharset";
    static final String IGNORE_DUPLICATES = "ignoreDuplicates";
    static final String IGNORE_KEY = "ignoreK";
    static final String IGNORE_VALUE = "ignoreV";
    static final String LANGUAGE = "language";
    static final String MAKE_BACKUP = "makeBackup";
    static final String MERGE_MISSING_KEYS = "mergeMissingKeys";
    static final String PRESERVE_BLANKS = "preserveBlanks";
    static final String RECENT_FILES = "recentFiles";
    static final String SAVE_LOG_ON_EXIT = "saveLogOnExit";
    static final String SAVE_ON_EXIT = "saveOnExit";
    static final String SCAN_PATH = "scanPath";
    static final String VISIBLE_WHITESPACE = "visibleWhitespace";
    static final String LINE_WRAP = "lineWrap";
    static final String REGEX_SEARCH = "regexSearch";
    static final String WILDCARD_SEARCH = "wildcardSearch";
    static final String CASE_SENSITIVE_SEARCH = "caseSensitiveSearch";
    static final String HIGHLIGHT_ON_DEFAULT = "highlightOnDefault";
    static final String ALLOW_UNENCODED_DEFAULT = "allowEnencodedDefault";
    static final String IGNORE_SCAN_EXTENSIONS = "ignoreScanExt";
    static final String SCAN_IGNORE_KEYS = "scanIgnoreKeys";
    static final String OPEN_MOST_RECENT = "openMostRecent";

    private static final Properties PROP = new Properties() {
            {
                BufferedInputStream in = null;
                try {
                    File f = new File(FILE_NAME);
                    if (!f.exists() && !f.createNewFile()) {
                        // TODO: do something here
                    } else {
                        in = new BufferedInputStream(new FileInputStream(f));
                        loadFromXML(in);
                    }
                } catch (IOException e) {
                    // ignore
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        };

    /**
     *
     */
    private Options() {
        // this is a singleton class.
    }

    /**
     *
     */
    static boolean getOpenMostRecent() {
        return toBoolean(PROP.getProperty(OPEN_MOST_RECENT, "false"));
    }

    /**
     *
     */
    static boolean getScanIgnoreKeys() {
        return toBoolean(PROP.getProperty(SCAN_IGNORE_KEYS, "true"));
    }

    /**
     *
     */
    static String getIgnoreScanExtensions() {
        String ext = ".jpg,.png,.gif,.jar,.zip,.class,.war,.properties";
        return PROP.getProperty(IGNORE_SCAN_EXTENSIONS, ext);
    }

    /**
     *
     */
    static boolean getHighlightOnDefault() {
        return toBoolean(PROP.getProperty(HIGHLIGHT_ON_DEFAULT, "false"));
    }

    /**
     *
     */
    static boolean getRegexSearch() {
        return toBoolean(PROP.getProperty(REGEX_SEARCH, "false"));
    }

    /**
     *
     */
    static boolean getWildcardSearch() {
        return toBoolean(PROP.getProperty(WILDCARD_SEARCH, "false"));
    }

    /**
     *
     */
    static boolean getCaseSensitiveSearch() {
        return toBoolean(PROP.getProperty(CASE_SENSITIVE_SEARCH, "false"));
    }

    /**
     *
     */
    static boolean getMakeBackup() {
        return toBoolean(PROP.getProperty(MAKE_BACKUP, "true"));
    }

    /**
     *
     */
    static boolean getSaveOnExit() {
        return toBoolean(PROP.getProperty(SAVE_ON_EXIT, "false"));
    }

    /**
     *
     */
    static boolean getSaveLogOnExit() {
        return toBoolean(PROP.getProperty(SAVE_LOG_ON_EXIT, "true"));
    }

    /**
     *
     */
    static boolean getConfirmOnExit() {
        return toBoolean(PROP.getProperty(CONFIRM_ON_EXIT, "true"));
    }

    /**
     *
     */
    static boolean getEscapeSpecialChars() {
        return toBoolean(PROP.getProperty(ESCAPE_SPECIAL_CHARS, "false"));
    }

    /**
     *
     */
    static boolean getUseLowerCase() {
        return toBoolean(PROP.getProperty(ESCAPE_TO_LOWER_CASE, "true"));
    }

    /**
     *
     */
    static boolean getMergeMissingKeys() {
        return toBoolean(PROP.getProperty(MERGE_MISSING_KEYS, "false"));
    }

    /**
     *
     */
    static boolean getPreserveBlanks() {
        return toBoolean(PROP.getProperty(PRESERVE_BLANKS, "true"));
    }

    /**
     *
     */
    static boolean getIgnoreDuplicates() {
        return toBoolean(PROP.getProperty(IGNORE_DUPLICATES, "true"));
    }

    /**
     * Returns the UI language.
     *
     */
    static String getLanguage() {
        return PROP.getProperty(LANGUAGE, Locale.getDefault().getLanguage());
    }

    /**
     * Returns the locale to load in the i18n editor automatically.
     *
     */
    static String getAutoloadLocale() {
        return PROP.getProperty(AUTOLOAD_LOCALE, "");
    }

    /**
     *
     * @see getMostRecentFile()
     */
    static String getRecentFiles() {
        return PROP.getProperty(RECENT_FILES, "");
    }

    /**
     *
     * @see getRecentFiles()
     */
    static File getMostRecentFile() {
        String recentFiles = getRecentFiles();
        File recentFile = null;
        if (recentFiles.length() != 0) {
            recentFile = new File(recentFiles.split(",")[0]);
        }

        return recentFile;
    }

    /**
     *
     */
    static String getFileEncodingCharset() {
        return PROP.getProperty(FILE_ENCODING_CHARSET, "UTF-8");
    }

    /**
     *
     */
    static String getCustomSearch() {
        return PROP.getProperty(CUSTOM_SEARCH, "");
    }

    /**
     *
     */
    static boolean getVisibleWhitespace() {
        return toBoolean(PROP.getProperty(VISIBLE_WHITESPACE, "true"));
    }

    /**
     *
     */
    static boolean getAutoAdjustDivider() {
        return toBoolean(PROP.getProperty(AUTO_ADJUST_DIVIDER, "true"));
    }

    /**
     *
     */
    static File getScanPath() {
        String path = PROP.getProperty(SCAN_PATH, null);
        if (path == null) {
            return null;
        }

        return new File(path);
    }

    /**
     *
     */
    static boolean getLineWrap() {
        return toBoolean(PROP.getProperty(LINE_WRAP, "false"));
    }

    /**
     *
     * @param key
     */
    static String getProperty(String key) {
        return PROP.getProperty(key, null);
    }

    //
    //
    //

    /**
     *
     * @param b
     */
    static void setOpenMostRecent(boolean b) {
        if (b) {
            PROP.setProperty(OPEN_MOST_RECENT, "true");
        } else {
            PROP.remove(OPEN_MOST_RECENT);
        }
    }

    /**
     *
     * @param b 
     */
    static void setScanIgnoreKeys(boolean b) {
        if (b) {
            PROP.remove(SCAN_IGNORE_KEYS);
        } else {
            PROP.setProperty(SCAN_IGNORE_KEYS, "false");
        }
    }

    /**
     *
     * @param ext
     */
    static void setIgnoreScanExtensions(String ext) {
        PROP.setProperty(IGNORE_SCAN_EXTENSIONS, ext);
    }

    /**
     *
     * @param b
     */
    static void setHighlightOnDefault(boolean b) {
        if (b) {
            PROP.setProperty(HIGHLIGHT_ON_DEFAULT, "true");
        } else {
            PROP.remove(HIGHLIGHT_ON_DEFAULT);
        }
    }

    /**
     *
     * @param b
     */
    static void setRegexSearch(boolean b) {
        if (b) {
            PROP.setProperty(REGEX_SEARCH, "true");
        } else {
            PROP.remove(REGEX_SEARCH);
        }
    }

    /**
     *
     * @param b
     */
    static void setWildcardSearch(boolean b) {
        if (b) {
            PROP.setProperty(WILDCARD_SEARCH, "true");
        } else {
            PROP.remove(WILDCARD_SEARCH);
        }
    }

    /**
     *
     * @param b
     */
    static void setCaseSensitiveSearch(boolean b) {
        if (b) {
            PROP.setProperty(CASE_SENSITIVE_SEARCH, "true");
        } else {
            PROP.remove(CASE_SENSITIVE_SEARCH);
        }
    }

    /**
     *
     * @param b
     */
    static void setMakeBackup(boolean b) {
        if (b) {
            PROP.remove(MAKE_BACKUP);
        } else {
            PROP.setProperty(MAKE_BACKUP, "false");
        }
    }

    /**
     *
     * @param b
     */
    static void setSaveOnExit(boolean b) {
        if (b) {
            PROP.setProperty(SAVE_ON_EXIT, "true");
        } else {
            PROP.remove(SAVE_ON_EXIT);
        }
    }

    /**
     *
     * @param b
     */
    static void setSaveLogOnExit(boolean b) {
        if (b) {
            PROP.remove(SAVE_LOG_ON_EXIT);
        } else {
            PROP.setProperty(SAVE_LOG_ON_EXIT, "false");
        }
    }

    /**
     *
     * @param b
     */
    static void setConfirmOnExit(boolean b) {
        if (b) {
            PROP.remove(CONFIRM_ON_EXIT);
        } else {
            PROP.setProperty(CONFIRM_ON_EXIT, "false");
        }
    }

    /**
     *
     * @param b
     */
    static void setEscapeSpecialChars(boolean b) {
        if (b) {
            PROP.setProperty(ESCAPE_SPECIAL_CHARS, "true");
        } else {
            PROP.remove(ESCAPE_SPECIAL_CHARS);
        }
    }

    /**
     *
     * @param b
     */
    static void setUseLowerCase(boolean b) {
        if (b) {
            PROP.remove(ESCAPE_TO_LOWER_CASE);
        } else {
            PROP.setProperty(ESCAPE_TO_LOWER_CASE, "false");
        }
    }

    /**
     *
     * @param b
     */
    static void setMergeMissingKeys(boolean b) {
        if (b) {
            PROP.setProperty(MERGE_MISSING_KEYS, "true");
        } else {
            PROP.remove(MERGE_MISSING_KEYS);
        }
    }

    /**
     *
     * @param b
     */
    static void setPreserveBlanks(boolean b) {
        if (b) {
            PROP.remove(PRESERVE_BLANKS);
        } else {
            PROP.setProperty(PRESERVE_BLANKS, "false");
        }
    }

    /**
     *
     * @param b
     */
    static void setIgnoreDuplicates(boolean b) {
        if (b) {
            PROP.setProperty(IGNORE_DUPLICATES, "true");
        } else {
            PROP.remove(IGNORE_DUPLICATES);
        }
    }

    /**
     * Sets the UI language.
     *
     * @param language
     */
    static void setLanguage(String language) {
        PROP.setProperty(LANGUAGE, language);
    }

    /**
     *
     * @param autoloadLocale
     */
    static void setAutoloadLocale(String autoloadLocale) {
        PROP.setProperty(AUTOLOAD_LOCALE, autoloadLocale);
    }

    /**
     *
     */
    static void removeAutoloadLocale() {
        PROP.remove(AUTOLOAD_LOCALE);
    }

    /**
     *
     * @param recentFiles
     */
    static void setRecentFiles(String recentFiles) {
        if (recentFiles == null) {
            PROP.remove(RECENT_FILES);
        } else {
            PROP.setProperty(RECENT_FILES, recentFiles);
        }
    }

    /**
     *
     * @param charset
     */
    static void setFileEncodingCharset(String charset) {
        PROP.setProperty(FILE_ENCODING_CHARSET, charset);
    }

    /**
     *
     * @param customSearch
     */
    static void setCustomSearch(String customSearch) {
        PROP.setProperty(CUSTOM_SEARCH, customSearch);
    }

    /**
     *
     * @param b
     */
    static void setVisibleWhitespace(boolean b) {
        if (b) {
            PROP.remove(VISIBLE_WHITESPACE);
        } else {
            PROP.setProperty(VISIBLE_WHITESPACE, "false");
        }
    }

    /**
     * Sets the locale to load in the i18n editor automatically.
     *
     * @param b
     */
    static void setAutoAdjustDivider(boolean b) {
        if (b) {
            PROP.remove(AUTO_ADJUST_DIVIDER);
        } else {
            PROP.setProperty(AUTO_ADJUST_DIVIDER, "false");
        }
    }

    /**
     *
     * @param file the srouce path
     */
    static void setScanPath(File file) {
        PROP.setProperty(SCAN_PATH, file.getAbsolutePath());
    }

    /**
     *
     * @param b
     */
    static void setLineWrap(boolean b) {
        if (b) {
            PROP.setProperty(LINE_WRAP, "true");
        } else {
            PROP.remove(LINE_WRAP);
        }
    }

    /**
     *
     * @param property
     */
    static void setProperty(String key, String value) {
        PROP.setProperty(key, value);
    }

    /**
     *
     */
    static void store() {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(FILE_NAME));
            PROP.storeToXML(out, null, "UTF-8");
        } catch (IOException e) { // UnsupportedEncodingException shadowed
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     *
     * @param key
     */
    static Object remove(String key) {
        return PROP.remove(key);
    }

    /**
     * Call this method when .properties gets removed. All properties that are
     * specific to a particular .properites file get cleaned.
     *
     * @param hashCode the hash code of the .properties path.
     */
    static void cleanup(long hashCode) {
        remove(IGNORE_KEY + hashCode);
        remove(IGNORE_VALUE + hashCode);
        remove(SCAN_PATH + hashCode);
    }

    //
    //
    //

    /**
     *
     * @param s
     */
    private static boolean toBoolean(String s) {
        return Boolean.parseBoolean(s);
    }
}
