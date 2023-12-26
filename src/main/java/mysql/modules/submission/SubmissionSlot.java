package mysql.modules.submission;

import constants.SubmissionType;

public class SubmissionSlot {

    private final long userId;
    private final long messageId;
    private final Long messageIdVideo;
    private final SubmissionType submissionType;
    private final String mediaUrl;
    private final String description;

    public SubmissionSlot(long userId, long messageId, Long messageIdVideo, SubmissionType submissionType, String mediaUrl, String description) {
        this.userId = userId;
        this.messageId = messageId;
        this.messageIdVideo = messageIdVideo;
        this.submissionType = submissionType;
        this.mediaUrl = mediaUrl;
        this.description = description;
    }

    public long getUserId() {
        return userId;
    }

    public long getMessageId() {
        return messageId;
    }

    public Long getMessageIdVideo() {
        return messageIdVideo;
    }

    public SubmissionType getSubmissionType() {
        return submissionType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getDescription() {
        return description;
    }

}
