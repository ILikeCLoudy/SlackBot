package com.SKALA.LikeCloudy.Slack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple builder to help construct Slack Block Kit structures.
 */
public class SlackBlockBuilder {
    private final List<Map<String, Object>> blocks = new ArrayList<>();

    /**
     * Adds a section block with mrkdwn text.
     */
    public SlackBlockBuilder section(String text) {
        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");

        Map<String, String> textObj = new HashMap<>();
        textObj.put("type", "mrkdwn");
        textObj.put("text", text);
        section.put("text", textObj);

        blocks.add(section);
        return this;
    }

    /**
     * Adds a divider block.
     */
    public SlackBlockBuilder divider() {
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        blocks.add(divider);
        return this;
    }

    public List<Map<String, Object>> build() {
        return blocks;
    }
}
