package com.sdj.code_sandbox;
import com.google.common.collect.Lists;
import com.sdj.code_sandbox.model.JudgeInfo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.sdj.code_sandbox.model.*;
import com.sdj.code_sandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author 沈德俊2022217204
 * 代码沙箱模板方法
 */
public abstract class JavaCodeSandboxTemplate implements CodeSandbox{
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000L;

    /**
     * 代码沙箱执行流程
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        String language = executeCodeRequest.getLanguage();

        //1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);
        try {
            //2. 编译代码，得到class文件
            ExecuteMessage executeCompileFileMessage = compileFile(userCodeFile);
            if (executeCompileFileMessage.getExitValue() != 0) {
                //编译失败，返回
                boolean isSystemError = executeCompileFileMessage.getExitValue() == -1 ||
                        (executeCompileFileMessage.getErrorMessage() != null &&
                                executeCompileFileMessage.getErrorMessage().contains("编译环境错误"));
                JudgeInfoMessageEnum errorType = isSystemError ?
                        JudgeInfoMessageEnum.SYSTEM_ERROR : JudgeInfoMessageEnum.COMPLIE_ERROR;
                executeCodeResponse.setOutputList(Lists.newArrayList());
                executeCodeResponse.setStatus(3);
                executeCodeResponse.setJudgeInfo(new JudgeInfo(errorType.getValue(), 0L, 0L));
                return executeCodeResponse;
            }

            //3. 执行代码，得到输出结果
            List<ExecuteMessage> executeMessageList = runCode(inputList, userCodeFile);

            //4. 收集整理输出结果
            executeCodeResponse = getOutput(executeMessageList);

            //5. 文件清理
            boolean del = deleteFile(userCodeFile);
            System.out.println("删除" + (del ? "成功" : "失败"));

            return executeCodeResponse;
        }catch (Exception e){
            executeCodeResponse.setOutputList(Lists.newArrayList());
            executeCodeResponse.setStatus(3);
            executeCodeResponse.setJudgeInfo(new JudgeInfo(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue(), 0L, 0L));
            executeCodeResponse.setMessage("系统错误："+e.getMessage());
            return  executeCodeResponse;
        }finally {
            deleteFile(userCodeFile);
        }
    }

    /**
     * 1。 把用户的代码保存为文件
     * @param code
     * @return
     */
    public File saveCodeToFile(String code){
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        //判断全局代码目录是否存在,没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        //把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2. 编译代码
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            Process complieProcess = Runtime.getRuntime().exec(compileCmd);
            executeMessage = ProcessUtils.runProcessAndGetMessage(complieProcess, "编译");
        } catch (IOException e) {
           executeMessage.setExitValue(-1);
            executeMessage.setErrorMessage("编译环境错误："+e.getMessage());
        }
        return executeMessage;
    }

    /**
     * 3.运行代码
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runCode(List<String> inputList,File userCodeFile){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        if (runProcess.isAlive()) {
                            System.out.println("超时了，中断");
                            runProcess.destroy();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
               throw new RuntimeException("执行错误",e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4. 整理输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutput(List<ExecuteMessage> executeMessageList){
        long maxTime = 0;
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        //正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5. 删除文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
           return del;
        }
        return true;
    }

    /**
     * 6. 返回错误响应
     *
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<String>());
        executeCodeResponse.setMessage(e.getMessage());
        //代码沙箱错误
        executeCodeResponse.setStatus(3);
        executeCodeResponse.setJudgeInfo(new JudgeInfo(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue(), 0L,0L));
        return executeCodeResponse;
    }
}
