package es.deusto.sd.ecoembesClient;

import java.util.Locale;

import javax.swing.SwingUtilities;

import es.deusto.sd.ecoembesClient.controller.ServiceController;
import es.deusto.sd.ecoembesClient.ui.LoginWindow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class EcoembesClientApp {

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        System.setProperty("java.awt.headless", "false");

        ConfigurableApplicationContext ctx =
                SpringApplication.run(EcoembesClientApp.class, args);

        ServiceController controller = ctx.getBean(ServiceController.class);

        SwingUtilities.invokeLater(() -> {
            new LoginWindow(controller).setVisible(true);
        });
    }
}
