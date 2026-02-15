package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController {

    private Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("pistaPadel/health")
    public ResponseEntity<String> health() {
        log.info("Recogiendo ");
        return ResponseEntity.ok("OK");
    }
}
