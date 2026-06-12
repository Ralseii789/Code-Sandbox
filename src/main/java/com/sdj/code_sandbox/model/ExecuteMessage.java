package com.sdj.code_sandbox.model;

import lombok.Data;

/**
 * @author 沈德俊2022217204
 * 进程执行信息
 */
@Data
public class ExecuteMessage {

    /**
     * 错误码
     */
    private Integer exitValue;

    /**
     * 正常信息
     */
    private String message;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 执行时间
     */
    private Long time;

    /**
     * 执行内存
     */
    private Long memory;
}
