package top.xiqiu.north.db;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class NorthNonePooledDataSource implements DataSource {
    /**
     * jdbc 驱动
     */
    private Driver driverInstance;

    private String driver;
    private String url;
    private String username;
    private String password;

    private Boolean autoCommit;
    private Integer defaultTransactionIsolationLevel;
    private Integer defaultNetworkTimeout;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setDefaultNetworkTimeout(Integer defaultNetworkTimeout) {
        this.defaultNetworkTimeout = defaultNetworkTimeout;
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
    }

    public NorthNonePooledDataSource() {
    }

    public NorthNonePooledDataSource(String driver, String url, String username, String password) {
        this.driver   = driver;
        this.url      = url;
        this.username = username;
        this.password = password;
    }

    private synchronized void initializeDriver() throws SQLException {
        if (driverInstance == null) {
            try {
                Class<?> driverType = Class.forName(this.driver);

                driverInstance = (Driver) driverType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new SQLException("Error setting driver on NorthNonePooledDataSource. Cause: " + e);
            }
        }
    }

    /**
     * Get connection
     */
    private Connection doGetConnection(String username, String password) throws SQLException {
        initializeDriver();
        Connection connection = DriverManager.getConnection(url, username, password);
        configureConnection(connection);

        return connection;
    }

    /**
     * Set config Connection
     */
    private void configureConnection(Connection connection) throws SQLException {
        if (defaultNetworkTimeout != null) {
            connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), defaultNetworkTimeout);
        }

        if (autoCommit != null && autoCommit != connection.getAutoCommit()) {
            connection.setAutoCommit(autoCommit);
        }

        if (defaultTransactionIsolationLevel != null) {
            connection.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
    }

    /**
     * <p>Attempts to establish a connection with the data source that
     * this {@code DataSource} object represents.
     *
     * @return a connection to the data source
     * @throws SQLException        if a database access error occurs
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value specified by the {@code setLoginTimeout} method
     *                             has been exceeded and has at least tried to cancel the
     *                             current database connection attempt
     */
    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    /**
     * <p>Attempts to establish a connection with the data source that
     * this {@code DataSource} object represents.
     *
     * @param username the database user on whose behalf the connection is
     *                 being made
     * @param password the user's password
     * @return a connection to the data source
     * @throws SQLException        if a database access error occurs
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value specified by the {@code setLoginTimeout} method
     *                             has been exceeded and has at least tried to cancel the
     *                             current database connection attempt
     * @since 1.4
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    /**
     * {@inheritDoc}
     *
     * @param out
     * @since 1.4
     */
    @Override
    public void setLogWriter(PrintWriter out) {
        DriverManager.setLogWriter(out);
    }

    /**
     * {@inheritDoc}
     *
     * @param seconds
     * @since 1.4
     */
    @Override
    public void setLoginTimeout(int seconds) {
        DriverManager.setLoginTimeout(seconds);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    /**
     * Return the parent Logger of all the Loggers used by this data source. This
     * should be the Logger farthest from the root Logger that is
     * still an ancestor of all of the Loggers used by this data source. Configuring
     * this Logger will affect all of the log messages generated by the data source.
     * In the worst case, this may be the root Logger.
     *
     * @return the parent Logger for this data source
     * @since 1.7
     */
    @Override
    public Logger getParentLogger() {
        // requires JDK version 1.6
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * <p>
     * If the receiver implements the interface then the result is the receiver
     * or a proxy for the receiver. If the receiver is a wrapper
     * and the wrapped object implements the interface then the result is the
     * wrapped object or a proxy for the wrapped object. Otherwise return the
     * the result of calling <code>unwrap</code> recursively on the wrapped object
     * or a proxy for that result. If the receiver is not a
     * wrapper and does not implement the interface, then an <code>SQLException</code> is thrown.
     *
     * @param iface A Class defining an interface that the result must implement.
     * @return an object that implements the interface. May be a proxy for the actual implementing object.
     * @throws SQLException If no object found that implements the interface
     * @since 1.6
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    /**
     * Returns true if this either implements the interface argument or is directly or indirectly a wrapper
     * for an object that does. Returns false otherwise. If this implements the interface then return true,
     * else if this is a wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped
     * object. If this does not implement the interface and is not a wrapper, return false.
     * This method should be implemented as a low-cost operation compared to <code>unwrap</code> so that
     * callers can use this method to avoid expensive <code>unwrap</code> calls that may fail. If this method
     * returns true then calling <code>unwrap</code> with the same argument should succeed.
     *
     * @param iface a Class defining an interface.
     * @return true if this implements the interface or directly or indirectly wraps an object that does.
     * @since 1.6
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
