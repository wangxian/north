package top.xiqiu.north.support;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.north.core.ViewEngine;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Pebble template 模版引擎
 *
 * @see <https://pebbletemplates.io/>
 */
public class PebbleViewEngine implements ViewEngine {

    private final PebbleEngine engine;

    public PebbleViewEngine(ServletContext servletContext) {
        ServletLoader servletLoader = new ServletLoader(servletContext);
        servletLoader.setCharset("UTF-8");
        servletLoader.setPrefix("/templates");
        servletLoader.setSuffix("");

        this.engine = new PebbleEngine.Builder()
                .autoEscaping(true)
                .cacheActive(false)
                .loader(servletLoader).build();
    }

    @Override
    public void render(ModelAndView modelAndView, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PebbleTemplate template = this.engine.getTemplate(modelAndView.getView());
        template.evaluate(resp.getWriter(), modelAndView.getModel());
    }
}
