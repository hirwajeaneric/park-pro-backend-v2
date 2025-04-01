package com.park.parkpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateParkRequestDto {
    private String name;
    private String location;
    private String description;
}
