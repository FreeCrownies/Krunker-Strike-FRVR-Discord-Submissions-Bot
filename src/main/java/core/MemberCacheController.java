package core;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MemberCacheController implements MemberCachePolicy {

    private static final MemberCacheController INSTANCE = new MemberCacheController();
    private final HashMap<Long, Instant> guildAccessMap = new HashMap<>();

    private MemberCacheController() {
    }

    public static MemberCacheController getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<Member> loadMember(Guild guild, long userId) {
        return loadMembers(guild, userId)
                .thenApply(memberList -> {
                    if (memberList.isEmpty()) {
                        return null;
                    } else {
                        return memberList.get(0);
                    }
                });
    }

    public CompletableFuture<List<Member>> loadMembers(Guild guild, long... userIds) {
        List<Long> userIdList = Arrays.stream(userIds).boxed().collect(Collectors.toList());
        return loadMembers(guild, userIdList);
    }

    public CompletableFuture<List<Member>> loadMembersWithUsers(Guild guild, List<User> users) {
        List<Long> userIdList = users.stream().map(ISnowflake::getIdLong).collect(Collectors.toList());
        return loadMembers(guild, userIdList);
    }

    public CompletableFuture<List<Member>> loadMembers(Guild guild, List<Long> userIds) {
        cacheGuild(guild);
        CompletableFuture<List<Member>> future = new CompletableFuture<>();

        ArrayList<Long> missingMemberIds = new ArrayList<>();
        ArrayList<Member> presentMembers = new ArrayList<>();
        userIds.forEach(userId -> {
            Member member = guild.getMemberById(userId);
            if (member != null) {
                presentMembers.add(member);
            } else {
                missingMemberIds.add(userId);
            }
        });

        if (guild.isLoaded() || missingMemberIds.isEmpty()) {
            future.complete(presentMembers);
        } else {
            guild.retrieveMembersByIds(missingMemberIds)
                    .onError(future::completeExceptionally)
                    .onSuccess(members -> {
                        presentMembers.addAll(members);
                        future.complete(presentMembers);
                    });
        }
        return future;
    }

    public CompletableFuture<List<Member>> loadMembersFull(Guild guild) {
        cacheGuild(guild);
        CompletableFuture<List<Member>> future = new CompletableFuture<>();
        if (guild.isLoaded()) {
            future.complete(guild.getMembers());
        } else {
            guild.loadMembers()
                    .onError(future::completeExceptionally)
                    .onSuccess(future::complete);
        }
        return future;
    }

    @Override
    public boolean cacheMember(@NotNull Member member) {
        Guild guild = member.getGuild();
        return ChunkingFilterController.getInstance().filter(guild.getIdLong()) ||
                guildIsCached(guild);
    }

    public void cacheGuild(Guild guild) {
        guildAccessMap.put(guild.getIdLong(), Instant.now().plus(Duration.ofMinutes(10)));
    }

    public void cacheGuildIfNotExist(Guild guild) {
        if (!guildAccessMap.containsKey(guild.getIdLong())) {
            cacheGuild(guild);
        }
    }

    private boolean guildIsCached(Guild guild) {
        Instant otherInstant = guildAccessMap.get(guild.getIdLong());
        return otherInstant != null && Instant.now().isBefore(otherInstant);
    }

    public int pruneAll() {
        AtomicInteger membersPruned = new AtomicInteger(0);
        ArrayList<Map.Entry<Long, Instant>> entries = new ArrayList<>(guildAccessMap.entrySet());

        for (Map.Entry<Long, Instant> entry : entries) {
            if (Instant.now().isAfter(entry.getValue())) {
                long guildId = entry.getKey();
                try {
                    ShardManager.getLocalGuildById(guildId).ifPresent(guild -> {
                        int n = guild.getMembers().size();
                        guild.pruneMemberCache();
                        membersPruned.addAndGet(n - guild.getMembers().size());
                    });
                    guildAccessMap.remove(guildId);
                } catch (Throwable e) {
                    MainLogger.get().error("Error on guild prune", e);
                }
            }
        }

        return membersPruned.get();
    }

}