/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.poseidon.domain;

import java.io.Serializable;
import java.util.List;

public class NewsGroup implements Serializable {

	private String title;

	private List<NewsEntry> entries;

	public NewsGroup() {

	}

	public NewsGroup(String title, List<NewsEntry> entries) {
		this.title = title;
		this.entries = entries;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<NewsEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<NewsEntry> entries) {
		this.entries = entries;
	}
}
