	package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class itemHistory
 */
@WebServlet("/history")
public class itemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public itemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id"); 
		// Any differences from doPost?
		try {
			JSONArray array = new JSONArray();
			DBConnection conn = DBConnectionFactory.getConnection();
			Set<Item> items = conn.getFavoriteItems(userId);
			
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.append("favorite", true);
				array.put(obj);
			}
			RpcHelper.writeJSONArray(response, array);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");
			JSONArray favorites = input.getJSONArray("favorite");
			List<String> favoriteList = new ArrayList<>();
			for (int i = 0; i < favorites.length(); i++) {
				favoriteList.add(favorites.get(i).toString());
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.setFavoriteItems(userId, favoriteList);
			conn.close();
			
			RpcHelper.writeJSONObject(response, new JSONObject().put("result", "success"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");
			JSONArray favorites = input.getJSONArray("favorite");
			List<String> favoriteList = new ArrayList<>();
			for (int i = 0; i < favorites.length(); i++) {
				favoriteList.add(favorites.get(i).toString());
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.unsetFavoriteItems(userId, favoriteList);
			conn.close();
			
			RpcHelper.writeJSONObject(response, new JSONObject().put("result", "success"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
