package shell.apps.CommandShell;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class Prompt {

	private String prefix;
	private String currentPath;
	private String suffix;
	private boolean showPath;

	public Prompt(String prefix, String fileSystemPath, String suffix) {
		this.prefix = prefix;
		this.currentPath = fileSystemPath;
		this.suffix = suffix;
		this.showPath = false;
	}

	/**
	 * true to print path in prompt, false otherwise. Default value is false
	 *
	 * @param showPath
	 */
	public void showPath(boolean showPath) {
		this.showPath = showPath;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String fileSystemPath) {
		this.currentPath = fileSystemPath;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	

	@Override
	public String toString() {

		if (prefix == null) {
			prefix = "";
		}
		if (currentPath == null) {
			currentPath = "";
		}
		if (suffix == null) {
			suffix = "";
		}


		return prefix + (this.showPath ? currentPath : "") + suffix;
	}
}
