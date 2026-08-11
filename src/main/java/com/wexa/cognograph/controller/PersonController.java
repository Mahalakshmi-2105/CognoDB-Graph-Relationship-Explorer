package com.wexa.cognograph.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wexa.cognograph.service.PersonService;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    private final PersonService service;
    public PersonController(PersonService service) { this.service = service; }

    @GetMapping("/{id}")
    public ResponseEntity<?> person(@PathVariable long id) {
        Map<String,Object> result = service.person(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/friends")
    public List<Map<String,Object>> friends(@PathVariable long id) { return service.friends(id); }

    @GetMapping("/{id}/friends-of-friends")
    public List<Map<String,Object>> friendsOfFriends(@PathVariable long id) { return service.friendsOfFriends(id); }

    @GetMapping("/{id}/network")
    public List<Map<String,Object>> network(@PathVariable long id) { return service.network(id); }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> databaseError(Exception e) {
        return ResponseEntity.internalServerError().body(Map.of(
            "error", "Unable to reach or query CognoDB",
            "message", "Please check the database connection and try again."
        ));
    }
}
