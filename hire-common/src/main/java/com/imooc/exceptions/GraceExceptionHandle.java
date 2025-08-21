package com.imooc.exceptions;

import com.imooc.result.GraceJSONResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GraceExceptionHandle {

    @ExceptionHandler(value = MyCustomException.class)
    @ResponseBody
    public GraceJSONResult returnMyCustomException(MyCustomException e) {
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }
}
