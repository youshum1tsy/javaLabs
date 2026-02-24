package com.example.library;

import javax.swing.*;
import com.example.library.forms.IntegralForm;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("sin(x)");

                IntegralForm form = new IntegralForm();
                frame.setContentPane(form.getRootPanel());

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}