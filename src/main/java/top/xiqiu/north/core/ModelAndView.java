package top.xiqiu.north.core;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private Map<String, Object> model;
    private String view;

    public String getView() {
        return view;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public ModelAndView(String view) {
        this.view  = view;
        this.model = Map.of();
    }

    public ModelAndView(String view, String key, Object value) {
        this.view  = view;
        this.model = new HashMap<>();
        this.model.put(key, value);
    }

    public ModelAndView(String view, Map<String, Object> model) {
        this.view  = view;
        this.model = new HashMap<>(model);
    }
}
