package com.sgfb.rent.controller;

import com.sgfb.rent.entity.TimeSlot;
import com.sgfb.rent.mapper.TimeSlotMapper;
import com.sgfb.rent.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-slots")
@CrossOrigin(origins = "*")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSlots() {
        List<TimeSlot> slots = timeSlotService.getAllSlots();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", slots);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/enabled")
    public ResponseEntity<Map<String, Object>> getEnabledSlots() {
        List<TimeSlot> slots = timeSlotService.getEnabledSlots();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", slots);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<Map<String, Object>> getSlotByDayOfWeek(@PathVariable int dayOfWeek) {
        List<TimeSlot> slots = timeSlotMapper.selectByDayOfWeek(dayOfWeek);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", slots);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/points/{dayOfWeek}")
    public ResponseEntity<Map<String, Object>> getTimePoints(@PathVariable int dayOfWeek) {
        List<String> points = timeSlotService.generateTimePoints(dayOfWeek);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", points);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveSlot(@RequestBody TimeSlot timeSlot) {
        boolean saved = timeSlotService.saveSlot(timeSlot);
        Map<String, Object> result = new HashMap<>();
        result.put("success", saved);
        result.put("message", saved ? "保存成功" : "保存失败");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSlot(@PathVariable int id) {
        boolean deleted = timeSlotService.deleteSlotById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", deleted);
        result.put("message", deleted ? "删除成功" : "删除失败");
        return ResponseEntity.ok(result);
    }
}
