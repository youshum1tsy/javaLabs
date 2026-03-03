package com.example.library.forms;

import com.example.library.InvalidDataException;
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
    private boolean clrClicked = false;
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
                if (column == 3) {
                    super.setValueAt(aValue, row, column);
                    return;
                }
                try {
                    double val = Double.parseDouble(aValue.toString().replace(',', '.'));
                    RecIntegral rec = listRecords.get(row);

                    double high = rec.getUpperBound();
                    double low = rec.getLowerBound();
                    double step = rec.getStep();
                    if (val < 0.000001 || val > 1000000) {
                        throw new InvalidDataException("Число " + val + " вне диапазона");
                    }
                    if (column == 0) {
                        if (high - val < 0) {
                            throw new InvalidDataException("Нижняя граница не может превышать верхнюю " + val + " > " + high);
                        }
                        if( high - val < step)
                        {
                            throw new InvalidDataException("Шаг не может быть больше интервала " + (high - val) + " < " + step);
                        }
                    }
                    if (column == 1) {
                        if (val - low < 0) {
                            throw new InvalidDataException("Нижняя граница не может превышать верхнюю " + low + " > " + val);
                        }
                        if( val - low < step)
                        {
                            throw new InvalidDataException("Шаг не может быть больше интервала " + (val - low) + " < " + step);
                        }
                    }
                    if (column == 2) {
                        if( high - low < val)
                        {
                            throw new InvalidDataException("Шаг не может быть больше интервала " + (high - low) + " < " + val);
                        }
                    }
                    super.setValueAt(aValue, row, column);

                    if (row >= 0 && row < listRecords.size()) {
                        if (column == 0) rec.setLowerBound(val);
                        else if (column == 1) rec.setUpperBound(val);
                        else if (column == 2) rec.setStep(val);

                        rec.setResult(0);
                        super.setValueAt("", row, 3);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(rootPanel, "Введите корректное число", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
                } catch (InvalidDataException e) {
                    JOptionPane.showMessageDialog(rootPanel, e.getMessage(), "Ошибка диапазона", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        table1.setModel(model);

        btnAdd.addActionListener(e -> {
            try {
                double low = Double.parseDouble(fieldLowerBound.getText());
                double high = Double.parseDouble(fieldUpperBound.getText());
                double step = Double.parseDouble(fieldStep.getText());
                if (high - low < 0) {
                    throw new InvalidDataException("Нижняя граница не может превышать верхнюю " + low + " > " + high);
                }
                if (high - low < step) {
                    throw new InvalidDataException("Шаг не может быть больше интервала " + (high - low) + " < " + step);
                }
                RecIntegral record = new RecIntegral(low, high, step, 0);

                listRecords.add(record);
                model.addRow(new Object[]{low, high, step, ""});

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(rootPanel, "Введите числа", "Ошибка формата", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidDataException ex) {
                JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), "Некорректные данные", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCalculate.addActionListener(e -> {
            try {
                int row = table1.getSelectedRow();
                if (row == -1) {
                    throw new ArrayIndexOutOfBoundsException("Строка не выбрана");
                }

                double left = Double.parseDouble(model.getValueAt(row, 0).toString());
                double right = Double.parseDouble(model.getValueAt(row, 1).toString());
                double step = Double.parseDouble(model.getValueAt(row, 2).toString());

                double res = RecIntegral.calculate(left, right, step);

                RecIntegral rec = listRecords.get(row);
                rec.setResult(res);
                model.setValueAt(String.format(java.util.Locale.US, "%.4f", res), row, 3);

            } catch (ArrayIndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(rootPanel, "Сначала выберите строку в таблице", "Ошибка выбора", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            try {
                int row = table1.getSelectedRow();
                if (row == -1) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                listRecords.remove(row);
                model.removeRow(row);

            } catch (ArrayIndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(rootPanel, "Нечего удалять: строка не выбрана", "Ошибка удаления", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnClear.addActionListener(e -> {
            clrClicked = true;
            model.setRowCount(0);
        });

        btnFill.addActionListener(e -> {
            clrClicked = false;
            model.setRowCount(0);
            for (RecIntegral rec : listRecords) {
                String resStr = (rec.getResult() == 0) ? "" : String.format("%.4f", rec.getResult());
                model.addRow(new Object[]{rec.getLowerBound(), rec.getUpperBound(), rec.getStep(), resStr});
            }
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}