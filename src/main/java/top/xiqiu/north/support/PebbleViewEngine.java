package top.xiqiu.north.support;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import top.xiqiu.north.core.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Pebble template 模版引擎
 *
 * @see <a href="https://pebbletemplates.io/">https://pebbletemplates.io/</a>
 */
public class PebbleViewEngine implements ViewEngine {

    private final PebbleEngine engine;

    public PebbleViewEngine(ServletContext servletContext) {
        // ServletLoader loader = new ServletLoader(servletContext);
        // loader.setCharset("UTF-8");
        // loader.setPrefix("/WEB-INF/templates");
        // loader.setSuffix("");

        // 最好使用 ClasspathLoader，这样不受 WEB-INF 目录的影响，目录更干净一些
        // ServletLoader 依赖于 servlet， 需要更改模版视图的位置
        ClasspathLoader loader = new ClasspathLoader();
        loader.setCharset("UTF-8");
        loader.setPrefix("templates");
        loader.setSuffix("");

        this.engine = new PebbleEngine.Builder()
                .autoEscaping(true)
                .cacheActive(false)
                .loader(loader).build();

    }

    @Override
    public void render(ModelAndView modelAndView, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PebbleTemplate template = this.engine.getTemplate(modelAndView.getView());
        template.evaluate(resp.getWriter(), modelAndView.getModel());
    }
}
