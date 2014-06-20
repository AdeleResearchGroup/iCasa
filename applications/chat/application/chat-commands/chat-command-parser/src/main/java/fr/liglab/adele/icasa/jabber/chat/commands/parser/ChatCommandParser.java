package fr.liglab.adele.icasa.jabber.chat.commands.parser;

/**
 * Created by donatien on 24/04/14.
 */
import org.apache.felix.ipojo.annotations.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Instantiate
@Provides
public class ChatCommandParser {

    public String parse(String message){
        String command=null;
        if(message.contains("turn")){
            Pattern patternTurn=Pattern.compile("(turn).(on|off).(the).?([^\\ ]*).(in the).?([^\\ ]*)");
            Matcher matcherTurn= patternTurn.matcher(message);
            Boolean b= matcherTurn.matches();
            if(b)command="turn -o "+matcherTurn.group(2)+" -d "+matcherTurn.group(4)+" -l "+matcherTurn.group(6);
        }else if(message.contains("give")){
            Pattern patternGet=Pattern.compile("(give).(me).(the).?([^\\ ]*).(in the).?([^\\ ]*)");
            Matcher matcherGet= patternGet.matcher(message);
            Boolean b= matcherGet.matches();
            if(b)command="get -d "+matcherGet.group(4)+" -l "+matcherGet.group(6);
        }else{
            Pattern patternGet=Pattern.compile("(set).(the).?([^\\ ]*).(to).?([^\\ ]*).(in the).?([^\\ ]*)");
            Matcher matcherGet= patternGet.matcher(message);
            Boolean b= matcherGet.matches();
            if(b)command="setto -d "+matcherGet.group(3)+" -z "+matcherGet.group(5)+" -l "+matcherGet.group(7);
        }
        return command;
    }


    @Validate
    public void start() {
      System.out.println("Parser started");
    }

    @Invalidate
    public void stop() {
        System.out.println("Parser stoped");
    }
}