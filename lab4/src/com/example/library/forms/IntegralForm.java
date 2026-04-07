package com.example.library.forms;

import com.example.library.InvalidDataException;
import com.example.library.RecIntegral;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.nio.file.Files;
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

        btnDelete.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                listRecords.remove(row);
                model.removeRow(row);
            }
        });

        btnClear.addActionListener(e -> model.setRowCount(0));
        btnFill.addActionListener(e -> {
            model.setRowCount(0);
            for (RecIntegral rec : listRecords) {
                String resStr = (rec.getResult() == 0) ? "" : String.format(Locale.US, "%.4f", rec.getResult());
                model.addRow(new Object[]{rec.getLowerBound(), rec.getUpperBound(), rec.getStep(), resStr});
            }
        });
    }


    public void saveAsText() {
        List<RecIntegral> toSave = listRecords.stream().collect(Collectors.toList());

        if (toSave.isEmpty()) {
            JOptionPane.showMessageDialog(rootPanel, "Нет вычисленных данных для сохранения!");
            return;
        }

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                for (RecIntegral rec : toSave) {
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


    public void saveAsJson() {
        // Сохраняем только те записи, которые были вычислены (результат != 0)
        List<RecIntegral> toSave = listRecords.stream().collect(Collectors.toList());

        if (toSave.isEmpty()) {
            JOptionPane.showMessageDialog(rootPanel, "Нет вычисленных данных для сохранения!");
            return;
        }

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter out = new PrintWriter(fc.getSelectedFile())) {
                StringBuilder sb = new StringBuilder();
                sb.append("[\n"); // Начало массива

                for (int i = 0; i < toSave.size(); i++) {
                    RecIntegral rec = toSave.get(i);
                    sb.append("  {\n");
                    sb.append(String.format(Locale.US, "    \"lowerBound\": %f,\n", rec.getLowerBound()));
                    sb.append(String.format(Locale.US, "    \"upperBound\": %f,\n", rec.getUpperBound()));
                    sb.append(String.format(Locale.US, "    \"step\": %f,\n", rec.getStep()));
                    sb.append(String.format(Locale.US, "    \"result\": %f\n", rec.getResult()));
                    sb.append("  }");

                    if (i < toSave.size() - 1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                }
                sb.append("]");

                out.print(sb.toString());
                JOptionPane.showMessageDialog(rootPanel, "Данные успешно сохранены в JSON!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка сохранения: " + e.getMessage());
            }
        }
    }

    public void loadFromJson() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = new String(Files.readAllBytes(fc.getSelectedFile().toPath()));

                content = content.trim();
                if (content.startsWith("[")) content = content.substring(1);
                if (content.endsWith("]")) content = content.substring(0, content.length() - 1);
                content = content.trim();

                if (content.isEmpty()) {
                    JOptionPane.showMessageDialog(rootPanel, "Файл пуст");
                    return;
                }

                listRecords.clear();
                model.setRowCount(0);

                String[] objects = content.split("(?<=\\}),");

                for (String objStr : objects) {
                    objStr = objStr.trim();
                    if (objStr.isEmpty()) continue;

                    double low = extractJsonValue(objStr, "lowerBound");
                    double high = extractJsonValue(objStr, "upperBound");
                    double step = extractJsonValue(objStr, "step");
                    double res = extractJsonValue(objStr, "result");

                    RecIntegral rec = new RecIntegral(low, high, step, res);
                    listRecords.add(rec);

                    String resStr = (res == 0) ? "" : String.format(Locale.US, "%.4f", res);
                    model.addRow(new Object[]{low, high, step, resStr});
                }

                JOptionPane.showMessageDialog(rootPanel, "Данные успешно загружены из JSON!");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPanel, "Ошибка при разборе JSON: " + e.getMessage());
            }
        }
    }

    private double extractJsonValue(String json, String key) {

        String searchKey = "\"" + key + "\":";
        int startPos = json.indexOf(searchKey);

        if (startPos == -1) return 0;

        startPos += searchKey.length();

        int endPos = json.indexOf(",", startPos);
        if (endPos == -1) {
            endPos = json.indexOf("}", startPos);
        }
        if (endPos == -1) {
            endPos = json.length();
        }

        String value = json.substring(startPos, endPos).trim();

        value = value.replace("\"", "");

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }



    public JPanel getRootPanel() {
        return rootPanel;
    }

}