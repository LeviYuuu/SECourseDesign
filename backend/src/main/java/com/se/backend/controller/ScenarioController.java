package com.se.backend.controller;
import com.se.backend.common.Result;
import com.se.backend.entity.ScenarioTemplate;
import com.se.backend.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/scenario")
public class ScenarioController {
    @Autowired private ScenarioService scenarioService;
    @GetMapping("/list")
    public Result<List<ScenarioTemplate>> list() {
        return Result.success(scenarioService.getAll());
    }
}
