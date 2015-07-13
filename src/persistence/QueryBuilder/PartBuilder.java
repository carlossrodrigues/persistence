package persistence.QueryBuilder;

import java.util.Iterator;
import java.util.Map;

public class PartBuilder {
	private String opDesc;
	private String separator;
	private Map<String, Object> values;
	public PartBuilder(String opDesc, String separator,
			Map<String, Object> values) {
		super();
		this.opDesc = opDesc;
		this.separator = separator;
		this.values = values;
	}
	
	public String mountQuery(){
		StringBuilder sb = new StringBuilder(opDesc);
		sb.append(" ");
		
		Iterator<String> it = values.keySet().iterator();
		if(it.hasNext()){
			String key = it.next();
			Object value = values.get(key);
			
			addColumnValue(sb, key, value);
			while(it.hasNext()){
				sb.append(this.separator);
				
				key = it.next();
				value = values.get(key);
				
				addColumnValue(sb, key, value);
			}
		}
		return sb.toString();
	}
	
	public void addColumnValue(StringBuilder sb, String column, Object value){
		sb.append(column);
		sb.append("=");
		
		if(value instanceof String){
			sb.append("'");
			sb.append(value);
			sb.append("'");
		}else if(value == null){
			sb.append("null");
		}else{
			sb.append(value);
		}
	}
}
