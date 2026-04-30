package com.example.debtmanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JTextField searchField;
    private List<Debt> debtList;
    private List<Debt> filteredList; // 用于搜索过滤

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainApp().setVisible(true);
        });
    }

    public MainApp() {
        setTitle("欠账管理软件 v1.1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 500);
        setLocationRelativeTo(null);

        // 加载数据
        debtList = DataManager.loadData();
        filteredList = new ArrayList<>(debtList);

        // 创建界面
        initUI();
        refreshTable();
        updateTotal();
    }

    private void initUI() {
        // 顶部：标题 + 输入区域
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("欠账管理系统");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        // 输入行
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        inputPanel.add(new JLabel("欠账人："));
        JTextField nameField = new JTextField(12);
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("欠款金额："));
        JTextField amountField = new JTextField(8);
        inputPanel.add(amountField);

        JButton addButton = new JButton("添加记录");
        addButton.setBackground(new Color(33, 150, 243));
        addButton.setForeground(Color.BLACK); // 字体颜色改为黑色
        inputPanel.add(addButton);

        topPanel.add(inputPanel, BorderLayout.CENTER);

        // 搜索行
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("搜索欠账人："));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton clearSearchButton = new JButton("清除搜索");
        searchPanel.add(clearSearchButton);

        topPanel.add(searchPanel, BorderLayout.SOUTH);

        // 中间：表格
        String[] columnNames = {"欠账人", "欠款金额(元)", "状态", "操作"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // 只有操作列可点击
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        // 按钮列（修复版）
        table.getColumn("操作").setCellRenderer(new ButtonRenderer());
        table.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        // 双击修改
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        // 获取真实的Debt对象（考虑搜索过滤）
                        String name = (String) tableModel.getValueAt(row, 0);
                        Debt debt = findDebtByName(name);
                        if (debt != null) {
                            showEditDialog(debt);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // 底部：总金额
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        totalLabel = new JLabel("总欠款：0.00 元");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(totalLabel);

        // --- 事件监听 ---

        // 添加按钮
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String amountText = amountField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入欠账人姓名", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "金额必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字金额", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            debtList.add(new Debt(name, amount));
            DataManager.saveData(debtList);
            applySearchFilter(); // 重新应用搜索
            refreshTable();
            updateTotal();

            nameField.setText("");
            amountField.setText("");
            nameField.requestFocus();
        });

        // 搜索框实时监听
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applySearchFilter(); refreshTable(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applySearchFilter(); refreshTable(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearchFilter(); refreshTable(); }
        });

        // 清除搜索按钮
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            applySearchFilter();
            refreshTable();
        });

        // 布局
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 应用搜索过滤
    private void applySearchFilter() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            filteredList = new ArrayList<>(debtList);
        } else {
            filteredList = debtList.stream()
                    .filter(d -> d.getName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }
    }

    // 根据名字找Debt对象
    private Debt findDebtByName(String name) {
        return debtList.stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        // 按姓名排序
        filteredList.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (Debt debt : filteredList) {
            Object[] row = {
                    debt.getName(),
                    String.format("%.2f", debt.getAmount()),
                    debt.isPaid() ? "已结清" : "未结清",
                    debt.isPaid() ? "取消结清" : "结清"
            };
            tableModel.addRow(row);
        }
    }

    private void updateTotal() {
        double total = debtList.stream()
                .filter(debt -> !debt.isPaid())
                .mapToDouble(Debt::getAmount)
                .sum();
        totalLabel.setText(String.format("总欠款：%.2f 元", total));
    }

    private void showEditDialog(Debt debt) {
        String newName = JOptionPane.showInputDialog(this, "修改欠账人姓名：", debt.getName());
        if (newName == null || newName.trim().isEmpty()) return;

        String newAmountStr = JOptionPane.showInputDialog(this, "修改欠款金额：", String.valueOf(debt.getAmount()));
        if (newAmountStr == null) return;

        try {
            double newAmount = Double.parseDouble(newAmountStr.trim());
            if (newAmount <= 0) {
                JOptionPane.showMessageDialog(this, "金额必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            debt.setName(newName.trim());
            debt.setAmount(newAmount);
            DataManager.saveData(debtList);
            applySearchFilter();
            refreshTable();
            updateTotal();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字金额", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 表格按钮渲染器
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // 表格按钮编辑器（修复版）
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                // 按钮点击逻辑
                if (currentRow >= 0 && currentRow < filteredList.size()) {
                    String name = (String) tableModel.getValueAt(currentRow, 0);
                    Debt debt = findDebtByName(name);
                    if (debt != null) {
                        debt.setPaid(!debt.isPaid());
                        DataManager.saveData(debtList);
                        refreshTable();
                        updateTotal();
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }
    }
}