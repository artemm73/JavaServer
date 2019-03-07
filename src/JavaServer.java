import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mysql.cj.xdevapi.JsonArray;
import java.sql.Connection;

class JavaServer {
    public static void main(String args[]) {
        System.out.println("Waiting connection on port 8081...");
        new Server();
    }
}

class Server {
    private ServerSocket server = null;
    private Socket client = null;
    public static int  numberOfOnline = 0;
    public Server() {
        new Database();
        try {
            try {
                server = new ServerSocket(8081);
                System.out.println("Waiting...");
                while (true) {
                    client = server.accept();
                    numberOfOnline++;
                    Runnable r = new ThreadEchoHandler(client);
                    Thread t =  new Thread(r);
                    t.start();
                }
            }
            finally {
                client.close();
                server.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}

class Database{
    private static String url = "jdbc:sqlserver://localhost:1433;databaseName=Project;Trusted_Connection=Yes;";
    private static String user = "root";
    private static String password = "root";
    private static Statement statement = null;
    private static Connection connection = null;
    private static MailServer mailServer;

    public Database(){
        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url,user,password);
            statement = connection.createStatement();
            mailServer = new MailServer();
            /*
            ResultSet rs = statement.executeQuery("SELECT * FROM StartData");
            while (rs.next()) {
                double start = rs.getDouble("Start");
                double end = rs.getDouble("End");
                System.out.println(String.format("%s;%s", start, end));
            }
            */
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Statement getStatement() {
        return statement;
    }

    public static Connection getConnection(){
        return connection;
   }

   public static MailServer getMailConnection(){
        return mailServer;
    }

    public static String getUser(){
        return user;
   }

    public static String getPassword(){
        return password;
   }

    public static String getUrl(){
        return url;
    }
}

class ThreadEchoHandler implements Runnable {
    private Socket client = null;
    private boolean stoped;
    public ThreadEchoHandler(Socket socket) {
        client = socket;

    }
    public void setStop() {
        stoped = true;
    }
    public void run() {
        try {
            InputStream inStream = client.getInputStream();
            BufferedReader inputLine = new BufferedReader(new InputStreamReader(inStream));
            OutputStream outStream = client.getOutputStream();
            PrintWriter out = new PrintWriter(outStream, true);
            BufferedReader bRead = new BufferedReader(new InputStreamReader(System.in));
            String fromClient;
            String toClient;
            JsonArray result;

            while ((fromClient = inputLine.readLine()) != null) {

                toClient = "";
                result = null;
                System.out.println("received: " + fromClient);
                String[] command = fromClient.split("\\s");
                switch (command[0]) {
                    case "-get":
                        result = Command.getTable(command[1], Database.getStatement());
                        break;
                    case "-remove":
                        Command.removeRow(command[1], command[2], Database.getConnection());
                        break;
                    case "-exit":
                        System.out.println("exit");
                        client.close();
                        Server.numberOfOnline--;
                        return;
                    case "-backup":
                        Command.backup(command[1], Database.getConnection());
                        break;
                    case "-drop":
                        Command.drop(Database.getConnection());
                        break;
                    case "-restore":
                        Command.restore(command[1], Database.getConnection());
                        break;
                    case "-check":
                        try {
                           toClient = Command.check(command[1],command[2], Database.getConnection());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "-registration":
                        try {
                            toClient = Command.insertUser(command[1],command[2], command[3], command[4],  Database.getConnection(),Database.getMailConnection());

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "-insert":
                        Command.insert(command[1], command[2], Database.getConnection());
                        break;
                    case "-privilege":
                        try {
                            toClient = String.format("%d", Command.getPrivilege(command[1], Database.getConnection()));
                        } catch (SQLException e) {
                            toClient = "error";
                        }
                        break;
                    case "-update":
                        Command.update(command[1], command[2], Database.getConnection());
                        break;
                    case "-plot":
                        result = Command.Plot(Database.getConnection(),Database.getStatement());
                }

                if (!(result == null)) {
                    toClient = result.toString();
                }
                System.out.println("send" + toClient);
                out.println(toClient);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class Command{
    public static void removeRow(String table, String data, Connection connection) {
        CallableStatement stmt = null;
        try {
            switch (table) {
                case "Пользователи":
                    stmt = connection.prepareCall("Exec remove_user ?");
                    stmt.setString(1, data);
                    break;
                case "Начальные_данные":
                    stmt = connection.prepareCall("Exec remove_data ?,?");
                    String[] str = data.split(";");
                    stmt.setString(1, str[0]);
                    stmt.setString(2, str[1]);
                    break;
                /*case "Снаряжение":
                    stmt = connection.prepareCall("{call remove_stuff(?)}");
                    break;
                case "Логи":
                    stmt = connection.prepareCall("{call remove_log(?)}");
                    break;*/
            }
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static JsonArray getTable(String table, Statement stmt) {
        JsonArray result = null;
          try {
          switch (table) {
              case "Пользователи":
                    result = User.getTable(stmt.executeQuery("SELECT * FROM Users"));
                    break;
              case "Начальные_данные":
                  result = Input_data.getTable(stmt.executeQuery("SELECT * FROM StartData"));
                  break;
              case "Условия":
                  result = Calculation.getTable(stmt.executeQuery("SELECT * FROM Condition1"));
                  break;

            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    public static void backup(String path, Connection connection){
        try {
            Statement stmt = connection.createStatement();
            String strSql = "BACKUP DATABASE Project TO DISK = '" + path + "'";
            stmt.execute(strSql);
            System.out.println("Backup created successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void drop(Connection connection){
        try {
            Statement stmt = connection.createStatement();
            String strSql = "USE master; DROP DATABASE Project";
            stmt.execute(strSql);
            System.out.println("Database drop successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void restore(String path, Connection connection){
        try {
            Statement stmt = connection.createStatement();
            String strSql = "RESTORE DATABASE Project FROM DISK = '" + path + "'";
            stmt.execute(strSql);
            System.out.println("Database restore successfully");
            strSql = "Use Project;";
            stmt.execute(strSql);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int getPrivilege(String username, Connection connection) throws SQLException{
        CallableStatement cStmt = connection.prepareCall("DECLARE @result int; Exec get_privilege ?,?");
        cStmt.registerOutParameter(2, Types.INTEGER);
        cStmt.setString(1, username);
        cStmt.execute();
        return cStmt.getInt(2);
    }

    public static String check(String login, String password, Connection connection) throws SQLException{
        CallableStatement cStmt = connection.prepareCall("DECLARE @result BIT; Exec check_user_password ?,?,?");
        cStmt.setString(1, login);
        cStmt.setString(2, password);
        cStmt.registerOutParameter(3, Types.BIT);
        cStmt.execute();
        if (cStmt.getBoolean(3)) {
            return "1";
        }
        else {
            return "";
        }
    }

    public static boolean checkExistsUser (String login, Connection connection) throws SQLException {
        CallableStatement cStmt = connection.prepareCall("DECLARE @result BIT; Exec check_user ?,?");
        cStmt.setString(1, login);
        cStmt.registerOutParameter(2, Types.BIT);
        cStmt.execute();
        if (cStmt.getBoolean(2))
            return true;
        return false;
    }

    public static String insertUser(String login, String password, String email,  String fName, Connection connection, MailServer mailServer) throws SQLException{
        String validExpression = "^[\\w][\\w\\d]{2,16}$";
        Pattern compare = Pattern.compile(validExpression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compare.matcher(login);
        String errorMessage = "";
        if(!matcher.matches()) {
            errorMessage += "bad login\n";
        }
        try {
            if (checkExistsUser(login, connection))
                errorMessage += "bad login\n";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        matcher = compare.matcher(fName);
        if(!matcher.matches()) {
            errorMessage += "bad character name\n";
        }
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (AddressException ex) {
            errorMessage += "bad email: " + ex.getMessage() + "\n";
        }

        if (!errorMessage.isEmpty())
            return errorMessage;
        CallableStatement cStmt = connection.prepareCall("Exec insert_user ?,?,?,?");
        cStmt.setString(1, login);
        cStmt.setString(2, password);
        cStmt.setString(3, email);
        cStmt.setString(4, fName);
        cStmt.execute();

        //Send Mail with login and password
        mailServer.sendEmail(login,password,email,fName);
        return "";
    }

    public static void insert(String tableName, String data, Connection connection){
        CallableStatement stmt = null;
        String[] str = data.split(";");
        try {
            switch (tableName) {
                case "Пользователи":
                    stmt = connection.prepareCall("Exec insert_user ?,?,?,?");
                    stmt.setString(1, str[0]);
                    stmt.setString(2, str[1]);
                    stmt.setString(3, str[2]);
                    stmt.setString(4, str[3]);
                    break;
                case "Начальные_данные":
                    stmt = connection.prepareCall("Exec insert_data ?,?");
                    stmt.setDouble(1, Double.parseDouble(str[0]));
                    stmt.setDouble(2, Double.parseDouble(str[1]));
                    break;
            }
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(String tableName, String data, Connection connection){
        CallableStatement stmt = null;
        String[] str = data.split(";");
        try {
            switch (tableName) {
                case "Пользователи":
                    stmt = connection.prepareCall("Exec update_user ?,?,?,?");
                    stmt.setString(1, str[0]);
                    stmt.setString(2, str[1]);
                    stmt.setString(3, str[2]);
                    stmt.setString(4, str[3]);
                    break;
                case "Начальные_данные":
                    stmt = connection.prepareCall("Exec update_data ?,?,?");
                    stmt.setInt(1, Integer.parseInt(str[0]));
                    stmt.setDouble(2, Double.parseDouble(str[1]));
                    stmt.setDouble(3, Double.parseDouble(str[2]));
                    break;
            }
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static JsonArray Plot(Connection connection, Statement statement)
    {
        JsonArray result = null;
        try
        {
            result = Plot.getTable(statement.executeQuery("SELECT * FROM StartData"));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return  result;
    }
}




