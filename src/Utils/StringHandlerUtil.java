package Utils;

public class StringHandlerUtil {
	public String removeEndCharacter(String targetString){
		String resultString = null;
		if(targetString.length()>1){
			resultString = targetString.substring(0, targetString.length()-1);
		}
		return resultString;
	}
	
	public String[] String2Array(String OriginalString,String regex){
		String[] resultArray = OriginalString.split(regex);
		return resultArray;
	}
	
	public boolean isEndCharacter(String targetString,String character){
		String endCharacter = targetString.substring(targetString.length()-1);
		return endCharacter.equals(character);
	}
}
