package com.gynt.webscrape;

import java.util.HashMap;

public class Dictionary {
	
	public static Dictionary SINGLETON = load(String.join("\n", "query=_query"
			, "id=_id"
			, "data=_data"
			, "array=array"
			, "loop=_loop"
			, ""));

	private static final String sep = "[ ]+[=]+[ ]+";
	
	private HashMap<String, String> map;

	public static final Dictionary load(String data) {
		Dictionary result = new Dictionary();
		result.map = toMap(data);
		return result;
	}

	private static final HashMap<String, String> toMap(String data) {
		HashMap<String, String> result = new HashMap<String, String>();
		String[] datas = data.split("[\n\r]+");
		for(String d : datas) {
			String[] dparts = d.split(sep);
			String key = dparts[0].trim();
			String value = dparts[1].trim();
			result.put(key, value);
		}
		return result;
	}
	
	String query() {
		return map.get("query");
	}
	
	String array() {
		return map.get("array");
	}
	
	String id() {
		return map.get("id");
	}
	
	String data() {
		return map.get("data");
	}
	
	String loop() {
		return map.get("loop");
	}

}
