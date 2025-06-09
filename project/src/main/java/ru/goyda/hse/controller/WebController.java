package ru.goyda.hse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {

    @GetMapping("/")
    // @GetMapping("/") обрабатывает GET-запрос по адресу /api/ и возвращает index.
    public String index() {
        return "index";
    }
} 