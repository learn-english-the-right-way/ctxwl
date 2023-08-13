package org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import java.io.Serializable;
import java.util.Objects;

@Entity
@IdClass(ReadingInducedWordlistEntryEntity.Key.class)
public class ReadingInducedWordlistEntryEntity {
    private String wordlistId;
    private String word;
    private Boolean canonical;

    @Id
    public String getWordlistId() {
        return wordlistId;
    }

    public void setWordlistId(String wordlistId) {
        this.wordlistId = wordlistId;
    }

    @Id
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setCanonical(Boolean canonical) {
        this.canonical = canonical;
    }

    public Boolean isCanonical() {
        return canonical;
    }

    public static class Key implements Serializable {
        private String wordlistId;
        private String word;

        public Key(String wordlistId, String word) {
            this.wordlistId = wordlistId;
            this.word = word;
        }

        public Key() {
        }

        public String getWordlistId() {
            return wordlistId;
        }

        public String getWord() {
            return word;
        }

        public String wordlistId() {
            return wordlistId;
        }

        public void setWordlistId(String wordlistId) {
            this.wordlistId = wordlistId;
        }

        public String word() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(wordlistId, key.wordlistId) && Objects.equals(word, key.word);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wordlistId, word);
        }
    }
}
