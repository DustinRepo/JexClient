package me.dustin.jex.load.impl;

import com.mojang.brigadier.ParseResults;
import net.minecraft.command.CommandSource;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.ChatMessageSigner;
import net.minecraft.text.Text;

public interface IClientPlayerEntity {
    ArgumentSignatureDataMap callSignArguments(ChatMessageSigner signer, ParseResults<CommandSource> parseResults, Text text);
}
