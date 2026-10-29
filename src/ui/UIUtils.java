package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Common UI Styling and Modern Component Helper for Hostel Management System.
 * Adheres strictly to standard Java Swing & AWT.
 */
public class UIUtils {

    // Modern Enterprise Color Palette
    public static final Color COLOR_PRIMARY = new Color(79, 70, 229);        // Indigo 600
    public static final Color COLOR_PRIMARY_HOVER = new Color(67, 56, 202);  // Indigo 700
    public static final Color COLOR_PRIMARY_PRESS = new Color(55, 48, 163);  // Indigo 800
    public static final Color COLOR_HEADER = new Color(15, 23, 42);          // Slate 900
    public static final Color COLOR_NAV_BG = new Color(15, 23, 42);          // Slate 900
    public static final Color COLOR_BG = new Color(248, 250, 252);           // Slate 50
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);    // Slate 900
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color COLOR_SUCCESS = new Color(4, 120, 87);         // Emerald 700
    public static final Color COLOR_DANGER = new Color(190, 18, 60);         // Rose 700
    public static final Color COLOR_WARNING = new Color(180, 83, 9);         // Amber 700
    public static final Color COLOR_INFO = new Color(29, 78, 216);           // Blue 700
    public static final Color COLOR_BORDER = new Color(226, 232, 240);       // Slate 200

    private static final Locale INDIA_LOCALE = new Locale("en", "IN");

    private static final class LoadKey {
        private final JComponent owner;
        private final String task;

        private LoadKey(JComponent owner, String task) {
            this.owner = owner;
            this.task = task;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof LoadKey key && owner == key.owner && task.equals(key.task);
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(owner) + task.hashCode();
        }
    }

    private static final ConcurrentMap<LoadKey, SwingWorker<?, ?>> ACTIVE_LOADS = new ConcurrentHashMap<>();

    // Modern Typography Tokens
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static void installTheme() {
        UIManager.put("Panel.background", COLOR_BG);
        UIManager.put("Label.foreground", COLOR_TEXT_PRIMARY);

        // Inputs styling
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", COLOR_TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", COLOR_PRIMARY);
        UIManager.put("TextField.selectionBackground", new Color(224, 231, 255));
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("PasswordField.foreground", COLOR_TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", COLOR_PRIMARY);
        UIManager.put("PasswordField.selectionBackground", new Color(224, 231, 255));
        UIManager.put("PasswordField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", COLOR_TEXT_PRIMARY);
        UIManager.put("TextArea.selectionBackground", new Color(224, 231, 255));
        UIManager.put("TextArea.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", COLOR_TEXT_PRIMARY);

        UIManager.put("ScrollPane.background", Color.WHITE);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(COLOR_BORDER, 1));
        UIManager.put("Viewport.background", Color.WHITE);

        UIManager.put("OptionPane.background", COLOR_BG);
        UIManager.put("OptionPane.messageForeground", COLOR_TEXT_PRIMARY);
    }

    public static <T> void loadInBackground(
            JComponent owner,
            String taskKey,
            String operation,
            Supplier<T> load,
            Consumer<T> apply) {
        LoadKey key = new LoadKey(owner, taskKey);
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() {
                return load.get();
            }

            @Override
            protected void done() {
                if (ACTIVE_LOADS.get(key) != this) {
                    return;
                }
                try {
                    apply.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showLoadError(owner, operation, e);
                } catch (ExecutionException e) {
                    showLoadError(owner, operation, e.getCause());
                } finally {
                    ACTIVE_LOADS.remove(key, this);
                }
            }
        };
        SwingWorker<?, ?> previous = ACTIVE_LOADS.put(key, worker);
        if (previous != null) {
            previous.cancel(true);
        }
        worker.execute();
    }

    private static void showLoadError(Component owner, String operation, Throwable error) {
        System.err.println(operation + " failed: " + error.getMessage());
        JOptionPane.showMessageDialog(owner,
                "Unable to " + operation.toLowerCase() + ". Please try again.",
                "Data Load Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Creates a modern button with rounded corners, smooth anti-aliased graphics, and hover/press feedback.
     */
    public static JButton createButton(String text, Color bg, Color fg) {
        return createStyledButton(text, bg, fg, bg.darker(), bg.brighter());
    }

    public static JButton createStyledButton(String text, Color bg, Color fg, Color borderCol, Color hoverBg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight();
                int arc = 8;

                if (!isEnabled()) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                } else if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                } else {
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                }

                // Border
                if (borderCol != null && isEnabled()) {
                    g2.setColor(borderCol);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                }

                // Text: MUST set text color after drawing border!
                g2.setColor(isEnabled() ? fg : new Color(71, 85, 105));
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setFont(getFont());
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };

        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.addPropertyChangeListener("enabled", event -> {
            btn.setBackground(btn.isEnabled() ? bg : new Color(226, 232, 240));
            btn.setForeground(btn.isEnabled() ? fg : new Color(71, 85, 105));
        });
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_PRIMARY, Color.WHITE, COLOR_PRIMARY_PRESS, COLOR_PRIMARY_HOVER);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_SUCCESS, Color.WHITE, new Color(4, 120, 87), new Color(16, 185, 129));
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, COLOR_DANGER, Color.WHITE, new Color(185, 28, 28), new Color(239, 68, 68));
    }

    public static JButton createSecondaryButton(String text) {
        // Modern clean light neutral button
        return createStyledButton(text, new Color(241, 245, 249), new Color(51, 65, 85), new Color(203, 213, 225), new Color(226, 232, 240));
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    public static JPanel createPageHeader(String titleText, String subtitleText) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(COLOR_TEXT_MUTED);

        textBlock.add(title);
        textBlock.add(Box.createVerticalStrut(3));
        textBlock.add(subtitle);

        panel.add(textBlock, BorderLayout.WEST);
        return panel;
    }

    public static JLabel createCounterBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249)); // Slate 100
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240)); // Slate 200
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(new Color(71, 85, 105)); // Slate 600
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        badge.setOpaque(false);
        return badge;
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
        table.setRowHeight(36);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(COLOR_PRIMARY);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.LEFT);
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                            boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                    source, value, selected, focused, row, column);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                setHorizontalAlignment(value instanceof Number ? JLabel.CENTER : JLabel.LEFT);
                setToolTipText(value == null ? null : value.toString());
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    setForeground(COLOR_TEXT_PRIMARY);
                } else {
                    setBackground(new Color(238, 242, 255));
                    setForeground(COLOR_PRIMARY);
                }
                return component;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);
    }

    public static String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount);
    }

    /**
     * Creates a styled card container panel with smooth rounded corners and subtle border.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = 12;

                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return panel;
    }
}
