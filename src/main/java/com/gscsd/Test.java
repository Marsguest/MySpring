package com.gscsd;

import com.gscsd.service.UserService;
import com.gscsd.service.UserServiceImpl;
import com.spring.GscsdApplicationContext;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;

public class Test {
    public static void main(String[] args) {
        GscsdApplicationContext applicationContext = new GscsdApplicationContext(AppConfig.class);


//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
        UserService userService = (UserService)applicationContext.getBean("userService");
        userService.test();
        //AbstractAutoProxyCreator

    }
}
