package com.example.library.forms;

import com.example.library.InvalidDataException;
import com.example.library.RecIntegral;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Locale;
import java.util.stream.Collectors;

public class IntegralForm {
    private JPanel rootPanel;
    private JTextField fieldLowerBound;
    private JTextField fieldUpperBound;
    private JTextField fieldStep;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnCalculate;
    private JTable table1;
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
                        if (high - val < 0) throw new InvalidDataException("Нижняя > верхней");
                        if (high - val < step) throw new InvalidDataException("Шаг больше интервала");
                    }
                    if (column == 1) {
                        if (val - low < 0) throw new InvalidDataException("Верхняя < нижней");
                        if (val - low < step) throw new InvalidDataException("Шаг больше интервала");
                    }
                    if (column == 2) {
                        if (high - low < val) throw new InvalidDataException("Шаг больше интервала");
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
                    JOptionPane.showMessageDialog(rootPanel, "Введите число", "Ошибка", JOptionPane.ERROR_MESSAGE);
                } catch (InvalidDataException e) {
                    JOptionPane.showMessageDialog(rootPanel, e.getMessage(), "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        table1.setModel(model);

        // Кнопка Добавить
        btnAdd.addActionListener(e -> {
            try {
                double low = Double.parseDouble(fieldLowerBound.getText().replace(',', '.'));
                double high = Double.parseDouble(fieldUpperBound.getText().replace(',', '.'));
                double step = Double.parseDouble(fieldStep.getText().replace(',', '.'));

                if (high - low < 0) throw new InvalidDataException("Нижняя граница > Верхней");
                if (high - low < step) throw new InvalidDataException("Шаг больше интервала");

                RecIntegral record = new RecIntegral(low, high, step, 0);
                listRecords.add(record);
                model.addRow(new Object[]{low, high, step, ""});
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка: " + ex.getMessage());
            }
        });

        // Кнопка Вычислить
        btnCalculate.addActionListener(e -> {
            try {
                int row = table1.getSelectedRow();
                if (row == -1) throw new Exception("Выберите строку");

                double left = Double.parseDouble(model.getValueAt(row, 0).toString());
                double right = Double.parseDouble(model.getValueAt(row, 1).toString());
                double step = Double.parseDouble(model.getValueAt(row, 2).toString());

                double res = RecIntegral.calculate(left, right, step);
                listRecords.get(row).setResult(res);
                model.setValueAt(String.format(Locale.US, "%.4f", res), row, 3);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, ex.getMessage());
            }
        });

        // Кнопка Удалить
        btnDelete.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                listRecords.remove(row);
                model.removeRow(row);
            }
        });

        // Кнопки Очистить и Заполнить
        btnClear.addActionListener(e -> model.setRowCount(0));
        btnFill.addActionListener(e -> {
            model.setRowCount(0);
            for (RecIntegral rec : listRecords) {
                String resStr = (rec.getResult() == 0) ? "" : String.format(Locale.US, "%.4f", rec.getResult());
                model.addRow(new Object[]{rec.getLowerBound(), rec.getUpperBound(), rec.getStep(), resStr});
            }
        });
    }

    // --- ЛАБОРАТОРНАЯ №4: РАБОТА С ФАЙЛАМИ ---

    public void saveAsText() {
        List<RecIntegral> toSave = listRecords.stream()
                .filter(r -> r.getResult() != 0)
                .collect(Collectors.toList());

        if (toSave.isEmpty()) {
            JOptionPane.showMessageDialog(rootPanel, "Нет вычисленных данных для сохранения!");
            return;
        }

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                for (RecIntegral rec : toSave) {
                    // Пишем данные через разделитель ;
                    fw.write(String.format(Locale.US, "%f;%f;%f;%f\n",
                            rec.getLowerBound(), rec.getUpperBound(), rec.getStep(), rec.getResult()));
                }
                JOptionPane.showMessageDialog(rootPanel, "Текстовый файл сохранен!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка записи: " + e.getMessage());
            }
        }
    }

    public void loadFromText() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            try (FileReader fr = new FileReader(fc.getSelectedFile());
                 BufferedReader br = new BufferedReader(fr)) {

                listRecords.clear();
                model.setRowCount(0);
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(";");
                    if (p.length == 4) {
                        double low = Double.parseDouble(p[0]);
                        double high = Double.parseDouble(p[1]);
                        double step = Double.parseDouble(p[2]);
                        double res = Double.parseDouble(p[3]);

                        RecIntegral rec = new RecIntegral(low, high, step, res);
                        listRecords.add(rec);
                        model.addRow(new Object[]{low, high, step, String.format(Locale.US, "%.4f", res)});
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка чтения: " + e.getMessage());
            }
        }
    }

    public void saveAsBinary() {
        List<RecIntegral> toSave = listRecords.stream()
                .filter(r -> r.getResult() != 0)
                .collect(Collectors.toList());

        if (toSave.isEmpty()) {
            JOptionPane.showMessageDialog(rootPanel, "Нет данных для сохранения!");
            return;
        }

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fc.getSelectedFile())))) {
                out.writeObject(toSave);
                JOptionPane.showMessageDialog(rootPanel, "Бинарный файл сохранен!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка сериализации: " + e.getMessage());
            }
        }
    }

    public void loadFromBinary() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(fc.getSelectedFile())))) {

                @SuppressWarnings("unchecked")
                List<RecIntegral> loaded = (List<RecIntegral>) in.readObject();

                listRecords.clear();
                listRecords.addAll(loaded);

                model.setRowCount(0);
                for (RecIntegral rec : listRecords) {
                    model.addRow(new Object[]{rec.getLowerBound(), rec.getUpperBound(), rec.getStep(),
                            String.format(Locale.US, "%.4f", rec.getResult())});
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка десериализации: " + e.getMessage());
            }
        }
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}