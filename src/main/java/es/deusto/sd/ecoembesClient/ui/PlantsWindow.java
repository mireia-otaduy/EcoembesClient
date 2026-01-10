package es.deusto.sd.ecoembesClient.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import es.deusto.sd.ecoembesClient.proxy.AuthProxy;
import es.deusto.sd.ecoembesClient.proxy.PlantProxy;

public class PlantsWindow extends JPanel {

    private final AuthProxy authProxy;
    private final String token;
    private final PlantProxy plantProxy;

    private final Color bgMain;
    private final Font fontSection;
    private final Font fontText;

    // contenedor que cambia según la opción del menú
    private JPanel contentPanel;

    public PlantsWindow(AuthProxy authProxy, String token,
                        Color bgMain, Font fontSection, Font fontText) {
        this.authProxy = authProxy;
        this.token = token;
        this.bgMain = bgMain;
        this.fontSection = fontSection;
        this.fontText = fontText;
        this.plantProxy = new PlantProxy("http://localhost:8081");
        initUI();
    }

    /* ====== INICIALIZACIÓN UI ====== */

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(bgMain);

        // panel central sin scroll, muestra solo una sección
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgMain);

        // por defecto mostramos la sección de capacidad
        contentPanel.add(createCapacitySection(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    /* ====== MÉTODOS PÚBLICOS PARA EL MENÚ ====== */

    public void showCapacitySection() {
        contentPanel.removeAll();
        contentPanel.add(createCapacitySection(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void showAssignSection() {
        contentPanel.removeAll();
        contentPanel.add(createAssignSection(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /* ====== HELPERS COMUNES ====== */

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

    /* ====== 1. PLANT CAPACITY (POR NOMBRE) ====== */

    private JPanel createCapacitySection() {
        JPanel panel = createSectionPanel("Get plant capacity");
        GridBagConstraints gbc = baseGbc();

        JTextField txtPlantName = new JTextField(15);
        JTextField txtDate = new JTextField(8);
        JTextArea txtResult = new JTextArea(3, 40);
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(label("Plant name:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPlantName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(label("Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        panel.add(txtDate, gbc);

        JButton btnCheck = new JButton("Check capacity");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnCheck, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(txtResult), gbc);

        btnCheck.addActionListener(e -> {
            try {
                String plantName = txtPlantName.getText().trim();
                LocalDate date = LocalDate.parse(txtDate.getText().trim());

                String json = plantProxy.getPlantCapacity(plantName, date, token);
                txtResult.setText(json);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Date must be yyyy-MM-dd.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Error checking capacity: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    /* ====== 2. ASSIGN DUMPSTER TO PLANT (POR ID) ====== */

    private JPanel createAssignSection() {
        JPanel panel = createSectionPanel("Assign dumpster to plant");
        GridBagConstraints gbc = baseGbc();

        JTextField txtDumpsterId = new JTextField(10);
        JTextField txtPlantName = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(label("Dumpster ID:"), gbc);
        gbc.gridx = 1;
        panel.add(txtDumpsterId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(label("Plant name:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPlantName, gbc);

        JButton btnAssign = new JButton("Assign dumpster");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnAssign, gbc);

        btnAssign.addActionListener(e -> {
            try {
                long dumpsterId = Long.parseLong(txtDumpsterId.getText().trim());
                String plantName = txtPlantName.getText().trim();

                boolean ok = plantProxy.assignDumpsterToPlant(dumpsterId, plantName, token);

                if (ok) {
                    JOptionPane.showMessageDialog(panel,
                            "Dumpster " + dumpsterId + " assigned to plant " + plantName,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Plant has not enough capacity for that dumpster.",
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Dumpster ID must be numeric.",
                        "Input error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Error assigning dumpster: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

}
