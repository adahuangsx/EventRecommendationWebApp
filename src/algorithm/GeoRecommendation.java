package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;



public class GeoRecommendation {
	  public List<Item> recommendItems(String userId, double lat, double lon) {
		  List<Item> recommendedItems = new ArrayList<>();
		  DBConnection conn = DBConnectionFactory.getConnection();
		// Step 1 Get all favorite items
		  Set<String> favoriteItemIDs = conn.getFavoriteItemIds(userId);
		  
		// Step 2 Get all categories of favorite items, sort by count
		  Map<String, Integer> allCategories = new HashMap<>();
		  for (String itemId : favoriteItemIDs) {
			  Set<String> categories = conn.getCategories(itemId);
			  for (String cat : categories) {
//				  if (allCategories.containsKey(cat)) {
//					  allCategories.put(cat, 1);
//				  }
//				  else {
//					  allCategories.put(cat, allCategories.get(cat) + 1);
//				  }
				  allCategories.put(cat, allCategories.getOrDefault(cat, 0) + 1);
			  }
		  }
		  List<Entry<String, Integer>> categoriesList = new ArrayList<>(allCategories.entrySet());
		  Collections.sort(categoriesList, new Comparator<Entry<String, Integer>>() {
			  @Override
			  public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				  return Integer.compare(e2.getValue(), e1.getValue());
			  }
		  });
		  
		// Step 3, do search based on category, filter out favorited events, sort by distance
		  Set<Item> visitedItems = new HashSet<>();
		  for (Entry<String, Integer> category : categoriesList) {
			  List<Item> items = conn.searchItems(lat, lon, category.getKey());
			  List<Item> filteredItems = new ArrayList<>();
			  for (Item item : items) {
				  if (!favoriteItemIDs.contains(item.getItemId()) &&
						  ! visitedItems.contains(item)) {
					  filteredItems.add(item);
				  }
			  }
			  Collections.sort(filteredItems, new Comparator<Item>() {
				  @Override
				  public int compare (Item i1, Item i2) {
					  return Double.compare(i1.getDistance(), i2.getDistance());
				  }
			  });
			  visitedItems.addAll(items);
			  recommendedItems.addAll(filteredItems);
		  }
		  return recommendedItems;
	  }
}
