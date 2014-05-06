/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.mail.service;

import java.io.File;

/**
 * Simple interface to specify a mail sender service !
 *
 */
public interface MailSender {

    /**
     * Method to send a report mail ! It will send a mail with attached report !
     * @param sendTo  : the people to send the report
     * @param subject : the subject of the mail
     * @param body : the content of the mail
     * @param attachementFile  : the attachement File to send
     * @return true if the mail is correctly send false if not !
     */

    boolean sendReport(String sendTo, String subject, String body, File attachementFile);

    /**
     * Method to send a simple mail !
     * @param sendTo  : the people to send the mail
     * @param subject : the subject of the mail
     * @param body : the content of the mail
     * @return true if the mail is correctly send false if not !
     */
    boolean sendSimpleMail(String sendTo, String subject, String body);

    /**
     * Method to send an alert mail !
     * @param sendTo : the author to send the alert
     * @return true if the mail is correctly send false if not !
     */
    boolean sendAlertMail(String sendTo);
}
