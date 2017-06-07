package net.fe.resources.files;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object representing a semantic version, as described <a href="http://semver.org/">here</a>.
 * This class implements comparators used to evaluate whether a version is more recent than another.
 * @author wellme
 */
public class Version implements Comparable<Version> {

	
	private int[] versionNumbers;
	
	/**
	 * Parses a string into a Version object.
	 * @param version the string to be parsed.
	 * @throws IllegalArgumentException If the version is improperly formatted.
	 */
	public Version(String version) throws IllegalArgumentException {
		Matcher matcher = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)").matcher(version);
		if(!matcher.find())
			throw new IllegalArgumentException(String.format("%s isn't a valid version", version));
		versionNumbers = new int[] {
			Integer.parseInt(matcher.group(1)),
			Integer.parseInt(matcher.group(2)),
			Integer.parseInt(matcher.group(3)),
		};
	}
	
	/**
	 * Returns the string representation of this version.
	 * @return returns the string representation of this version.
	 */
	@Override
	public String toString() {
		return String.format("v%s.%s.%s", versionNumbers[0], versionNumbers[1], versionNumbers[2]);
	}
	
	@Override
	public int compareTo(Version o) {
		int value;
		for(int i = 0; i < versionNumbers.length; i++)
			if((value = Integer.compare(versionNumbers[i], o.versionNumbers[i])) != 0)
				return value;
		return 0;
	}

	
}
