package core;

import net.dv8tion.jda.api.utils.ChunkingFilter;

public class ChunkingFilterController implements ChunkingFilter {

    private static final ChunkingFilterController INSTANCE = new ChunkingFilterController();

    private ChunkingFilterController() {
    }

    public static ChunkingFilterController getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean filter(long guildId) {
        return guildId == 1160968659091599380L;
    }

}