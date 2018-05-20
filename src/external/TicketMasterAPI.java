package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "E75TtykQ7aojeeEHFXFZz2Dqvt9Yy5wO";
	
	/**
	 * Helper methods
	 */

	//  {
	//    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
	//    ...
	//    "_embedded": {
	//	    "venues": [
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embeded")) {
			JSONObject embeded = event.getJSONObject("_embeded");
			
			if (!embeded.isNull("venues")) {
				JSONArray venues = embeded.getJSONArray("venues");
				//++i, i++ ,对于整数浮点数无习惯， c++有分别
				//只关心一个数据不用for循环
				//if (venues.length() > 0) { 但万一第一个为空
				for (int i = 0; i < venues.length(); ++i) {
					
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder sb = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							sb.append(" ");
							sb.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							sb.append(" ");
							sb.append(address.getString("line3"));
						}
					}
					if (!venue.isNull("city")) {
						
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							sb.append(" ");
							sb.append(city.getString("name"));
						}
					}
					if (!sb.toString().equals("")) {
					return sb.toString();	
					}
				}
			}
		}

		return "";
	}


	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			//if (images.length() > 0) {
			for (int i = 0; i < images.length(); i++) {	
				JSONObject image = images.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
				
			}
		}
		return "";
	}

	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classfications")) {
			JSONArray classfications = event.getJSONArray("classfications");
			for (int i = 0; i < classfications.length(); i++) {
				JSONObject classfication = classfications.getJSONObject(i);
				if (!classfication.isNull("segment")) {
					JSONObject segment = classfication.getJSONObject("segment");
					if (!segment.isNull("name")) {
					    String name = segment.getString("name");
						categories.add(name);
					}
					
				}
			}
		}

		return categories;
	}

	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
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
			//categories, imageURL, address(复合信息) 在helper function里
			builder.setCategories(getCategories(event));
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
			
		}

		return itemList;
	}

	public List<Item> search(double lat, double lon, String keyword) {
         if (keyword == null) {
        	 keyword = DEFAULT_KEYWORD;
         }
         
         try {
        	 keyword = java.net.URLEncoder.encode(keyword, "utf8");//使机器处理字符方便不会溢出//UTF-8
         } catch (Exception e) {
        	 e.printStackTrace();
         }
         
         String geohash = GeoHash.encodeGeohash(lat, lon, 8);
         
      // Make your url query part like: "apikey=12345&geoPoint=abcd&keyword=music&radius=50" 拼接url
 		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geohash, keyword, 50);
 		try {
 			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
 			int responseCode = connection.getResponseCode();//发送请求 并获得状态http//1.1.200;debug according to he respone code
 			System.out.println("\nSending 'GET' request to URL:" + URL + "?" + query);
 			System.out.println("Response code: " + responseCode);
 			
 			BufferedReader in = new  BufferedReader (new InputStreamReader(connection.getInputStream()));
 			String inputline;
 			StringBuilder response = new StringBuilder();//分配一次内存， 把所有string拼接起来再返回string
 			//string 在java中immutable 不可变，d多线程时保证不用枷锁 string不会变了stirngbuffer 
 			while((inputline = in.readLine()) != null) {
 				response.append(inputline);
 			}
 			in.close();//or use try, will not forget write close();
 			connection.disconnect();
 			
 			JSONObject obj = new JSONObject(response.toString());
 			//for TicketMaster all data in embedded events, firstly ensure the embedded is not empty
 			if (obj.isNull("_embedded")) {
 				return new ArrayList<>();
 			}
 			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return getItemList(events);

 		} catch (Exception e) {
 			//esception 里要处理问题
 			e.printStackTrace();
 		}
 		return new ArrayList<>();

 	   
     }
	  
	  private void queryAPI(double lat, double lon) {
		  //检测search获取结果是否准确 提供debug方法
			List<Item> events = search(lat, lon, null);
			try {
			   for (Item event : events) {
			        System.out.println(event.toJSONObject());
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	  /**
		 * Main entry for sample TicketMaster API requests.
		 */
		public static void main(String[] args) {
			TicketMasterAPI tmApi = new TicketMasterAPI();
			// Mountain View, CA
			// tmApi.queryAPI(37.38, -122.08);
			// London, UK
			// tmApi.queryAPI(51.503364, -0.12);
			// Houston, TX
			tmApi.queryAPI(29.682684, -95.295410);
		}
		




}
