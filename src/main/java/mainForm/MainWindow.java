package mainForm;

import Database.Hooks.DataBaseServices;
import Parser.ContractParser;

import javax.swing.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainWindow {
    public JPanel MainPanel;
    private JPanel PanelGrid;
    private JTable JTableVersionControll;
    private JLabel StatusUpdater;
    private JButton парситьButton;
    private JLabel StatusParser;
    private JProgressBar ProgressBar;

    public MainWindow() {
        Date lastUpdate = DataBaseServices.getLastParsingDate();

        if (lastUpdate != null) {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            StatusUpdater.setText("Последнее обновление: " + df.format(lastUpdate));
        } else {
            StatusUpdater.setText("Парсинг ещё не выполнялся");
        }
        парситьButton.addActionListener(e -> {
            // Запуск парсера в отдельном потоке, чтобы не блокировать UI
            new Thread(() -> {
                try {
                    ContractParser contractParser = new ContractParser(ProgressBar,StatusParser );
                    contractParser.fullParseProcess();
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(MainPanel,
                                    "Ошибка при запуске парсера: " + ex.getMessage(),
                                    "Ошибка", JOptionPane.ERROR_MESSAGE)
                    );
                    ex.printStackTrace();
                }
            }).start();
        });
    }
}
