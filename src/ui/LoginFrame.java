package ui;

import dao.UserDAO;
import database.DBConnection;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Modern, Minimalist & Elegant Authentication window for the Hostel Management System.
 */
public class LoginFrame extends JFrame {

    private ModernTextField txtUsername;
    private ModernPasswordField txtPassword;
    private ModernPrimaryButton btnLogin;
    private JButton btnTogglePassword;
    private AlertBanner alertBanner;
    private boolean passwordVisible = false;
    private char passwordEchoChar;
    private final UserDAO userDAO = new UserDAO();

    // Design Tokens & Color Palette
    private static final Color COLOR_PAGE_BG = new Color(248, 250, 252);     // Slate 50
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_CARD_BORDER = new Color(226, 232, 240); // Slate 200
    private static final Color COLOR_TEXT_HEADING = new Color(15, 23, 42);   // Slate 900
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    private static final Color COLOR_TEXT_LABEL = new Color(51, 65, 85);     // Slate 700
    private static final Color COLOR_PRIMARY = new Color(79, 70, 229);        // Indigo 600
    private static final Color COLOR_PRIMARY_HOVER = new Color(67, 56, 202);  // Indigo 700
    private static final Color COLOR_PRIMARY_PRESS = new Color(55, 48, 163);  // Indigo 800
    private static final Color COLOR_INPUT_BORDER = new Color(203, 213, 225); // Slate 300
    private static final Color COLOR_INPUT_FOCUS = new Color(79, 70, 229);    // Indigo 600

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Hostel Management System - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 610);
        setMinimumSize(new Dimension(430, 570));
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        getContentPane().setBackground(COLOR_PAGE_BG);
        setLayout(new BorderLayout());

        // Center Wrapper
        JPanel pageWrapper = new JPanel(new GridBagLayout());
        pageWrapper.setBackground(COLOR_PAGE_BG);
        pageWrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Card Container with soft elevation
        CardPanel card = new CardPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setPreferredSize(new Dimension(380, 470));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 1. Minimalist Brand Emblem
        BrandEmblem emblem = new BrandEmblem();
        c.gridy = row++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 14, 0);
        card.add(emblem, c);

        // 2. Title
        JLabel lblTitle = new JLabel("Hostel Management", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblTitle.setForeground(COLOR_TEXT_HEADING);
        c.gridy = row++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(lblTitle, c);

        // 3. Subtitle
        JLabel lblSubtitle = new JLabel("Sign in to your staff portal", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(COLOR_TEXT_MUTED);
        c.gridy = row++;
        c.insets = new Insets(0, 0, 16, 0);
        card.add(lblSubtitle, c);

        // 4. Alert Banner (dynamically visible on error/info)
        alertBanner = new AlertBanner();
        c.gridy = row++;
        c.insets = new Insets(0, 0, 12, 0);
        card.add(alertBanner, c);

        // 5. Username Label
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(COLOR_TEXT_LABEL);
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(lblUsername, c);

        // 6. Username Input
        txtUsername = new ModernTextField("Enter your username");
        c.gridy = row++;
        c.insets = new Insets(0, 0, 14, 0);
        card.add(txtUsername, c);

        // 7. Password Header (Label on left, Show/Hide on right)
        JPanel passwordHeader = new JPanel(new BorderLayout());
        passwordHeader.setOpaque(false);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(COLOR_TEXT_LABEL);
        passwordHeader.add(lblPassword, BorderLayout.WEST);

        btnTogglePassword = new JButton("Show");
        btnTogglePassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTogglePassword.setForeground(COLOR_PRIMARY);
        btnTogglePassword.setBorderPainted(false);
        btnTogglePassword.setContentAreaFilled(false);
        btnTogglePassword.setFocusPainted(false);
        btnTogglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePassword.setMargin(new Insets(0, 0, 0, 0));
        btnTogglePassword.addActionListener(e -> togglePassword());
        passwordHeader.add(btnTogglePassword, BorderLayout.EAST);

        c.gridy = row++;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(passwordHeader, c);

        // 8. Password Input
        txtPassword = new ModernPasswordField("Enter your password");
        passwordEchoChar = txtPassword.getEchoChar();
        c.gridy = row++;
        c.insets = new Insets(0, 0, 18, 0);
        card.add(txtPassword, c);

        // 9. Sign In Button
        btnLogin = new ModernPrimaryButton("Sign In");
        btnLogin.addActionListener(this::handleLogin);
        c.gridy = row++;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(btnLogin, c);

        JButton studentPortal = new JButton("Student? Sign in to your portal");
        studentPortal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        studentPortal.setForeground(COLOR_PRIMARY);
        studentPortal.setBorderPainted(false);
        studentPortal.setContentAreaFilled(false);
        studentPortal.setFocusPainted(false);
        studentPortal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        studentPortal.addActionListener(event -> new StudentLoginFrame().setVisible(true));
        c.gridy = row++;
        c.insets = new Insets(10, 0, 0, 0);
        card.add(studentPortal, c);

        // Outer layout: Card and bottom security note
        JPanel column = new JPanel(new BorderLayout(0, 14));
        column.setOpaque(false);
        column.add(card, BorderLayout.CENTER);

        JLabel footer = new JLabel("Protected campus access for authorized staff and students", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(new Color(148, 163, 184)); // Slate 400
        column.add(footer, BorderLayout.SOUTH);

        GridBagConstraints pageConstraints = new GridBagConstraints();
        pageConstraints.gridx = 0;
        pageConstraints.gridy = 0;
        pageConstraints.anchor = GridBagConstraints.CENTER;
        pageWrapper.add(column, pageConstraints);

        add(pageWrapper, BorderLayout.CENTER);

        // Enter key handling
        getRootPane().setDefaultButton(btnLogin);
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(this::handleLogin);
    }

    private Image createAppIcon() {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(99, 102, 241),
                0, size, new Color(67, 56, 202)
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, size, size, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = size / 2;
        int cy = size / 2;
        int[] rx = {cx - 9, cx, cx + 9};
        int[] ry = {cy, cy - 8, cy};
        g2.drawPolyline(rx, ry, 3);
        g2.drawLine(cx - 7, cy, cx - 7, cy + 8);
        g2.drawLine(cx + 7, cy, cx + 7, cy + 8);
        g2.drawLine(cx - 7, cy + 8, cx + 7, cy + 8);
        g2.drawRoundRect(cx - 3, cy + 2, 6, 6, 2, 2);

        g2.dispose();
        return img;
    }

    private void togglePassword() {
        passwordVisible = !passwordVisible;
        txtPassword.setEchoChar(passwordVisible ? (char) 0 : passwordEchoChar);
        btnTogglePassword.setText(passwordVisible ? "Hide" : "Show");
        txtPassword.requestFocusInWindow();
    }

    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        char[] password = txtPassword.getPassword();

        if (username.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');
            alertBanner.showError("Please enter both username and password.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");
        alertBanner.showInfo("Connecting securely...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                try {
                    if (!DBConnection.testConnection()) {
                        throw new SQLException("The database server is unreachable.");
                    }
                    return userDAO.authenticate(username, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                Arrays.fill(password, '\0');
                txtPassword.setText("");
                btnLogin.setEnabled(true);
                btnLogin.setText("Sign In");

                try {
                    User user = get();
                    if (user == null) {
                        alertBanner.showError("Invalid username or password.");
                        txtPassword.requestFocusInWindow();
                        return;
                    }
                    new DashboardFrame(user).setVisible(true);
                    dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    alertBanner.showError("Sign in was interrupted. Please try again.");
                } catch (ExecutionException e) {
                    alertBanner.showError("Unable to connect to database. Check connection.");
                }
            }
        }.execute();
    }

    // =========================================================================
    // Custom Minimalist Modern Components
    // =========================================================================

    /**
     * Card Panel with smooth rounded corners and subtle multi-layer elevation.
     */
    private static class CardPanel extends JPanel {
        CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 20;

            // Ambient Soft Shadow
            for (int i = 1; i <= 5; i++) {
                g2.setColor(new Color(15, 23, 42, 3));
                g2.fillRoundRect(i, i + 2, w - i * 2, h - i * 2, arc + 2, arc + 2);
            }

            // White Card Surface
            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 2, arc, arc);

            // Clean 1px border
            g2.setColor(COLOR_CARD_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 2, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Modern Brand Emblem with smooth gradient and vector hostel icon.
     */
    private static class BrandEmblem extends JComponent {
        BrandEmblem() {
            setPreferredSize(new Dimension(52, 52));
            setMinimumSize(new Dimension(52, 52));
            setMaximumSize(new Dimension(52, 52));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int size = 50;
            int arc = 16;

            // Gradient Badge Background
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(99, 102, 241),    // Indigo 500
                    0, size, new Color(67, 56, 202)   // Indigo 700
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(1, 1, size, size, arc, arc);

            // Subtle inner rim
            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, size - 1, size - 1, arc, arc);

            // Draw Minimalist Residence Icon
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = size / 2 + 1;
            int cy = size / 2 + 1;

            // Roof
            int[] rx = {cx - 13, cx, cx + 13};
            int[] ry = {cy, cy - 11, cy};
            g2.drawPolyline(rx, ry, 3);

            // House Body
            g2.drawLine(cx - 10, cy, cx - 10, cy + 11);
            g2.drawLine(cx + 10, cy, cx + 10, cy + 11);
            g2.drawLine(cx - 10, cy + 11, cx + 10, cy + 11);

            // Modern Archway
            g2.drawRoundRect(cx - 4, cy + 3, 8, 8, 3, 3);

            g2.dispose();
        }
    }

    /**
     * Modern text field with rounded corners, focus glow, and placeholder text.
     */
    private static class ModernTextField extends JTextField {
        private final String placeholder;
        private boolean isFocused = false;
        private boolean isHovered = false;

        ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(COLOR_TEXT_HEADING);
            setCaretColor(COLOR_PRIMARY);
            setSelectionColor(new Color(224, 231, 255));
            setPreferredSize(new Dimension(316, 42));
            setMinimumSize(new Dimension(280, 42));
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setOpaque(false);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 10;

            // Background
            g2.setColor(isFocused ? Color.WHITE : new Color(248, 250, 252));
            g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);

            // Border & Focus Glow
            if (isFocused) {
                g2.setColor(new Color(99, 102, 241, 50));
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

                g2.setColor(COLOR_INPUT_FOCUS);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            } else if (isHovered) {
                g2.setColor(new Color(148, 163, 184)); // Slate 400
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            } else {
                g2.setColor(COLOR_INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);

            // Ghost Placeholder
            if (getText().isEmpty() && !isFocused) {
                Graphics2D gp = (Graphics2D) g.create();
                gp.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                gp.setFont(getFont());
                gp.setColor(new Color(148, 163, 184));
                FontMetrics fm = gp.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, 14, y);
                gp.dispose();
            }
        }
    }

    /**
     * Modern password field with rounded corners, focus glow, and placeholder text.
     */
    private static class ModernPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean isFocused = false;
        private boolean isHovered = false;

        ModernPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(COLOR_TEXT_HEADING);
            setCaretColor(COLOR_PRIMARY);
            setSelectionColor(new Color(224, 231, 255));
            setPreferredSize(new Dimension(316, 42));
            setMinimumSize(new Dimension(280, 42));
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setOpaque(false);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 10;

            // Background
            g2.setColor(isFocused ? Color.WHITE : new Color(248, 250, 252));
            g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);

            // Border & Focus Glow
            if (isFocused) {
                g2.setColor(new Color(99, 102, 241, 50));
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

                g2.setColor(COLOR_INPUT_FOCUS);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            } else if (isHovered) {
                g2.setColor(new Color(148, 163, 184)); // Slate 400
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            } else {
                g2.setColor(COLOR_INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);

            // Ghost Placeholder
            if (getPassword().length == 0 && !isFocused) {
                Graphics2D gp = (Graphics2D) g.create();
                gp.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                gp.setFont(getFont());
                gp.setColor(new Color(148, 163, 184));
                FontMetrics fm = gp.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, 14, y);
                gp.dispose();
            }
        }
    }

    /**
     * Modern primary button with rounded corners, smooth hover and press states.
     */
    private static class ModernPrimaryButton extends JButton {
        ModernPrimaryButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(316, 44));
            setMinimumSize(new Dimension(280, 44));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();
            int arc = 10;

            if (!isEnabled()) {
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(new Color(148, 163, 184));
            } else if (getModel().isPressed()) {
                g2.setColor(COLOR_PRIMARY_PRESS);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(Color.WHITE);
            } else if (getModel().isRollover()) {
                g2.setColor(COLOR_PRIMARY_HOVER);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(Color.WHITE);
            } else {
                GradientPaint gp = new GradientPaint(
                        0, 0, COLOR_PRIMARY,
                        0, h, new Color(67, 56, 202)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(Color.WHITE);
            }

            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    /**
     * Modern Alert Banner that collapses when empty and shows styled message chips.
     */
    private static class AlertBanner extends JPanel {
        private final JLabel lblText = new JLabel();
        private boolean isError = false;

        AlertBanner() {
            setOpaque(false);
            setVisible(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(6, 32, 6, 12));
            setPreferredSize(new Dimension(316, 36));
            setMinimumSize(new Dimension(280, 36));

            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            add(lblText, BorderLayout.CENTER);
        }

        void showError(String text) {
            this.isError = true;
            lblText.setForeground(new Color(220, 38, 38));
            lblText.setText(text);
            setVisible(true);
            revalidate();
            repaint();
        }

        void showInfo(String text) {
            this.isError = false;
            lblText.setForeground(new Color(37, 99, 235));
            lblText.setText(text);
            setVisible(true);
            revalidate();
            repaint();
        }

        void clear() {
            lblText.setText("");
            setVisible(false);
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!isVisible() || lblText.getText().isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();
            int arc = 8;

            Color bg = isError ? new Color(254, 242, 242) : new Color(239, 246, 255);
            Color border = isError ? new Color(254, 202, 202) : new Color(191, 219, 254);
            Color iconBg = isError ? new Color(239, 68, 68) : new Color(59, 130, 246);

            // Background & Border
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Vector Icon Badge
            int iconSize = 16;
            int ix = 10;
            int iy = (h - iconSize) / 2;
            g2.setColor(iconBg);
            g2.fillOval(ix, iy, iconSize, iconSize);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            String symbol = isError ? "!" : "i";
            int sx = ix + (iconSize - fm.stringWidth(symbol)) / 2;
            int sy = iy + (iconSize - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(symbol, sx, sy);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
