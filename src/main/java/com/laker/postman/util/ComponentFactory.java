package com.laker.postman.util;

import javax.swing.*;

public class ComponentFactory {
    public JButton createIconButton(String tooltip, String iconPath, java.awt.event.ActionListener action) {
        JButton button = new JButton();
        // 简易图标加载，实际项目中请替换为正式图标加载逻辑
        button.setToolTipText(tooltip);
        // 使用现有图标加载方式占位
        return button;
    }
}
