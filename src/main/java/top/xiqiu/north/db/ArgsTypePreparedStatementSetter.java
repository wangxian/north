package top.xiqiu.north.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 预处理SQL参数绑定
 */
public class ArgsTypePreparedStatementSetter implements PreparedStatementSetter {
    private Object[] args;
    private int[] argTypes;

    public ArgsTypePreparedStatementSetter(Object[] args, int[] argTypes) {
        if (args != null && argTypes != null
                && args.length > 0 & argTypes.length > 0
                && args.length > argTypes.length) {
            throw new RuntimeException("ArgsTypePreparedStatementSetter: args and argTypes parameters must match");
        }

        this.args     = args;
        this.argTypes = argTypes;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        // 指定绑定参数的类型，类型的枚举值定义在 java.sql.Types，类似：Types.VARCHAR
        if (args != null && args.length > 0 && argTypes != null && argTypes.length > 0) {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i], argTypes[i]);
            }
        } else if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
        }
    }
}
