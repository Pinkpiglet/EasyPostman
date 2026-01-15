package com.laker.postman.panel;

import com.laker.postman.common.SingletonFactory;
import com.laker.postman.common.SingletonBasePanel;
import com.laker.postman.panel.sidebar.SidebarTabPanel;
import com.laker.postman.util.FontsUtil;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 包含了左侧的标签页面板和右侧的请求编辑面板。
 * 左侧标签页面板包含集合、工作区、压测等功能页。
 */
@Slf4j
public class MainPanel extends SingletonBasePanel {

    @Override
    protected void initUI() {
        setLayout(new BorderLayout()); // 设置布局为 BorderLayout
        JPanel loading = new JPanel(new GridBagLayout());
        loading.setOpaque(true);
        loading.setBackground(new Color(248, 250, 252));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 2, 6);
        JLabel iconLabel = new JLabel(new ImageIcon(createRoundLogo(
                com.laker.postman.common.constants.Icons.LOGO.getImage(), 32)));
        loading.add(iconLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(2, 6, 2, 6);
        JLabel loadingLabel = new JLabel(I18nUtil.getMessage(MessageKeys.GENERAL_LOADING), JLabel.CENTER);
        loadingLabel.setFont(FontsUtil.getDefaultFont(Font.PLAIN));
        loadingLabel.setForeground(new Color(100, 110, 120));
        loading.add(loadingLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(2, 6, 6, 6);
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(120, 1));
        separator.setForeground(new Color(220, 224, 230));
        loading.add(separator, gbc);

        add(loading, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> {
            remove(loading);
            add(SingletonFactory.getInstance(SidebarTabPanel.class), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }

    private Image createRoundLogo(Image source, int size) {
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
        Image scaled = source.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return output;
    }

    @Override
    protected void registerListeners() {
        // no-op
    }
}
