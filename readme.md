<img src="https://png.pngtree.com/element_our/20200630/ourlarge/pngtree-old-man-vector-icon-material-image_2275474.jpg" width="90px"/>

# Papi

A very basic connection pooled datasource.

### 1000 inserts

| Who      | millis |
| ----------- | ----------- |
| Papi      | 715       |
| HikariCP  | 271        |
| ApacheCP  | 426
| Basic     | 133,384    |

Not as fast as the other guys, but reliable and basic! 
The code is simple, it's ridiculous! Come on 'Papi'!

#### Copy Code

```

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

public class Papi {

    Logger Log = Logger.getLogger("Papi");

    int cc;

    Properties props;
    Queue<Object> queue;

    public Papi(New config){
        this.cc = config.c;
        this.props = config.props;
        this.queue = new LinkedBlockingDeque<>();
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
            Log.info("papi ready!");
        }catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    protected void addConnection() throws InterruptedException {
        Connection connection = createConnection();
        if(connection != null) {
            queue.add(connection);
        }
    }

    public Connection getConnection() {
        if(queue.peek() != null) {
            new Executable(this).run();
            return (Connection) queue.poll();
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

    public static class PapiException extends SQLException{
        public PapiException(String message){
            super(message);
        }
    }

    public Connection getConnection(String username, String password) throws PapiException {
        throw new PapiException("this is a simple implementation, use get connection() no parameters");
    }

}
```

### Then to initialize

```
Papi datasource = new Papi.New()
        .connections(numberOfConnections)
        .driver("org.h2.Driver")
        .url("jdbc:h2:~/.papi")
        .user("sa")
        .password("")
        .make();

Connection connection = datasource.createConnection();

```

