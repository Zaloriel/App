package console;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("console")
public class ConsoleRunner implements CommandLineRunner {

    private final ConsoleUI consoleUI;

    public ConsoleRunner(ConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Приложение запущено в консольном режиме!");
        consoleUI.startInteractiveMode();
        System.exit(0);
    }
}
