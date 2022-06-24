package me.dustin.jex.event.chat;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import me.dustin.events.core.Event;

import java.util.List;


public class EventSortSuggestions extends Event {

    private final Suggestions suggestions;
    private final String text;
    private final List<Suggestion> output;

    public EventSortSuggestions(Suggestions suggestions, String text, List<Suggestion> output) {
        this.suggestions = suggestions;
        this.text = text;
        this.output = output;
    }

    public Suggestions getSuggestions() {
        return suggestions;
    }

    public String getText() {
        return text;
    }

    public List<Suggestion> getOutput() {
        return output;
    }
}
