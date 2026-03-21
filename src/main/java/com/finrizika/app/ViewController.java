package com.finrizika.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    public ViewController() {
    }

    @GetMapping(value = { "/" })
    public String forwardToDashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping(value = { "/{path:^(?!api$)[^\\.]*}", "/**/{path:^(?!api$)[^\\.]*}" })
    public String forwardUnmatchedPaths() {
        return "forward:/index.html";
    }

}
