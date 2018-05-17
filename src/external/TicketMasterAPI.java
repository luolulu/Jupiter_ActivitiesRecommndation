package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "E75TtykQ7aojeeEHFXFZz2Dqvt9Yy5wO";
	
	public JSONArray search(double lat, double lon, String keyword) {
         if (keyword == null) {
        	 keyword = DEFAULT_KEYWORD;
         }
         
         try {
        	 keyword = java.net.URLEncoder.encode(keyword, "UIF-8");//使机器处理字符方便不会溢出
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
 				return new JSONArray();
 			}
 			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return events;

 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new JSONArray();

 	   
     }
	  
	  private void queryAPI(double lat, double lon) {
		  //检测search获取结果是否准确 提供debug方法
			JSONArray events = search(lat, lon, null);
			try {
			    for (int i = 0; i < events.length(); i++) {
			        JSONObject event = events.getJSONObject(i);
			        System.out.println(event);
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
