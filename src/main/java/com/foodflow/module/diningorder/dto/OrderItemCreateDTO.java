package com.foodflow.module.diningorder.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderItemCreateDTO {
    @NotEmpty
    List<@Valid OrderItemDTO> items;
}
