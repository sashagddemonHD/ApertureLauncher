import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;

public class ApertureLauncher extends JFrame {
    private static final String SAVE_FILE = "pirate_steam_db.txt";
    
    static class GameItem {
        String name; String path;
        public GameItem(String name, String path) { this.name = name; this.path = path; }
    }

    private final List<GameItem> gameList = new ArrayList<>();
    private JPanel gamesGridPanel;
    private final Color COLOR_BG = new Color(27, 40, 56);       
    private final Color COLOR_CARD = new Color(38, 56, 77);     
    private final Color COLOR_ACCENT = new Color(102, 192, 244); 

    public ApertureLauncher() {
        loadGamesFromDisk();
        setTitle("Aperture Pirate Steam v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("APERTURE LAUNCHER (Локальный Стим)", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JButton addGameBtn = new JButton("+ Добавить пиратку (.exe/.bat)");
        addGameBtn.setFont(new Font("Arial", Font.BOLD, 14));
        addGameBtn.setBackground(COLOR_ACCENT); addGameBtn.setForeground(Color.BLACK);
        addGameBtn.setFocusable(false);
        addGameBtn.addActionListener(e -> {
            playTone(600, 80, 0.3);
            showAddGameDialog();
        });

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(addGameBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        gamesGridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gamesGridPanel.setBackground(COLOR_BG);
        gamesGridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(gamesGridPanel);
        scrollPane.setBorder(null); scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("GLaDOS приветствует вас в пиратском секторе. Тестируйте игры безопасно.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Courier New", Font.ITALIC, 12));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(statusLabel, BorderLayout.SOUTH);

        updateGamesGrid();
    }

    private void showAddGameDialog() {
        JTextField nameField = new JTextField();
        JButton fileBrowseBtn = new JButton("Выбрать файл...");
        JTextField pathField = new JTextField(); pathField.setEditable(false);

        fileBrowseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) { return f.isDirectory() || f.getName().toLowerCase().endsWith(".exe") || f.getName().toLowerCase().endsWith(".bat"); }
                public String getDescription() { return "Исполняемые файлы (*.exe, *.bat)"; }
            });
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                pathField.setText(selectedFile.getAbsolutePath());
                if (nameField.getText().isEmpty()) {
                    String fileName = selectedFile.getName();
                    nameField.setText(fileName.substring(0, fileName.lastIndexOf('.')));
                }
            }
        });

        Object[] message = { "Название игры:", nameField, "Путь к файлу запуска:", pathField, fileBrowseBtn };
        int option = JOptionPane.showConfirmDialog(this, message, "Добавление новой игры", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim(); String path = pathField.getText().trim();
            if (!name.isEmpty() && !path.isEmpty()) {
                gameList.add(new GameItem(name, path));
                saveGamesToDisk(); updateGamesGrid();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка: Поля не должны быть пустыми!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateGamesGrid() {
        gamesGridPanel.removeAll();
        for (GameItem game : gameList) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBackground(COLOR_CARD); card.setBorder(BorderFactory.createLineBorder(new Color(50, 75, 100), 2, true));

            JPanel coverPanel = new JPanel(new BorderLayout());
            coverPanel.setBackground(new Color(23, 32, 45)); coverPanel.setPreferredSize(new Dimension(0, 100));
            JLabel iconLabel = new JLabel("🎮", SwingConstants.CENTER); iconLabel.setFont(new Font("Arial", Font.PLAIN, 36));
            coverPanel.add(iconLabel, BorderLayout.CENTER);

            JLabel nameLabel = new JLabel(game.name, SwingConstants.CENTER);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16)); nameLabel.setForeground(Color.WHITE);
            nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JButton playBtn = new JButton("ИГРАТЬ");
            playBtn.setFont(new Font("Arial", Font.BOLD, 14)); playBtn.setBackground(new Color(92, 126, 17)); playBtn.setForeground(Color.WHITE);
            playBtn.setFocusable(false);

            playBtn.addActionListener(e -> {
                playTone(880, 150, 0.5);
                try {
                    File file = new File(game.path);
                    if (!file.exists()) {
                        JOptionPane.showMessageDialog(this, "Файл не найден по пути:\n" + game.path, "Ошибка запуска", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Desktop.getDesktop().open(file);
                } catch (Exception ex) {
                    try { Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "start", "", game.path}); }
                    catch (IOException ioEx) { JOptionPane.showMessageDialog(this, "Критическая ошибка вызова процесса: " + ioEx.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE); }
                }
            });

            card.add(coverPanel, BorderLayout.NORTH); card.add(nameLabel, BorderLayout.CENTER); card.add(playBtn, BorderLayout.SOUTH);
            card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            gamesGridPanel.add(card);
        }
        gamesGridPanel.revalidate(); gamesGridPanel.repaint();
    }

    private void saveGamesToDisk() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            for (GameItem game : gameList) { writer.println(game.name + "|||" + game.path); }
        } catch (IOException e) { System.err.println("Не удалось сохранить базу данных игр: " + e.getMessage()); }
    }

    private void loadGamesFromDisk() {
        File file = new File(SAVE_FILE); if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|\\|\\|");
                if (parts.length == 2) { gameList.add(new GameItem(parts[0], parts[1])); }
            }
        } catch (IOException e) { System.err.println("Не удалось прочитать базу данных игр: " + e.getMessage()); }
    }

    private void playTone(int hz, int msecs, double vol) {
        new Thread(() -> {
            try {
                float rate = 8000f; byte[] buf = new byte[msecs * 8];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (rate / hz) * 2.0 * Math.PI; buf[i] = (byte) (Math.sin(angle) * 127.0 * vol);
                }
                AudioFormat af = new AudioFormat(rate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af); sdl.start(); sdl.write(buf, 0, buf.length); sdl.drain(); sdl.close();
            } catch (Exception ignored) {}
        }).start();
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new ApertureLauncher().setVisible(true)); }
}
