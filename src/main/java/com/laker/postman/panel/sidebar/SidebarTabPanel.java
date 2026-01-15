package com.laker.postman.panel.sidebar;

import com.laker.postman.common.SingletonBasePanel;
import com.laker.postman.common.constants.ModernColors;
import com.laker.postman.model.SidebarTab;
import com.laker.postman.model.TabInfo;
import com.laker.postman.util.FontsUtil;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 顶部 Tab 导航 + 内容区的 AHAS 风格布局
 */
@Slf4j
public class SidebarTabPanel extends SingletonBasePanel {
    private static final String LOADING_PANEL_NAME = "loadingPanel";

    @Getter
    private JTabbedPane tabbedPane;
    @Getter
    private transient List<TabInfo> tabInfos;

    private JPanel contentPanel;

    private final Set<Integer> loadedTabIndexes = new HashSet<>();
    private final Set<Integer> loadingTabIndexes = new HashSet<>();

    @Override
    protected void initUI() {
        setLayout(new BorderLayout());

        tabbedPane = createContentTabbedPane();
        tabInfos = new ArrayList<>();
        for (SidebarTab tab : SidebarTab.values()) {
            tabInfos.add(tab.toTabInfo());
        }
        for (int i = 0; i < tabInfos.size(); i++) {
            TabInfo info = tabInfos.get(i);
            tabbedPane.addTab(info.title, info.icon, createLoadingPanel());
        }

        tabbedPane.addChangeListener(e -> ensureTabComponentLoaded(tabbedPane.getSelectedIndex()));

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        tabbedPane.setSelectedIndex(0);
        ensureTabComponentLoaded(0);
    }



    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setName(LOADING_PANEL_NAME);
        panel.setOpaque(false);
        JLabel label = new JLabel(I18nUtil.getMessage(MessageKeys.GENERAL_LOADING));
        label.setForeground(ModernColors.getTextSecondary());
        panel.add(label);
        return panel;
    }

    private JTabbedPane createContentTabbedPane() {
        JTabbedPane pane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        pane.setFont(FontsUtil.getDefaultFont(Font.PLAIN));
        pane.setOpaque(true);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabAreaInsets = new Insets(8, 12, 6, 12);
                tabInsets = new Insets(8, 16, 8, 16);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                contentBorderInsets = new Insets(0, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 8;
                int inset = 2;
                int bgX = x + inset;
                int bgY = y + 4;
                int bgW = w - inset * 2;
                int bgH = h - 8;

                if (isSelected) {
                    g2.setColor(new Color(22, 119, 255, 36));
                    g2.fillRoundRect(bgX, bgY, bgW, bgH, arc, arc);
                    g2.setColor(ModernColors.PRIMARY);
                    g2.drawRoundRect(bgX, bgY, bgW, bgH, arc, arc);
                }
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                if (!isSelected) {
                    g.setColor(ModernColors.getDividerBorderColor());
                    g.drawLine(x + w - 1, y + 8, x + w - 1, y + h - 10);
                }
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // no border
            }
        });
        return pane;
    }

    private void ensureTabComponentLoaded(int index) {
        if (index < 0 || index >= tabInfos.size()) {
            return;
        }
        if (loadedTabIndexes.contains(index) || loadingTabIndexes.contains(index)) {
            return;
        }
        TabInfo info = tabInfos.get(index);
        tabbedPane.setComponentAt(index, createLoadingPanel());
        loadingTabIndexes.add(index);
        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                final JPanel[] panelRef = new JPanel[1];
                SwingUtilities.invokeAndWait(() -> panelRef[0] = info.getPanel());
                return panelRef[0];
            }

            @Override
            protected void done() {
                try {
                    JPanel realPanel = get();
                    tabbedPane.setComponentAt(index, realPanel);
                    loadedTabIndexes.add(index);
                } catch (Exception ex) {
                    log.warn("Load tab failed: {}", ex.getMessage());
                } finally {
                    loadingTabIndexes.remove(index);
                }
            }
        };
        worker.execute();
    }

    public void updateSidebarExpansion() {
        // no-op (sidebar removed for top tab layout)
    }

    @Override
    protected void registerListeners() {
        // no-op
    }
}
