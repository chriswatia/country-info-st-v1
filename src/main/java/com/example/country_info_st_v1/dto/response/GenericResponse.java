package com.example.country_info_st_v1.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericResponse {
    private String message;
    private String statusCode;
    private Object data;
}
