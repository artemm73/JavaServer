import com.mysql.cj.xdevapi.JsonArray;
import com.mysql.cj.xdevapi.JsonString;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class User{
    private String login, fName, lName, email;
    public User(String login, String fName, String lName, String email){
        this.login = login;
        this.fName = fName;
        this.lName = lName;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getfName() {
        return fName;
    }
    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }
    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public static JsonArray getTable(ResultSet rs) throws SQLException {
        JsonArray users = new JsonArray();
        users.add(new JsonString().setValue("Login;FirstName;LastName;Email"));
        while (rs.next()) {
            String login = rs.getString("Login");
            String fName = rs.getString("FirstName");
            String lName = rs.getString("LastName");
            String email = rs.getString("Email");
            users.add(new JsonString().setValue(String.format("%s;%s;%s;%s", login, fName, lName, email)));
        }
        return users;
    }
}