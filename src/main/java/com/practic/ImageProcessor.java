package com.practic;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class ImageProcessor extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private JLabel imageLabel;

    public ImageProcessor() {
        imageLabel = new JLabel(); // ������������� imageLabel
        add(imageLabel); // ���������� imageLabel � JFrame
        // �������� � ����������� �����������
        selectImage();

        // �������� ����
        createMenu();

        // ��������� ���� ����������
        setupWindow();
    }


    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private void selectImage() {
        // ����������� ������������ ������� ����������� ��� ������������ ����������� �� ���������
        Object[] options = {"������� �����������", "������������ ����������� �� ���������"};
        int choice = JOptionPane.showOptionDialog(this,
                "������ ������� ����������� � ���������� ��� ������������ ����������� �� ���������?",
                "����� �����������",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            // ������������ ������ �������� ����������� � ����������
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("�������� �����������");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "gif", "bmp"));

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    originalImage = ImageIO.read(file);
                    processedImage = deepCopy(originalImage);
                    imageLabel.setIcon(new ImageIcon(processedImage));
                    pack(); // �������� ������ ���� � ������������ � ����� ������������
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "�� ������� ��������� �����������: " + e.getMessage(), "������", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            // ������������ ������ ������������� ����������� �� ���������
            try {
                // �������� ����������� �� ��������� �� �������� ����������
                // ������� ���� � ����������� �� ��������� � ����� �������
                originalImage = ImageIO.read(getClass().getResource("/image.png"));
                processedImage = deepCopy(originalImage);
                imageLabel.setIcon(new ImageIcon(processedImage));
                pack(); // �������� ������ ���� � ������������ � ������������ �� ���������
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "�� ������� ��������� ����������� �� ���������: " + e.getMessage(), "������", JOptionPane.ERROR_MESSAGE);
            }
        }
    }





    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("�����");
        menuBar.add(menu);

        // ���������� ������� ����
        addMenuItem(menu, "������� �����", e -> chooseChannel());
        addMenuItem(menu, "����������� ������� ������", e -> toggleGrayscale());
        addMenuItem(menu, "�������� �����������", e -> cropImage());
        addMenuItem(menu, "�������� �������", e -> increaseBrightness());
        addMenuItem(menu, "���������� ����", e -> drawCircle());
        addMenuItem(menu, "�������� ���������", e -> resetChanges());
        addMenuItem(menu, "������� �����������", e -> selectImage());

        setJMenuBar(menuBar);
    }

    private void setupWindow() {
        // �������� ������
        try {
            Image icon = ImageIO.read(getClass().getResource("/icon.jpg")); // �������� "/icon.jpg" �� ���������� ���� � ������ ����� ������
            setIconImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "�� ������� ��������� ������: " + e.getMessage(), "������", JOptionPane.ERROR_MESSAGE);
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

    // ����� ��� ������ ��������� ������
    private void chooseChannel() {
        String[] options = {"�������", "�������", "�����"};
        int choice =
                JOptionPane.showOptionDialog(
                        this,
                        "�������� �����:",
                        "����� ������",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);

        switch (choice) {
            case 0: // �������
                showChannel(Color.RED);
                break;
            case 1: // �������
                showChannel(Color.GREEN);
                break;
            case 2: // �����
                showChannel(Color.BLUE);
                break;
            default:
                // ������ �� ������, ���� ������������ ������ ���������� ����
        }
    }

    // ����� ��� ������������ � ������� ������
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

    // ����� ��� ������� �����������
    private void cropImage() {
        try {
            String input =
                    JOptionPane.showInputDialog(
                            this, "������� ���������� x, y � ������� ������� width, height ����� �������:");
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split(",");
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int width = Integer.parseInt(parts[2].trim());
                int height = Integer.parseInt(parts[3].trim());

                // ��������, ��� ���������� � ������� ��������� � �������� �����������
                if (x >= 0 && y >= 0 && width > 0 && height > 0 &&
                        x + width <= processedImage.getWidth() && y + height <= processedImage.getHeight()) {
                    processedImage = processedImage.getSubimage(x, y, width, height);
                    imageLabel.setIcon(new ImageIcon(processedImage));
                    imageLabel.repaint(); // �������� ���� �����, ����� �������� �����������
                } else {
                    JOptionPane.showMessageDialog(this, "������������ ���������� ��� ������� �������.",
                            "������", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "������������ ������ �����.",
                    "������", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ����� ��� ���������� �������
    private void increaseBrightness() {
        try {
            String input = JOptionPane.showInputDialog(this, "������� �������� ��� ���������� ������� (��������, 1.5):");
            if (input != null && !input.isEmpty()) {
                float scaleFactor = Float.parseFloat(input);

                if (scaleFactor >= 1.1) {
                    // �������� ���� ����������� � ��������� �������� ��� RescaleOp
                    float[] scales = new float[processedImage.getColorModel().getNumComponents()];
                    float[] offsets = new float[processedImage.getColorModel().getNumComponents()];
                    Arrays.fill(scales, scaleFactor);
                    Arrays.fill(offsets, 0);

                    RescaleOp op = new RescaleOp(scales, offsets, null);
                    processedImage = op.filter(processedImage, null);
                    imageLabel.setIcon(new ImageIcon(processedImage));
                    imageLabel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "������ ���������� ������� ������ ���� ������ ��� ����� 1.1.", "������", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "������ ��� ���������� �������: " + e.getMessage(), "������", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ����� ��� ��������� �����
    private void drawCircle() {
        try {
            String input =
                    JOptionPane.showInputDialog(
                            this, "������� ���������� ������ ����� x, y � ������ ����� �������:");
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split(",");
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int radius = Integer.parseInt(parts[2].trim());

                // ��������, ��� ���������� ������ � ������ ��������� � �������� �����������
                if (x - radius >= 0 && y - radius >= 0 && x + radius <= processedImage.getWidth() &&
                        y + radius <= processedImage.getHeight()) {
                    Graphics2D g2d = processedImage.createGraphics();
                    g2d.setColor(Color.RED);
                    g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                    g2d.dispose();
                    imageLabel.setIcon(new ImageIcon(processedImage));
                } else {
                    JOptionPane.showMessageDialog(this, "������������ ���������� ������ ��� ������.",
                            "������", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "������������ ������ �����.",
                    "������", JOptionPane.ERROR_MESSAGE);
        }
    }


    // ����� ��� ������ ���������
    private void resetChanges() {
        processedImage = deepCopy(originalImage);
        imageLabel.setIcon(new ImageIcon(processedImage));
        imageLabel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageProcessor());
    }
}
