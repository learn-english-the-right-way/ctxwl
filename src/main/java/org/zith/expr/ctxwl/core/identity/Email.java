package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface Email {
    String getAddress();

    void sendMessage(Message message);

    void link(User user);

    Optional<User> getUser();

    record Message(String subject, String text) {
    }
}
