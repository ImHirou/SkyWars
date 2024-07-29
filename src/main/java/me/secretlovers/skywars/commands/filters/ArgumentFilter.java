package me.secretlovers.skywars.commands.filters;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.secretlovers.skywars.commands.CommandFilter;
import me.secretlovers.skywars.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ArgumentFilter implements CommandFilter {

    ArgumentCondition condition;
    int conditionValue;
    String declineMessage;
    boolean showHelp;

    public ArgumentFilter(ArgumentCondition condition, int conditionValue, String declineMessage, boolean showHelp) {
        this.condition = condition;
        this.conditionValue = conditionValue;
        this.declineMessage = declineMessage;
        this.showHelp = showHelp;
    }

    public ArgumentFilter(ArgumentCondition condition, int conditionValue, String declineMessage) {
        this(condition, conditionValue, declineMessage, true);
    }

    @Override
    public boolean canContinue(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        return condition.is(conditionValue, subCommandArgs.length);
    }

    @Override
    public String[] getDeclineMessage(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        return showHelp ? new String[]{declineMessage, subCommand.getHelp()} : new String[]{declineMessage};
    }

    public enum ArgumentCondition {

        GREATER_THAN {
            @Override
            public boolean is(int conditionVal, int valToCheck) {
                return valToCheck > conditionVal;
            }
        },
        LOWER_THAN {
            @Override
            public boolean is(int conditionVal, int valToCheck) {
                return valToCheck < conditionVal;
            }
        },
        EQUALS {
            @Override
            public boolean is(int conditionVal, int valToCheck) {
                return valToCheck == conditionVal;
            }
        };


        public abstract boolean is(int conditionVal, int valToCheck);

    }

}
