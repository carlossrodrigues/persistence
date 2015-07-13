package persistence.util;

import org.json.JSONObject;

public class JSONIndent {
	public String indent(JSONObject json){
		int tab = 0;
		
		StringBuilder sb = new StringBuilder();
		
		char[] c1 = json.toString().toCharArray();
		for(char c : c1){			
			if(c == '{'){
				sb.append(c);
				
				sb.append("\n");
				
				tab++;
				
				addTab(sb, tab);
			}else if(c == '}'){
				sb.append("\n");
				
				tab--;
				
				addTab(sb, tab);
				
				sb.append(c);
			}else if(c == ','){
				sb.append(c);
				
				sb.append("\n");
				
				addTab(sb, tab);
			}else{
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	private void addTab(StringBuilder sb, int n){
		for(int i=0; i<n; i++){
			sb.append("\t");
		}
	}
}
