package com.example.skillup.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SynonymRequest {
    public record CreateSynonymRequest(
            @NotBlank String locale,
            String comment,
            @NotEmpty List<@NotBlank String> terms
    ){}
    public record AddTermsReq(
            @NotEmpty List<@NotBlank String> terms
    ) {}

    public record DeleteSynonymRequest(){}


}
