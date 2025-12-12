package fpt.org.inblue;

import fpt.org.inblue.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InblueApplication implements CommandLineRunner {

    private final UserRepository userRepository;

    public InblueApplication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(InblueApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }

}
