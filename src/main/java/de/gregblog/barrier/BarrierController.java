package de.gregblog.barrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/barrier")
public class BarrierController {
    private static final Logger LOG = LoggerFactory.getLogger(BarrierController.class);
    private final BarrierService barrierService;

    public BarrierController(BarrierService barrierService) {
        this.barrierService = barrierService;
    }

    @GetMapping("/await")
    public void await(@RequestParam(required = false) Integer timeoutSeconds) {
        barrierService.await(timeoutSeconds);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleTimeoutException(BarrierTimeoutException e) {
        LOG.error(e.getMessage(), e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handlePartyLeftException(PartyLeftBarrierException e) {
        LOG.error(e.getMessage(), e);
    }

    @ExceptionHandler
    public ResponseEntity<Void> handleBarrierException(BarrierException e) {
        if (e.isShutdown()) {
            LOG.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.GONE).build();
        } else {
            LOG.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
