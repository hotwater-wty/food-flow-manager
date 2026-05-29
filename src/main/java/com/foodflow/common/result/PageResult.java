package com.foodflow.common.result;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {
    private Long total;
    private Integer pageNo;
    private Integer pageSize;
    private List<T> records;
}
