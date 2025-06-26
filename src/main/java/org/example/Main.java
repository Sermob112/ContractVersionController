package org.example;

import Database.Hooks.HibernateUtil;

import Parser.ContractParser;
import mainForm.MainWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {


        try {
            initializeHibernate();
        } catch (Exception e) {
            System.err.println("Ошибка инициализации Hibernate:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Не удалось инициализировать Hibernate:\n" + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return; // Остановка, если Hibernate не запущен
        }

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

        try {

            initializeHibernate();

//            ContractParser contractParser = new ContractParser();
//            contractParser.fullParseProcess();

        } catch (Exception e) {
            System.err.println("Ошибка в основном потоке:");
            e.printStackTrace();
        }
    }

    private static void initializeHibernate() {
        HibernateUtil.getSessionFactory(); // Если не получится — выбросит ошибку
        System.out.println("Hibernate успешно инициализирован");
    }
}
