package com.badfic.philbot.commands.slash;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.service.AudioPlayerSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
public class PlaySlashCommand extends BaseSlashCommand {
    private final AudioPlayerManager audioPlayerManager;
    private final AudioPlayer audioPlayer;
    private final AudioPlayerSendHandler audioPlayerSendHandler;

    public PlaySlashCommand(AudioPlayerManager audioPlayerManager, AudioPlayer audioPlayer, AudioPlayerSendHandler audioPlayerSendHandler) {
        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayer;
        this.audioPlayerSendHandler = audioPlayerSendHandler;
        name = "play";
        options = List.of(new OptionData(OptionType.STRING, "track", "Either a URL or a search term for the track to play", true, false));
        help = "Play a track by URL or by searching";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();

        final var songQuery = event.getOption("track").getAsString().strip();
        final var songUrl = Constants.isUrl(songQuery) ? songQuery : "ytmsearch: " + songQuery;

        audioPlayerManager.loadItem(songUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                getVoiceChannel(event, interactionHook).ifPresent(voiceChannel -> {
                    connectToVoiceChannelAndPlay(voiceChannel, audioTrack);
                    replyToInteractionHook(event, interactionHook, songUrl + " added to queue.");
                });
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                getVoiceChannel(event, interactionHook).ifPresent(voiceChannel -> {
                    var firstTrack = audioPlaylist.getSelectedTrack();

                    if (firstTrack == null) {
                        firstTrack = audioPlaylist.getTracks().getFirst();
                    }

                    connectToVoiceChannelAndPlay(voiceChannel, firstTrack);

                    replyToInteractionHook(event, interactionHook, songUrl + " playlist added to queue.");
                });
            }

            @Override
            public void noMatches() {
                replyToInteractionHook(event, interactionHook, "No track found at " + songUrl);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                replyToInteractionHook(event, interactionHook, "Could not play track: " + e.getMessage());
            }
        });
    }

    private Optional<VoiceChannel> getVoiceChannel(SlashCommandInteractionEvent event, CompletableFuture<InteractionHook> interactionHook) {
        final var voiceState = event.getMember().getVoiceState();
        if (!voiceState.inAudioChannel()) {
            replyToInteractionHook(event, interactionHook, "You can only play a song when you are in a voice channel.");
            return Optional.empty();
        }

        return Optional.of(voiceState.getChannel().asVoiceChannel());
    }

    private void connectToVoiceChannelAndPlay(VoiceChannel voiceChannel, AudioTrack audioTrack) {
        final var audioManager = voiceChannel.getGuild().getAudioManager();

        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(voiceChannel);
            audioManager.setSendingHandler(audioPlayerSendHandler);
        }
        audioPlayer.startTrack(audioTrack, false);
    }
}
