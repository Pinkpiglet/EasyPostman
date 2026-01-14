package com.laker.postman.util;

import javax.swing.*;

public class MenuFactory {
    public JMenu createMenu(String title) {
        return new JMenu(title);
    }

    public JMenuItem createMenuItem(String title) {
        return new JMenuItem(title);
    }

    public JRadioButtonMenuItem createRadioMenuItem(String title) {
        return new JRadioButtonMenuItem(title);
    }
}
