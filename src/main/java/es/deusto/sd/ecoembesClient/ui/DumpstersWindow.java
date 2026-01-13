package es.deusto.sd.ecoembesClient.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import es.deusto.sd.ecoembesClient.controller.ServiceController;
import es.deusto.sd.ecoembesClient.data.Dumpster;
import es.deusto.sd.ecoembesClient.proxy.AuthProxy;
import es.deusto.sd.ecoembesClient.proxy.DumpsterProxy;

public class DumpstersWindow extends JPanel {

    private final ServiceController serviceController;
    private final String token;

    private final Color bgMain;
    private final Font fontSection;
    private final Font fontText;

    // contenedor que cambia según la opción del menú
    private JPanel contentPanel;

    public DumpstersWindow(ServiceController serviceController, String token,
                           Color bgMain, Font fontSection, Font fontText) {
        this.serviceController = serviceController;
        this.token = token;
        this.bgMain = bgMain;
        this.fontSection = fontSection;
        this.fontText = fontText;
        initUI();
    }

    /* ====== INICIALIZACIÓN UI ====== */

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(bgMain);

        // panel central sin scroll, muestra solo una sección
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgMain);

        // por defecto mostramos la sección de creación
        contentPanel.add(createCreateDumpsterSection(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    /* ====== MÉTODOS PÚBLICOS PARA EL MENÚ ====== */

    public void showCreateSection() {
        contentPanel.removeAll();
        contentPanel.add(createCreateDumpsterSection(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void showUpdateSection() {
        contentPanel.removeAll();
        contentPanel.add(createUpdateCapacitySection(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void showUsageSection() {
        contentPanel.removeAll();
        contentPanel.add(createUsageByIdSection(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void showStatusSection() {
        contentPanel.removeAll();
        contentPanel.add(createUsageByPostalCodeSection(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /* ====== SECTION HELPERS ====== */

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 230, 230)),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                fontSection,
                new Color(0, 77, 64)
        ));
        return panel;
    }

    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(fontText);
        return l;
    }

    /* ====== 1. CREATE DUMPSTER ====== */

    private JPanel createCreateDumpsterSection() {
        JPanel panel = createSectionPanel("Create dumpster");
        GridBagConstraints gbc = baseGbc();

        JTextField txtId = new JTextField(10);
        JTextField txtPC = new JTextField(6);
        JTextField txtCity = new JTextField(12);
        JTextField txtAddress = new JTextField(18);
        JComboBox<String> cbType = new JComboBox<>(new String[]{
                "Organic", "Plastic", "Glass", "Paper", "Metal"
        });

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(label("Dumpster ID:"), gbc);
        gbc.gridx = 1;
        panel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(label("Postal code:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPC, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(label("City:"), gbc);
        gbc.gridx = 1;
        panel.add(txtCity, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(label("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(label("Type:"), gbc);
        gbc.gridx = 1;
        panel.add(cbType, gbc);

        JButton btnCreate = new JButton("Create dumpster");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnCreate, gbc);

        btnCreate.addActionListener(e -> {
            try {
                long id = Long.parseLong(txtId.getText().trim());
                int pc = Integer.parseInt(txtPC.getText().trim());
                String city = txtCity.getText().trim();
                String address = txtAddress.getText().trim();
                String type = (String) cbType.getSelectedItem();

                serviceController.createDumpster(id, pc, city, address, type, token);
                JOptionPane.showMessageDialog(panel,
                        "Dumpster " + id + " created successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "ID and postal code must be numeric.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Error creating dumpster: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    /* ====== 2. UPDATE CAPACITY ====== */

    private JPanel createUpdateCapacitySection() {
        JPanel panel = createSectionPanel("Update dumpster capacity");
        GridBagConstraints gbc = baseGbc();

        JTextField txtId = new JTextField(10);
        JTextField txtContainers = new JTextField(6);
        JLabel lblResult = new JLabel(" ");
        lblResult.setForeground(new Color(0, 77, 64));
        lblResult.setFont(fontText);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(label("Dumpster ID:"), gbc);
        gbc.gridx = 1;
        panel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(label("Containers:"), gbc);
        gbc.gridx = 1;
        panel.add(txtContainers, gbc);

        JButton btnUpdate = new JButton("Update capacity");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnUpdate, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(lblResult, gbc);

        btnUpdate.addActionListener(e -> {
            try {
                long id = Long.parseLong(txtId.getText().trim());
                int containers = Integer.parseInt(txtContainers.getText().trim());

                String response = serviceController.updateDumpster(id, containers, token);
                lblResult.setText("Updated dumpster " + id + " | response: " + response);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Please enter valid numeric values for ID and containers.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Error updating dumpster: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel leftPanel = panel; // existing form
        JScrollPane rightPanel = createDumpstersScrollPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);
        split.setResizeWeight(0.6);
        
        JPanel endpanel = new JPanel(new BorderLayout());
        endpanel.add(split, BorderLayout.CENTER);

        return endpanel;
    }

    /* ====== 3. USAGE BY ID ====== */

    private JPanel createUsageByIdSection() {
        JPanel panel = createSectionPanel("Usage by dumpster ID");
        GridBagConstraints gbc = baseGbc();

        JTextField txtId = new JTextField(10);
        JTextField txtFrom = new JTextField(8);
        JTextField txtTo = new JTextField(8);
        JTextArea txtResult = new JTextArea(4, 40);
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(label("Dumpster ID:"), gbc);
        gbc.gridx = 1;
        panel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(label("From date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        panel.add(txtFrom, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(label("To date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        panel.add(txtTo, gbc);

        JButton btnSearch = new JButton("Search usage");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnSearch, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(txtResult), gbc);

        btnSearch.addActionListener(e -> {
            try {
                long id = Long.parseLong(txtId.getText().trim());
                String fromStr = txtFrom.getText().trim();
                String toStr = txtTo.getText().trim();

                LocalDate from = LocalDate.parse(fromStr);
                LocalDate to = LocalDate.parse(toStr);

                String result = serviceController.getUsageById(id, from, to, token);
                txtResult.setText(result);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Please enter a valid numeric ID.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Dates must be in format yyyy-MM-dd.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Error querying usage: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel leftPanel = panel; // existing form
        JScrollPane rightPanel = createDumpstersScrollPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);
        split.setResizeWeight(0.6);
        
        JPanel endpanel = new JPanel(new BorderLayout());
        endpanel.add(split, BorderLayout.CENTER);

        return endpanel;
    }

    /* ====== 4. STATUS BY POSTAL CODE ====== */

    private JPanel createUsageByPostalCodeSection() {
        JPanel panel = createSectionPanel("Status by postal code");
        GridBagConstraints gbc = baseGbc();

        JTextField txtPC = new JTextField(6);
        JTextField txtDate = new JTextField(8);
        JTextArea txtResult = new JTextArea(3, 40);
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);

        gbc.gridx = 0;	gbc.gridy = 0;
        panel.add(label("Postal code:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPC, gbc);

        gbc.gridx = 0;	gbc.gridy = 1;
        panel.add(label("Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        panel.add(txtDate, gbc);

        JButton btnCheck = new JButton("Check status");
        gbc.gridx = 0;	gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnCheck, gbc);

        gbc.gridx = 0;	gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(txtResult), gbc);

        btnCheck.addActionListener(e -> {
            try {
                int pc = Integer.parseInt(txtPC.getText().trim());
                LocalDate date = LocalDate.parse(txtDate.getText().trim()); // yyyy-MM-dd

                String result = serviceController.getStatusByPostalCode(pc, date, token);
                txtResult.setText(result);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Postal code must be numeric.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Date must be yyyy-MM-dd.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Error checking status: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel leftPanel = panel; // existing form
        JScrollPane rightPanel = createDumpstersScrollPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);
        split.setResizeWeight(0.6);
        
        JPanel endpanel = new JPanel(new BorderLayout());
        endpanel.add(split, BorderLayout.CENTER);

        return endpanel;
    }
    
    private JScrollPane createDumpstersScrollPanel() {

        JTextArea txtDumpsters = new JTextArea(18, 30);
        txtDumpsters.setEditable(false);
        txtDumpsters.setFont(fontText);
        txtDumpsters.setLineWrap(true);
        txtDumpsters.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(txtDumpsters);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 230, 230)),
                "All dumpsters",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                fontSection,
                new Color(0, 77, 64)
        ));

        // load immediately
        try {
            List<Dumpster> dumpsters = serviceController.getAllDumpsters(token);

            // Convert the list to a readable string
            StringBuilder sb = new StringBuilder();
            for (Dumpster d : dumpsters) {
                sb.append(d.toString()); // or format the fields as you like
                sb.append("\n");
            }

            txtDumpsters.setText(sb.toString());
        } catch (Exception e) {
            txtDumpsters.setText("Unable to load dumpsters.");
        }

        return scroll;
    }

}
