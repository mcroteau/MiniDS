package dev;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class MiniDS implements DataSource {

    Logger log = Logger.getLogger("MiniDS");

    int connections;
    Properties props;
    Queue<Connection> available;

    public MiniDS(Builder builder){
        this.props = builder.props;
        this.connections = builder.connections;
        this.available = new LinkedBlockingDeque<>();
        this.make();
    }

    public void make() {
        try {
            Executable executable = null;
            for (int qzo = 0; qzo < connections; qzo++) {
                executable = new Executable(this);
                executable.run();
            }
            executable.join();
            log.info("miniDS ready!");
        }catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    protected void addConnection() throws InterruptedException {
        Connection connection = createConnection();
        if(connection != null)
            available.add(connection);
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

    @Override
    public Connection getConnection() {
        if(available.peek() != null) {
            new Executable(this).run();
            return available.poll();
        }
        return getConnection();
    }

    public static class Executable extends Thread {

        MiniDS miniDS;

        public Executable(MiniDS miniDS){
            this.miniDS = miniDS;
        }

        @Override
        public void run() {
            try {
                miniDS.addConnection();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class MiniDSException extends SQLException{
        public MiniDSException(String message){
            super(message);
        }
    }

    public static class Builder{

        int connections;

        String url;
        String user;
        String password;
        String driver;

        Properties props;

        public Builder withUrl(String url){
            this.url = url;
            return this;
        }
        public Builder withConnections(int connections){
            this.connections = connections;
            return this;
        }
        public Builder withUser(String dbUser){
            this.user = dbUser;
            return this;
        }
        public Builder withPassword(String password){
            this.password = password;
            return this;
        }
        public Builder withDriver(String driver){
            this.driver = driver;
            return this;
        }
        public MiniDS make() {
            try{
                Class.forName(driver);

                props = new Properties();
                props.setProperty("user", user);
                props.setProperty("password", password);
                props.setProperty("url", url);

            }catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            return new MiniDS(this);
        }
    }


    @Override
    public Connection getConnection(String username, String password) throws MiniDSException {
        throw new MiniDSException("this is a simple implementation, use get connection() no parameters");
    }

    @Override
    public PrintWriter getLogWriter() throws MiniDSException { throw new MiniDSException("do you reall need a log writer."); }

    @Override
    public void setLogWriter(PrintWriter out) throws MiniDSException {throw new MiniDSException("do you really need a log writer."); }

    @Override
    public void setLoginTimeout(int seconds) throws MiniDSException {throw new MiniDSException("you might not need a login timeout"); }

    @Override
    public int getLoginTimeout() throws MiniDSException { throw new MiniDSException("nah, we don't think you need this.");}

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException("no parent logger here."); }

    @Override
    public <T> T unwrap(Class<T> iface) throws MiniDSException { throw new MiniDSException("bare essentials only."); }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws MiniDSException { throw new MiniDSException("bare essentials only."); }

}
