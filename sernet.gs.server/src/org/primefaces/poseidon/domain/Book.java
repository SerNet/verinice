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

public class Book implements Serializable {

    private String title;
	private String author;
	private String publisher;
	private Integer pages;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public Integer getPages() {
		return pages;
	}

	public void setPages(Integer pages) {
		this.pages = pages;
	}

	public boolean equals(Object obj) {
		if(!(obj instanceof Book))
			return false;

		Book book = (Book) obj;

		return (book.getTitle() != null && book.getTitle().equals(title)) && (book.getAuthor() != null && book.getAuthor().equals(author));
	}

	public int hashCode() {
		int hash = 1;
		if(title != null)
			hash = hash * 31 + title.hashCode();

		if(author != null)
			hash = hash * 29 + author.hashCode();

		return hash;
	}
}
