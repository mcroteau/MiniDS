package dev;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class Papi implements DataSource {

    Logger log = Logger.getLogger("Papi");

    int cc;

    Properties props;
    private final ThreadLocal<List<Object>> threads;
    List<Object> list;

    public Papi(New config){
        this.idx = 0;
        this.cc = config.c;
        this.props = config.props;
        this.threads = ThreadLocal.withInitial(() -> new ArrayList<>(16));
        this.list = threads.get();
        this.make();
    }

    public void make() {
        try {
            Executable executable = null;
            for (int qzo = 0; qzo < cc; qzo++) {
                executable = new Executable(this);
                executable.run();
            }
            executable.join();
            log.info("papi ready!");
        }catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    protected void addConnection() throws InterruptedException {
        Connection connection = createConnection();
        if(connection != null) {
            idx++;
            if(idx >= (cc - 1)){
                idx = 0;
            }
            list.add(connection);
        }
    }

    int idx;

    @Override
    public Connection getConnection() {
        if(list.size() > 0) {
            new Executable(this).run();
            return (Connection) list.remove(idx);
        }
        return getConnection();
    }

    protected Connection createConnection(){
        Connection connection;
        try{
            connection = DriverManager.getConnection(
                    props.getProperty("url"),
                    props);
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException("Problem connecting to the database", ex);
        }
        return connection;
    }

    public static class Executable extends Thread {

        Papi papi;

        public Executable(Papi papi){
            this.papi = papi;
        }

        @Override
        public void run() {
            try {
                papi.addConnection();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class PapiException extends SQLException{
        public PapiException(String message){
            super(message);
        }
    }


    public static class New {
        int c;
        String ur;
        String u;
        String p;
        String d;

        Properties props;

        public New url(String ur){
            this.ur = ur;
            return this;
        }
        public New connections(int c){
            this.c = c;
            return this;
        }
        public New user(String u){
            this.u = u;
            return this;
        }
        public New password(String p){
            this.p = p;
            return this;
        }
        public New driver(String d){
            this.d = d;
            return this;
        }
        public Papi make(){
            try {
                Class.forName(d);

                props = new Properties();
                props.setProperty("user", u);
                props.setProperty("password", p);
                props.setProperty("url", ur);

            }catch (Exception ex){
                ex.printStackTrace();
            }

            return new Papi(this);
        }
    }




    @Override
    public Connection getConnection(String username, String password) throws PapiException {
        throw new PapiException("this is a simple implementation, use get connection() no parameters");
    }

    @Override
    public PrintWriter getLogWriter() throws PapiException { throw new PapiException("do you reall need a log writer."); }

    @Override
    public void setLogWriter(PrintWriter out) throws PapiException {throw new PapiException("do you really need a log writer."); }

    @Override
    public void setLoginTimeout(int seconds) throws PapiException {throw new PapiException("you might not need a login timeout"); }

    @Override
    public int getLoginTimeout() throws PapiException { throw new PapiException("nah, we don't think you need this.");}

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException("no parent logger here."); }

    @Override
    public <T> T unwrap(Class<T> iface) throws PapiException { throw new PapiException("bare essentials only."); }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws PapiException { throw new PapiException("bare essentials only."); }

}
