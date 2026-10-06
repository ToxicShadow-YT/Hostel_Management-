package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Common UI Styling and Component Helper for Hostel Management System.
 * Adheres strictly to standard Java Swing & AWT.
 */
public class UIUtils {

    // Harmonious modern color palette
    public static final Color COLOR_PRIMARY = new Color(30, 58, 138);       // #1E3A8A Navy
    public static final Color COLOR_PRIMARY_HOVER = new Color(37, 99, 235); // #2563EB Blue
    public static final Color COLOR_HEADER = new Color(15, 23, 42);         // #0F172A Dark Slate
    public static final Color COLOR_BG = new Color(241, 245, 249);          // #F1F5F9 Soft Light
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_PRIMARY = new Color(30, 41, 59);   // #1E293B
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // #64748B
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // #10B981 Emerald
    public static final Color COLOR_DANGER = new Color(220, 38, 38);        // #DC2626 Red
    public static final Color COLOR_WARNING = new Color(217, 119, 6);       // #D97706 Amber
    public static final Color COLOR_INFO = new Color(14, 165, 233);         // #0EA5E9 Sky
    public static final Color COLOR_BORDER = new Color(203, 213, 225);      // #CBD5E1

    // Standard Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Styles a standard button with primary theme colors and hover effect.
     */
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(7, 15, 7, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add subtle hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, COLOR_PRIMARY, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, COLOR_SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, COLOR_DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, new Color(100, 116, 139), Color.WHITE);
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_PRIMARY);
        return label;
    }

    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    /**
     * Styles a JTable for clean, readable tabular data presentation.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(28);
        table.setGridColor(new Color(226, 232, 240));
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(COLOR_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));

        // Center align headers
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Center align cell renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);
    }

    /**
     * Creates a styled card container panel with subtle border.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        return panel;
    }
}
