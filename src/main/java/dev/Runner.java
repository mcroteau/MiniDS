package dev;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
import java.util.logging.Logger;

public class Runner {

    private static Logger Log = Logger.getLogger("Runner");

    public static void main(String[] arguments) throws FileNotFoundException, SQLException, InterruptedException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Path uri = Paths.get("src", "main", "resources", "create-db.sql");
        File createFile = new File(uri.toAbsolutePath().toString());

//        BasicDataSource dataSource = new BasicDataSource.Builder().build();

//        MiniDS datasource = new MiniDS.Builder()
//                .withConnections(159)
//                .withDriver("org.h2.Driver")
//                .withUrl("jdbc:h2:~/.miniDS")
//                .withUser("sa")
//                .withPassword("")
//                .make();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:~/.miniDs");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(159);
        config.setAutoCommit(false);
        config.setDriverClassName("org.h2.Driver");
        HikariDataSource datasource = new HikariDataSource(config);


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
            String dbPath = System.getProperty("user.home") + File.separator + ".miniDS";
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
