package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.utils.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultResponse<T> {
    private T data;
    private boolean success;
    private String errorMessage;
    private String errorCode;


    public String getErrorCode() {
        if (StringUtils.isEmpty(errorCode)) {
            return "AUTH000";
        }
        return errorCode;
    }

}
