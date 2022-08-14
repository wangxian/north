package top.xiqiu.north.core;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private Map<String, Object> model;
    private String view;

    /**
     * 实例化
     *
     * @param view 视图名称，如：user/login.html，且支持 redirect:/login 跳转
     */
    public ModelAndView(String view) {
        this.view  = view;
        this.model = Map.of();
    }

    /**
     * 实例化 - 并设置模板变量
     *
     * @param view  视图名称，如：user/login.html，且支持 redirect:/login 跳转
     * @param key   模板变量键值
     * @param value 模版变量的值
     */
    public ModelAndView(String view, String key, Object value) {
        this.view  = view;
        this.model = new HashMap<>();
        this.model.put(key, value);
    }

    /**
     * 实例化 - 直接初始化所有模版变量（注意：覆盖已有模版变量）
     *
     * @param view  视图名称，如：user/login.html，且支持 redirect:/login 跳转
     * @param model 模版变量
     */
    public ModelAndView(String view, Map<String, Object> model) {
        this.view  = view;
        this.model = new HashMap<>(model);
    }

    /**
     * 获取模版视图
     */
    public String getView() {
        return view;
    }

    /**
     * 获取所有模版变量
     */
    public Map<String, Object> getModel() {
        return model;
    }

    /**
     * 设置模版变量
     *
     * @param key   模板变量键值
     * @param value 模版变量的值
     */
    public void put(String key, Object value) {
        if (this.model == null) {
            this.model = new HashMap<String, Object>();
        }

        this.model.put(key, value);
    }
}
