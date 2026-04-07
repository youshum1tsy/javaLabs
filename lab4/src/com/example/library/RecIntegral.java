package com.example.library;

import static java.lang.Math.sin;

import java.io.*;

public class RecIntegral implements Externalizable  {
    private double lowerBound;
    private double upperBound;
    private double step;
    private double result;

    public RecIntegral() {}

    public RecIntegral(double lowerBound, double upperBound, double step, double result) throws InvalidDataException {
        checkValue(lowerBound);
        checkValue(upperBound);
        checkValue(step);

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.step = step;
        this.result = result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(lowerBound);
        out.writeDouble(upperBound);
        out.writeDouble(step);
        out.writeDouble(result);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        this.lowerBound = in.readDouble();
        this.upperBound = in.readDouble();
        this.step = in.readDouble();
        this.result = in.readDouble();
    }

    private void checkValue(double val) throws InvalidDataException {
        if (val < 0.000001 || val > 1000000) {
            throw new InvalidDataException("Значение " + val + " вне диапазона [0.000001, 1000000]");
        }
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
        if (step <= 0) return 0;
        int n = (int)((right - left) / step);
        if (n <= 0) return 0;

        double result = 0;
        for (int i = 0; i < n; i++) {
            double x0 = left + i * step;
            double x1 = x0 + step;
            if (x1 > right) x1 = right;
            result += (f(x0) + f(x1)) * (x1 - x0) / 2;
        }
        return result;
    }

}