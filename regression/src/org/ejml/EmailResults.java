/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Class for sending e-mails. Has a function to load account information and another for sending.
 */
public class EmailResults {

    public String emailUsername;
    public String emailPassword;
    public String emailDestination;

    /**
     * Loads e-mail information from a simple text file
     */
    public boolean loadEmailFile( File file ) {
        if (!file.exists()) {
            System.err.println("E-Mail login file doesn't exist");
            return false;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            emailUsername = reader.readLine();
            emailPassword = reader.readLine();
            emailDestination = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Sends an e-mail with the specified subject and text message. Will be formatted as a fixed width font
     * int HTML
     */
    public void send( String subject, String text ) {
        // Format the text using fixed sized font
        text = "<!DOCTYPE html><html lang=\"en\"><body><pre>" + text + "</pre></body></html>\n";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailUsername, emailPassword);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername + "@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestination));
            message.setSubject(subject);
            message.setContent(text, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("Sent summary to " + emailDestination);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
