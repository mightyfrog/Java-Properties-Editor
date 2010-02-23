package org.mightyfrog.i18n.editor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarFile;

/**
 *
 */
class I18N  {
    //
    public static final String BASE = "i18n";

    //
    private static ResourceBundle _rb = null;


    /**
     *
     */
    private I18N() {
    }

    /**
     *
     * @param locale
     */
    static void init() {
        Locale locale = Util.getLocale(Options.getLanguage());
        if (!isAvailable(locale)) {
            locale = Locale.ROOT;
        }
        _rb = ResourceBundle.getBundle(BASE, locale);
    }

    /**
     *
     */
    static Locale getLocale() {
        return _rb.getLocale();
    }

    /**
     *
     * @param locale
     */
    static boolean isAvailable(Locale locale) {
        return Arrays.asList(getAvailableLocales()).contains(locale);
    }

    /**
     * Returns available locales.
     *
     */
    static Locale[] getAvailableLocales() {
        List<String> list = new ArrayList<String>();
        URL url =
            I18N.class.getProtectionDomain().getCodeSource().getLocation();
        File file = null;
        try {
            file = new File(url.toURI());
            JarFile jarFile = new JarFile(file);
            Enumeration enm = jarFile.entries();
            while (enm.hasMoreElements()) {
                String path = "" + enm.nextElement();
                if (path.startsWith(BASE)) {
                    list.add(path.substring(BASE.length(), path.length()));
                }
            }
        } catch (URISyntaxException e) {
            // won't happen
        } catch (IOException e) {
            // TODO: show err dialog here
            return new Locale[]{};
        }
        File parentDir = file.getParentFile(); // file is never null
        File[] files = parentDir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            String name = f.getName();
            if (name.startsWith(BASE) && name.endsWith(".properties")) {
                // TODO: this condition could pick up the wrong properties
                list.add(name.substring(BASE.length(), name.length()));
            }
        }
        File workingDir = new File(System.getProperty("user.dir"));
        if (!workingDir.toString().equals(parentDir.toString())) {
            files = workingDir.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    continue;
                }
                String name = f.getName();
                if (name.startsWith(BASE) && name.endsWith(".properties")) {
                    // TODO: this condition could pick up the wrong properties
                    list.add(name.substring(BASE.length(), name.length()));
                }
            }
        }
        Locale[] locales = new Locale[list.size()];
        for (int i = 0; i < locales.length; i++) {
            String str = list.get(i);
            if (str.charAt(0) == '.') {
                locales[i] = Locale.ROOT;
            } else {
                String localeStr = str.substring(1, str.indexOf("."));
                locales[i] = Util.getLocale(localeStr);
            }
        }

        return locales;
    }

    /**
     *
     * @param key
     */
    static String get(String key) {
        if (_rb == null) {
            init();
        }
        String value = null;
        try {
            value =_rb.getString(key);
        } catch (MissingResourceException e) {
        }

        return value;
    }

    /**
     *
     * @param key
     * @param inserts
     */
    static String get(String key, Object... inserts) {
        String s = get(key);
        if (s != null) {
            for (int i = 0; i < inserts.length; i++) {
                s = s.replace("{" + i + "}", String.valueOf((inserts[i])));
            }
        }

        return s;
    }
}
