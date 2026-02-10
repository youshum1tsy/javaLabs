package com.example.library.forms;

import com.example.library.Calculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class IntegralForm {
    private JPanel rootPanel;
    private JTextField fieldLowerBound;
    private JTextField fieldUpperBound;
    private JTextField fieldStep;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnCalculate;
    private JTable table1;

    private DefaultTableModel model;

    public IntegralForm() {
        String[] columns = {"Нижняя", "Верхняя", "Шаг", "Результат"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                if (column == 3) {
                    return false;
                }
                return true;
            };
        };
        table1.setModel(model);


        btnAdd.addActionListener(e -> {
            double low = Double.parseDouble(fieldLowerBound.getText());
            double high = Double.parseDouble(fieldUpperBound.getText());
            double step = Double.parseDouble(fieldStep.getText());

            model.addRow(new Object[]{low, high, step, ""});
        });

        btnCalculate.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                double left = Double.parseDouble(model.getValueAt(row, 0).toString());
                double right = Double.parseDouble(model.getValueAt(row, 1).toString());
                double step = Double.parseDouble(model.getValueAt(row, 2).toString());
                double res = Calculator.calculate(left, right, step);

                model.setValueAt(String.format("%.4f", res), row, 3);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) model.removeRow(row);
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}