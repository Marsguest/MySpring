package com.spring;

import sun.text.resources.iw.FormatData_iw_IL;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GscsdApplicationContext {
    //配置类
    private Class configClass;

    //单例池
    private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
    //存扫描到的所有的bean的注解定义
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    //构造函数
    public GscsdApplicationContext(Class configClass) {
        this.configClass = configClass;

        //解析配置类
        //ComponentScan注解--->扫描路径--->扫描---->Beandefiniton ---->BeandefinitionMap
        try {
            scan(configClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                //单例bean对象
                Object bean =  createBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }

    }

    //创建bean
    private Object createBean(String beanName,BeanDefinition beanDefinition){
        Class clazz =  beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            
            // 依赖注入的位置
            for (Field declaredField:clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)){

                    Object bean =  getBean(declaredField.getName());
                    //给属性去赋值
                    declaredField.setAccessible(true);
                    declaredField.set(instance,bean);

                }
            }
            // Aware回调
            if (instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }
            //初始化
            if (instance instanceof InitializingBean){
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // BeanPostProcessor
            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }


            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void scan(Class configClass) throws Exception{
        ComponentScan componentScanAnnotation = (ComponentScan)configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".","/");
        //System.out.println(path);

        // 扫描
        // 类加载器
        ClassLoader classLoader = GscsdApplicationContext.class.getClassLoader(); //app类加载器
        String resource =  classLoader.getResource(path).getPath();

        //中文编码的处理
        try {
            resource = URLDecoder.decode(resource, "UTF-8");
            //System.out.println(resource);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        File file = new File(resource);
        //System.out.println(file);

        if (file.isDirectory()){
            //System.out.println("is directory");
            File[] files = file.listFiles();
            for (File f:files) {
                //System.out.println(f);
                String fileName = f.getAbsolutePath();
                if (fileName.endsWith(".class")){
                    String className = transFileToPackage(fileName);
                    //System.out.println(tarStr);
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)){
                            // 表示当前这个类是一个Bean
                            // 解析类 判断这个bean是单例bean还是原型bean

                            if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                               BeanPostProcessor beanPostProcessor = (BeanPostProcessor)clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(beanPostProcessor);
                            }

                            // BeanDefinition
                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if (clazz.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());

                            }else{
                                //没有scope注解表示这个类是单例的
                                beanDefinition.setScope("singleton");
                            }

                            beanDefinitionMap.put(beanName,beanDefinition);
                        }

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private String transFileToPackage(String srcStr){

        String className = srcStr.substring(srcStr.indexOf("com"),srcStr.indexOf(".class"));
        className = className.replace("\\",".");
        return className;
    }

    public Object getBean(String beanName){
        if (beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                Object o = singletonObjects.get(beanName);
                return o;
            }else{
                //原型bean  创建bean对象
                Object bean = createBean(beanName,beanDefinition);
                return bean;
            }

        }else{
            //不存在对应的Bean
            throw new NullPointerException("不存在对应的Bean");
        }
    }
}
