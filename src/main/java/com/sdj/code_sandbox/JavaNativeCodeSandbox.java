package com.sdj.code_sandbox;

import com.sdj.code_sandbox.model.ExecuteCodeRequest;
import com.sdj.code_sandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * @author 沈德俊2022217204
 * Java原生实现代码沙箱
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
