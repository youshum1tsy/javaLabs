package com.example.library;

import static java.lang.Math.sin;

public class RecIntegral {
    private double lowerBound;
    private double upperBound;
    private double step;
    private double result;
    public RecIntegral(double lowerBound, double upperBound, double step, double result) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.step = step;
        this.result = result;
    }

    public double getLowerBound() { return lowerBound; }
    public void setLowerBound(double lowerBound) { this.lowerBound = lowerBound; }

    public double getUpperBound() { return upperBound; }
    public void setUpperBound(double upperBound) { this.upperBound = upperBound; }

    public double getStep() { return step; }
    public void setStep(double step) { this.step = step; }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

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