import java.io.*;
import java.util.*;
import java.lang.System;


public class Try {

	public static void main(String[] ignored) {
		try {
		File f = new File("f.txt");
		File g = new File("g.txt");
		File r = new File ("r.txt");
		ArrayList<String> a = new ArrayList<String>();
		FileWriter out = new FileWriter(r, false);
		
		BufferedReader b = new BufferedReader(new FileReader(f));
		String line1;
		while ((line1 = b.readLine()) != null) {
			a.add(line1);
		}
		a.add("whatwhatwhat");
		BufferedReader x = new BufferedReader(new FileReader(g));
		String line2;
		while ((line2 = x.readLine()) != null) {
			a.add(line2);
		}
		for (String s : a) {
			System.out.println(s);
			out.write(s);
			out.write(System.lineSeparator());
		}

		out.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
                