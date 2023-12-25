package commands.runnables.dm;

import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.runnables.NavigationAbstract;
import core.EmbedFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.util.Locale;

@CommandProperties(
        trigger = "submit",
        emoji = "\uD83D\uDCDD"
)
public class SubmitCommand extends NavigationAbstract {

    private static final int
            HOME = 0;

    public SubmitCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(CommandEvent event, String args) throws Throwable {
        registerNavigationListener(event.getUser());
        return true;
    }

    @Draw(state = HOME)
    public EmbedBuilder onDrawHome(User user) {
        return EmbedFactory.getEmbedDefault(this, getString("state0_description"), getString("state0_title"));
    }

}
