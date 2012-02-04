package router66;

import java.io.*;
import java.net.URL;

public class WebsiteReader
{
	public static BufferedReader read(String url) throws Exception{
		return new BufferedReader(
			new InputStreamReader(
				new URL(url).openStream(),"UTF-8"));}
}