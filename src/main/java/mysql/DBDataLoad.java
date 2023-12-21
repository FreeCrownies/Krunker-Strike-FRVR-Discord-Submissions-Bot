package mysql;

import mysql.interfaces.SQLConsumer;
import mysql.interfaces.SQLFunction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBDataLoad<T> {

    private final PreparedStatement preparedStatement;

    public DBDataLoad(String table, String requiredAttributes) {
        this(table, requiredAttributes, "1", ps -> {
        });
    }

    public DBDataLoad(String table, String requiredAttributes, String where) {
        this(table, requiredAttributes, where, ps -> {});
    }

    public DBDataLoad(String table, String requiredAttributes, String where, SQLConsumer<PreparedStatement> wherePreparedStatementConsumer) {
        try {
            if (requiredAttributes.isEmpty()) throw new SQLException("No attributes specified!");

            String sqlString = String.format("SELECT %s FROM %s WHERE %s;", requiredAttributes, table, where);

            preparedStatement = DBMain.getInstance().preparedStatement(sqlString);
            wherePreparedStatementConsumer.accept(preparedStatement);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DBDataLoad(String table, String requiredAttributes, String where, String orderBy, OrderBy order, SQLConsumer<PreparedStatement> wherePreparedStatementConsumer) {
        try {
            if (requiredAttributes.isEmpty()) throw new SQLException("No attributes specified!");

            String sqlString = String.format("SELECT %s FROM %s WHERE %s ORDER BY %s %s;", requiredAttributes, table, where, orderBy, order.toString());

            preparedStatement = DBMain.getInstance().preparedStatement(sqlString);
            wherePreparedStatementConsumer.accept(preparedStatement);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> getList(SQLFunction<ResultSet, T> function) {
        try {
            ResultSet resultSet = preparedStatement.getResultSet();
            ArrayList<T> list = new ArrayList<>();

            while (resultSet.next()) {
                try {
                    T value = function.apply(resultSet);
                    if (!Objects.isNull(value)) list.add(value);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            resultSet.close();
            preparedStatement.close();

            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <U> Map<U, T> getMap(SQLFunction<T, U> getKeyFunction, SQLFunction<ResultSet, T> function) {
        try {
            ResultSet resultSet = preparedStatement.getResultSet();
            HashMap<U, T> map = new HashMap<>();

            while (resultSet.next()) {
                try {
                    T value = function.apply(resultSet);
                    if (value != null) map.put(getKeyFunction.apply(value), value);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            resultSet.close();
            preparedStatement.close();

            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <U> Map<U, T> getMap(SQLFunction<T, U> getKeyFunction, SQLFunction<ResultSet, T> function, SQLFunction<T, Boolean> validityFunction) {
        try {
            ResultSet resultSet = preparedStatement.getResultSet();
            HashMap<U, T> map = new HashMap<>();

            while (resultSet.next()) {
                try {
                    T value = function.apply(resultSet);
                    if (value != null && validityFunction.apply(value)) map.put(getKeyFunction.apply(value), value);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            resultSet.close();
            preparedStatement.close();

            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public T getOrDefault(SQLFunction<ResultSet, T> function, DefaultFunction<T> getDefault) {
        T t = null;
        try {
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                try {
                    t = function.apply(resultSet);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    t = getDefault.apply();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return t;
    }

    public enum OrderBy {
        ASC("ASC"),
        DESC("DESC");

        private final String s;

        OrderBy(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    public interface DefaultFunction<T> {
        T apply() throws Throwable;
    }

}