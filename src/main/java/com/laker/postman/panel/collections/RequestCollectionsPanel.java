package com.laker.postman.panel.collections;

import com.laker.postman.common.SingletonFactory;
import com.laker.postman.common.SingletonBasePanel;
import com.laker.postman.common.constants.ModernColors;
import com.laker.postman.panel.collections.left.RequestCollectionsLeftPanel;
import com.laker.postman.panel.collections.right.RequestEditPanel;
import com.laker.postman.util.FontsUtil;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * 请求集合面板，包含左侧的请求集合列表和右侧的请求编辑面板
 */
@Slf4j
public class RequestCollectionsPanel extends SingletonBasePanel {
    @Override
    protected void initUI() {
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        RequestCollectionsLeftPanel requestCollectionsLeftPanel = SingletonFactory.getInstance(RequestCollectionsLeftPanel.class);
        RequestEditPanel rightRequestEditPanel = SingletonFactory.getInstance(RequestEditPanel.class);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, requestCollectionsLeftPanel, rightRequestEditPanel);
        mainSplit.setContinuousLayout(true);
        mainSplit.setDividerLocation(260);
        mainSplit.setDividerSize(4);

        content.add(createCardPanel(mainSplit), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ModernColors.getDividerBorderColor()));
        header.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel(I18nUtil.getMessage(MessageKeys.MENU_COLLECTIONS));
        title.setFont(FontsUtil.getDefaultFont(Font.BOLD));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        left.setOpaque(false);
        left.add(title);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel createCardPanel(JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernColors.getDividerBorderColor()),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected void registerListeners() {
        // no op
    }
}