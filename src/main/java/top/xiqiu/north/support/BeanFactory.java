package top.xiqiu.north.support;

import top.xiqiu.north.core.ScanClassWithAnnotations;

import java.util.List;

public class BeanFactory {
    /**
     * 获得 bean - byName
     */
    public static Object getBean(String name) {
        List<BeanStoredEntity> storedBeans = ScanClassWithAnnotations.getStoredBeans();
        for (BeanStoredEntity bean : storedBeans) {
            if (bean.getBeanName().equals(name)) {
                return bean.getInstance();
            }
        }

        return null;
    }

    /**
     * 获得 bean - byClassType
     */
    public static <T> T getBean(Class<T> clazz) {
        List<BeanStoredEntity> storedBeans = ScanClassWithAnnotations.getStoredBeans();
        for (BeanStoredEntity bean : storedBeans) {
            if (bean.getBeanClassType() == clazz) {
                return (T) bean.getInstance();
            }
        }

        return null;
    }

    /**
     * 获得 bean - byName + byClassType
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        List<BeanStoredEntity> storedBeans = ScanClassWithAnnotations.getStoredBeans();
        for (BeanStoredEntity bean : storedBeans) {
            if (bean.getBeanName().equals(name) && bean.getBeanClassType() == clazz) {
                return (T) bean.getInstance();
            }
        }

        return null;
    }
}
