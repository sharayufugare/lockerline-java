package com.lockerline.door;

import com.lockerline.error.NoSuchDoor;
import com.lockerline.error.NoSuchTower;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/towers/{tower}/doors")
public class DoorController {

    private final JdbcClient jdbcClient;

    public DoorController(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private record DoorRow(String no, String sizeClass, String currentParcelId) {}

    @GetMapping
    public WallView getWall(
            @PathVariable String tower,
            @RequestParam(required = false) SizeClass size,
            @RequestParam(required = false) DoorState state) {

        if (!"A".equalsIgnoreCase(tower)) {
            throw new NoSuchTower();
        }

        List<DoorRow> rows = jdbcClient.sql("SELECT no, size_class, current_parcel_id FROM doors")
                .query((rs, rowNum) -> new DoorRow(
                        rs.getString("no"),
                        rs.getString("size_class"),
                        rs.getString("current_parcel_id")
                )).list();

        List<WallDoor> doors = rows.stream()
                .map(r -> new WallDoor(
                        r.no(),
                        SizeClass.valueOf(r.sizeClass()),
                        r.currentParcelId() == null ? DoorState.free : DoorState.holding
                ))
                .filter(d -> size == null || d.size() == size)
                .filter(d -> state == null || d.state() == state)
                .toList();

        int freeCount = (int) doors.stream().filter(d -> d.state() == DoorState.free).count();

        return new WallView(tower, freeCount, doors);
    }

    @GetMapping("/{no}")
    public WallDoor getDoor(@PathVariable String tower, @PathVariable String no) {
        if (!"A".equalsIgnoreCase(tower)) {
            throw new NoSuchTower();
        }

        List<DoorRow> rows = jdbcClient.sql("SELECT no, size_class, current_parcel_id FROM doors WHERE no = ?")
                .param(no)
                .query((rs, rowNum) -> new DoorRow(
                        rs.getString("no"),
                        rs.getString("size_class"),
                        rs.getString("current_parcel_id")
                )).list();

        if (rows.isEmpty()) {
            throw new NoSuchDoor();
        }

        DoorRow r = rows.get(0);
        return new WallDoor(
                r.no(),
                SizeClass.valueOf(r.sizeClass()),
                r.currentParcelId() == null ? DoorState.free : DoorState.holding
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid parameter: " + ex.getName());
        pd.setType(URI.create("/errors/validation"));
        pd.setTitle("Bad Request");
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(pd);
    }

    @ExceptionHandler(NoSuchTower.class)
    public ResponseEntity<ProblemDetail> handleNoSuchTower(NoSuchTower ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Tower not found");
        pd.setType(URI.create("/errors/no-such-tower"));
        pd.setTitle("Not Found");
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(pd);
    }

    @ExceptionHandler(NoSuchDoor.class)
    public ResponseEntity<ProblemDetail> handleNoSuchDoor(NoSuchDoor ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Door not found");
        pd.setType(URI.create("/errors/no-such-door"));
        pd.setTitle("Not Found");
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(pd);
    }
}