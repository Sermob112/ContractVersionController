package org.example;

import mainForm.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Создаем экземпляр вашей формы
            MainWindow mainWindow = new MainWindow();

            // Создаем главное окно
            JFrame frame = new JFrame("Мое приложение");
            frame.setContentPane(mainWindow.MainPanel); // Устанавливаем главную панель
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack(); // Автоподбор размера
            frame.setLocationRelativeTo(null); // Центрирование
            frame.setVisible(true);
        });
    }
}