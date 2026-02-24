package com.example.library.forms;

import com.example.library.RecIntegral;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class IntegralForm {
    private JPanel rootPanel;
    private JTextField fieldLowerBound;
    private JTextField fieldUpperBound;
    private JTextField fieldStep;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnCalculate;
    private JTable table1;
    private boolean clrClicked;
    private JButton btnClear;
    private JButton btnFill;

    private DefaultTableModel model;

    private List<RecIntegral> listRecords = new ArrayList<>();

    public IntegralForm() {
        String[] columns = {"Нижняя", "Верхняя", "Шаг", "Результат"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 3;
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);

                if (row >= 0 && row < listRecords.size() && column < 3) {

                    double val = Double.parseDouble(aValue.toString());
                    RecIntegral rec = listRecords.get(row);

                    if (column == 0) rec.setLowerBound(val);
                    else if (column == 1) rec.setUpperBound(val);
                    else if (column == 2) rec.setStep(val);

                    rec.setResult(0);
                    super.setValueAt("", row, 3);

                }
            }
        };
        table1.setModel(model);

        btnAdd.addActionListener(e -> {
            if (clrClicked == false) {
                double low = Double.parseDouble(fieldLowerBound.getText());
                double high = Double.parseDouble(fieldUpperBound.getText());
                double step = Double.parseDouble(fieldStep.getText());

                RecIntegral record = new RecIntegral(low, high, step, 0);
                listRecords.add(record);
                model.addRow(new Object[]{low, high, step, ""});
            }
        });

        btnCalculate.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                double left = Double.parseDouble(model.getValueAt(row, 0).toString());
                double right = Double.parseDouble(model.getValueAt(row, 1).toString());
                double step = Double.parseDouble(model.getValueAt(row, 2).toString());

                double res = RecIntegral.calculate(left, right, step);

                RecIntegral rec = listRecords.get(row);
                rec.setResult(res);
                rec.setLowerBound(left);
                rec.setUpperBound(right);
                rec.setStep(step);

                model.setValueAt(String.format("%.4f", res), row, 3);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                listRecords.remove(row);
                model.removeRow(row);
            }
        });

        btnClear.addActionListener(e -> {
            clrClicked = true;
            model.setRowCount(0);
            //listRecords.clear();
        });

        btnFill.addActionListener(e -> {
            clrClicked = false;
            model.setRowCount(0);
            for (RecIntegral rec : listRecords) {
                String resStr = rec.getResult() == 0 ? "" : String.format("%.4f", rec.getResult());
                model.addRow(new Object[]{
                        rec.getLowerBound(),
                        rec.getUpperBound(),
                        rec.getStep(),
                        resStr
                });
            }
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}