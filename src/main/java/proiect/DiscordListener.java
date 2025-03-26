package proiect;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class DiscordListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getInteraction().getName()) {
            case "say" -> {
                String content = event.getOption("content", OptionMapping::getAsString);
                assert content != null; // Discord doesn't even allow you to call this command with an empty content but whatever ig
                event.reply(content).queue();
            }
            case "ping" -> event.reply("Pong!").queue();

            default -> {
            }
        }
    }
}
