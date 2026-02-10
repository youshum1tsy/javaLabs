package com.example.library;

import static java.lang.Math.sin;

public class Calculator {

    private static double f(double x) {
        return sin(x);
    }

    public static double calculate(double left, double right, double step) {
        int n = (int)((right - left) / step);
        double result = 0;
        double last = left + n * step;

        for (int i = 0; i < n - 1; i++) {
            double x0 = left + i * step;
            double x1 = last < right ? (f(last) + f(right)) * (right - last) / 2 : left + (i + 1) * step;
            result += (f(x0) + f(x1)) * step / 2;
        }
        return result;
    }
}