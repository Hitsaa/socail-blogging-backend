package com.hitsa.bloggingsite.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class MailContentBuilder {

    private final TemplateEngine templateEngine;

    public String build(String message) {
        Context context = new Context();
        context.setVariable("message", message);
        return templateEngine.process("mailTemplate", context);
        // thymeleaf will automatically add message to our html template and will be sent in the form of text
        // when user will opt for authentication. User will get an email for verification and in that verification
        // he will get the message.
    }
}