package rpc;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	public static void writeJSONObject(HttpServletResponse response, JSONObject obj) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(obj);
		out.close();
	}
	
	public static void writeJSONArray(HttpServletResponse response, JSONArray array) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(array);
		out.close();
	}
	
}
