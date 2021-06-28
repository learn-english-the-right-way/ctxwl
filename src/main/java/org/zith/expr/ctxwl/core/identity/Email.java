package org.zith.expr.ctxwl.core.identity;

public interface Email {
    String getAddress();

    void sendMessage(Message message);

    record Message(String subject, String text) {
    }
}
