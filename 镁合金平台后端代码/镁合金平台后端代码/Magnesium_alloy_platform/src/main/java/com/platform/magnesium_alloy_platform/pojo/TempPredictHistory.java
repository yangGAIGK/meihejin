package com.platform.magnesium_alloy_platform.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempPredictHistory {
    private Integer id;
    private String fileName;
    private Double temp;
    private LocalDateTime time;
}
