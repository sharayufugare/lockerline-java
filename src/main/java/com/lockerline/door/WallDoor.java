package com.lockerline.door;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WallDoor(
    String no,
    SizeClass size,
    DoorState state
) {}