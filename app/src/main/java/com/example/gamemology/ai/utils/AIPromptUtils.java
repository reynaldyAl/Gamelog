package com.example.gamemology.ai.utils;

import com.example.gamemology.models.Game;

public class AIPromptUtils {

    /**
     * Creates a system prompt with game context for the AI
     * @param game The game to provide context for
     * @return A system prompt string
     */
    public static String createGameContextPrompt(Game game) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("You are Gamemology Assistant, an AI expert about video games. ");
        promptBuilder.append("You provide helpful information and recommendations about games. ");

        if (game != null) {
            promptBuilder.append("\n\nThe user is currently viewing information about: ");
            promptBuilder.append("\nGame: ").append(game.getName());

            if (game.getGenres() != null && !game.getGenres().isEmpty()) {
                promptBuilder.append("\nGenres: ");
                for (int i = 0; i < game.getGenres().size(); i++) {
                    promptBuilder.append(game.getGenres().get(i));
                    if (i < game.getGenres().size() - 1) {
                        promptBuilder.append(", ");
                    }
                }
            }

            if (game.getReleased() != null) {
                promptBuilder.append("\nReleased: ").append(game.getReleased());
            }

            if (game.getRating() > 0) {
                promptBuilder.append("\nRating: ").append(game.getRating());
            }

            promptBuilder.append("\n\nProvide information related to this game when relevant to the user's questions.");
        }

        return promptBuilder.toString();
    }

    /**
     * Creates a search prompt for natural language search
     * @param query The natural language query
     * @return A prompt for extracting search parameters
     */
    public static String createSearchPrompt(String query) {
        return "I need to search for games based on the following query: \"" + query + "\"\n\n" +
                "Please extract search parameters as a JSON object with these possible fields:\n" +
                "- keywords: general search terms\n" +
                "- genres: list of genres mentioned\n" +
                "- platforms: list of platforms mentioned\n" +
                "- minRating: minimum rating threshold (0-5)\n" +
                "- yearFrom: earliest release year to include\n" +
                "- yearTo: latest release year to include\n\n" +
                "ONLY return a valid JSON object, nothing else. If a parameter isn't mentioned, don't include it.";
    }
}