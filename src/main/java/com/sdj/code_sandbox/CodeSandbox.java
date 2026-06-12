package com.sdj.code_sandbox;

import com.sdj.code_sandbox.model.ExecuteCodeRequest;
import com.sdj.code_sandbox.model.ExecuteCodeResponse;

/**
 * @author 沈德俊2022217204
 * 代码沙箱接口
 */
public interface CodeSandbox {
    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
