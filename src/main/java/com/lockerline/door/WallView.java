package com.lockerline.door;

import java.util.List;

public record WallView(
    String tower,
    int free,
    List<WallDoor> doors
) {}