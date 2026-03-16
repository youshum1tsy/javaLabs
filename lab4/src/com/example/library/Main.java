package com.example.library;

import javax.swing.*;
import com.example.library.forms.IntegralForm;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Интеграл sin(x)");
            IntegralForm form = new IntegralForm();

            JMenuBar menuBar = new JMenuBar();

            JMenu fileMenu = new JMenu("Файл");

            JMenuItem itemSaveText = new JMenuItem("Сохранить в текст");
            JMenuItem itemLoadText = new JMenuItem("Загрузить из текста");
            JMenuItem itemSaveBin  = new JMenuItem("Сохранить (бинарно)");
            JMenuItem itemLoadBin  = new JMenuItem("Загрузить (бинарно)");

            itemSaveText.addActionListener(e -> form.saveAsText());
            itemLoadText.addActionListener(e -> form.loadFromText());
            itemSaveBin.addActionListener(e -> form.saveAsBinary());
            itemLoadBin.addActionListener(e -> form.loadFromBinary());

            fileMenu.add(itemSaveText);
            fileMenu.add(itemLoadText);
            fileMenu.addSeparator();
            fileMenu.add(itemSaveBin);
            fileMenu.add(itemLoadBin);

            menuBar.add(fileMenu);

            // УСТАНАВЛИВАЕМ МЕНЮ В FRAME
            frame.setJMenuBar(menuBar);
            // ---------------------

            frame.setContentPane(form.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}