package es.deusto.sd.ecoembesClient.ui;

import es.deusto.sd.ecoembesClient.controller.ServiceController;
import es.deusto.sd.ecoembesClient.proxy.AuthProxy;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class LoginWindow extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ServiceController serviceController;

    private JTextField txtEmail;
    private JPasswordField txtPassword;

    // Same palette as MainWindow
    private final Color bgMain     = new Color(224, 242, 241); // soft teal
    private final Color bgHeader   = new Color(38, 166, 154);  // teal
    private final Color bgBorder   = new Color(0, 121, 107);   // darker
    private final Color accentText = Color.WHITE;

    private final Font fontTitle  = new Font("Segoe UI", Font.BOLD, 24);
    private final Font fontText   = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font fontButton = new Font("Segoe UI", Font.PLAIN, 14);

    public LoginWindow(ServiceController serviceController) {
        this.serviceController = serviceController;
        initComponents();
    }

    private void initComponents() {
        // General configuration
        setTitle("EcoembesClient – Login");
        setSize(480, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgMain);

        // Top header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bgHeader);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, bgBorder));

        JLabel lblTitle = new JLabel("Ecoembes Control Panel – Login");
        lblTitle.setFont(fontTitle);
        lblTitle.setForeground(accentText);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(lblTitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Center panel with fields
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(bgMain);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(bgBorder, 1),
                "Enter your credentials",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                fontText,
                bgBorder
        );
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(fontText);
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(lblEmail, gbc);

        txtEmail = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(txtEmail, gbc);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(fontText);
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(18);
        gbc.gridx = 1; gbc.gridy = 1;
        centerPanel.add(txtPassword, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with login button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(bgMain);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, bgBorder));

        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(fontButton);
        //btnLogin.setBackground(bgHeader);
        //btnLogin.setForeground(accentText);
        btnLogin.setFocusPainted(false);

        bottomPanel.add(btnLogin);
        add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields.",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String token = serviceController.login(email, password);
            if (token != null) {
                JOptionPane.showMessageDialog(this,
                        "Login successful.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);

                // Pass email so MainWindow and AccountWindow can show it
                MainWindow main = new MainWindow(serviceController, token, email);
                main.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Connection error with Ecoembes (8081): " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
/*
    // Standalone main for quick testing (optional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuthProxy proxy = new AuthProxy("http://localhost:8081");
            LoginWindow w = new LoginWindow(serviceController)
            		w.setVisible(true);
        });
    }*/
  }
