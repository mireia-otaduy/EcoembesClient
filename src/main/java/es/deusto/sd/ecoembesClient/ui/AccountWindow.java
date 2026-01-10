package es.deusto.sd.ecoembesClient.ui;

import es.deusto.sd.ecoembesClient.proxy.AuthProxy;

import javax.swing.*;
import java.awt.*;


public class AccountWindow extends JFrame {

    private final AuthProxy authProxy;
    private final String token;
    private final String userEmail;
    private final MainWindow parent;

    private final Color bgMain     = new Color(224, 242, 241);
    private final Color bgHeader   = new Color(38, 166, 154);
    private final Color accentText = Color.WHITE;

    private final Font fontTitle = new Font("Segoe UI", Font.BOLD, 20);
    private final Font fontText  = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font fontBtn   = new Font("Segoe UI", Font.PLAIN, 13);

    public AccountWindow(AuthProxy authProxy, String token,
                         String userEmail, MainWindow parent) {
        this.authProxy = authProxy;
        this.token = token;
        this.userEmail = userEmail;
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        setTitle("Account details");
        setSize(480, 260);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgMain);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bgHeader);

        JLabel lblTitle = new JLabel("Employee account", SwingConstants.CENTER);
        lblTitle.setFont(fontTitle);
        lblTitle.setForeground(accentText);

        header.add(lblTitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

     // Center with icon + info
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(bgMain);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // "Avatar" usando un icono estándar y texto
        Icon avatarIcon = UIManager.getIcon("OptionPane.informationIcon");
        JLabel avatarLabel = new JLabel(" ", avatarIcon, SwingConstants.CENTER);
        avatarLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        avatarLabel.setVerticalTextPosition(SwingConstants.BOTTOM);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        center.add(avatarLabel, gbc);

        // Email
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblEmail = new JLabel("Email: " + userEmail);
        lblEmail.setFont(fontText);
        center.add(lblEmail, gbc);

        // Password
        gbc.gridy = 2;
        JLabel lblPassword = new JLabel("Password: ********");
        lblPassword.setFont(fontText);
        center.add(lblPassword, gbc);

        add(center, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(bgMain);

        JButton btnClose = new JButton("Close");
        btnClose.setFont(fontBtn);
        btnClose.addActionListener(e -> dispose());

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(fontBtn);
        btnLogout.addActionListener(e -> doLogout());

        bottom.add(btnClose);
        bottom.add(btnLogout);

        add(bottom, BorderLayout.SOUTH);
    }

    private void doLogout() {
        int resp = JOptionPane.showConfirmDialog(
                this,
                "Do you really want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                // Try to logout on the server. If the token is already invalid,
                // we simply ignore it and continue.
                authProxy.logout(token);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error during logout: " + ex.getMessage(),
                        "Logout", JOptionPane.ERROR_MESSAGE);
            }

            // Close account window and main window, return to login
            dispose();
            parent.dispose();
            SwingUtilities.invokeLater(() ->
                    new LoginWindow(authProxy).setVisible(true));
        }
    }

}
