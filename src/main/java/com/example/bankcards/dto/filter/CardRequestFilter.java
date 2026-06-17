package com.example.bankcards.dto.filter;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record CardRequestFilter(

        @Parameter(description = "Тип заявки", example = "ISSUE")
        @Schema(allowableValues = {"ISSUE", "BLOCK", "ACTIVATE", "DELETE"})
        @Pattern(
                regexp = "ISSUE|BLOCK|ACTIVATE|DELETE",
                message = "requestType должен быть одним из значений: ISSUE, BLOCK, ACTIVATE, DELETE"
        )
        String requestType,

        @Parameter(description = "Статус заявки", example = "PENDING")
        @Schema(allowableValues = {"PENDING", "APPROVED", "REJECTED"})
        @Pattern(
                regexp = "PENDING|APPROVED|REJECTED",
                message = "status должен быть одним из значений: PENDING, APPROVED, REJECTED"
        )
        String status

) {
}