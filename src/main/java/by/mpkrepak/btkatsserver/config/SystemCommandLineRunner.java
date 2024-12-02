package by.mpkrepak.btkatsserver.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class SystemCommandLineRunner implements CommandLineRunner {
    @Value("${settings.add_permission}")
    private String addPermission;
    @Override
    public void run(String... args) throws Exception {
        System.out.println(addPermission);
        Process process = Runtime.getRuntime().exec(addPermission);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        process.waitFor();
    }
}
