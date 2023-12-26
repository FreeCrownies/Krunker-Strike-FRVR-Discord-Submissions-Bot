package mysql;

import com.mysql.cj.jdbc.MysqlDataSource;
import core.MainLogger;
import mysql.interfaces.SQLConsumer;
import mysql.interfaces.SQLFunction;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBMain implements DriverAction {

    private static final DBMain INSTANCE = new DBMain();
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final ArrayList<DBCache> caches = new ArrayList<>();
    private Connection connection = null;

    private DBMain() {
    }

    public static DBMain getInstance() {
        return INSTANCE;
    }

    public static String instantToDateTimeString(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault()));
    }

    public static String localDateToDateString(LocalDate localDate) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
    }

    public static java.util.Date getDateFormat(String string) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LocalDate getLocalDate(String string) {
        return Date.valueOf(string).toLocalDate();
    }

    public void connect() throws SQLException, InterruptedException {
        MysqlDataSource rv = new MysqlDataSource();
        rv.setServerName(System.getenv("DB_HOST"));
        rv.setPortNumber(Integer.parseInt(System.getenv("DB_PORT")));
        rv.setUser(System.getenv("DB_USERNAME"));
        rv.setPassword(System.getenv("DB_PASSWORD"));
        rv.setDatabaseName(System.getenv("DB_DATABASE"));
        rv.setAllowMultiQueries(true);
        rv.setAutoReconnect(true);
        rv.setCharacterEncoding("UTF-8");
        rv.setServerTimezone(TimeZone.getDefault().getID());
        rv.setRewriteBatchedStatements(true);

        MainLogger.get().info("Connecting with database {}", rv.getUrl());
        connection = rv.getConnection();

        SQLSchema.onCreate();
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            MainLogger.get().error("Error when closing database connection", e);
        }
    }

    public void addDBCached(DBCache dbCache) {
        if (!caches.contains(dbCache)) {
            caches.add(dbCache);
        }
    }

    public void invalidateGuildId(long guildId) {
        caches.forEach(c -> c.invalidateGuildId(guildId));
    }

    public Connection getConnection() {
        return connection;
    }

    public <T> T get(String sql, SQLFunction<ResultSet, T> resultSetFunction) throws SQLException, InterruptedException {
        SQLException exception = null;
        for (int i = 0; i < 3; i++) {
            try (Statement statement = getConnection().createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSetFunction.apply(resultSet);
            } catch (SQLException e) {
                // ignore
                exception = e;
                Thread.sleep(5000);
            }
        }

        throw exception;
    }

    public <T> T get(String sql, SQLConsumer<PreparedStatement> preparedStatementConsumer, SQLFunction<ResultSet, T> resultSetFunction) throws SQLException, InterruptedException {
        SQLException exception = null;
        for (int i = 0; i < 3; i++) {
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
                preparedStatementConsumer.accept(preparedStatement);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSetFunction.apply(resultSet);
                }
            } catch (SQLException e) {
                exception = e;
                Thread.sleep(5000);
            }
        }

        throw exception;
    }

    public <T> CompletableFuture<T> asyncGet(String sql, SQLFunction<ResultSet, T> resultSetFunction) {
        CompletableFuture<T> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                T t = get(sql, resultSetFunction);
                future.complete(t);
            } catch (Throwable e) {
                future.completeExceptionally(e);
                MainLogger.get().error("Exception for query: " + sql, e);
            }
        });

        return future;
    }

    public <T> CompletableFuture<T> asyncGet(String sql, SQLConsumer<PreparedStatement> preparedStatementConsumer, SQLFunction<ResultSet, T> resultSetFunction) {
        CompletableFuture<T> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                T t = get(sql, preparedStatementConsumer, resultSetFunction);
                future.complete(t);
            } catch (Throwable e) {
                future.completeExceptionally(e);
                MainLogger.get().error("Exception for query: " + sql, e);
            }
        });

        return future;
    }

    public int update(String sql) throws SQLException, InterruptedException {
        SQLException exception = null;
        for (int i = 0; i < 3; i++) {
            try (Statement statement = getConnection().createStatement()) {
                return statement.executeUpdate(sql);
            } catch (SQLException e) {
                exception = e;
                Thread.sleep(5000);
            }
        }

        throw exception;
    }

    public int update(String sql, SQLConsumer<PreparedStatement> preparedStatementConsumer) throws SQLException, InterruptedException {
        SQLException exception = null;
        for (int i = 0; i < 3; i++) {
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
                preparedStatementConsumer.accept(preparedStatement);
                return preparedStatement.executeUpdate();
            } catch (SQLException e) {
                exception = e;
                Thread.sleep(5000);
            }
        }

        throw exception;
    }

    /**
     * This method interrupts the thread until the query is executed successfully.
     */
    public int execute(String sql, SQLConsumer<PreparedStatement> preparedStatementConsumer) throws SQLException, InterruptedException {
        SQLException exception = null;
        for (int i = 0; i < 3; i++) {
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
                preparedStatementConsumer.accept(preparedStatement);
                if (!preparedStatement.execute()) {
                    // If you need the update count, you can retrieve it here
                    return preparedStatement.getUpdateCount();
                } else {
                    return 0;
                }
            } catch (SQLException e) {
                exception = e;
                Thread.sleep(5000);
            }
        }

        throw exception;
    }

    public CompletableFuture<Integer> asyncUpdate(String sql) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                int n = update(sql);
                future.complete(n);
            } catch (Throwable e) {
                future.completeExceptionally(e);
                MainLogger.get().error("Exception for query: " + sql, e);
            }
        });

        return future;
    }

    public CompletableFuture<Integer> asyncUpdate(String sql, SQLConsumer<PreparedStatement> preparedStatementConsumer) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                int n = update(sql, preparedStatementConsumer);
                future.complete(n);
            } catch (Throwable e) {
                future.completeExceptionally(e);
                MainLogger.get().error("Exception for query: " + sql, e);
            }
        });

        return future;
    }

    public Statement statementExecuted(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
        return statement;
    }

    public PreparedStatement preparedStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    public void deregister() {
        MainLogger.get().info("Driver deregistered");
    }

}
