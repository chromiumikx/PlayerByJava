package com.chromiumikx.playerbyjava;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class JRegexExample {
	
	private static String REGEX = "a*b";
	private static String Input = "aabfooaabfooabfoobf00";
	private static String REPLACE = "-";

	public static void main_Re(){
		Pattern re = Pattern.compile(REGEX);
		Matcher ma = re.matcher(Input);
		StringBuffer sb = new StringBuffer();
		while (ma.find()){
			ma.appendReplacement(sb, REPLACE);
		}
		ma.appendTail(sb);//�����һ��ƥ�乤����ʣ����ַ����ӽ�StringBuffer
		System.out.println(sb.toString());
	}

}
