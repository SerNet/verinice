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
package org.primefaces.poseidon.view.misc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.poseidon.domain.Book;

@ManagedBean
@ViewScoped
public class CollectorView implements Serializable {

    private Book book;

	private List<Book> books;

    @PostConstruct
    public void init() {
        book = new Book();
        books = new ArrayList<Book>();

    }

	public void createNew() {
		if(books.contains(book)) {
			FacesMessage msg = new FacesMessage("Dublicated", "This book has already been added");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
        else {
            books.add(book);
            book = new Book();
        }
	}

	public String reinit() {
		book = new Book();

		return null;
	}

	public Book getBook() {
		return book;
	}

	public List<Book> getBooks() {
		return books;
	}
}
