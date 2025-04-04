package org.misha;

import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.*;
import java.util.*;

public class App {

    // Правая часть уравнения y' = f(x, y)
    static double f(double x, double y) {
        return y - x * x + 1;
    }

    // Точное решение
    static double exact(double x) {
        return (x + 1) * (x + 1) - 0.5 * Math.exp(x);
    }

    // Метод трапеций (симметричная схема)
    static List<Double> solveTrapezoid(double x0, double y0, double xEnd, double h) {
        List<Double> result = new ArrayList<>();
        double x = x0;
        double y = y0;
        result.add(y);

        while (x + 1e-10 < xEnd) {
            double yPredict = y + h * f(x, y); // Предсказание (Эйлер)
            double xNext = x + h;

            // Итерационное уточнение yNext
            double yNext = yPredict;
            for (int i = 0; i < 5; i++) {
                yNext = y + h / 2 * (f(x, y) + f(xNext, yNext));
            }

            result.add(yNext);
            x = xNext;
            y = yNext;
        }

        return result;
    }

    // Чтение input.txt
    static double[] readInput(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        double[] data = new double[4];
        for (int i = 0; i < 4; i++) {
            data[i] = Double.parseDouble(reader.readLine());
        }
        reader.close();
        return data;
    }

    // Запись результата
    static void writeResult(String filename, List<Double> yValues, double x0, double h) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filename));
        double x = x0;
        for (double y : yValues) {
            writer.printf(Locale.US, "%.5f %.8f\n", x, y);
            x += h;
        }
        writer.close();
    }

    // Сравнение с точным решением + оценка порядка точности
    static void writeErrorAnalysis(String filename, List<Double> yH, List<Double> yH2, double x0, double h) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filename));
        writer.println("x\tExact\tY_h\tY_h2\tError_h\tError_h2\tOrder");
        double x = x0;
        for (int i = 0; i < yH.size(); i++) {
            double exact = exact(x);
            double errH = Math.abs(yH.get(i) - exact);
            double errH2 = Math.abs(yH2.get(i * 2) - exact);
            double order = Math.log(errH / errH2) / Math.log(2);
            writer.printf(Locale.US, "%.2f\t%.8f\t%.8f\t%.8f\t%.2e\t%.2e\t%.2f\n",
                    x, exact, yH.get(i), yH2.get(i * 2), errH, errH2, order);
            x += h;
        }
        writer.close();
    }

    // Построение графика
    static void plot(List<Double> yH, List<Double> yH2, double x0, double h, double xEnd) throws IOException {
        int n = yH.size();
        int n2 = yH2.size();

        List<Double> xValuesH = new ArrayList<>();
        List<Double> xValuesH2 = new ArrayList<>();
        List<Double> exactValues = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            xValuesH.add(x0 + i * h);
        }
        for (int i = 0; i < n2; i++) {
            xValuesH2.add(x0 + i * h / 2);
        }
        for (double x = x0; x <= xEnd + 1e-8; x += h / 100) {
            exactValues.add(exact(x));
        }

        List<Double> xExact = new ArrayList<>();
        for (double x = x0; x <= xEnd + 1e-8; x += h / 100) {
            xExact.add(x);
        }

        XYChart chart = new XYChartBuilder().width(800).height(600).title("Symmetric Scheme").xAxisTitle("x").yAxisTitle("y").build();
        chart.addSeries("Exact", xExact, exactValues).setMarker(SeriesMarkers.NONE);
        chart.addSeries("h", xValuesH, yH).setMarker(SeriesMarkers.CIRCLE);
        chart.addSeries("h/2", xValuesH2, yH2).setMarker(SeriesMarkers.SQUARE);
        BitmapEncoder.saveBitmap(chart, "chart", BitmapEncoder.BitmapFormat.PNG);
    }

    public static void main(String[] args) throws IOException {
        double[] input = readInput("input.txt");
        double x0 = input[0], y0 = input[1], xEnd = input[2], h = input[3];

        List<Double> yH = solveTrapezoid(x0, y0, xEnd, h);
        List<Double> yH2 = solveTrapezoid(x0, y0, xEnd, h / 2);

        writeResult("output_h.txt", yH, x0, h);
        writeResult("output_h2.txt", yH2, x0, h / 2);
        writeErrorAnalysis("error_analysis.txt", yH, yH2, x0, h);
        plot(yH, yH2, x0, h, xEnd);
    }
}


