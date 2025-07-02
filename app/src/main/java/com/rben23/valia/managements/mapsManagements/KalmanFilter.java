package com.rben23.valia.managements.mapsManagements;

public class KalmanFilter {

    private double Q = 0.0001; // Ruido del proceso
    private double R = 0.1; // Ruido de la medición
    private double X = 0; // Estado estimado
    private double P = 1; // Variabilidad del error
    private double K; // Ganancia de Kalman

    // Semaforo para la primera medición
    private boolean isInitialized = false;

    public KalmanFilter(double processNoise, double measurementNoise) {
        this.Q = processNoise;
        this.R = measurementNoise;
    }

    public double update(double measurement) {
        // Inicializamos el estado con la primera medición
        if (!isInitialized) {
            X = measurement;
            isInitialized = true;
            return X;
        }

        // Aplicamos la ecuación de Kalman
        P = P + Q;
        K = P / (P + R);
        X = X + K * (measurement - X);
        P = (1 - K) * P;

        return X;
    }

}
