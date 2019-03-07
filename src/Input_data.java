import com.mysql.cj.xdevapi.JsonArray;
import com.mysql.cj.xdevapi.JsonString;
//import org.json.JSONArray;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Input_data {
    private double t_start;
    private double t_end;
    private int key;

    public Input_data(int key, double start, double end){
        this.key = key;
        t_start = start;
        t_end = end;
    }

    public double getStart() {
        return t_start;
    }
    public void setStart(double start) {
        t_start = start;
    }

    public static JsonArray getTable(ResultSet rs) throws SQLException {
        JsonArray t_starts = new JsonArray();
        t_starts.add(new JsonString().setValue("ID;Start;End"));
        while (rs.next()) {
            int key = rs.getInt("ID");
            double start = rs.getDouble("Start");
            double end = rs.getDouble("End");
            t_starts.add(new JsonString().setValue(String.format("%s;%s;%s",key, start, end)));
        }
        return t_starts;
    }
}
