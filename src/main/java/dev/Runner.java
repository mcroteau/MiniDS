package dev;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.h2.tools.RunScript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public class Runner {

    private static Logger Log = Logger.getLogger("Runner");

    public static void main(String[] arguments) throws FileNotFoundException, SQLException, InterruptedException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Path uri = Paths.get("src", "main", "resources", "create-db.sql");
        File createFile = new File(uri.toAbsolutePath().toString());

//        BasicDataSource datasource = new BasicDataSource.Builder().build();


        Papi datasource = new Papi.New()
                                .connections(159)
                                .driver("org.h2.Driver")
                                .url("jdbc:h2:~/.papi")
                                .user("sa")
                                .password("")
                                .make();

//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl("jdbc:h2:~/.papi");
//        config.setUsername("sa");
//        config.setPassword("");
//        config.setMaximumPoolSize(159);
//        config.setAutoCommit(false);
//        config.setDriverClassName("org.h2.Driver");
//        HikariDataSource datasource = new HikariDataSource(config);

//        Properties props = new Properties();
//        props.setProperty("driverClassName", "org.h2.Driver");
//        props.setProperty("user", "sa");
//        props.setProperty("password", "");
//
//        ConnectionFactory connectionFactory =
//                new DriverManagerConnectionFactory("jdbc:h2:~/.papi",props);
//
//        PoolableConnectionFactory poolableConnectionFactory =
//                new PoolableConnectionFactory(connectionFactory, null);
//
//
//        ObjectPool<PoolableConnection> connectionPool =
//                new GenericObjectPool<>(poolableConnectionFactory);
//
//        poolableConnectionFactory.setPool(connectionPool);
//        PoolingDataSource<PoolableConnection> datasource =
//                new PoolingDataSource<>(connectionPool);

        Connection conn = datasource.getConnection();
        RunScript.execute(conn, new FileReader(createFile));
        conn.commit();
        conn.close();

        long start = System.currentTimeMillis();

        int total = 0;
        for (int Q = 0; Q < Integer.valueOf(arguments[0]); Q++) {
            try {
                String sql = "insert into todos (title) values ('Todo " + Q + "')";
                Connection connection = datasource.getConnection();
                Statement stmt = connection.createStatement();
                stmt.execute(sql);

//                Connection connection = datasource.getConnection();
//                String sql = "select count(*) from todos";
//                Statement stmt = connection.createStatement();
//                ResultSet rs = stmt.executeQuery(sql);
//                if(rs.next()){
//                     Long.parseLong(rs.getObject(1).toString());
//                }

                connection.commit();
                connection.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            total++;
        }

        long end = System.currentTimeMillis();
        long diff = end - start;

        Log.info("processed " + total + " in " + diff);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            String dbPath = System.getProperty("user.home") + File.separator + ".papi";
            File dbMvFile = new File(dbPath + ".mv.db");
            if (dbMvFile.exists()) {
                dbMvFile.delete();
            }
            File dbTraceFile = new File(dbPath + ".trace.db");
            if (dbTraceFile.exists()) {
                dbTraceFile.delete();
            }
        }));
        Log.info("shutting down...");

    }

}
