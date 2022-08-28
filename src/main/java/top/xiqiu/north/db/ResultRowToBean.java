package top.xiqiu.north.db;

import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultRowToBean {

    /**
     * 驼峰字符串 转 下划线字符串
     * userName To user_name
     * user_name To user_name
     *
     * @param camelCaseStr 驼峰字符串
     * @return 下划线字符串
     */
    public static String toUnderlineCase(String camelCaseStr) {
        if (camelCaseStr == null) {
            return null;
        }

        final char[] charArray = camelCaseStr.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] >= 65 && charArray[i] <= 90) {
                // char 操作快捷方法  char variable += int value 依然是 char 类型，故下面 2个 方法作用一致
                // stringBuilder.append("_").append(charArray[i] += 32);
                stringBuilder.append("_").append((char) (charArray[i] + 32));
            } else {
                stringBuilder.append(charArray[i]);
            }
        }

        return stringBuilder.toString();
    }

    public static <T> T process(ResultSet rs, Class<T> requiredType) {
        T row = null;
        try {
            // 实例化T类型
            row = requiredType.getConstructor().newInstance();

            // // 表字段列表
            // ArrayList<String> columns = new ArrayList<>();
            // final ResultSetMetaData metaData = rs.getMetaData();
            // for (int i = 1, columnCount = metaData.getColumnCount(); i <= columnCount; i++) {
            //     columns.add(metaData.getColumnName(i));
            // }

            // 获取 bean 属性字段
            final Field[] fields = requiredType.getDeclaredFields();
            for (Field field : fields) {
                String underlineName = toUnderlineCase(field.getName());

                // 被 transient 修饰，跳过赋值
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                // 数据库字段的值
                Object value = null;

                try {

                    if (field.getType() == Integer.class || field.getType() == int.class) {
                        value = rs.getInt(underlineName);
                    } else if (field.getType() == Double.class || field.getType() == double.class) {
                        value = rs.getDouble(underlineName);
                    } else if (field.getType() == Long.class || field.getType() == long.class) {
                        value = rs.getLong(underlineName);
                    } else if (field.getType() == Date.class) {
                        value = rs.getDate(underlineName);
                    } else if (field.getType() == String.class) {
                        value = rs.getString(underlineName);
                    } else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                        value = rs.getBoolean(underlineName);
                    } else {
                        value = rs.getObject(underlineName);
                    }
                } catch (SQLException e) {
                    LoggerFactory.getLogger(ResultRowToBean.class)
                                 .error("ResultRowToBean: mapping db field {} -> {} cause error = {}",
                                        field.getName(),
                                        underlineName,
                                        e.getLocalizedMessage());
                    continue;
                }

                // 使用 PropertyDescriptor 设置属性值
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), requiredType);
                propertyDescriptor.getWriteMethod().invoke(row, value);
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return row;
    }
}
