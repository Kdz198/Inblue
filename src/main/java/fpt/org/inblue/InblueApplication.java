package fpt.org.inblue;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import fpt.org.inblue.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync
public class InblueApplication implements CommandLineRunner {

    private final UserRepository userRepository;


    public static void main(String[] args) {
        SpringApplication.run(InblueApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {

    }
}
