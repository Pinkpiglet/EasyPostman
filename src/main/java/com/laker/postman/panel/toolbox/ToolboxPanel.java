package com.laker.postman.panel.toolbox;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.laker.postman.common.SingletonBasePanel;
import com.laker.postman.common.constants.ModernColors;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;


/**
 * 工具箱面板 - 包含开发常用工具
 * Toolbox Panel - Contains common development tools
 */
@Slf4j
public class ToolboxPanel extends SingletonBasePanel {

    private JTabbedPane toolTabs;
    private final List<Supplier<JPanel>> tabFactories = new ArrayList<>();
    private final Set<Integer> loadedTabs = new HashSet<>();


    @Override
    protected void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ModernColors.getDividerBorderColor()));

        // 创建标签页容器
        toolTabs = new JTabbedPane(SwingConstants.TOP);

        // 添加各种工具标签页 - 按功能分类和使用频率排序
        initToolTabs();
        toolTabs.addChangeListener(e -> ensureTabLoaded(toolTabs.getSelectedIndex()));
        ensureTabLoaded(0);

        add(toolTabs, BorderLayout.CENTER);

    }

    /**
     * 初始化所有工具标签页
     */
    private void initToolTabs() {
        // 1. 数据格式化工具
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_JSON),
                createThemedIcon("icons/format.svg"),
                JsonToolPanel::new
        );


        // 2. 编码解码工具
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_ENCODER),
                createThemedIcon("icons/code.svg"),
                EncoderPanel::new
        );


        // 3. 哈希计算工具（单向加密）
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_HASH),
                createThemedIcon("icons/hash.svg"),
                HashPanel::new
        );


        // 4. 加密解密工具（双向加密）
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_CRYPTO),
                createThemedIcon("icons/security.svg"),
                CryptoPanel::new
        );


        // 5. 时间戳转换工具
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_TIMESTAMP),
                createThemedIcon("icons/time.svg"),
                TimestampPanel::new
        );


        // 6. UUID生成器
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_UUID),
                createThemedIcon("icons/plus.svg"),
                UuidPanel::new
        );


        // 7. 文本对比工具
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_DIFF),
                createThemedIcon("icons/file.svg"),
                DiffPanel::new
        );


        // 8. Cron表达式工具
        addLazyToolTab(
                I18nUtil.getMessage(MessageKeys.TOOLBOX_CRON),
                createThemedIcon("icons/time.svg"),
                CronPanel::new
        );

    }

    /**
     * 创建主题适配的 SVG 图标
     * 使用 ColorFilter 使图标颜色自动跟随主题的按钮前景色
     *
     * @param iconPath SVG 图标路径
     * @return 主题适配的图标
     */
    private Icon createThemedIcon(String iconPath) {
        FlatSVGIcon icon = new FlatSVGIcon(iconPath, 16, 16);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UIManager.getColor("Button.foreground")));
        return icon;
    }

    /**
     * 添加工具标签页
     *
     * @param title   标签页标题
     * @param icon    标签页图标
     * @param factory 标签页面板工厂
     */
    private void addLazyToolTab(String title, Icon icon, Supplier<JPanel> factory) {
        tabFactories.add(factory);
        toolTabs.addTab(title, icon, createLoadingPanel());
    }

    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(I18nUtil.getMessage(MessageKeys.GENERAL_LOADING));
        label.setForeground(ModernColors.getTextSecondary());
        panel.add(label);
        return panel;
    }

    private void ensureTabLoaded(int index) {
        if (index < 0 || index >= tabFactories.size() || loadedTabs.contains(index)) {
            return;
        }
        toolTabs.setComponentAt(index, createLoadingPanel());
        SwingUtilities.invokeLater(() -> {
            if (loadedTabs.contains(index)) {
                return;
            }
            toolTabs.setComponentAt(index, tabFactories.get(index).get());
            loadedTabs.add(index);
            toolTabs.revalidate();
            toolTabs.repaint();
        });
    }


    @Override
    protected void registerListeners() {
        // 可以在这里注册监听器，如标签页切换事件等
    }
}
