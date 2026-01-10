package es.deusto.sd.ecoembesClient;

import java.util.Locale;

import javax.swing.SwingUtilities;

import es.deusto.sd.ecoembesClient.proxy.AuthProxy;
import es.deusto.sd.ecoembesClient.ui.LoginWindow;

public class EcoembesClientApp {
	
    public static void main(String[] args) {
    	Locale.setDefault(Locale.ENGLISH);
        SwingUtilities.invokeLater(() -> {
            AuthProxy proxy = new AuthProxy("http://localhost:8081");
            new LoginWindow(proxy).setVisible(true);
        });
    }
}

