package com.actbroker.ActBroker.controller;

import com.actbroker.ActBroker.service.BrokerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/act-broker")
public class BrokerController {

    BrokerService service = new BrokerService();

    @GetMapping("/{document}")
    public ResponseEntity<String> getBroker(@PathVariable String document) {
        return ResponseEntity.ok(service.getBroker(document, false).toString());
    }

    @PutMapping("/activate/{document}")
    public ResponseEntity<String> activateBroker(@PathVariable String document) {
        return ResponseEntity.ok(service.activateBroker(document).toString());
    }
}
