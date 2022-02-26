package com.spring;


public interface InitializingBean {

    //在属性设置完成之后执行
    void afterPropertiesSet() throws Exception;
}
