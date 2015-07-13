package persistence.QueryBuilder;

import java.util.Map;

public class QueryBuilder {
	private String opPrefix;
	private String tableName;
	private Map<String, Object> columnValues;
	private Map<String, Object> whereValues;
	public QueryBuilder(String opPrefix, String tableName, Map<String, Object> columnValues, Map<String, Object> whereValues) {
		super();
		this.opPrefix = opPrefix;
		this.tableName = tableName;
		this.columnValues = columnValues;
		this.whereValues = whereValues;
	}

	public String mountQuery(){
		StringBuilder sb = new StringBuilder(opPrefix);

		sb.append(" ");
		sb.append(tableName);

		if(columnValues != null){
			PartBuilder columnsSet = new PartBuilder("SET", ", ", columnValues);
			
			sb.append(" ");

			sb.append(columnsSet.mountQuery());
		}

		if(whereValues != null){
			PartBuilder whereSet = new PartBuilder("WHERE", " AND ", whereValues);

			sb.append(" ");

			sb.append(whereSet.mountQuery());
		}
		return sb.toString();
	}
}