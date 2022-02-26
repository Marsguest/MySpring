package com.gscsd.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class GscsdBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前");
        if (beanName.equals("userService")){
            ((UserServiceImpl)bean).setBeanName("gscsd sss");
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        if (beanName.equals("userService")){
            Object proxyInstance =  Proxy.newProxyInstance(GscsdBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    //前置增强
                    System.out.println("前置增强 代理逻辑");

                    return method.invoke(bean,args);

                }
            });
            return proxyInstance;
        }


        return bean;
    }
}
