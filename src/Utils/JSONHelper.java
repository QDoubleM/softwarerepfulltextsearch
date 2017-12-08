package Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class JSONHelper {
	public static void ResponseList( @SuppressWarnings("rawtypes") List list,HttpServletResponse response) throws IOException {
		   response.setContentType("text/html;charset=utf-8");
		   PrintWriter out = response.getWriter();
		   out.print(new Gson().toJson(list));
		   out.close();
	}
}
