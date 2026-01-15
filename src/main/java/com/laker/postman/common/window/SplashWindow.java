package com.laker.postman.common.window;

import com.formdev.flatlaf.FlatLaf;
import com.laker.postman.common.SingletonFactory;
import com.laker.postman.common.constants.Icons;
import com.laker.postman.common.constants.ModernColors;
import com.laker.postman.frame.MainFrame;
import com.laker.postman.ioc.Component;
import com.laker.postman.ioc.PostConstruct;
import com.laker.postman.util.FontsUtil;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;
import com.laker.postman.util.SystemUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * 启动欢迎窗口（Splash Window），用于主程序加载时的过渡。
 */
@Slf4j
@Component
public class SplashWindow extends JWindow {
    @Serial
    private static final long serialVersionUID = 1L; // 添加序列化ID
    public static final int MIN_TIME = 1000; // 最小显示时间，避免闪屏

    private JLabel statusLabel; // 状态标签，用于显示加载状态
    private JProgressBar progressBar;
    private volatile boolean isDisposed = false; // 标记窗口是否已释放



    /**
     * Bean 初始化方法 - 在依赖注入完成后自动调用
     * 在 EDT 线程中初始化 UI 组件
     */
    @PostConstruct
    public void init() {
        try {
            // 确保在 EDT 线程中初始化 UI
            if (SwingUtilities.isEventDispatchThread()) {
                initUI();
            } else {
                SwingUtilities.invokeAndWait(this::initUI);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            log.error("初始化 SplashWindow 被中断", e);
            dispose();
            throw new SplashWindowInitializationException("Failed to initialize splash window", e);
        } catch (Exception e) {
            log.error("初始化 SplashWindow 失败", e);
            // 如果初始化失败，确保窗口被正确释放
            dispose();
            throw new SplashWindowInitializationException("Failed to initialize splash window", e);
        }
    }

    /**
     * 初始化 UI 组件
     */
    private void initUI() {
        JPanel content = createContentPanel();
        initializeWindow(content);
    }

    /**
     * 创建主要内容面板
     */
    private JPanel createContentPanel() {
        JPanel content = getJPanel();

        // Logo
        content.add(createLogoPanel(), BorderLayout.CENTER);

        // 应用信息
        content.add(createInfoPanel(), BorderLayout.NORTH);

        // 状态面板
        content.add(createStatusPanel(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * 创建Logo面板
     */
    private JPanel createLogoPanel() {
        // 创建容器面板，用于绘制圆形背景
        JPanel logoContainer = new JPanel() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 100; // 缩小圆形尺寸
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // 绘制外层光晕
                g2.setColor(getDecorativeDotColor(25));
                g2.fillOval(x - 6, y - 6, size + 12, size + 12);

                // 绘制中层光晕
                g2.setColor(getDecorativeDotColor(40));
                g2.fillOval(x - 4, y - 4, size + 8, size + 8);

                // 绘制圆形背景
                g2.setColor(getDecorativeDotColor(95));
                g2.fillOval(x, y, size, size);

                // 绘制内部微妙阴影，增加立体感
                g2.setColor(getDecorativeDotColor(15));
                g2.fillOval(x + 2, y + 2, size - 4, size - 4);

                // 绘制边框高光
                g2.setColor(isDarkTheme() ? new Color(255, 255, 255, 120) : Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x + 1, y + 1, size - 2, size - 2);

                g2.dispose();
            }
        };
        logoContainer.setOpaque(false);
        logoContainer.setLayout(new BorderLayout());
        // 调整容器尺寸（100 + 12 = 112，设为 120 留边距）
        logoContainer.setPreferredSize(new Dimension(120, 120));

        Image scaledImage = Icons.LOGO.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoContainer.add(logoLabel, BorderLayout.CENTER);

        return logoContainer;
    }

    /**
     * 创建应用信息面板
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);

        // 应用名称
        JLabel appNameLabel = new JLabel(I18nUtil.getMessage(MessageKeys.APP_NAME), SwingConstants.CENTER);
        appNameLabel.setFont(FontsUtil.getDefaultFontWithOffset(Font.BOLD, +8));
        appNameLabel.setForeground(getTextColor());
        infoPanel.add(appNameLabel);

        // 版本号
        JLabel versionLabel = new JLabel(SystemUtil.getCurrentVersion(), SwingConstants.CENTER);
        versionLabel.setFont(FontsUtil.getDefaultFontWithOffset(Font.BOLD, +3));
        versionLabel.setForeground(getTextColor());
        infoPanel.add(versionLabel);

        return infoPanel;
    }

    /**
     * 创建状态面板
     */
    private JPanel createStatusPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel(I18nUtil.getMessage(MessageKeys.SPLASH_STATUS_STARTING), SwingConstants.CENTER);
        statusLabel.setFont(FontsUtil.getDefaultFontWithOffset(Font.PLAIN, +3)); // 比标准字体大3号
        statusLabel.setForeground(getTextColor());
        statusLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        progressBar = createProgressBar();
        progressBar.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        bottomPanel.add(statusLabel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(progressBar);
        return bottomPanel;

    }

    /**
     * 初始化窗口
     */
    private void initializeWindow(JPanel content) {
        setContentPane(content);
        setSize(380, 260); // 设置窗口大小
        setLocationRelativeTo(null);  // 居中显示


        // 安全设置透明度和置顶
        setupWindowProperties();

        setVisible(true); // 显示窗口
    }

    /**
     * 安全设置窗口属性
     */
    private void setupWindowProperties() {
        try {
            setBackground(new Color(0, 0, 0, 0)); // 透明背景
        } catch (Exception e) {
            log.warn("设置透明背景失败，使用默认背景", e);
        }

        try {
            setAlwaysOnTop(true); // 窗口总在最上层
        } catch (Exception e) {
            log.warn("设置窗口置顶失败", e);
        }
    }

    private static JPanel getJPanel() {
        JPanel content = new JPanel() { // 自定义面板，绘制渐变背景和圆角
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                boolean isDark = FlatLaf.isLafDark();

                GradientPaint gp = isDark
                        ? ModernColors.createDarkBackgroundGradient(getWidth(), getHeight())
                        : ModernColors.createPrimaryToSecondaryGradient(getWidth(), getHeight());

                g2d.setPaint(gp);
                // 圆角背景
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);

                // 添加微妙的光泽效果
                Color highlightStart = isDark ? new Color(255, 255, 255, 8) : ModernColors.whiteWithAlpha(40);
                Color highlightEnd = isDark ? new Color(255, 255, 255, 0) : ModernColors.whiteWithAlpha(0);
                GradientPaint glossPaint = new GradientPaint(
                        0, 0, highlightStart,
                        0, getHeight() / 2.0f, highlightEnd
                );
                g2d.setPaint(glossPaint);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 32, 32);

                // 添加边框高光，与主题背景 (60, 63, 65) 协调
                Color borderColor = isDark ? new Color(75, 77, 80, 100) : ModernColors.whiteWithAlpha(80);
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 32, 32);

                g2d.dispose();
            }
        };
        content.setLayout(new BorderLayout(0, 10)); // 使用 BorderLayout 布局
        content.setOpaque(false); // 设置透明背景
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24)); // 设置内边距
        return content;
    }

    private JProgressBar createProgressBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setBorderPainted(false);
        bar.setStringPainted(false);
        bar.setOpaque(true);
        bar.setBackground(getProgressTrackColor());
        bar.setForeground(getProgressFillColor());
        bar.setPreferredSize(new Dimension(220, 6));
        bar.setMaximumSize(new Dimension(220, 6));
        return bar;
    }

    private void updateProgressBar(int value) {
        if (progressBar == null) {
            return;
        }
        Runnable update = () -> progressBar.setValue(Math.min(100, Math.max(0, value)));
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    public void setStatus(String statusKey) {
        if (!isDisposed) {
            SwingUtilities.invokeLater(() -> updateStatusLabel(statusKey));
        }
    }


    /**
     * 更新状态标签
     */
    private void updateStatusLabel(String statusKey) {
        if (!isDisposed && statusLabel != null) {
            statusLabel.setText(I18nUtil.getMessage(statusKey));
        }
    }

    public void initMainFrame() {
        SwingWorker<MainFrame, String> worker = new SwingWorker<>() {
            @Override
            protected MainFrame doInBackground() {
                long start = System.currentTimeMillis();

                publish(MessageKeys.SPLASH_STATUS_LOADING_MAIN);
                setProgress(30);
                MainFrame mainFrame = SingletonFactory.getInstance(MainFrame.class);

                publish(MessageKeys.SPLASH_STATUS_INITIALIZING);
                setProgress(60);
                mainFrame.initComponents();

                publish(MessageKeys.SPLASH_STATUS_READY);
                setProgress(100);

                long cost = System.currentTimeMillis() - start;
                log.info("main frame initComponents cost: {} ms", cost);

                ensureMinimumDisplayTime(cost);
                return mainFrame;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                // 在EDT中更新状态
                if (!chunks.isEmpty() && !isDisposed) {
                    setStatus(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (isDisposed) return;

                        setStatus(MessageKeys.SPLASH_STATUS_DONE);
                        MainFrame mainFrame = get();

                        // 启动渐隐动画关闭 SplashWindow
                        startFadeOutAnimation(mainFrame);

                    } catch (Exception e) {
                        handleMainFrameLoadError(e);
                    }
                });
            }
        };
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                updateProgressBar((Integer) evt.getNewValue());
            }
        });
        worker.execute();
    }


    /**
     * 确保最小显示时间
     */
    private void ensureMinimumDisplayTime(long cost) {
        if (cost < MIN_TIME) {
            try {
                Thread.sleep(MIN_TIME - cost);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                log.warn("Thread interrupted while sleeping", interruptedException);
            }
        }
    }

    /**
     * 处理主窗口加载错误
     */
    private void handleMainFrameLoadError(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        log.error("加载主窗口失败", e);

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null,
                I18nUtil.getMessage(MessageKeys.SPLASH_ERROR_LOAD_MAIN),
                I18nUtil.getMessage(MessageKeys.GENERAL_ERROR),
                JOptionPane.ERROR_MESSAGE
        ));
        System.exit(1);
    }

    /**
     * 启动渐隐动画
     */
    private void startFadeOutAnimation(MainFrame mainFrame) {
        if (isDisposed) return;

        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null) {
                mainFrame.setVisible(true);
                mainFrame.toFront();
                mainFrame.requestFocus();
            }
            disposeSafely();
        });
    }

    /**
     * 安全释放资源
     */
    private void disposeSafely() {
        if (isDisposed) return;

        isDisposed = true;

        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }

    /**
     * 重写dispose方法，确保资源完全释放
     */
    @Override
    public void dispose() {
        if (!isDisposed) {
            disposeSafely();
        } else {
            super.dispose();
        }
    }

    /**
     * 检查当前是否为暗色主题
     */
    private boolean isDarkTheme() {
        return FlatLaf.isLafDark();
    }

    private Color getProgressFillColor() {
        return isDarkTheme() ? ModernColors.PRIMARY_LIGHT : ModernColors.whiteWithAlpha(235);
    }

    private Color getProgressTrackColor() {
        return isDarkTheme() ? ModernColors.whiteWithAlpha(35) : ModernColors.whiteWithAlpha(90);
    }

    /**
     * 获取主题适配的文字颜色
     */
    private Color getTextColor() {
        // 暗色主题使用更亮的文字颜色，提升可读性
        return isDarkTheme() ? new Color(230, 230, 230) : ModernColors.whiteWithAlpha(220);
    }


    /**
     * 获取主题适配的装饰点颜色
     */
    private Color getDecorativeDotColor(int alpha) {
        // 暗色主题使用更柔和的透明度，并稍微提亮
        if (isDarkTheme()) {
            // 使用更高的基础亮度，但降低透明度，使其更柔和
            int adjustedAlpha = (int) (alpha * 0.6); // 降低到 60% 透明度
            return new Color(255, 255, 255, adjustedAlpha);
        }
        return ModernColors.whiteWithAlpha(alpha);
    }
}
