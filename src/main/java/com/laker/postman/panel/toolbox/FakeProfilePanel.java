package com.laker.postman.panel.toolbox;

import com.laker.postman.common.constants.ModernColors;
import com.laker.postman.util.FontsUtil;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FakeProfilePanel extends JPanel {
    private static final DateTimeFormatter BIRTHDAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_BATCH = 1000;
    private static final String HEADER_LINE = "name,phone,idCard,province,city,district,address,email,company,creditCode";
    private static final String[] JSON_KEYS = {
            "name", "phone", "idCard", "province", "city", "district", "address", "email", "company", "creditCode"
    };

    private final JComboBox<String> provinceCombo;
    private final JSpinner countSpinner;
    private final JTable outputTable;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;

    private final Random random = new SecureRandom();

    private static final List<String> SURNAMES = List.of(
            "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨",
            "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜",
            "戚", "谢", "邹", "喻", "柏", "水", "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎",
            "鲁", "韦", "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐",
            "费", "廉", "岑", "薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬", "安", "常"
    );

    private static final List<String> GIVEN_NAMES = List.of(
            "子涵", "雨涵", "梓涵", "诗涵", "梦涵", "宇轩", "浩然", "俊杰", "子轩", "思远", "晨曦", "子墨",
            "欣怡", "佳琪", "可欣", "嘉怡", "若曦", "梓萱", "语嫣", "安琪", "雅琪", "佳怡", "梓彤", "紫涵",
            "明轩", "嘉诚", "子豪", "天佑", "博文", "弘文", "思成", "君浩", "子骞", "浩宇", "鹏涛", "炎彬"
    );

    private static final List<String> PHONE_PREFIX = List.of(
            "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
            "145", "147", "149",
            "150", "151", "152", "153", "155", "156", "157", "158", "159",
            "166", "167",
            "170", "171", "172", "173", "175", "176", "177", "178",
            "180", "181", "182", "183", "184", "185", "186", "187", "188", "189",
            "190", "191", "193", "195", "196", "197", "198", "199"
    );

    private static final List<String> EMAIL_DOMAINS = List.of(
            "example.com", "mail.com", "qq.com", "163.com", "gmail.com"
    );

    private static final List<String> COMPANY_PREFIX = List.of(
            "华", "盛", "天", "金", "银", "星", "云", "创", "瑞", "宏", "鼎", "远"
    );

    private static final List<String> COMPANY_INDUSTRY = List.of(
            "科技", "信息", "软件", "咨询", "贸易", "物流", "教育", "医疗", "金融", "传媒", "网络"
    );

    private static final List<String> COMPANY_SUFFIX = List.of(
            "有限公司", "股份有限公司", "科技有限公司", "发展有限公司"
    );

    private static final Map<String, String> PROVINCE_CODES = Map.ofEntries(
            Map.entry("北京", "110000"),
            Map.entry("天津", "120000"),
            Map.entry("河北", "130000"),
            Map.entry("山西", "140000"),
            Map.entry("内蒙古", "150000"),
            Map.entry("辽宁", "210000"),
            Map.entry("吉林", "220000"),
            Map.entry("黑龙江", "230000"),
            Map.entry("上海", "310000"),
            Map.entry("江苏", "320000"),
            Map.entry("浙江", "330000"),
            Map.entry("安徽", "340000"),
            Map.entry("福建", "350000"),
            Map.entry("江西", "360000"),
            Map.entry("山东", "370000"),
            Map.entry("河南", "410000"),
            Map.entry("湖北", "420000"),
            Map.entry("湖南", "430000"),
            Map.entry("广东", "440000"),
            Map.entry("广西", "450000"),
            Map.entry("海南", "460000"),
            Map.entry("重庆", "500000"),
            Map.entry("四川", "510000"),
            Map.entry("贵州", "520000"),
            Map.entry("云南", "530000"),
            Map.entry("西藏", "540000"),
            Map.entry("陕西", "610000"),
            Map.entry("甘肃", "620000"),
            Map.entry("青海", "630000"),
            Map.entry("宁夏", "640000"),
            Map.entry("新疆", "650000"),
            Map.entry("台湾", "710000"),
            Map.entry("香港", "810000"),
            Map.entry("澳门", "820000")
    );

    private static final Map<String, List<String>> PROVINCE_CITIES = Map.ofEntries(
            Map.entry("北京", List.of("北京市")),
            Map.entry("上海", List.of("上海市")),
            Map.entry("重庆", List.of("重庆市")),
            Map.entry("天津", List.of("天津市")),
            Map.entry("广东", List.of("广州市", "深圳市")),
            Map.entry("浙江", List.of("杭州市", "宁波市")),
            Map.entry("江苏", List.of("南京市", "苏州市")),
            Map.entry("四川", List.of("成都市")),
            Map.entry("湖北", List.of("武汉市")),
            Map.entry("湖南", List.of("长沙市")),
            Map.entry("山东", List.of("济南市", "青岛市")),
            Map.entry("福建", List.of("福州市", "厦门市"))
    );

    private static final Map<String, List<String>> CITY_DISTRICTS = Map.ofEntries(
            Map.entry("北京市", List.of("东城区", "西城区", "朝阳区", "海淀区")),
            Map.entry("上海市", List.of("黄浦区", "浦东新区", "静安区", "闵行区")),
            Map.entry("重庆市", List.of("渝中区", "江北区", "南岸区", "九龙坡区")),
            Map.entry("天津市", List.of("和平区", "河西区", "南开区", "滨海新区")),
            Map.entry("广州市", List.of("天河区", "越秀区", "海珠区")),
            Map.entry("深圳市", List.of("南山区", "福田区", "罗湖区")),
            Map.entry("杭州市", List.of("西湖区", "滨江区", "拱墅区")),
            Map.entry("宁波市", List.of("海曙区", "鄞州区")),
            Map.entry("南京市", List.of("玄武区", "鼓楼区", "雨花台区")),
            Map.entry("苏州市", List.of("姑苏区", "吴中区")),
            Map.entry("成都市", List.of("锦江区", "青羊区", "高新区")),
            Map.entry("武汉市", List.of("江汉区", "武昌区", "汉阳区")),
            Map.entry("长沙市", List.of("岳麓区", "天心区")),
            Map.entry("济南市", List.of("历下区", "槐荫区")),
            Map.entry("青岛市", List.of("市南区", "崂山区")),
            Map.entry("福州市", List.of("鼓楼区", "台江区")),
            Map.entry("厦门市", List.of("思明区", "湖里区"))
    );

    public FakeProfilePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel configPanel = new JPanel(new BorderLayout(10, 10));
        TitledBorder configBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_CONFIG)
        );
        configPanel.setBorder(configBorder);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.add(new JLabel(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_PROVINCE) + ":"));
        provinceCombo = new JComboBox<>(buildProvinceOptions());
        provinceCombo.setPreferredSize(new Dimension(140, 28));
        row.add(provinceCombo);

        row.add(new JLabel(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COUNT) + ":"));
        countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_BATCH, 1));
        countSpinner.setPreferredSize(new Dimension(80, 28));
        row.add(countSpinner);

        JButton generateButton = new JButton(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_GENERATE));
        generateButton.setPreferredSize(new Dimension(120, 30));
        generateButton.setFocusPainted(false);
        row.add(generateButton);

        JButton copyButton = new JButton(I18nUtil.getMessage(MessageKeys.BUTTON_COPY));
        copyButton.setPreferredSize(new Dimension(100, 30));
        copyButton.setFocusPainted(false);
        row.add(copyButton);

        JButton exportExcelButton = new JButton(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_EXPORT_EXCEL));
        exportExcelButton.setPreferredSize(new Dimension(120, 30));
        exportExcelButton.setFocusPainted(false);
        row.add(exportExcelButton);

        JButton exportJsonButton = new JButton(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_EXPORT_JSON));
        exportJsonButton.setPreferredSize(new Dimension(120, 30));
        exportJsonButton.setFocusPainted(false);
        row.add(exportJsonButton);

        JButton clearButton = new JButton(I18nUtil.getMessage(MessageKeys.BUTTON_CLEAR));
        clearButton.setPreferredSize(new Dimension(100, 30));
        clearButton.setFocusPainted(false);
        row.add(clearButton);

        configPanel.add(row, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(FontsUtil.getDefaultFont(Font.PLAIN));
        statusLabel.setForeground(ModernColors.getTextSecondary());
        configPanel.add(statusLabel, BorderLayout.SOUTH);

        add(configPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_NAME),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_PHONE),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_IDCARD),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_PROVINCE),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_CITY),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_DISTRICT),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_ADDRESS),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_EMAIL),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_COMPANY),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_COLUMN_CREDIT_CODE)
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        outputTable = new JTable(tableModel);
        outputTable.setFont(FontsUtil.getDefaultFont(Font.PLAIN));
        outputTable.setRowHeight(24);
        outputTable.getTableHeader().setReorderingAllowed(false);
        outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel columnModel = outputTable.getColumnModel();
        int[] widths = {80, 120, 200, 80, 100, 100, 160, 180, 200, 160};
        for (int i = 0; i < widths.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) outputTable.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setFont(FontsUtil.getDefaultFont(Font.BOLD));
        outputTable.getTableHeader().setDefaultRenderer(headerRenderer);
        outputTable.getTableHeader().setOpaque(true);
        outputTable.getTableHeader().setBackground(new Color(246, 248, 250));
        outputTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ModernColors.getDividerBorderColor()));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(cellRenderer);
        }
        outputTable.setShowGrid(true);
        outputTable.setGridColor(ModernColors.getDividerBorderColor());
        outputTable.setIntercellSpacing(new Dimension(0, 0));
        outputTable.setSelectionBackground(new Color(225, 238, 255));
        outputTable.setSelectionForeground(ModernColors.getTextPrimary());
        outputTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(outputTable);
        TitledBorder outputBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_OUTPUT)
        );
        scrollPane.setBorder(outputBorder);
        add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateProfiles());
        copyButton.addActionListener(e -> copyOutput());
        exportExcelButton.addActionListener(e -> exportAsCsv());
        exportJsonButton.addActionListener(e -> exportAsJson());
        clearButton.addActionListener(e -> clearOutput());
    }

    private String[] buildProvinceOptions() {
        List<String> options = new ArrayList<>();
        options.add(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_PROVINCE_RANDOM));
        options.addAll(PROVINCE_CODES.keySet());
        return options.toArray(new String[0]);
    }

    private void generateProfiles() {
        int count = (int) countSpinner.getValue();
        String selected = (String) provinceCombo.getSelectedItem();
        boolean randomProvince = selected == null || selected.equals(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_PROVINCE_RANDOM));

        tableModel.setRowCount(0);
        for (int i = 0; i < count; i++) {
            String province = randomProvince ? pickRandomProvinceName() : selected;
            String provinceCode = PROVINCE_CODES.get(province);
            String name = randomName();
            String phone = randomPhone();
            String idCard = randomIdCard(provinceCode);
            String city = pickRandomCity(province);
            String district = pickRandomDistrict(city);
            String address = province + city + district + randomStreetDetail();
            String email = randomEmail(name);
            String company = randomCompanyName(province, city);
            String creditCode = randomUnifiedCreditCode(province);
            tableModel.addRow(new Object[]{name, phone, idCard, province, city, district, address, email, company, creditCode});
        }
        statusLabel.setText(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_STATUS, String.valueOf(count)));
    }

    private void copyOutput() {
        if (tableModel.getRowCount() == 0) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(buildCsvContent().trim()), null);
    }

    private void clearOutput() {
        tableModel.setRowCount(0);
        statusLabel.setText(" ");
    }

    private void exportAsCsv() {
        if (tableModel.getRowCount() == 0) {
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_EXPORT_EXCEL));
        fileChooser.setSelectedFile(new File("profiles_" + System.currentTimeMillis() + ".csv"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(buildCsvContent());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        I18nUtil.getMessage(MessageKeys.GENERAL_ERROR),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportAsJson() {
        if (tableModel.getRowCount() == 0) {
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(I18nUtil.getMessage(MessageKeys.TOOLBOX_PROFILE_EXPORT_JSON));
        fileChooser.setSelectedFile(new File("profiles_" + System.currentTimeMillis() + ".json"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(buildJsonContent());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        I18nUtil.getMessage(MessageKeys.GENERAL_ERROR),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String buildCsvContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER_LINE).append("\n");
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                sb.append(tableModel.getValueAt(row, col));
                if (col < tableModel.getColumnCount() - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildJsonContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            sb.append("  {");
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                String key = JSON_KEYS[col];
                String value = String.valueOf(tableModel.getValueAt(row, col));
                sb.append("\"").append(escapeJson(key)).append("\": \"")
                        .append(escapeJson(value)).append("\"");
                if (col < tableModel.getColumnCount() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            if (row < tableModel.getRowCount() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String randomName() {
        String surname = SURNAMES.get(random.nextInt(SURNAMES.size()));
        String given = GIVEN_NAMES.get(random.nextInt(GIVEN_NAMES.size()));
        if (random.nextBoolean()) {
            String extra = GIVEN_NAMES.get(random.nextInt(GIVEN_NAMES.size()));
            given = given.substring(0, 1) + extra.substring(0, 1);
        }
        return surname + given;
    }

    private String pickRandomProvinceName() {
        List<String> provinces = new ArrayList<>(PROVINCE_CODES.keySet());
        return provinces.get(random.nextInt(provinces.size()));
    }

    private String pickRandomCity(String province) {
        List<String> cities = PROVINCE_CITIES.get(province);
        if (cities == null || cities.isEmpty()) {
            return province + "市";
        }
        return cities.get(random.nextInt(cities.size()));
    }

    private String pickRandomDistrict(String city) {
        List<String> districts = CITY_DISTRICTS.get(city);
        if (districts == null || districts.isEmpty()) {
            return "城区";
        }
        return districts.get(random.nextInt(districts.size()));
    }

    private String randomStreetDetail() {
        return (10 + random.nextInt(90)) + "号" + (1 + random.nextInt(30)) + "栋" + (1 + random.nextInt(30)) + "室";
    }

    private String randomEmail(String name) {
        String base = name.length() > 2 ? name.substring(0, 2) : name;
        String domain = EMAIL_DOMAINS.get(random.nextInt(EMAIL_DOMAINS.size()));
        return base + (100 + random.nextInt(900)) + "@" + domain;
    }

    private String randomCompanyName(String province, String city) {
        String prefix = COMPANY_PREFIX.get(random.nextInt(COMPANY_PREFIX.size()));
        String industry = COMPANY_INDUSTRY.get(random.nextInt(COMPANY_INDUSTRY.size()));
        String suffix = COMPANY_SUFFIX.get(random.nextInt(COMPANY_SUFFIX.size()));
        return province + city + prefix + industry + suffix;
    }

    private String randomUnifiedCreditCode(String province) {
        String region = PROVINCE_CODES.getOrDefault(province, pickRandomProvinceCode());
        StringBuilder base = new StringBuilder();
        base.append("9"); // 登记管理部门
        base.append("1"); // 机构类别
        base.append(region.substring(0, 6));
        base.append(randomBase31(9));
        char check = calculateUnifiedCreditCheckCode(base.toString());
        return base.append(check).toString();
    }

    private String randomBase31(int length) {
        String chars = "0123456789ABCDEFGHJKLMNPQRTUWXY";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private char calculateUnifiedCreditCheckCode(String base) {
        String chars = "0123456789ABCDEFGHJKLMNPQRTUWXY";
        int[] weights = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            int code = chars.indexOf(base.charAt(i));
            sum += code * weights[i];
        }
        int logic = 31 - (sum % 31);
        if (logic == 31) {
            logic = 0;
        }
        return chars.charAt(logic);
    }

    private String randomPhone() {
        String prefix = PHONE_PREFIX.get(random.nextInt(PHONE_PREFIX.size()));
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String randomIdCard(String provinceCode) {
        String area = provinceCode != null ? provinceCode : pickRandomProvinceCode();
        LocalDate birth = randomBirthDate();
        String birthStr = birth.format(BIRTHDAY_FORMAT);
        int seq = 100 + random.nextInt(900);
        String base = area + birthStr + seq;
        char check = calculateIdCheckCode(base);
        return base + check;
    }

    private String pickRandomProvinceCode() {
        List<String> values = new ArrayList<>(PROVINCE_CODES.values());
        return values.get(random.nextInt(values.size()));
    }

    private LocalDate randomBirthDate() {
        int year = 1970 + random.nextInt(35); // 1970-2004
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return LocalDate.of(year, month, day);
    }

    private char calculateIdCheckCode(String base17) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] codes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < base17.length(); i++) {
            sum += (base17.charAt(i) - '0') * weights[i];
        }
        return codes[sum % 11];
    }
}
