package org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.zith.expr.ctxwl.common.hibernate.DataAccessor;
import org.zith.expr.ctxwl.common.wordnet.WordAsPartOfSpeech;
import org.zith.expr.ctxwl.common.wordnet.WordNet;
import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingInducedWordlist;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookupValueLike;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingInducedWordlistRepositoryImpl implements ReadingInducedWordlistRepository {

    private final SessionFactory sessionFactory;
    private final DataAccessor.Factory dataAccessorFactory;
    private final WordNet wordNet;

    public ReadingInducedWordlistRepositoryImpl(SessionFactory sessionFactory, WordNet wordNet) {
        this.sessionFactory = sessionFactory;
        this.wordNet = wordNet;
        dataAccessorFactory = DataAccessor.Factory.of(e -> e instanceof OptimisticLockException, 5);
    }

    final <T> T withTransaction(Function<Session, T> operation) {
        return dataAccessorFactory.create(operation).execute(sessionFactory);
    }

    @Override
    public ReadingInducedWordlist getWordlist(String id) {
        return new ReadingInducedWordlistImpl(this, id);
    }

    @Override
    public void consume(List<ReadingEvent> events) {
        var lemmasByWordlistIds = events.stream()
                .flatMap(event -> {
                    if (event instanceof ReadingEvent.AddingReadingInspiredLookup addingLookup) {
                        return Stream.of(addingLookup);
                    }
                    return Stream.empty();
                })
                .map(ReadingEvent.AddingReadingInspiredLookup::value)
                .collect(Collectors.groupingBy(
                        l -> l.session().getWordlist(),
                        Collectors.mapping(ReadingInspiredLookupValueLike::criterion, Collectors.toSet())));
        var baseFormCache = new HashMap<String, List<String>>();
        record WordInfo(String word, boolean canonical) {
        }
        var wordsByWordlistIds = lemmasByWordlistIds.entrySet().stream()
                .map(e -> Map.entry(e.getKey(),
                        e.getValue().stream()
                                .map(lemma ->
                                        Optional.of(baseFormCache.computeIfAbsent(lemma, word ->
                                                        // TODO WordNet doesn't include prepositions, pronoun,
                                                        //  conjunctions, interjections.
                                                        wordNet.getBaseForms(word).stream()
                                                                .map(WordAsPartOfSpeech::word).distinct().toList()))
                                                .filter(ws -> !ws.isEmpty())
                                                .map(ws -> ws.stream().map(w -> new WordInfo(w, true)).toList())
                                                .orElseGet(() -> List.of(new WordInfo(lemma, false))))
                                .flatMap(Collection::stream)
                                .toList()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        withTransaction(session -> {
            for (var entry : wordsByWordlistIds.entrySet()) {
                var wordlistId = entry.getKey();
                for (var wordInfo : entry.getValue()) {
                    var entity = session.byId(ReadingInducedWordlistEntryEntity.class)
                            .loadOptional(new ReadingInducedWordlistEntryEntity.Key(wordlistId, wordInfo.word()))
                            .orElseGet(() -> {
                                var freshEntity = new ReadingInducedWordlistEntryEntity();
                                freshEntity.setWordlistId(wordlistId);
                                freshEntity.setWord(wordInfo.word());
                                freshEntity.setCanonical(wordInfo.canonical());
                                return freshEntity;
                            });
                    session.persist(entity);
                }
            }
            return null;
        });
    }
}
