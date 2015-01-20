package de.dakror.replacementparser;

import java.io.InputStream;
import java.net.URL;

/**
 * @author Maximilian Stark | Dakror
 */
public interface InputStreamProvider {
	public InputStream provide(URL url);
}
