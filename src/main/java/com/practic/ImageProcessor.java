package com.practic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageProcessor extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private JLabel imageLabel;

    public ImageProcessor() {

        // Загрузка и отображение изображения
        loadImage();

        // Создание меню
        createMenu();

        // Настройка окна приложения
        setupWindow();
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private void loadImage() {
        try {
            originalImage = ImageIO.read(new File("practic/image.png")); // Укажите путь к изображению
            processedImage = deepCopy(originalImage);
            imageLabel = new JLabel(new ImageIcon(processedImage));
            add(imageLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Опции");
        menuBar.add(menu);

        // Добавление пунктов меню
        addMenuItem(menu, "Выбрать канал", e -> chooseChannel());
        addMenuItem(menu, "Переключить оттенки серого", e -> toggleGrayscale());
        addMenuItem(menu, "Обрезать изображение", e -> cropImage());
        addMenuItem(menu, "Повысить яркость", e -> increaseBrightness());
        addMenuItem(menu, "Нарисовать круг", e -> drawCircle());
        addMenuItem(menu, "Сбросить изменения", e -> resetChanges());

        setJMenuBar(menuBar);
    }

    private void setupWindow() {
        // Загрузка иконки
        try {
            Image icon = ImageIO.read(new File("practic/icon.jpg")); // Укажите путь к файлу иконки
            setIconImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void addMenuItem(JMenu menu, String title, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }

    private void showChannel(Color channelColor) {
        int width = processedImage.getWidth();
        int height = processedImage.getHeight();
        BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = processedImage.getRGB(x, y);
                int channel =
                        channelColor.equals(Color.RED)
                                ? (rgb >> 16) & 0xff
                                : channelColor.equals(Color.GREEN)
                                ? (rgb >> 8) & 0xff
                                : channelColor.equals(Color.BLUE) ? rgb & 0xff : 0;
                channelImage.setRGB(x, y, (255 << 24) | (channel << 16) | (channel << 8) | channel);
            }
        }

        imageLabel.setIcon(new ImageIcon(channelImage));
        imageLabel.repaint();
    }

    // Метод для выбора цветового канала
    private void chooseChannel() {
        String[] options = {"Красный", "Зеленый", "Синий"};
        int choice =
                JOptionPane.showOptionDialog(
                        this,
                        "Выберите канал:",
                        "Выбор канала",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);

        switch (choice) {
            case 0: // Красный
                showChannel(Color.RED);
                break;
            case 1: // Зеленый
                showChannel(Color.GREEN);
                break;
            case 2: // Синий
                showChannel(Color.BLUE);
                break;
            default:
                // Ничего не делаем, если пользователь закрыл диалоговое окно
        }
    }

    // Метод для переключения в оттенки серого
    private void toggleGrayscale() {
        for (int x = 0; x < processedImage.getWidth(); x++) {
            for (int y = 0; y < processedImage.getHeight(); y++) {
                Color originalColor = new Color(originalImage.getRGB(x, y));
                int grayLevel =
                        (int)
                                (originalColor.getRed() * 0.299
                                        + originalColor.getGreen() * 0.587
                                        + originalColor.getBlue() * 0.114);
                Color grayColor = new Color(grayLevel, grayLevel, grayLevel);
                processedImage.setRGB(x, y, grayColor.getRGB());
            }
        }
        imageLabel.setIcon(new ImageIcon(processedImage));
    }

    // Метод для обрезки изображения
    private void cropImage() {
        try {
            String input =
                    JOptionPane.showInputDialog(
                            this, "Введите координаты x, y и размеры обрезки width, height через запятую:");
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split(",");
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int width = Integer.parseInt(parts[2].trim());
                int height = Integer.parseInt(parts[3].trim());

                // Проверка, что координаты и размеры находятся в пределах изображения
                if (x >= 0 && y >= 0 && width > 0 && height > 0 &&
                        x + width <= processedImage.getWidth() && y + height <= processedImage.getHeight()) {
                    processedImage = processedImage.getSubimage(x, y, width, height);
                    imageLabel.setIcon(new ImageIcon(processedImage));
                } else {
                    JOptionPane.showMessageDialog(this, "Некорректные координаты или размеры обрезки.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректный формат ввода.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для увеличения яркости
    private void increaseBrightness() {
        try {
            String input =
                    JOptionPane.showInputDialog(
                            this, "Введите значение для увеличения яркости (например, 1.5):");
            if (input != null && !input.isEmpty()) {
                float scaleFactor = Float.parseFloat(input);

                // Проверка, что фактор увеличения яркости больше или равен 1.1
                if (scaleFactor >= 1.1) {
                    RescaleOp op = new RescaleOp(new float[] {scaleFactor, scaleFactor, scaleFactor, 1f},
                            new float[] {0, 0, 0, 0}, null);
                    processedImage = op.filter(processedImage, null); // Используйте processedImage здесь
                    imageLabel.setIcon(new ImageIcon(processedImage));
                } else {
                    JOptionPane.showMessageDialog(this, "Фактор увеличения яркости должен быть больше или равен 1.1.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректный формат ввода.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для рисования круга
    private void drawCircle() {
        try {
            String input =
                    JOptionPane.showInputDialog(
                            this, "Введите координаты центра круга x, y и радиус через запятую:");
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split(",");
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int radius = Integer.parseInt(parts[2].trim());

                // Проверка, что координаты центра и радиус находятся в пределах изображения
                if (x - radius >= 0 && y - radius >= 0 && x + radius <= processedImage.getWidth() &&
                        y + radius <= processedImage.getHeight()) {
                    Graphics2D g2d = processedImage.createGraphics();
                    g2d.setColor(Color.RED);
                    g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                    g2d.dispose();
                    imageLabel.setIcon(new ImageIcon(processedImage));
                } else {
                    JOptionPane.showMessageDialog(this, "Некорректные координаты центра или радиус.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректный формат ввода.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Метод для сброса изменений
    private void resetChanges() {
        processedImage = deepCopy(originalImage);
        imageLabel.setIcon(new ImageIcon(processedImage));
        imageLabel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageProcessor());
    }
}