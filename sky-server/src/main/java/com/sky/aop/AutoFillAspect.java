package com.sky.aop;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Slf4j
@Aspect
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("公共字段填充");

        // 首先获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autofill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autofill.value();


        //获取传入参数
        Object[] args = joinPoint.getArgs();

        if(args == null || args.length == 0){
            return;
        }
        //默认第一个参数是我们自动填充的对象
        Object entity = args[0];

        //获取我们填充需要的参数
        LocalDateTime localDateTime = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //使用反射为entity进行填充
        if(operationType == OperationType.INSERT){
            log.info("开始进行insert自动填充公共段");
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, localDateTime);
                setCreateTime.invoke(entity, localDateTime);
                setUpdateUser.invoke(entity, currentId);
                setCreateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE) {
            log.info("开始进行update自动填充公共段");
            try {
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, localDateTime);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

}
