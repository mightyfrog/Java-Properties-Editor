package org.mightyfrog.i18n.editor;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import javax.swing.JOptionPane;

import org.json.*;

/**
 *
 *
 * @author Shigehiro Soejima
 */
class Util {
    static final int SORT_NONE = 0;
    static final int SORT_DEFAULT = 1;
    static final int SORT_ALPHABET = 2;

    static final int DELIMITER_EQUAL = 0;
    static final int DELIMITER_COLON = 1;
    static final int DELIMITER_SPACE = 2;
    static final int DELIMITER_NO_CHANGE = 3;

    static final char EOL_CHAR_CR = '\r';
    static final char EOL_CHAR_LF = '\n';
    static final String EOL_STR_CRLF = "\r\n";
    static final String EOL_STR_LF = "\n";
    static final String EOL_STR_CR = "\r";

    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String GOOGLE_API_URL =
        "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&";
    private static final String REFERER =
        "http://www.mysite.com/index.html";

    /**
     * Creates a Util.
     *
     */
    private Util() {
        // this class is not allowed to be instantiated.
    }

    /**
     * Invokes Google translation API.
     *
     * @param source the source language
     * @param dest the destination language
     * @param keys the keys to translate
     * @throws java.io.IOException
     */
    static String[] translate(String source, String dest, String... keys)
        throws IOException {
        if (keys.length == 0) {
            throw new IllegalArgumentException("Empty vararg");
        }
        ArrayList<ArrayList<String>> arrayCluster =
            new ArrayList<ArrayList<String>>();
        ArrayList<String> array = new ArrayList<String>();
        int charCount = 0;
        for (String s : keys) {
            try {
                s = "&q=" + URLEncoder.encode(s, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // won't happen
            }
            int len = s.length();;
            if (len > 500) {
                // too long, ignore it
                continue;
            }
            charCount += len;
            array.add(s);
            if (charCount > 500 && array.size() > 1) {
                arrayCluster.add(array);
                array = new ArrayList<String>();
                charCount = 0;
            }
        }
        if (!array.isEmpty()) {
            if (array.size() == 1) {
                array.add("&q="); // padding to get array result
            }
            arrayCluster.add(array);
        }
        ArrayList<String> responseList = new ArrayList<String>();
        BufferedReader reader = null;
        for (ArrayList<String> al : arrayCluster) {
            try {
                URL url = new URL(buildURL(al, source, dest));
                URLConnection con = url.openConnection();
                con.addRequestProperty("Referer", REFERER);
                InputStream is = con.getInputStream();
                Charset cs = Charset.forName("UTF-8");
                reader = new BufferedReader(new InputStreamReader(is, cs));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject json = new JSONObject(sb.toString());
                if (json.getInt("responseStatus") == 200) {
                    JSONArray jArray = json.getJSONArray("responseData");
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject obj = ((JSONObject) jArray.get(i)).
                            getJSONObject("responseData");
                        responseList.add(obj.getString("translatedText"));
                    }
                } else {
                    getRootFrame().logStart(I18N.get("Log.Start.google.api.error"));
                    getRootFrame().logEnd(json.getString("responseDetails"));
                    return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
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

        return responseList.toArray(new String[]{});
    }

    /**
     * Builds the request URL.
     *
     * @param list
     * @param source the source language
     * @param dest the destination language
     */
    private static final String buildURL(ArrayList<String> list,
                                         String source, String dest) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
        }

        return "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0" +
            sb.toString() + "&langpair=" + source + "%7C" + dest;
    }

    /**
     *
     * @param source the source language
     * @param dest the destination language
     * @param key the key to translate
     * @throws java.io.IOException
     */
    static String translate(String source, String dest, String key)
        throws IOException {
        String url = GOOGLE_API_URL + "&q=" +
            URLEncoder.encode(key, "UTF-8") + // UnsupportedEncodingException
            "&langpair=" + source + "%7C" + dest;

        String translated = null;
        BufferedReader reader = null;
        try {
            URLConnection con = new URL(url).openConnection();
            con.addRequestProperty("Refere", REFERER);
            reader =
                new BufferedReader(new InputStreamReader(con.getInputStream(),
                                                         Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            if (json.getInt("responseStatus") == 200) {
                json = json.getJSONObject("responseData");
                translated = json.getString("translatedText");
            } else {
                // do something here
            }
        } catch (IOException e) {
            // java.io.UnsupportedEncodingException shadowed
            throw e;
        } catch (JSONException e) {
            e.printStackTrace();
            // do something here
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return translated;
    }

    /**
     * Tests whether the specified key is valid or not. If not, escapes it
     * to a valid one.
     *
     * @param key
     */
    static String escapeKey(String key) {
        Properties p = new Properties();
        try {
            p.load(new StringReader(key));
            for (String escapedKey : p.stringPropertyNames()) {
                // should be entered once
                key = escapedKey;
            }
        } catch (IOException e) {
            // ignore
        }

        return key;
    }

    /**
     * Escapes the return character.
     *
     * @param str
     */
    static String escapeString(String str) {
        return str.replaceAll("\n", "\\\\n");
    }

    /**
     *
     * @param name the file name, like i18n_ja.properties
     */
    static P getPropertiesByFileName(String name) {
        return getRootFrame().getPropertiesByFileName(name);
    }

    /**
     * Constructs a Locale object from the given locale string.
     *
     * @param localeString ex. en_US
     */
    static Locale getLocale(String localeString) {
        if (localeString.length() == 0) {
            return Locale.ROOT;
        }
        String[] split = localeString.split("_");
        Locale locale = null;
        switch (split.length) {
        case 1:
            locale = new Locale(split[0]);
            break;
        case 2:
            locale = new Locale(split[0], split[1]);
            break;
        case 3:
            locale = new Locale(split[0], split[1], split[2]);
            break;
        }

        return locale;
    }

    /**
     * Constructs a Locale object from the given file.
     *
     * @param file
     */
    static Locale getLocale(File file) {
        if (file.getName().equals(getDefaultFile().getName())) {
            return Locale.ROOT;
        }
        String name = file.getName();
        String localeString = name.substring(getDefaultName().length() + 1,
                                             name.lastIndexOf("."));
        return getLocale(localeString);
    }

    /**
     * Exports the string to the system clipboard.
     *
     * @param str
     */
    static void exportToClipboard(String str) {
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
    }

    /**
     *
     */
    static String getClipboardData() {
        String text = null;
        try {
            text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().
                getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException e) {
            // ignore, the clipboard has non string data
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }

    /**
     * Returns the I18NEditor object.
     *
     */
    static I18NEditor getRootFrame() {
        return (I18NEditor) JOptionPane.getRootFrame();
    }

    /**
     * Returns the default properties file.
     *
     */
    static File getDefaultFile() {
        return getRootFrame().getDefaultFile();
    }

    /**
     * Returns the default property name.
     *
     */
    static String getDefaultName() {
        String name = getDefaultFile().getName();

        return name.substring(0, name.lastIndexOf("."));
    }

    /**
     * Returns the currently opened properites.
     *
     */
    static P getLocalizedProperties() {
        return getRootFrame().getLocalizedProperties();
    }

    /**
     * Returns the value for the specified key from the currently opened
     * properties.
     *
     * @param key
     */
    static String getLocalizedValue(String key) {
        return getLocalizedProperties().getProperty(key);
    }

    /**
     * Returns the name of the currently opened properties file.
     *
     */
    static String getLocalizedFileName() {
        return getRootFrame().getLocalizedProperties().getFile().getName();
    }

    /**
     * Persists the specified string to the specified file.
     *
     * @param str
     * @param file
     * @throws java.io.IOException
     */
    static void saveStringToFile(String str, File file)
        throws IOException {
        Reader reader = null;
        Writer writer = null;
        try {
            reader = new StringReader(str);
            writer = new FileWriter(file);
            int n = 0;
            char[] c = new char[256];
            while ((n = reader.read(c)) != -1) {
                writer.write(c, 0, n);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Makes a copy of the specified file.
     *
     * @param f1
     * @param f2
     * @throws java.io.IOException
     */
    static void copyFile(File f1, File f2) throws IOException {
        copyFile(f1, f2, 10000);
    }

    /**
     * Makes a copy of the specified file.
     *
     * @param f1
     * @param f2
     * @param allocSize
     * @throws java.io.IOException
     */
    static void copyFile(File f1, File f2, int allocSize)
        throws IOException {
        FileChannel ifc = null;
        FileChannel ofc = null;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(f1);
            ifc = in.getChannel();
            out = new FileOutputStream(f2);
            ofc = out.getChannel();
            ByteBuffer buffer = ByteBuffer.allocateDirect(allocSize); // 100KB
            while (true) {
                buffer.clear();
                if (ifc.read(buffer) < 0){
                    break;
                }
                buffer.flip();
                ofc.write(buffer);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (ifc != null) {
                try {
                    ifc.close();
                } catch (IOException e) {
                }
            }
            if (ofc != null) {
                try {
                    ofc.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     *
     * @param text
     */
    static String nativeToAscii(String text) {
        // TODO: can be optimized
        if (text.isEmpty()) {
            return text;
        }
        String str = text;
        Properties p = new Properties();
        Class<?> c = p.getClass();
        try {
            Method m = c.getDeclaredMethod("saveConvert", String.class,
                                           boolean.class, boolean.class);
            m.setAccessible(true);
            str = (String) m.invoke(p, text, false, true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        char[] chars = str.toCharArray();
        if (Options.getUseLowerCase()) {
            // this part is for consistency between the saveConvert method
            // (upper case) and native2ascii tool (lower case)
            boolean backslash = (chars[0] == '\\');
            for (int i = 0; i < chars.length; i ++) {
                if (chars[i] == 'u' && backslash) {
                    for (int j = 0; j < 4; j++) {
                        i++;
                        chars[i] = Character.toLowerCase(chars[i]);
                    }
                }
                backslash = chars[i] == '\\';
            }
        }

        String s = new String(chars);
        if (!Options.getEscapeSpecialChars()) {
            s = s.replace("\\=", "=");
            s = s.replace("\\:", ":");
            s = s.replace("\\#", "#");
            s = s.replace("\\!", "!");
        }

        return s;
    }

    /**
     * Adds the working directory to the classpath.
     *
     */
    static void addWorkingDirectoryToClasspath() throws Exception {
        try {
            Method method =
                URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            File file = new File(System.getProperty("user.dir"));
            method.invoke(Util.class.getClassLoader(),
                          new Object[]{file.toURI().toURL()});
        } catch (Exception e) {
            // NoSuchMethodException, IllegalAccessException
            // InvocationTargetException, MalformedURLException
            throw e;
        }
    }

    /**
     *
     * @param count
     * @param root
     */
    static int getFileCount(int count, File root) {
        if (!root.isDirectory()) {
            count++;
        }
        File[] files = root.listFiles();
        if (files == null) {
            return count;
        }
        for (File file : files) {
            count = getFileCount(count, file);
        }

        return count;
    }
}
