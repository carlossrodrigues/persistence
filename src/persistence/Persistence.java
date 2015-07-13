package persistence;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class Persistence {

	private JSONObject persistence;
	private EntityManager manager;

	public Persistence(){
		this.persistence = readPersistence();
		manager = new EntityManager(persistence);
	}
	public Persistence(String servidor, String database, String user, String pass) throws JSONException{
		this.persistence = new JSONObject();

		this.persistence.put(
				"database_config",
				new JSONObject()
				.put("server", servidor)
				.put("database", database)
				.put("user", user)
				.put("pass", pass));

		this.persistence.put("class_map", new JSONObject());

		manager = new EntityManager(persistence);
	}

	public <T extends Entity> void map(Class<T> clazz) throws JSONException{
		JSONObject classMap = new JSONObject();

		this.persistence.getJSONObject("class_map").put(clazz.getName(), classMap);

		classMap.put("table_name", clazz.getSimpleName());

		JSONObject attrs = new JSONObject();
		classMap.put("attributes", attrs);

		Field[] fields = clazz.getDeclaredFields();

		for(Field field : fields){
			JSONObject attr = new JSONObject();

			attrs.put(field.getName(), attr);

			defineType(field, attr);

			defineColumnName(field, classMap, attr);

			defineGettersAndSetters(field, attr);
		}
	}

	public void defineGettersAndSetters(Field field, JSONObject attr) throws JSONException{
		String name = field.getName();

		name = ((name.charAt(0)+"").toUpperCase())+name.substring(1, name.length());

		attr.put("set_method", "set"+name);
		attr.put("get_method", "get"+name);
	}

	public void defineColumnName(Field field, JSONObject classMap, JSONObject attr) throws JSONException{
		String type = attr.getString("type");

		String columnName;
		if(type.equals("collection")){
			columnName = "id_"+classMap.getString("table_name").toLowerCase();
		}else if(type.equals("entity")){
			columnName = "id_"+field.getName();
		}else{
			columnName = field.getName();
		}

		attr.put("column_name", columnName);
	}

	public void defineType(Field field, JSONObject attr) throws JSONException {
		Class type = field.getType();
		
		attr.put("class_type", type.getName());
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (type.isPrimitive() || Number.class.isAssignableFrom(type) || String.class.isAssignableFrom(type)) {
			map.put("type", "primitive");
		}else if (Calendar.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type)){
			map.put("type", "temporal");
			map.put("temporal_type", "date");
		}else if (Collection.class.isAssignableFrom(type)){
			map.put("type", "collection");
			map.put("mapped_by", "other");

			StringBuilder sb = new StringBuilder();
			boolean add = false;
			for(char c : field.getGenericType().toString().toCharArray()){
				if(c == '<') add = true;
				else if (c == '>') add = false;
				else if(add){
					sb.append(c);
				}
			}

			map.put("collection_type", sb.toString());
		}else if (Entity.class.isAssignableFrom(type)){
			map.put("type", "entity");
		}

		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();

			attr.put(key, map.get(key));
		}
	}

	public void show(){
		System.out.println(new JSONIndent().indent(persistence));
	}

	private JSONObject readPersistence(){
		JSONObject obj = null;

		try {
			StringBuilder sb = new StringBuilder();

			InputStream inputStream = EntityManager.class.getResource("/META-INF/persistence.json").openStream();
			Scanner scn = new Scanner(inputStream);

			while(scn.hasNextLine()){
				sb.append(scn.nextLine()+"\n");
			}

			try {
				obj = new JSONObject(sb.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return obj;
	}

	public EntityManager getEntityManager(){
		return manager;
	}
}
