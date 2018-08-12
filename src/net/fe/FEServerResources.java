package net.fe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.fe.network.Server;

public class FEServerResources {
	
	private static final File CONFIG = new File("server.config");
	private static Properties prop = getProperties();
	
	private static Properties getProperties() {
		if (prop == null) {
			prop = new Properties();
			try {
				final boolean isPatch = CONFIG.exists();
				//should probably also have a check for directory  && !f.isDirectory() 
				//but unless the user creates it, that won't be true. No clear way of handling it.
				if (CONFIG.exists()) {
					try(InputStream in = new FileInputStream(CONFIG)) {
						prop.load(in);
					}
				} else {
					//make file and populate it
					CONFIG.createNewFile();
				}
				
				final Properties defaultProps = getDefaultProperties();
				for (String key : prop.stringPropertyNames())
					defaultProps.remove(key);
				
				for (String key : defaultProps.stringPropertyNames())
					prop.setProperty(key, defaultProps.getProperty(key));
				
				if (! defaultProps.isEmpty()) {
					try(OutputStream out = new FileOutputStream(CONFIG, isPatch)) {
						if (isPatch)
							out.write('\n');
						defaultProps.store(out, (isPatch ? "---Patch---" : "---Initial Configuration---"));
					}
				}
			} catch (IOException e){
				e.printStackTrace();
				prop = getDefaultProperties();
			}
		}
		return prop;
	}

	private static Properties getDefaultProperties() {
		Properties prop = new Properties();
		prop.put("PORT", ""+Server.DEFAULT_PORT);
		return prop;
	}
	
	public static short getPort() {
		return Short.parseShort(prop.getProperty("PORT"));
	}
}
