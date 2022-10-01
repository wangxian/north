package top.xiqiu.north.support;

/**
 * 路由方法的参数属性
 */
public class MethodParameter {
    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数的默认值
     */
    private String defaultValue;

    /**
     * 是否必须参数，否则抛出异常信息
     */
    private boolean isRequired;

    /**
     * 参数类型
     */
    private Class<?> classType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }
}
