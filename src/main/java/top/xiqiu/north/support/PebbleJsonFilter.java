package top.xiqiu.north.support;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.escaper.SafeString;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.unbescape.html.HtmlEscape;
import top.xiqiu.north.core.JsonConverter;

import java.util.List;
import java.util.Map;

/**
 * json filter
 * 用法：{{ userInfo | json }}
 */
public class PebbleJsonFilter implements Filter {
    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input == null) {
            return new SafeString("null");
        }

        // 使用 HtmlEscape 转义字符串中非法字符（如 \ " ' 等）
        if (input instanceof String) {
            return new SafeString("\"" + HtmlEscape.escapeHtml4Xml(input.toString()) + "\"");
        }

        return new SafeString(new JsonConverter().stringify(input));
    }

    @Override
    public List<String> getArgumentNames() {
        return null;
    }
}
