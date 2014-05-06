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
package fr.liglab.adele.icasa.mail.service.impl;

import fr.liglab.adele.icasa.mail.service.MailSender;
import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.mail.Mail;
import org.ow2.chameleon.mail.MailSenderService;

import java.io.File;

/**
 * This class provides method to send medical report by mail !
 *
 */
@Component(immediate = true)
@Instantiate (name = "Platform-MailService-0")
@Provides (specifications = MailSender.class)
public class MailSenderImpl implements MailSender {

    @Requires
    private MailSenderService m_sender;

    @Validate
    public void start() {
    }

    @Invalidate
    public void stop() {
    }

    @Override
    public boolean sendReport(String sendTo, String subject, String body, File medicalReport) {
        try {
            m_sender.send(new Mail().to(sendTo)
                    .subject(subject)
                    .body(body)
                    .attach(medicalReport));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean sendSimpleMail(String sendTo, String subject, String body) {
        try {
            m_sender.send(new Mail().to(sendTo)
                    .subject(subject)
                    .body(body));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean sendAlertMail(String sendTo) {
       try {
            m_sender.send(new Mail().to(sendTo)
                    .subject("This is an alert mail")
                    .body("This is an alert mail to inform you that there is a pb in your house. iCasa Platform Team."));
       } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
