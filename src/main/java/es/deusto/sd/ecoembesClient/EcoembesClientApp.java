package es.deusto.sd.ecoembesClient;

import java.util.Locale;

import javax.swing.SwingUtilities;

import es.deusto.sd.ecoembesClient.proxy.AuthProxy;
import es.deusto.sd.ecoembesClient.controller.ServiceController;
import es.deusto.sd.ecoembesClient.ui.LoginWindow;

public class EcoembesClientApp {
	
    public static void main(String[] args) {
    	Locale.setDefault(Locale.ENGLISH);
        SwingUtilities.invokeLater(() -> {
            ServiceController controller = new ServiceController("http://localhost:8081");
            new LoginWindow(controller).setVisible(true);
        });
    }
}

