package com.sgfb.lab.controller;

import com.sgfb.lab.service.AnnounceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnnounceController {

    @Autowired
    private AnnounceService announceService;

    @GetMapping("/announce")
    public ResponseEntity<Map<String, Object>> getAnnounce() {
        String announce = announceService.getAnnounce();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", announce);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/announce")
    public ResponseEntity<Map<String, Object>> updateAnnounce(@RequestBody Map<String, String> data) {
        String announce = data.get("announce");
        boolean success = announceService.updateAnnounce(announce);
        
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "公告更新成功");
        } else {
            result.put("success", false);
            result.put("message", "公告更新失败");
        }
        
        return ResponseEntity.ok(result);
    }
}
