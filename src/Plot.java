import com.mysql.cj.xdevapi.JsonArray;
import com.mysql.cj.xdevapi.JsonString;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Plot
{
    private double[] x;
    private double[] y;

    public Plot(double[] x,double y[])
    {
        this.x=x;
        this.y=y;
    }

    public double[] getX() {
        return x;
    }
    public void setX(double[] x) {
        this.x=x;
    }

    public double[] getY() {
        return y;
    }
    public void setY(double[] y) {
        this.y=y;
    }

    public static JsonArray getTable(ResultSet rs) throws SQLException {
        JsonArray t_starts = new JsonArray();
        t_starts.add(new JsonString().setValue("Start;End"));
        while (rs.next()) {
            double start = rs.getDouble("Start");
            double end = rs.getDouble("End");
            t_starts.add(new JsonString().setValue(String.format("%s;%s",start, end)));
        }
        return t_starts;
    }
}
