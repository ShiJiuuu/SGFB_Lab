package com.sgfb.lab.controller;

import com.sgfb.lab.entity.Classroom;
import com.sgfb.lab.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @GetMapping("/classrooms")
    public ResponseEntity<Map<String, Object>> getAllClassrooms() {
        List<Classroom> classrooms = classroomService.getAllClassrooms();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", classrooms);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/classrooms/available")
    public ResponseEntity<Map<String, Object>> getAvailableClassrooms(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime borrowTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime returnTime) {

        List<Classroom> classrooms = classroomService.getAvailableClassroomsByTime(borrowTime, returnTime);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", classrooms);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/classrooms/{id}")
    public ResponseEntity<Map<String, Object>> updateClassroomStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> data) {
        Integer status = data.get("status");

        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setStatus(status);

        boolean success = classroomService.updateById(classroom);

        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "状态更新成功");
        } else {
            result.put("success", false);
            result.put("message", "状态更新失败");
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/classrooms/{id}/info")
    public ResponseEntity<Map<String, Object>> updateClassroomInfo(@PathVariable Integer id, @RequestBody Classroom classroomData) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setName(classroomData.getName());
        classroom.setLocation(classroomData.getLocation());
        classroom.setCapacity(classroomData.getCapacity());
        classroom.setStatus(classroomData.getStatus());

        boolean success = classroomService.updateById(classroom);

        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "更新成功");
        } else {
            result.put("success", false);
            result.put("message", "更新失败");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/classrooms")
    public ResponseEntity<Map<String, Object>> addClassroom(@RequestBody Classroom classroom) {
        boolean success = classroomService.save(classroom);

        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "添加成功");
        } else {
            result.put("success", false);
            result.put("message", "添加失败");
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/classrooms/{id}")
    public ResponseEntity<Map<String, Object>> deleteClassroom(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();

        if (classroomService.isRoomInUse(id)) {
            result.put("success", false);
            result.put("message", "该教室正在被预约记录使用，无法删除");
            return ResponseEntity.ok(result);
        }

        boolean success = classroomService.removeById(id);

        if (success) {
            result.put("success", true);
            result.put("message", "删除成功");
        } else {
            result.put("success", false);
            result.put("message", "删除失败");
        }

        return ResponseEntity.ok(result);
    }
}
