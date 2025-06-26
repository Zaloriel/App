package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/users")
    public String showUserInterface() {
        return "users";
    }


    @GetMapping("/web")
    public String redirectToUsers() {
        return "redirect:/users";
    }
}