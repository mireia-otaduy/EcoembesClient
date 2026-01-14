package es.deusto.sd.ecoembesClient.ui;

import es.deusto.sd.ecoembesClient.controller.ServiceController;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final ServiceController serviceController;
    private final String token;
    private final String userEmail;   // email of logged-in employee

    // Central panel that changes with each section
    private JPanel contentPanel;

    // Reusable windows
    private DumpstersWindow dumpstersWindow;
    private PlantsWindow plantsWindow;

    // Softer color palette
    private final Color bgMain      = new Color(224, 242, 241); // very soft teal
    private final Color bgHeader    = new Color(38, 166, 154);  // teal
    private final Color bgMenu      = new Color(0, 121, 107);   // darker teal
    private final Color accentText  = Color.WHITE;
    private final Color cardBorder  = new Color(200, 230, 230);

    private final Font fontMenu   = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font fontTitle  = new Font("Segoe UI", Font.BOLD, 22);
    private final Font fontCard   = new Font("Segoe UI", Font.BOLD, 16);
    private final Font fontText   = new Font("Segoe UI", Font.PLAIN, 13);

    public MainWindow(ServiceController serviceController, String token, String userEmail) {
        this.serviceController = serviceController;
        this.token = token;
        this.userEmail = userEmail;
        initUI();
    }

    private void initUI() {
        setTitle("Ecoembes Control Panel");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgMain);

        setJMenuBar(createMenuBar());
        add(createHeaderPanel(), BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgMain);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);

        // inicializamos ventanas reutilizables usando el ServiceController
        dumpstersWindow = new DumpstersWindow(serviceController, token, bgMain, fontCard, fontText);
        plantsWindow    = new PlantsWindow(serviceController, token, bgMain, fontCard, fontText);

        showDashboard();
    }

    /* ===================  MENU BAR  =================== */

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(bgMenu);

        JMenu mDumpsters = new JMenu("Dumpsters");
        mDumpsters.setFont(fontMenu);
        mDumpsters.setForeground(accentText);

        JMenuItem miCreateDumpster = new JMenuItem("Create dumpster");
        JMenuItem miUsage          = new JMenuItem("Usage queries");
        JMenuItem miUpdate         = new JMenuItem("Update containers");
        JMenuItem miStatus         = new JMenuItem("Status by postal code");

        miCreateDumpster.addActionListener(e -> showDumpstersCreate());
        miUsage.addActionListener(e -> showDumpstersUsage());
        miUpdate.addActionListener(e -> showDumpstersUpdate());
        miStatus.addActionListener(e -> showDumpstersStatus());

        mDumpsters.add(miCreateDumpster);
        mDumpsters.add(miUpdate);
        mDumpsters.add(miUsage);
        mDumpsters.add(miStatus);

        JMenu mPlants = new JMenu("Plants");
        mPlants.setFont(fontMenu);
        mPlants.setForeground(accentText);

        JMenuItem miCapacity = new JMenuItem("Capacity check");
        JMenuItem miAssign   = new JMenuItem("Assign dumpster to plant");

        miCapacity.addActionListener(e -> showPlantCapacity());
        miAssign.addActionListener(e -> showAssignDumpster());

        mPlants.add(miCapacity);
        mPlants.add(miAssign);

        JMenu mSession = new JMenu("Session");
        mSession.setFont(fontMenu);
        mSession.setForeground(accentText);

        JMenuItem miDetails = new JMenuItem("Details");
        JMenuItem miLogout  = new JMenuItem("Logout");

        miDetails.addActionListener(e -> openAccountWindow());
        miLogout.addActionListener(e -> doLogout());

        mSession.add(miDetails);
        mSession.add(miLogout);

        menuBar.add(mDumpsters);
        menuBar.add(mPlants);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(mSession);

        return menuBar;
    }

    /* ===================  HEADER  =================== */

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bgHeader);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, bgMenu));

        JLabel lblTitle = new JLabel("Ecoembes – Waste & Plant Control Panel");
        lblTitle.setFont(fontTitle);
        lblTitle.setForeground(accentText);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(lblTitle, BorderLayout.CENTER);

        return header;
    }

    /* ===================  DASHBOARD  =================== */

    private void showDashboard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgMain);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Left card: dumpsters
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createCard(
                "Dumpsters",
                "Create new dumpsters, update estimated containers\n" +
                        "and review usage history by date range.",
                "Go to dumpsters",
                e -> showDumpstersCreate()
        ), gbc);

        // Middle card: plants
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(createCard(
                "Plants",
                "Check plant capacity, assign dumpsters\n" +
                        "and notify external systems (PlasSB / ContSocket).",
                "Go to plants",
                e -> showPlantCapacity()
        ), gbc);

        // Right card: session / overview
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(createCard(
                "Session & overview",
                "View current account details and close your session safely\n" +
                        "from the Ecoembes control panel.",
                "View details",
                e -> openAccountWindow()
        ), gbc);

        setContentPanel(panel);
    }

    private JPanel createCard(String title, String text,
                              String buttonText, java.awt.event.ActionListener onClick) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cardBorder),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(fontCard);
        lblTitle.setForeground(new Color(0, 77, 64)); // dark teal

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(fontText);
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton btn = new JButton(buttonText);
        btn.setFont(fontMenu);
        btn.addActionListener(onClick);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(area, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btn);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    /* ===================  DUMPSTERS SECTIONS  =================== */

    private void showDumpstersBase() {
        setContentPanel(dumpstersWindow);
    }

    private void showDumpstersCreate() {
        showDumpstersBase();
        dumpstersWindow.showCreateSection();
    }

    private void showDumpstersUpdate() {
        showDumpstersBase();
        dumpstersWindow.showUpdateSection();
    }

    private void showDumpstersUsage() {
        showDumpstersBase();
        dumpstersWindow.showUsageSection();
    }

    private void showDumpstersStatus() {
        showDumpstersBase();
        dumpstersWindow.showStatusSection();
    }

    /* ===================  PLANTS SECTIONS  =================== */

    private void showPlantsBase() {
        setContentPanel(plantsWindow);
    }

    private void showPlantCapacity() {
        showPlantsBase();
        plantsWindow.showCapacitySection();
    }

    private void showAssignDumpster() {
        showPlantsBase();
        plantsWindow.showAssignSection();
    }

    /* ===================  AUX PANEL SWITCH  =================== */

    private void setContentPanel(JPanel newPanel) {
        getContentPane().remove(contentPanel);
        contentPanel = newPanel;
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /* ===================  SESSION DETAILS  =================== */

    private void openAccountWindow() {
        AccountWindow aw = new AccountWindow(serviceController, token, userEmail, this);
        aw.setVisible(true);
    }

    /* ===================  LOGOUT  =================== */

    private void doLogout() {
        int resp = JOptionPane.showConfirmDialog(
                this,
                "Do you really want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                serviceController.logout(token);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error during logout: " + ex.getMessage(),
                        "Logout", JOptionPane.ERROR_MESSAGE);
            }

            SwingUtilities.invokeLater(() -> {
                dispose();
                new LoginWindow(serviceController).setVisible(true);
            });
        }
    }
}
