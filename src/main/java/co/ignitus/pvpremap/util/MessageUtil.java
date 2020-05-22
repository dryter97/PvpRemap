package co.ignitus.pvpremap.util;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.files.MessagesFile;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtil {

    private static MessagesFile messagesFile = PvpRemap.getInstance().getMessagesFile();

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String format(String input, String... strings) {
        String message = format(input);
        for (int i = 0; i < strings.length; i += 2)
            message = message.replace(strings[i], strings[i + 1]);
        return message;
    }

    public static List<String> format(List<String> input, String... replace) {
        return input.stream().map(line -> format(line, replace)).collect(Collectors.toList());
    }

    public static String getMessage(String path, String... replace) {
        return format(messagesFile.getFileConfiguration().getString(path, "&cUnknown Message"), replace);
    }

}
