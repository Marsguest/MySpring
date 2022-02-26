package com.gscsd.service;

import com.spring.*;

@Component("userService")
@Scope("prototype") //原型bean  多个getBean将返回多个不同的对象
public class UserServiceImpl implements BeanNameAware,InitializingBean,UserService {

    @Autowired
    private OrderService orderService;

    private String beanName;

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 对属性进行验证等等
        System.out.println("初始化");
    }

    @Override
    public void test(){

    }

}

