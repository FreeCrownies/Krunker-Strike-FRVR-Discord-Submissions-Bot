package constants;

import core.Program;

public interface AssetIds {

    long OWNER_USER_ID = 547064638597234688L;
    long SUPPORT_SERVER_ID = 556102583538810920L;
    long SPACEY_USER_ID = 789822495141658674L;
    long TEST_SERVER_ID = 839785857300299776L;
    long KEN_SERVER_ID = 650214993001971713L;

    long ATOM_ROLE_ID = 928020353081561171L;

    long USER_WACKELPUDDING_ID = 535908200709619733L;
    Long[] BOT_ADMINS = {OWNER_USER_ID, USER_WACKELPUDDING_ID};

    long VERIFICATION_CHANNEL_ID = Program.productionMode() ? 981254027427852358L : 981254095883079751L;
    long UPVOTES_CHANNEL_ID = Program.productionMode() ? 986248727977160734L : 986248965370564638L;
    long VOTER_ROLE_ID = 986249415461306429L;
    long EVENT_CHANNEL = 955524858433900615L;

}