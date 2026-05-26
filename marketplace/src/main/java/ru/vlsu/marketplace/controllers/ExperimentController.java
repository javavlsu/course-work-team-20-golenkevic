package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.marketplace.services.ExperimentRunner;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/experiments")
@RequiredArgsConstructor
@Slf4j
public class ExperimentController {

    private final ExperimentRunner runner;

    @PostMapping("/{n}")
    public ResponseEntity<Map<String, Object>> run(@PathVariable int n) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("experiment", n);
        try {
            switch (n) {
                case 1 -> runner.exp1();
                case 2 -> runner.exp2();
                case 3 -> runner.exp3();
                default -> {
                    resp.put("error", "Используй n = 1, 2 или 3");
                    return ResponseEntity.badRequest().body(resp);
                }
            }
            resp.put("result", "OK");
        } catch (RuntimeException ex) {
            resp.put("result", "FAILED");
            resp.put("exception", ex.getMessage());
            log.warn("Experiment {} failed: {}", n, ex.getMessage());
        }
        return ResponseEntity.ok(resp);
    }
}
