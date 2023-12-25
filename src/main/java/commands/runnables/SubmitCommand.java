package commands.runnables;

import commands.CommandEvent;
import commands.listeners.CommandProperties;

import java.util.Locale;

@CommandProperties(
        trigger = "submit",
        emoji = "\uD83D\uDCDD"
)
public class SubmitCommand extends NavigationAbstract {

    public SubmitCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(CommandEvent event, String args) throws Throwable {
        registerNavigationListener(event.getUser());
        return true;
    }

}
