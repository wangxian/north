package top.xiqiu.north.support;

/**
 * Bean 对象缓存结构体
 */
public class BeanStoredEntity {
    private String beanName;
    private Class<?> beanClassType;

    /**
     * 实例对象
     */
    private Object instance;

    public BeanStoredEntity(String beanName, Class<?> beanType, Object instance) {
        this.beanName      = beanName;
        this.beanClassType = beanType;
        this.instance      = instance;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClassType() {
        return beanClassType;
    }

    public Object getInstance() {
        return instance;
    }
}
