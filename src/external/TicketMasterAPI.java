package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	/*
	 * Macro definition:
	 */
	private static final String API_KEY = "BrBaVF7b6g44r2YDkaiGIY3ISI7hAZnS";
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final int GEO_PRECISION = 8;
	private static final String RADIUS = "50";
	private static final int SUCCESS_CODE = 200;
	
	/**
	* Helper methods used to extract information needed in Item from JsonObject
	*/
	// {
	// "name": "laioffer",
	// "id": "12345",
	// "url": "www.laioffer.com",
	// ...
	// "_embedded": {
	// 		"venues": [
	// 		{
	// 			"address": {
	// 				"line1": "101 First St,",
	// 				"line2": "Suite 101",
	// 				"line3": "...",
	// 			},
	// 			"city": {
	// 				"name": "San Francisco"
	// 			}
	// 			...
	// 		},
	// 		...
	// 	  ]
	// }
	// ...
	// }
	/*
	 * execute the search based on the given position(by lat and lon) and keyword
	 * through calling API provided by TicketMaster
	 * return a Json array
	 */
	
	/*
	 * generate the address in the form of string from the json object
	 */
	private static String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				if (venues.length() > 0) {
					JSONObject venue = venues.getJSONObject(0);
					StringBuilder sb = new StringBuilder();
					String[] lines = {"line1", "line2", "line3"};
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						for (String lineStr : lines) {
							if (!address.isNull(lineStr)) {
								sb.append(address.getString(lineStr));
								sb.append(" ");
							}
						}
					}
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							sb.append(city.getString("name"));
						}
					}
					return sb.toString();
				}
			}
		}
		return "";
	}
	
	/*
	 * generate the image url in the form of string from the json object
	 */
	private static String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			for (int i = 0; i < images.length(); i++) {
				JSONObject image = images.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}
	
	/*
	 * generate the  in the form of string from the json object
	 */
	private static Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		
		return categories;
	}
	
	// Convert JSONArray to a list of item objects.
	private static List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			// call three helper functions to set up the three complex attributes
			builder.setAddress(getAddress(event));
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageUrl(event));
			// set up the naive attributes
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			// add a new item to the list
			itemList.add(builder.build());
		}
		return itemList;
	}
	
	
	public static List<Item> search(double lat, double lon, String keyword) {
		// set up the keyword
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;	
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// construct the query URL
		// set up the geo hash string based on lat and lon
		String geoStr = GeoHash.encodeGeohash(lat, lon, GEO_PRECISION);
		String query = String.format("%s?apikey=%s&keyword=%s&geoPoint=%s&radius=%s", URL, API_KEY, keyword, geoStr, RADIUS);
		System.out.println(query);
		// execute the connection based on the full query URL
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(query).openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
			System.out.println(responseCode);
			if (responseCode == SUCCESS_CODE) {
				// the connection is success case
				// get the response as a Json object
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				// The line above can be trapped in a try(...) {} 
				//     and save the "in.close()"
				String inputLine;
				StringBuilder sb =  new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
				in.close();
				// according to https://developer.ticketmaster.com/products-and-docs/apis/discovery-api/v2/
				// responseJson contain _links, _embedded, page
				JSONObject responseJson = new JSONObject(sb.toString());
				// extract the field "_embedded"(a Json object) from the response
				// according to https://developer.ticketmaster.com/products-and-docs/apis/discovery-api/v2/
				// embedded contains events (an Json Array)
				JSONObject embedded = responseJson.optJSONObject("_embedded");
				if (embedded == null) {
					// empty response case
					return new ArrayList<Item>();
				}
				return getItemList(embedded.getJSONArray("events"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// will reach here only when exception
		return new ArrayList<Item>();
	}

	/*
	 * a print function to show JSON array returned from TicketMaster for debugging
	 */
	private static void queryAPI(double lat, double lon) {
		List<Item> results = search(lat, lon, null);
		for (int i = 0; i < results.size(); i++) {
			Item result = results.get(i);
			System.out.println(result.toJSONObject());
		}
	}
	
	
	public static void main(String args[]) {
		// call the queryAPI on some position for debugging
		// Mountain View, CA
		queryAPI(37.38, -122.08);
	}
	

}
