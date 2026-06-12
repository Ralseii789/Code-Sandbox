package com.sdj.code_sandbox.utils;

import cn.hutool.core.date.StopWatch;
import com.sdj.code_sandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 沈德俊2022217204
 * 进程工具类
 *
 */
public class ProcessUtils {
    /**
     * 执行指令并返回信息
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess,String opName){
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //等待程序执行，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            if(exitValue == 0){
                //正常退出
                System.out.println(opName + "成功");
                //分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(), StandardCharsets.UTF_8));
                List<String> outputStrList = new ArrayList<>();
                //逐行读取
                String complieOutputLine;
                while((complieOutputLine = bufferedReader.readLine())!=null){
                    outputStrList.add(complieOutputLine);
                }
               executeMessage.setMessage(StringUtils.join(outputStrList,"\n"));
            }else{
                //异常退出
                System.out.println(opName + "失败，错误码： "+exitValue);
                //分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(),StandardCharsets.UTF_8));
                List<String> outputStrList = new ArrayList<>();
                //逐行读取
                String complieOutputLine;
                while((complieOutputLine = bufferedReader.readLine())!=null){
                    outputStrList.add(complieOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList,"\n"));
                //分批获取进程的错误信息
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream(), StandardCharsets.UTF_8));
                List<String> errorOutputStrList = new ArrayList<>();
                //逐行读取
                String errorComplieOutputLine;
                while((errorComplieOutputLine = errorBufferedReader.readLine())!=null){
                    errorOutputStrList.add(errorComplieOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorOutputStrList,"\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        }catch (Exception e){
            e.printStackTrace();
        }
        return executeMessage;
    }

}
