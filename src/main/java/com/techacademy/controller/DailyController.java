package com.techacademy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("daily")
public class DailyController {

    private final DailyService dailyService;

    public DailyController(DailyService dailyService) {
        this.dailyService = dailyService;
    }


    @Getmapping
    public String list(Model model) {
        model.addAttribute("dailyList", dailyService.findALL());
        model.addAttribute("listSize", dailyService.findALL().size());

        return "daily/list";
    }



}
