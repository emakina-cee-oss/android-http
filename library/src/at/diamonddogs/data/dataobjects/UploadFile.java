/*
 * Copyright (C) 2012 the diamond:dogs|group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.diamonddogs.data.dataobjects;

import java.io.File;

/**
 * Represents a file to be uploaded
 */
public class UploadFile {

	private String parameterName;
	private String name;
	/** the file to be uploaded */
	private File file;

	@SuppressWarnings("javadoc")
	public String getParameterName() {
		return parameterName;
	}

	@SuppressWarnings("javadoc")
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	@SuppressWarnings("javadoc")
	public String getName() {
		return name;
	}

	@SuppressWarnings("javadoc")
	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("javadoc")
	public File getFile() {
		return file;
	}

	@SuppressWarnings("javadoc")
	public void setFile(File file) {
		this.file = file;
	}

}
