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
package org.primefaces.poseidon.service;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.poseidon.domain.Document;

@ManagedBean(name = "documentService")
@ApplicationScoped
public class DocumentService {

    public TreeNode createDocuments() {
        TreeNode root = new DefaultTreeNode(new Document("Files", "-", "Folder"), null);

		TreeNode documents = new DefaultTreeNode(new Document("Documents", "-", "Folder"), root);
		TreeNode pictures = new DefaultTreeNode(new Document("Pictures", "-", "Folder"), root);
		TreeNode movies = new DefaultTreeNode(new Document("Movies", "-", "Folder"), root);

		TreeNode work = new DefaultTreeNode(new Document("Work", "-", "Folder"), documents);
		TreeNode primefaces = new DefaultTreeNode(new Document("PrimeFaces", "-", "Folder"), documents);

		//Documents
		TreeNode expenses = new DefaultTreeNode("document", new Document("Expenses.doc", "30 KB", "Word Document"), work);
		TreeNode resume = new DefaultTreeNode("document", new Document("Resume.doc", "10 KB", "Word Document"), work);
		TreeNode refdoc = new DefaultTreeNode("document", new Document("RefDoc.pages", "40 KB", "Pages Document"), primefaces);

		//Pictures
		TreeNode barca = new DefaultTreeNode("picture", new Document("barcelona.jpg", "30 KB", "JPEG Image"), pictures);
		TreeNode primelogo = new DefaultTreeNode("picture", new Document("logo.jpg", "45 KB", "JPEG Image"), pictures);
		TreeNode optimus = new DefaultTreeNode("picture", new Document("optimusprime.png", "96 KB", "PNG Image"), pictures);

		//Movies
		TreeNode pacino = new DefaultTreeNode(new Document("Al Pacino", "-", "Folder"), movies);
		TreeNode deniro = new DefaultTreeNode(new Document("Robert De Niro", "-", "Folder"), movies);

		TreeNode scarface = new DefaultTreeNode("movie", new Document("Scarface", "15 GB", "Movie File"), pacino);
		TreeNode carlitosWay = new DefaultTreeNode("movie", new Document("Carlitos' Way", "24 GB", "Movie File"), pacino);

		TreeNode goodfellas = new DefaultTreeNode("movie", new Document("Goodfellas", "23 GB", "Movie File"), deniro);
		TreeNode untouchables = new DefaultTreeNode("movie", new Document("Untouchables", "17 GB", "Movie File"), deniro);

        return root;
    }

    public TreeNode createCheckboxDocuments() {
        TreeNode root = new CheckboxTreeNode(new Document("Files", "-", "Folder"), null);

		TreeNode documents = new CheckboxTreeNode(new Document("Documents", "-", "Folder"), root);
		TreeNode pictures = new CheckboxTreeNode(new Document("Pictures", "-", "Folder"), root);
		TreeNode movies = new CheckboxTreeNode(new Document("Movies", "-", "Folder"), root);

		TreeNode work = new CheckboxTreeNode(new Document("Work", "-", "Folder"), documents);
		TreeNode primefaces = new CheckboxTreeNode(new Document("PrimeFaces", "-", "Folder"), documents);

		//Documents
		TreeNode expenses = new CheckboxTreeNode("document", new Document("Expenses.doc", "30 KB", "Word Document"), work);
		TreeNode resume = new CheckboxTreeNode("document", new Document("Resume.doc", "10 KB", "Word Document"), work);
		TreeNode refdoc = new CheckboxTreeNode("document", new Document("RefDoc.pages", "40 KB", "Pages Document"), primefaces);

		//Pictures
		TreeNode barca = new CheckboxTreeNode("picture", new Document("barcelona.jpg", "30 KB", "JPEG Image"), pictures);
		TreeNode primelogo = new CheckboxTreeNode("picture", new Document("logo.jpg", "45 KB", "JPEG Image"), pictures);
		TreeNode optimus = new CheckboxTreeNode("picture", new Document("optimusprime.png", "96 KB", "PNG Image"), pictures);

		//Movies
		TreeNode pacino = new CheckboxTreeNode(new Document("Al Pacino", "-", "Folder"), movies);
		TreeNode deniro = new CheckboxTreeNode(new Document("Robert De Niro", "-", "Folder"), movies);

		TreeNode scarface = new CheckboxTreeNode("movie", new Document("Scarface", "15 GB", "Movie File"), pacino);
		TreeNode carlitosWay = new CheckboxTreeNode("movie", new Document("Carlitos' Way", "24 GB", "Movie File"), pacino);

		TreeNode goodfellas = new CheckboxTreeNode("movie", new Document("Goodfellas", "23 GB", "Movie File"), deniro);
		TreeNode untouchables = new CheckboxTreeNode("movie", new Document("Untouchables", "17 GB", "Movie File"), deniro);

        return root;
    }
}
