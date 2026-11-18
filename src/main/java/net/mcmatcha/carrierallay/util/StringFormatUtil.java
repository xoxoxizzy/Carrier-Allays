package net.mcmatcha.carrierallay.util;

import java.util.Map;

public class StringFormatUtil {
    
    private Map<String, String> templates;

    public StringFormatUtil(Map<String, String> templates) {
        this.templates = templates;
    }

    public String format(String str) {
        
        for (String template : templates.keySet()) 
            str = str.replace(template, templates.get(template));
    
        return str;
    }
}
