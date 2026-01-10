package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("report_dimension_scores")
public class ReportDimensionScore {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("report_id")
    private Long reportId;

    @TableField("dimension_code")
    private String dimensionCode; // LOGIC, EMPATHY, KNOWLEDGE

    private BigDecimal score; // 0-10

    private String comment;
}
