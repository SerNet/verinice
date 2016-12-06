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
package org.primefaces.poseidon.view.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.poseidon.domain.Mail;

@ManagedBean
@ViewScoped
public class MailboxView implements Serializable{

    private TreeNode mailboxes;

    private List<Mail> mails;

    private Mail mail;

    private TreeNode mailbox;

    @PostConstruct
    public void init() {
        mailboxes = new DefaultTreeNode("root", null);

        TreeNode inbox = new DefaultTreeNode("i","Inbox", mailboxes);
		TreeNode sent = new DefaultTreeNode("s", "Sent", mailboxes);
		TreeNode trash = new DefaultTreeNode("t", "Trash", mailboxes);
        TreeNode junk = new DefaultTreeNode("j", "Junk", mailboxes);

        TreeNode gmail = new DefaultTreeNode("Gmail", inbox);
        TreeNode hotmail = new DefaultTreeNode("Hotmail", inbox);

        mails = new ArrayList<Mail>();
        mails.add(new Mail("optimus@primefaces.com", "Team Meeting", "Meeting to discuss roadmap", new Date()));
        mails.add(new Mail("spammer@spammer.com", "You've won Lottery", "Send me your credit card info to claim", new Date()));
        mails.add(new Mail("spammer@spammer.com", "Your email has won", "Click the exe file to claim", new Date()));
        mails.add(new Mail("primefaces-commits", "[primefaces] r4491 - Layout mailbox sample", "Revision:4490 Author:cagatay.civici" ,new Date()));
    }

    public TreeNode getMailboxes() {
        return mailboxes;
    }

    public List<Mail> getMails() {
        return mails;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public TreeNode getMailbox() {
        return mailbox;
    }

    public void setMailbox(TreeNode mailbox) {
        this.mailbox = mailbox;
    }

    public void send() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Mail Sent!"));
    }
}
