package org.mightyfrog.i18n.editor;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 *
 */
class MenuFactory {
    /**
     *
     * @param text
     */
    static JMenu createMenu(String text) {
        JMenu menu = new JMenu(I18N.get(text));
        String mnemonic = I18N.get(text + ".mnemonic");
        if (mnemonic != null) {
            char c = mnemonic.charAt(0);
            menu.setMnemonic(c);
            setDisplayedMnemonicIndex(menu, c, text);
        }

        return menu;
    }

    /**
     *
     * @param text
     */
    static JMenuItem createMenuItem(String text) {
        JMenuItem menuItem = new JMenuItem(I18N.get(text));
        setMnemonicInfo(menuItem, text);

        return menuItem;
    }

    /**
     *
     * @param text
     */
    static JCheckBoxMenuItem createCheckBoxMenuItem(String text) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(I18N.get(text));
        setMnemonicInfo(menuItem, text);

        return menuItem;
    }

    /**
     *
     * @param text
     */
    static JRadioButtonMenuItem createRadioButtonMenuItem(String text) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(I18N.get(text));
        setMnemonicInfo(menuItem, text);

        return menuItem;
    }

    //
    //
    //

    /**
     *
     * @param menuItem
     */
    private static void setMnemonicInfo(JMenuItem menuItem, String text) {
        String mnemonic = I18N.get(text + ".mnemonic");
        if (mnemonic != null) {
            char c = mnemonic.charAt(0);
            menuItem.setMnemonic(c);
            setDisplayedMnemonicIndex(menuItem, c, text);
        }
        String accelerator = I18N.get(text + ".accelerator");
        if (accelerator != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
    }

    /**
     *
     * @param menuItem
     * @param mnemonic
     * @param text
     */
    private static void setDisplayedMnemonicIndex(JMenuItem menuItem,
                                                  char mnemonic,
                                                  String text) {
        String indexStr = I18N.get(text + ".mnemonic.index");
        if (indexStr != null) {
            int index = 0;
            try {
                index = Integer.parseInt(indexStr);
            } catch (NumberFormatException e) {
            }
            if (Character.toUpperCase(I18N.get(text).charAt(index)) == mnemonic) {
                menuItem.setDisplayedMnemonicIndex(index);
            }
        }
    }
}
