package com.chukong.sdkdemo.dns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

/**
 * @author yihua.huang@dianping.com <br>
 * @date: 13-7-14 <br>
 * Time: 下午4:36 <br>
 */
public class AnswerHandler {

    // b._dns-sd._udp.0.129.37.10.in-addr.arpa.
    private final Pattern filterPTRPattern = Pattern
            .compile(".*\\.(\\d+\\.\\d+\\.\\d+\\.\\d+\\.in-addr\\.arpa\\.)");

    private String filterPTRQuery(String query) {
        Matcher matcher = filterPTRPattern.matcher(query);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return query;
        }
    }

    public boolean handle(MessageWrapper request, MessageWrapper response) {
        Record question = request.getMessage().getQuestion();
        String query = question.getName().toString();
        int type = question.getType();
        if (type == Type.PTR) {
            query = filterPTRQuery(query);
        }
        // some client will query with any
        if (type == Type.ANY) {
            type = Type.A;
        }
        String answer = "127.0.0.1";
        if (answer != null) {
            try {
                /*
                System.out.println("query = " + query);
                System.out.println("question.getDClass() = " + question.getDClass());
                System.out.println("question.getName() = " + question.getName());
                System.out.println("answer = " + answer);
                System.out.println("type = " + type);
                */

                RecordBuilder builder = new RecordBuilder();
                builder.dclass(question.getDClass());
                builder.name(question.getName());
                builder.answer(answer);
                builder.type(type);
                Record record = builder.toRecord();
                response.getMessage().addRecord(question, Section.QUESTION);
                response.getMessage().addRecord(record, Section.ANSWER);
                //System.out.println("answer\t" + Type.string(type) + "\t"
                //            + DClass.string(question.getDClass()) + "\t"
                //            + answer + "\n");
                response.setHasRecord(true);
                System.out.println("res = " + response.getMessage().toString());
                return false;
            } catch (Exception e) {
                System.out.println("AnswerHandler handling exception " + e);
                e.printStackTrace();
            }
        }
        return true;
    }
}