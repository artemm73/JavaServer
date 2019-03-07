import com.mysql.cj.xdevapi.JsonArray;
import com.mysql.cj.xdevapi.JsonString;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Calculation {
    private double x;
    private double y;
    private int key;
    private int dimentional;
    private double error;
    private String comments;



    public static JsonArray getTable(ResultSet rs) throws SQLException {
        JsonArray data = new JsonArray();
        data.add(new JsonString().setValue("ID1;Dimentional;X;Y;Comments;Error"));
        while (rs.next()) {
            double x = rs.getDouble("X");
            double y = rs.getDouble("Y");
            int key = rs.getInt("ID1");
            int dimentional = rs.getInt("Dimentional");
            double error = rs.getDouble("Error");
            String comments = rs.getString("Comments");
            data.add(new JsonString().setValue(String.format("%s;%s;%s;%s;%s;%s",key, dimentional, x, y, comments, error)));
        }
        return data;
    }
}
