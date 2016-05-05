package com.gmail.minionemails.Factions;

import java.io.File;
import java.io.FilenameFilter;

public class FileUtil {

	public FileUtil() {}
	
	public String[] listFiles(String dir)
	  {
	    File directory = new File(dir);
	    if (!directory.isDirectory())
	    {
	      System.out.println("No directory provided");
	      return null;
	    }
	    FilenameFilter filefilter = new FilenameFilter()
	    {
	      public boolean accept(File dir, String name)
	      {
	        return name.endsWith(".json");
	      }
	    };
	    return directory.list(filefilter);
	  }
}
