package persistence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import persistence.querybuilder.QueryBuilder;
import persistence.util.Cache;

public class EntityManager {
	private JSONObject persistence;
	private Connection conexao;
	private Cache cache;

	public EntityManager(JSONObject persistence){
		this.persistence = persistence;
		this.cache = new Cache();
	}

	public void save(Entity obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, SQLException{
		save(obj, new HashMap<String, Object>());
	}
	private void save(Entity obj, Map<String, Object> columns) throws JSONException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		JSONObject classMap = persistence.getJSONObject("class_map").getJSONObject(obj.getClass().getName());

		String table = classMap.getString("table_name");
		System.out.println("\nsalvando "+obj.getClass().getName()+" em "+table);

		JSONObject attributes = classMap.getJSONObject("attributes");

		ArrayList<JSONObject> collections = new ArrayList<JSONObject>();

		Iterator<String> it = attributes.keys();
		while(it.hasNext()){
			String key = it.next();
			if(!key.equalsIgnoreCase("id")){
				JSONObject attr = attributes.getJSONObject(key);

				String type = attr.getString("type");
				if(type.equals("collection")){
					collections.add(attr);
				}else{
					String column = attr.getString("column_name");
					String getMethod = attr.getString("get_method");

					Method method = getMethodByName(obj.getClass(), getMethod);

					Object value = method.invoke(obj, new Object[]{});

					if(type.equals("entity")){
						value = ((Entity) value).getId();
					}else if(type.equals("temporal")){
						String temporalType = attr.getString("temporal_type");

						SimpleDateFormat format = null;
						if(temporalType.equals("date")){
							format = new SimpleDateFormat("yyyyMMdd");
						}else if(temporalType.equals("time")){
							format = new SimpleDateFormat("HHmmss");
						}else if(temporalType.equals("timestamp")){
							format = new SimpleDateFormat("yyyyMMddHHmmss");
						}

						if(value instanceof Date){
							value = format.format((Date)value);
						}else if(value instanceof Calendar){
							value = format.format(((Calendar)value).getTime());
						}
					}

					columns.put(column, value);
				}
			}
		}
		
		String operation = null;
		Map<String, Object> where = null;
		if(obj.getId() == 0){
			operation = "INSERT INTO";
		}else{
			operation = "UPDATE";
			where = new HashMap<String, Object>();
			where.put(attributes.getJSONObject("id").getString("column_name"), obj.getId());
		}
		
		QueryBuilder qb = new QueryBuilder(operation, table, columns, where);

		String query;
		System.out.println(query = qb.mountQuery());

		PreparedStatement ps = conexao.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		ps.execute();

		ResultSet rs = ps.getGeneratedKeys();
		if(rs.next()){
			obj.setId(rs.getInt(1));
		}
		rs.close();
		ps.close();

		if(!collections.isEmpty()){

			System.out.println("Coleção identificada!");

			for(JSONObject objC : collections){
				String getMethod = objC.getString("get_method");

				Method method = getMethodByName(obj.getClass(), getMethod);

				Object value = method.invoke(obj, new Object[]{});

				Collection<Entity> c = (Collection) value;

				for(Entity e : c){
					this.save(e);
				}
			}
		}
	}
	public <T extends Entity> T findById(Class<T> clazz, int id) throws JSONException, SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		cache.start();
		T t = this.findById0(clazz, id);
		cache.close();
		
		return t;
	}
	public <T extends Entity> List<T> find(Class<T> clazz, Map<String, Object> where) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException, JSONException, SQLException{
		cache.start();
		List<T> list = this.find0(clazz, where);
		cache.close();
		
		return list;
	}

	private <T> T findById0(Class<T> clazz, int id) throws JSONException, SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		JSONObject attributes = persistence.getJSONObject("class_map").getJSONObject(clazz.getName()).getJSONObject("attributes");

		JSONObject idObj = attributes.getJSONObject("id");

		HashMap<String, Object> where = new HashMap<String, Object>();
		where.put(idObj.getString("column_name"), id);

		List<T> list = find0(clazz, where);

		T t = null;
		if(!list.isEmpty()){
			t = list.get(0);
		}

		return t;
	}

	private <T> List<T> find0(Class<T> clazz, Map<String, Object> where) throws JSONException, SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		List<T> array = new ArrayList<T>();

		JSONObject classMap = persistence.getJSONObject("class_map").getJSONObject(clazz.getName());
		
		JSONObject attributes = new JSONObject(classMap.getJSONObject("attributes").toString());

		String table = classMap.getString("table_name");

		QueryBuilder qb = new QueryBuilder("SELECT * FROM", table, null, where);

		String query;
		System.out.println(query = qb.mountQuery());

		PreparedStatement ps = conexao.prepareStatement(query);
		ResultSet rs = ps.executeQuery();

		JSONObject idAttr = attributes.getJSONObject("id");		
		attributes.remove("id");
		
		while(rs.next()){
			int id = rs.getInt(idAttr.getString("column_name"));
			
			T obj = this.cache.getFromCache(clazz, id); 

			if(obj == null){
				obj = clazz.newInstance();
				
				((Entity) obj).setId(id);
				
				this.cache.addToCache(clazz, (Entity) obj);
				
				Iterator<String> it = attributes.keys();
				while(it.hasNext()){
					String key = it.next();
					
					System.out.println(key);

					JSONObject attr = attributes.getJSONObject(key);

					String columnName = attr.getString("column_name");

					String setMethod = attr.getString("set_method");

					String type = attr.getString("type");

					Method method = getMethodByName(clazz, setMethod);

					Object value = null;

					if(type.equalsIgnoreCase("entity")){
						value = rs.getObject(columnName);
						value = findById0(method.getParameterTypes()[0], (int) value);
					}else if(type.equals("collection")){
						String collectionType = attr.getString("collection_type");

						HashMap<String, Object> whereCol = new HashMap<String, Object>();
						whereCol.put(columnName, rs.getInt(idAttr.getString("column_name")));

						value = find0(Class.forName(collectionType), whereCol);
					}else if(type.equals("temporal")){
						String temporalType = attr.getString("temporal_type");

						SimpleDateFormat format = null;
						if(temporalType.equals("date")){
							format = new SimpleDateFormat("yyyyMMdd");
						}else if(temporalType.equals("time")){
							format = new SimpleDateFormat("HHmmss");
						}else if(temporalType.equals("timestamp")){
							format = new SimpleDateFormat("yyyyMMddHHmmss");
						}

						value = rs.getObject(columnName);
						Calendar c = Calendar.getInstance();
						try {
							c.setTime(format.parse(value+""));
						} catch (ParseException e) {
							value = null;
						}
						value = c;
					}else{
						value = rs.getObject(columnName);
					}

					method.invoke(obj, value);
				}				
			}
			array.add(obj);
		}

		rs.close();
		ps.close();

		return array;
	}

	private Method getMethodByName(Class clazz, String name){
		System.out.println("Obtendo método "+name+" em "+clazz.getName());
		Method[] ms = clazz.getMethods();
		for(Method m : ms){
			if(m.getName().equals(name))
				return m;
		}
		return null;
	}

	public boolean openConnection() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			String url = "jdbc:mysql://:server/:schema".replaceAll(
					":server",
					persistence.getJSONObject("database_config").getString(
							"server")).replaceAll(
									":schema",
									persistence.getJSONObject("database_config").getString(
											"database"));

			this.conexao = DriverManager.getConnection(
					url,
					persistence.getJSONObject("database_config").getString(
							"user"),
							persistence.getJSONObject("database_config").getString(
									"pass"));

			return true;
		} catch (ClassNotFoundException | SQLException e) {
			throw e;
		}
	}
	public boolean closeConnection() throws SQLException{
		conexao.close();
		conexao = null;
		return true;
	}

	public boolean begin() {
		boolean oppened = conexao != null;
		if (oppened) {
			try {
				PreparedStatement ps = conexao
						.prepareStatement("START TRANSACTION");
				ps.execute();
				ps.close();

				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return oppened;
		}

	}

	public boolean commit() {
		boolean oppened = conexao != null;
		if (oppened) {
			try {
				PreparedStatement ps = conexao.prepareStatement("COMMIT");
				ps.execute();
				ps.close();

				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return oppened;
		}
	}

	public boolean rollback() {
		boolean oppened = conexao != null;
		if (oppened) {
			try {
				PreparedStatement ps = conexao.prepareStatement("ROLLBACK");
				ps.execute();
				ps.close();

				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return oppened;
		}
	}
}
