package me.dustin.jex.load.impl;

import com.mojang.brigadier.ParseResults;
import net.minecraft.command.CommandSource;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageMetadata;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface IClientPlayerEntity {
    ArgumentSignatureDataMap callSignArguments(MessageMetadata signer, ParseResults<CommandSource> parseResults, @Nullable Text preview, LastSeenMessageList lastSeenMessages);
}
