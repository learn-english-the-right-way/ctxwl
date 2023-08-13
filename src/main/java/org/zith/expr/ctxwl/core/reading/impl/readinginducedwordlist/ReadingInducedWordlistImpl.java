package org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist;

import org.zith.expr.ctxwl.core.reading.ReadingInducedWordlist;

import java.util.List;

public class ReadingInducedWordlistImpl implements ReadingInducedWordlist {

    private final ReadingInducedWordlistRepositoryImpl parent;
    private final String id;

    public ReadingInducedWordlistImpl(ReadingInducedWordlistRepositoryImpl parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<String> getWords() {
        return parent.withTransaction(session -> {
            var cb = session.getCriteriaBuilder();
            var q = cb.createQuery(ReadingInducedWordlistEntryEntity.class);
            var r = q.from(ReadingInducedWordlistEntryEntity.class);
            q.where(cb.equal(r.get(ReadingInducedWordlistEntryEntity_.wordlistId), id));
            return session.createQuery(q).list().stream().map(ReadingInducedWordlistEntryEntity::getWord).toList();
        });
    }

    @Override
    public void delete(String word) {
        parent.withTransaction(session -> {
            ReadingInducedWordlistEntryEntity wordlistEntry = session.get(ReadingInducedWordlistEntryEntity.class, new ReadingInducedWordlistEntryEntity.Key(id, word));
            if (wordlistEntry != null) {
                session.remove(wordlistEntry);
            }
            return null;
        });
    }
}
