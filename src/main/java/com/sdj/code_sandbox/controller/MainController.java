package com.sdj.code_sandbox.controller;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.sdj.code_sandbox.JavaNativeCodeSandbox;
import com.sdj.code_sandbox.model.ExecuteCodeRequest;
import com.sdj.code_sandbox.model.ExecuteCodeResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author 沈德俊2022217204
 */
@RestController("/")
public class MainController {

    // 定义鉴权请求头和密钥，保证安全
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    /**
     * 项目健康检查
     * @return
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response){
        //安全性认证
        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        //获取期望的签名
        String sign = DigestUtil.sha256Hex(AUTH_REQUEST_SECRET + jsonStr);
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if(!sign.equals(authHeader)){
            response.setStatus(403);
            return null;
        }
        if(executeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
